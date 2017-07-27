package com.semisky.ym_multimedia.radio.fragment;

import com.ypy.eventbus.EventBus;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.semisky.ym_multimedia.BaseFragment;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.radio.adapter.FMAdapter;
import com.semisky.ym_multimedia.radio.dao.DBConfiguration;
import com.semisky.ym_multimedia.radio.dao.FMChannelDBManager;
import com.semisky.ym_multimedia.radio.event.SearchFMResultEvent;
import com.semisky.ym_multimedia.radio.horizontallistview.AbsHorizontalListView;
import com.semisky.ym_multimedia.radio.horizontallistview.AbsHorizontalListView.OnScrollListener;
import com.semisky.ym_multimedia.radio.horizontallistview.HorizontalAdapterView;
import com.semisky.ym_multimedia.radio.horizontallistview.HorizontalAdapterView.OnItemClickListener;
import com.semisky.ym_multimedia.radio.horizontallistview.HorizontalListView;
import com.semisky.ym_multimedia.radio.model.IRadioInfoCallback;
import com.semisky.ym_multimedia.radio.model.IRadioPlayCallback;
import com.semisky.ym_multimedia.radio.model.IRadioPlayModel;
import com.semisky.ym_multimedia.radio.model.IRadioPlayModelImp;
import com.semisky.ym_multimedia.radio.model.ISearchNearStrongRadioCallback;
import com.semisky.ym_multimedia.radio.model.ISearchNearStrongRadioModel;
import com.semisky.ym_multimedia.radio.model.ISearchNearStrongRadioModelImp;
import com.semisky.ym_multimedia.radio.model.ISwitchFMAMCallback;
import com.semisky.ym_multimedia.radio.model.ISwitchFMAMModel;
import com.semisky.ym_multimedia.radio.model.ISwitchFMAMModelImp;
import com.semisky.ym_multimedia.radio.model.RadioInfoReceiver;
import com.semisky.ym_multimedia.radio.utils.RadioConstants;
import com.semisky.ym_multimedia.radio.utils.RadioStatus;
import com.semisky.ym_multimedia.radio.utils.RadioUtil;
import com.semisky.ym_multimedia.radio.utils.SortCursor;
import com.semisky.ym_multimedia.radio.utils.Toaster;

public class FMFragment extends BaseFragment implements OnSeekBarChangeListener, OnItemClickListener, OnScrollListener,
		IRadioPlayCallback, IRadioInfoCallback, ISearchNearStrongRadioCallback, ISwitchFMAMCallback {
	private View fmView;
	private TextView tv_type, tv_frequency, tv_unit;
	private TextView tv_min, tv_max;
	private SeekBar sb_frequency;
	private HorizontalListView lv_frequency;
	private LinearLayout linear_scrollbar;
	private TextView scrollbar;
	private FMAdapter mFmAdapter;
	private Cursor mCursor;
	private IRadioPlayModel mIRadioPlayModel;
	private ISearchNearStrongRadioModel mISearchNearStrongRadioModel;
	private ISwitchFMAMModel mISwitchFMAMModel;// 切换FM、AM频点Model

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logger.logD("FMFragment-----------------------onCreateView");
		fmView = inflater.inflate(R.layout.radio_content, container, false);
		createView(inflater, container, savedInstanceState);
		((RadioFragment) (FMFragment.this.getParentFragment())).isShowRadar(true);
		return fmView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Logger.logD("FMFragment-----------------------onResume");
		// 注册相关
		register();
		// 搜索时被盖住，再回来
		refreshListView();
		// 滚动到选中item
		lv_frequency.post(scrollRunnable);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		((RadioFragment) (FMFragment.this.getParentFragment())).isShowRadar(true);
		Logger.logD("FMFragment-----------------------onConfigurationChanged");
	}

	/**
	 * 显示搜台总数
	 */
	private void showSearchTotal(int number) {

		((RadioFragment) (FMFragment.this.getParentFragment())).showSearchTotal(number);
	}

	@Override
	public void initModel() {
		mIRadioPlayModel = IRadioPlayModelImp.getInstance(getContext());
		mIRadioPlayModel.addIRadioPlayCallback(this);

		RadioInfoReceiver.getInstance(getContext()).addIRadioInfoCallback(this);

		mISearchNearStrongRadioModel = ISearchNearStrongRadioModelImp.getInstance(getContext());
		mISearchNearStrongRadioModel.addISearchNearStrongRadioCallback(this);

		mISwitchFMAMModel = ISwitchFMAMModelImp.getInstance(getContext());
		mISwitchFMAMModel.addISwitchFMAMCallback(this);
	}

	@Override
	public void initLeftViews() {
	}

	@Override
	public void initRightViews() {
	}

	@Override
	public void initMiddleViews() {
		tv_type = (TextView) fmView.findViewById(R.id.tv_type);
		tv_type.setText(R.string.fm);
		tv_frequency = (TextView) fmView.findViewById(R.id.tv_frequency);
		tv_frequency.setText(RadioUtil.formatFloatFrequency(RadioStatus.currentFrequency / RadioConstants.FM_MULTIPLE));
		tv_unit = (TextView) fmView.findViewById(R.id.tv_unit);
		tv_unit.setText(R.string.mhz);
		tv_min = (TextView) fmView.findViewById(R.id.tv_min);
		tv_min.setText(RadioUtil.formatFloatFrequency(RadioConstants.FMMIN / RadioConstants.FM_MULTIPLE));
		tv_max = (TextView) fmView.findViewById(R.id.tv_max);
		tv_max.setText(RadioUtil.formatFloatFrequency(RadioConstants.FMMAX / RadioConstants.FM_MULTIPLE));
		linear_scrollbar = (LinearLayout) fmView.findViewById(R.id.linear_scrollbar);
		scrollbar = (TextView) fmView.findViewById(R.id.scrollbar);
		initListView();
		initSeekbar();
	}

	private void initListView() {
		lv_frequency = (HorizontalListView) fmView.findViewById(R.id.lv_frequency);
		mCursor = FMChannelDBManager.getInstance(getContext()).getFMChannelsCursor();
		if (mCursor != null) {
			mFmAdapter = new FMAdapter(getContext(),
					new SortCursor(mCursor, DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY));
			lv_frequency.setAdapter(mFmAdapter);
			lv_frequency.setOnItemClickListener(this);
			if (mFmAdapter.getCount() > 0) {
				if (mFmAdapter.getCount() <= 5) {
					linear_scrollbar.setVisibility(View.VISIBLE);
					scrollbar.setVisibility(View.INVISIBLE);
				} else {
					linear_scrollbar.setVisibility(View.VISIBLE);
					scrollbar.setX(0);
					lv_frequency.setOnScrollListener(this);
				}
			} else {
				linear_scrollbar.setVisibility(View.INVISIBLE);
			}
		}
	}

	/**
	 * 设置选中位置
	 * 
	 * @param frequency
	 * @param smoothScrollToPlaying
	 *            是否滚动动画到当前播放
	 */
	private void setSelection(int frequency, boolean smoothScrollToPlaying) {
		if (RadioStatus.currentType == RadioConstants.TYPE_FM) {
			int position = mFmAdapter.getPosition(frequency);
			if (smoothScrollToPlaying) {
				lv_frequency.smoothScrollToPosition(position);
			} else {
				lv_frequency.setSelection(position);
			}
		}
	}

	/**
	 * 此方法在数据量变化时无效
	 */
	@SuppressWarnings("deprecation")
	private void refreshListView() {
		if (lv_frequency != null && mFmAdapter != null) {
			mFmAdapter.getCursor().requery();
			mFmAdapter.notifyDataSetChanged();
		}
	}

	/** 初始化刻度条 */
	private void initSeekbar() {
		sb_frequency = (SeekBar) fmView.findViewById(R.id.sb_frequency);
		sb_frequency.setMax(RadioConstants.FMMAX - RadioConstants.FMMIN);
		// 播放频点回调方法onRadioPlay中会刷新刻度条显示
		mIRadioPlayModel.playRadio(RadioConstants.TYPE_FM, RadioStatus.currentFrequency);
		sb_frequency.setOnSeekBarChangeListener(this);
	}

	/**
	 * 只更新刻度
	 * 
	 * @param progress
	 */
	private void setProgress(int progress) {
		// 符合范围的值才可以设进去，否则会造成切换别的频道类型再切回来，刻度在头或尾的情况
		if (progress >= 0 && progress <= sb_frequency.getMax()) {
			sb_frequency.setProgress(progress);
		}
	}

	@Override
	public int getSystemUITitleResId() {
		return R.string.radio;
	}

	@Override
	public void setListener() {
	}

	@Override
	public void register() {
		if (!EventBus.getDefault().isRegistered(this))
			EventBus.getDefault().register(this);
	}

	/** 列表滚动到选中 */
	private Runnable scrollRunnable = new Runnable() {

		@Override
		public void run() {
			setSelection(RadioStatus.currentFrequency, true);
		}

	};

	// @Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SearchFMResultEvent event) {
		switch (event.getState()) {
		case CLEARLIST:
			if (linear_scrollbar != null) {
				linear_scrollbar.setVisibility(View.INVISIBLE);
			}
			// showSearchTotal(0);
			break;
		case UNFINISH:
			tv_frequency.setText(RadioUtil.formatFloatFrequency(event.getFrequency() / RadioConstants.FM_MULTIPLE));
			setProgress(event.getFrequency() - RadioConstants.FMMIN);
			break;
		case FINISH:
			initListView();
			if (lv_frequency.getCount() > 0) {
				itemClick(lv_frequency, lv_frequency.getChildAt(0), 0, lv_frequency.getItemIdAtPosition(0), false);
				showSearchTotal(lv_frequency.getCount());
			} else {
				Toaster.makeText(getContext(), R.string.search_fail);
				// 播放回初始频道
				mIRadioPlayModel.playRadio(RadioStatus.currentType, RadioStatus.currentFrequency);
				showSearchTotal(0);
			}
			break;
		case INTERRUPT:
			Toaster.makeText(getContext(), R.string.search_interrupt);
			initListView();
			if (lv_frequency.getCount() > 0) {// 播放第一个FM强信号台
				itemClick(lv_frequency, lv_frequency.getChildAt(0), 0, lv_frequency.getItemIdAtPosition(0), false);
			} else {// 一个好台都没有
				// 播放回初始频道
				mIRadioPlayModel.playRadio(RadioStatus.currentType, RadioStatus.currentFrequency);
			}
			break;
		case TIMEOUT:
			Toaster.makeText(getContext(), R.string.search_fail);
			// 播放回初始频道
			mIRadioPlayModel.playRadio(RadioStatus.currentType, RadioStatus.currentFrequency);
			showSearchTotal(0);
			break;
		default:
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {// 拖动时调用（setProgress时fromUser为false，手指拖动时为true）
		if (fromUser) {
			mIRadioPlayModel.playRadio(RadioConstants.TYPE_FM,
					RadioConstants.FMMIN + (int) (Math.round(progress / 10d) * 10));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {// 手指按下时调用
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {// 停止拖动（手指抬起时才会调用）
	}

	@Override
	public void onScrollStateChanged(AbsHorizontalListView view, int scrollState) {
		switch (scrollState) {
		case SCROLL_STATE_IDLE:
			final int i = lv_frequency.getFirstVisiblePosition();
			if (lv_frequency.getChildCount() > 0) {
				if (-(lv_frequency.getChildAt(0).getLeft()) >= lv_frequency.getChildAt(0).getWidth() / 2) {

					lv_frequency.smoothScrollToPositionFromLeft(i + 1, 1, 2000);

				} else {
					lv_frequency.smoothScrollToPositionFromLeft(i, 1, 2000);
				}
			}
			break;
		}
	}

	@Override
	public void onScroll(AbsHorizontalListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onScrollRange(AbsHorizontalListView view, int range) {
		if (scrollbar.getWidth() == 0) {
			int itemWidth = lv_frequency.getChildAt(0).getWidth();
			float itemNums = view.getWidth() * 1f / itemWidth;
			scrollbar.setWidth((int) (itemNums * 100 * view.getWidth() / range));
		}
	}

	@Override
	public void onScrollOffset(AbsHorizontalListView view, int offset) {
		if (view.getScrollRange() > 0) {
			scrollbar.setX((offset * view.getWidth()) / view.getScrollRange());
		}
	}

	@Override
	public void onItemClick(HorizontalAdapterView<?> parent, View view, int position, long id) {
		itemClick(parent, view, position, id, true);
	}

	private void itemClick(HorizontalAdapterView<?> parent, View view, int position, long id, boolean fromUser) {
		switch (parent.getId()) {
		case R.id.lv_frequency:
			Cursor cursor = mFmAdapter.getCursor();
			if (cursor != null) {
				cursor.moveToPosition(position);
				int frequency = cursor
						.getInt(cursor.getColumnIndex(DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY));
				Logger.logD("C" + getString(R.string.fm) + ", frequency = " + frequency + getString(R.string.mhz));

				if (fromUser && RadioStatus.currentFrequency == frequency) {// 如果是用户点击并且点击的item频点就是正在播放的频点，不做任何操作
					Logger.logD("FMFragment-----------------------this frequency is playing");
				} else {
					mIRadioPlayModel.playRadio(RadioConstants.TYPE_FM, frequency);
				}
			}
			break;
		default:
			break;
		}
	}

	private void refreshAll(final int frequency) {
		if (getActivity() != null && Looper.myLooper() != Looper.getMainLooper()) {// 不在UI线程
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					tv_frequency.setText(RadioUtil.formatFloatFrequency(frequency / RadioConstants.FM_MULTIPLE));
					setProgress(frequency - RadioConstants.FMMIN);
					if (lv_frequency.getCount() > 0) {// 列表为空就不需要进行任何刷新
						refreshListView();
						setSelection(frequency, true);
					}
				}
			});
		} else {
			tv_frequency.setText(RadioUtil.formatFloatFrequency(frequency / RadioConstants.FM_MULTIPLE));
			setProgress(frequency - RadioConstants.FMMIN);
			if (lv_frequency.getCount() > 0) {// 列表为空就不需要进行任何刷新
				refreshListView();
				setSelection(frequency, true);
			}
		}
	}

	private void refreshAll(final boolean isFinish, final int frequency) {
		if (getActivity() != null && Looper.myLooper() != Looper.getMainLooper()) {// 不在UI线程
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					tv_frequency.setText(RadioUtil.formatFloatFrequency(frequency / RadioConstants.FM_MULTIPLE));
					setProgress(frequency - RadioConstants.FMMIN);
					if (lv_frequency.getCount() > 0) {// 列表为空就不需要进行任何刷新
						if (isFinish) {
							refreshListView();
							setSelection(frequency, true);
						}
					}
				}
			});
		} else {
			tv_frequency.setText(RadioUtil.formatFloatFrequency(frequency / RadioConstants.FM_MULTIPLE));
			setProgress(frequency - RadioConstants.FMMIN);
			if (lv_frequency.getCount() > 0) {// 列表为空就不需要进行任何刷新
				if (isFinish) {
					refreshListView();
					setSelection(frequency, true);
				}
			}
		}
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Logger.logD("FMFragment-----------------------onStop()");
		// unregister();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregister();
		Logger.logD("FMFragment-----------------------onDestroy()");
		// if (mFmAdapter != null && mFmAdapter.getCursor() != null) {
		// mFmAdapter.getCursor().close();
		// }
		lv_frequency.removeCallbacks(scrollRunnable);
	}

	@Override
	public void unregister() {
		EventBus.getDefault().unregister(this);
		mISwitchFMAMModel.removeISwitchFMAMCallback(this);
		mISearchNearStrongRadioModel.removeISearchNearStrongRadioCallback(this);

		mIRadioPlayModel.removeIRadioPlayCallback(this);

		RadioInfoReceiver.getInstance(getContext()).removeIRadioInfoCallback(this);
	}

	@Override
	public void onRadioPlay(int radioType, int frequency) {
		refreshAll(frequency);
	}

	@Override
	public void onRadioInfoSearchNearStrongRadioResult(final boolean isFinish, final int frequency) {
		refreshAll(isFinish, frequency);
	}

	@Override
	public void onSearchNearStrongRadioResult(boolean isFinish, int frequency) {
		refreshAll(isFinish, frequency);
	}

	@Override
	public void onSwitchFMAMPrepare(int radioType, boolean resetFragment) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beginSwitchFMToFM() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beginSwitchFMToAM() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beginSwitchAMToFM() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beginSwitchAMToAM() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopSearchWhenSwitch() {
		initListView();

		showSearchTotal(lv_frequency.getCount());

	}

	@Override
	public void beginSwitchFMToFMWhenSearchNearStrongRadio() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beginSwitchFMToAMWhenSearchNearStrongRadio() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beginSwitchAMToFMWhenSearchNearStrongRadio() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beginSwitchAMToAMWhenSearchNearStrongRadio() {
		// TODO Auto-generated method stub

	}

}
