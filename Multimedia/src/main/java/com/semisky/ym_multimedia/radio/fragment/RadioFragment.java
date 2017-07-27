package com.semisky.ym_multimedia.radio.fragment;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.semisky.ym_multimedia.BaseFragment;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.common.controller.FragmentSwitchController;
import com.semisky.ym_multimedia.common.utils.AppUtil;
import com.semisky.ym_multimedia.common.utils.CommonConstants;
import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.radio.dao.AMChannelDBManager;
import com.semisky.ym_multimedia.radio.dao.FMChannelDBManager;
import com.semisky.ym_multimedia.radio.model.IRadioInfoCallback;
import com.semisky.ym_multimedia.radio.model.IRadioPlayModel;
import com.semisky.ym_multimedia.radio.model.IRadioPlayModelImp;
import com.semisky.ym_multimedia.radio.model.IRadioSwitchCallback;
import com.semisky.ym_multimedia.radio.model.IRadioSwitchModel;
import com.semisky.ym_multimedia.radio.model.IRadioSwitchModelImp;
import com.semisky.ym_multimedia.radio.model.ISearchAllAMModel;
import com.semisky.ym_multimedia.radio.model.ISearchAllAMModelImp;
import com.semisky.ym_multimedia.radio.model.ISearchAllFMModel;
import com.semisky.ym_multimedia.radio.model.ISearchAllFMModelImp;
import com.semisky.ym_multimedia.radio.model.ISearchNearStrongRadioCallback;
import com.semisky.ym_multimedia.radio.model.ISearchNearStrongRadioModel;
import com.semisky.ym_multimedia.radio.model.ISearchNearStrongRadioModelImp;
import com.semisky.ym_multimedia.radio.model.IStepRadioModel;
import com.semisky.ym_multimedia.radio.model.IStepRadioModelImp;
import com.semisky.ym_multimedia.radio.model.ISwitchFMAMCallback;
import com.semisky.ym_multimedia.radio.model.ISwitchFMAMModel;
import com.semisky.ym_multimedia.radio.model.ISwitchFMAMModelImp;
import com.semisky.ym_multimedia.radio.model.RadioInfoReceiver;
import com.semisky.ym_multimedia.radio.model.RadioVolumeModel;
import com.semisky.ym_multimedia.radio.utils.PreferencesUtil;
import com.semisky.ym_multimedia.radio.utils.ProtocolUtil;
import com.semisky.ym_multimedia.radio.utils.RadioConstants;
import com.semisky.ym_multimedia.radio.utils.RadioStatus;
import com.semisky.ym_multimedia.radio.utils.RadioStatus.SearchNearStrongChannel;
import com.semisky.ym_multimedia.radio.utils.SettingsUtil;

public class RadioFragment extends BaseFragment implements OnClickListener, IRadioSwitchCallback, IRadioInfoCallback,
		ISearchNearStrongRadioCallback, ISwitchFMAMCallback, OnLongClickListener, OnTouchListener {
	private View contentView;
	private FMFragment mFmFragment;
	private AMFragment mAmFragment;
	private Button btn_search, btn_previous, btn_next, btn_band, btn_sound, btn_switch;// 左侧按钮
	private LinearLayout ll_radar_distance;// 近远程
	private ImageView iv_radar_distance;// 近远程
	private TextView tv_radar_distance;// 近远程
	private TextView tv_radar_track;// 单声道立体声
	private TextView searchTotal;// 搜台总数
	private ImageButton ib_music, ib_video, ib_bt_music;// 右上角按钮
	private AudioManager mAudioManager;
	private IRadioSwitchModel mIRadioSwitchModel;// 开关Model
	private IRadioPlayModel mIRadioPlayModel;// 播放某频点Model
	private RadioInfoReceiver mRadioInfoReceiver;// 实时接收频点信息Model
	private ISearchAllFMModel mISearchAllFMModel;// 搜索所有FM频点Model
	private ISearchAllAMModel mISearchAllAMModel;// 搜索所有AM频点Model
	private ISearchNearStrongRadioModel mISearchNearStrongRadioModel;// 搜索上下一个强信号台Model
	private ISwitchFMAMModel mISwitchFMAMModel;// 切换FM、AM频点Model
	private RadioVolumeModel mRadioVolumeModel;// 收音机声音操作Model
	private IStepRadioModel mIStepRadioModel;// 步进操作Model
	private boolean firstResume = true;// 是否是第一次执行onResume
	private boolean firstGetInto = true;// 是否是第一次进入
	private boolean isFirstLongClick = true;// 是否是第一次长按上一个/下一个
	private boolean isTouchable = false;// 上一个/下一个是否进行触摸操作

	private final RadioHandler mRadioHandler = new RadioHandler(this);

	private static class RadioHandler extends Handler {
		private static WeakReference<RadioFragment> mReference;

		public RadioHandler(RadioFragment fragment) {
			mReference = new WeakReference<RadioFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mReference.get() == null) {
				return;
			}

			switch (msg.what) {
			case RadioConstants.START_PLAY:
				mReference.get().startPlay();
				break;
			case RadioConstants.STOP_PLAY:
				mReference.get().stopPlay();
				break;
			case RadioConstants.MSG_SEARCH_NEAR_STRONG_RADIO:
				if ((Boolean) msg.obj) {// 播放且显示
					mReference.get().mIRadioPlayModel.playRadio(RadioStatus.currentType, msg.arg1);
					mReference.get().mRadioVolumeModel.fadeUpVolume();
				}
				break;
			case RadioConstants.MSG_SWITCH_FRAGMENT:
				mReference.get().mISwitchFMAMModel.switchRadioType(msg.arg1, true);
				break;
			case RadioConstants.MSG_RADIO_STERO_INFO:// 单声道立体声
				mReference.get().radarTrackShow(msg.arg1);
				break;
			case RadioConstants.MSG_RADIO_PREVIOUS: // 上一个
				mReference.get().mISearchNearStrongRadioModel.searchPreviousStrongRadio();
				break;
			case RadioConstants.MSG_RADIO_NEXT: // 下一个
				mReference.get().mISearchNearStrongRadioModel.searchNextStrongRadio();
				break;
			case RadioConstants.MSG_RADIO_SWITCH: // Radio按键切换FM/AM
				mReference.get().bandSwitch();
				break;
			case RadioConstants.MSG_CANCLE_LONGCLICK: // 上一个、下一个按钮取消步进模式
				mReference.get().isFirstLongClick = true;
				mReference.get().isTouchable = false;

				Logger.logD("RadioFragment-----------------上一个、下一个按钮取消步进模式");
				break;
			case RadioConstants.MSG_CANCLE_SEARCH_TOTAL_SHOW: // 取消显示搜台总数
				mReference.get().searchTotal.setVisibility(View.INVISIBLE);
				Logger.logD("RadioFragment-----------------取消显示搜台总数");
				break;
			default:
				break;
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logger.logD("RadioFragment-----------------------onCreateView");
		contentView = inflater.inflate(R.layout.fragment_radio, container, false);
		createView(inflater, container, savedInstanceState);
		loadData();
		setMemoryFragment();
		// 注册相关
		register();
		firstSearch();
		return contentView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Logger.logD("RadioFragment-----------------------onResume");
		if (!firstResume) {// 非第一次就执行，即第一次不执行
			// 进了收音机页，如果没有音频焦点，就要把音频焦点申请回来
			if (!RadioStatus.hasFocus) {
				requestAudioFocus();
			}
			if (isOpened() && RadioStatus.searchNearState == SearchNearStrongChannel.NEITHER
					&& !RadioStatus.isSearchingFM && !RadioStatus.isSearchingAM) {// 防止在搜台时倒车，回来后把声音拉回来
				if (mRadioVolumeModel.getCurrentVolumeRatio() < 1.0f) {// 如果只是切至后台播放，回来之后不要再重新操作声音
					mRadioHandler.removeMessages(RadioConstants.START_PLAY);
					mRadioHandler.sendEmptyMessage(RadioConstants.START_PLAY);
				}
			}
		}
		ProtocolUtil.getInstance(getContext()).registerKeyPressListener(mIKeyPressInterface);
		firstResume = false;// 不是第一次了
		
		if(!SettingsUtil.getInstance().isRadioLatestOpened(getContext())){
			mIRadioSwitchModel.switchOnOff(true);
		}
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Logger.logD("RadioFragment-----------------------onConfigurationChanged");
		btn_search.setText(R.string.search);
		btn_previous.setText(R.string.previous);
		btn_next.setText(R.string.next);
		btn_band.setText(R.string.band);
		btn_sound.setText(R.string.sound);
		switchButtonStatus();
		radarTrackShow(RadioStatus.radarTrackStatus);
	}

	/** 初始化Model层 */
	@Override
	public void initModel() {
		mRadioInfoReceiver = RadioInfoReceiver.getInstance(getContext());
		mRadioInfoReceiver.registerRadioInfoReceiver();
		mRadioInfoReceiver.addIRadioInfoCallback(this);

		mIRadioSwitchModel = IRadioSwitchModelImp.getInstance(getContext());
		mIRadioSwitchModel.addIRadioSwitchCallback(this);

		mIRadioPlayModel = IRadioPlayModelImp.getInstance(getContext());

		mISearchAllFMModel = ISearchAllFMModelImp.getInstance(getContext());

		mISearchAllAMModel = ISearchAllAMModelImp.getInstance(getContext());

		mIStepRadioModel = IStepRadioModelImp.getInstance(getContext());

		mISearchNearStrongRadioModel = ISearchNearStrongRadioModelImp.getInstance(getContext());
		mISearchNearStrongRadioModel.addISearchNearStrongRadioCallback(this);

		mISwitchFMAMModel = ISwitchFMAMModelImp.getInstance(getContext());
		mISwitchFMAMModel.addISwitchFMAMCallback(this);

		mRadioVolumeModel = RadioVolumeModel.getInstance(getContext());
	}

	private void loadData() {
		// 获取最大音量
		mRadioVolumeModel.setMaxRadioVolume(ProtocolUtil.getInstance(getContext()).getMaxRadioVolume());
		// 获取远近程
		RadioStatus.rangeType = PreferencesUtil.getInstance().getLongOrShortRange(getContext());
		RadioStatus.currentType = PreferencesUtil.getInstance().getLatestRadioType(getContext());
		RadioStatus.currentFrequency = PreferencesUtil.getInstance().getLatestRadioFrequency(getContext());
		// longOrShortRange();
	}

	@Override
	public void initLeftViews() {
		// 搜索按钮
		btn_search = (Button) contentView.findViewById(R.id.btn_search);
		// 上一个按钮
		btn_previous = (Button) contentView.findViewById(R.id.btn_previous);
		// 下一个按钮
		btn_next = (Button) contentView.findViewById(R.id.btn_next);
		// 波段按钮
		btn_band = (Button) contentView.findViewById(R.id.btn_band);
		// 音效按钮
		btn_sound = (Button) contentView.findViewById(R.id.btn_sound);
		// 开关按钮
		btn_switch = (Button) contentView.findViewById(R.id.btn_switch);
		// 进了收音机页，就要把音频焦点抢回来
		requestAudioFocus();
		switchButtonStatus();
	}

	/**
	 * 显示搜台总数
	 */
	public void showSearchTotal(int number) {
		searchTotal.setVisibility(View.VISIBLE);
		String s = (String) (getResources().getText(R.string.search_total));
		searchTotal.setText(s + number);
		// 3秒后取消显示搜台总数
        mRadioHandler.removeMessages(RadioConstants.MSG_CANCLE_SEARCH_TOTAL_SHOW);
        mRadioHandler.sendEmptyMessageDelayed(RadioConstants.MSG_CANCLE_SEARCH_TOTAL_SHOW, 2000);
		

	}

	private void switchButtonStatus() {
		// if (SettingsUtil.getInstance().isRadioLatestOpened(getContext())) {//
		// 最后是打开的
		// btn_switch.setText(R.string.close);
		// mIRadioSwitchModel.switchOnOff(true);
		// } else {// 最后是关闭的
		// btn_switch.setText(R.string.open);
		// mIRadioSwitchModel.switchOnOff(false);
		// }

		// 只有关闭一种模式，不能切换
		btn_switch.setText(R.string.close);
		mIRadioSwitchModel.switchOnOff(true);
	}

	@Override
	public void initRightViews() {
		// 近程远程
		ll_radar_distance = (LinearLayout) contentView.findViewById(R.id.ll_radar_distance);
		iv_radar_distance = (ImageView) contentView.findViewById(R.id.iv_radar_distance);
		tv_radar_distance = (TextView) contentView.findViewById(R.id.tv_radar_distance);
		// 单声道、立体声
		tv_radar_track = (TextView) contentView.findViewById(R.id.tv_radar_track);

		searchTotal = (TextView) contentView.findViewById(R.id.tv_search_total);
	}

	@Override
	public void initMiddleViews() {
		ib_music = (ImageButton) contentView.findViewById(R.id.ib_music);
		ib_video = (ImageButton) contentView.findViewById(R.id.ib_video);
		ib_bt_music = (ImageButton) contentView.findViewById(R.id.ib_bt_music);
	}

	@Override
	public int getSystemUITitleResId() {
		return R.string.radio;
	}

	@Override
	public void setListener() {
		btn_search.setOnClickListener(this);
		btn_previous.setOnClickListener(this);
		btn_next.setOnClickListener(this);
		btn_band.setOnClickListener(this);
		btn_sound.setOnClickListener(this);
		btn_switch.setOnClickListener(this);
		ib_music.setOnClickListener(this);
		ib_video.setOnClickListener(this);
		ib_bt_music.setOnClickListener(this);
		ll_radar_distance.setOnClickListener(this);
		// tv_radar_track.setOnClickListener(this);

		btn_previous.setOnLongClickListener(this);
		btn_next.setOnLongClickListener(this);
		btn_previous.setOnTouchListener(this);
		btn_next.setOnTouchListener(this);
	}

	/**
	 * 设置显示记忆的Fragment
	 */
	private void setMemoryFragment() {
		switchFragment(PreferencesUtil.getInstance().getMemoryFragmentFlag(getContext()));
	}

	/**
	 * 切换Fragment
	 * 
	 * @param radioType
	 *            电台类型
	 */
	private void switchFragment(int radioType) {
		// 开启Fragment事务
		FragmentTransaction mTransaction = getChildFragmentManager().beginTransaction();
		switch (radioType) {
		case RadioConstants.TYPE_FM:
			if (mFmFragment == null) {
				mFmFragment = new FMFragment();
			}
			mTransaction.replace(R.id.id_container, mFmFragment);
			break;
		case RadioConstants.TYPE_AM:
			if (mAmFragment == null) {
				mAmFragment = new AMFragment();
			}
			mTransaction.replace(R.id.id_container, mAmFragment);
			break;
		default:
			break;
		}
		// 事务提交
		// 因为RadioReceiver中有发来切换电台类型的请求事件，而且有可能是在onSaveInstanceState之后接收到，导致异常，所以使用commitAllowingStateLoss
		mTransaction.commitAllowingStateLoss();
		PreferencesUtil.getInstance().setMemoryFragmentFlag(getContext(), radioType);
	}

	/**
	 * 当第一次从一个Activity启动Fragment，然后再去启动子Fragment的时候，存在指向Activity的变量，
	 * 但当退出这些Fragment之后回到Activity，
	 * 然后再进入Fragment的时候，这个变量变成null，这就很容易明了为什么抛出的异常是activity has been destroyed，
	 * Fragment在Detached之后都会被reset掉，但是它并没有对ChildFragmentManager做reset，
	 * 所以会造成ChildFragmentManager的状态错误。
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		try {
			Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void register() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SHUTDOWN);
		IntentFilter filterRadar = new IntentFilter();
		filterRadar.addAction(RadioConstants.ACTION_RADIO_STERO_INFO);
		getContext().registerReceiver(mReceiver, filter);
	}

	/** 是否打开了收音机 */
	public boolean isOpened() {
//		 return SettingsUtil.getInstance().isRadioLatestOpened(getContext());
		// 默认只有打开
		return true;
	}

	/** 开始播放 */
	private void startPlay() {
		// 防止点开收音机应用时，默认声音过高而产生砰的一声，先将声音设为0
		ProtocolUtil.getInstance(getContext()).setVolumn(android.media.AudioSystem.STREAM_RADIO, 0, 0);
		mRadioVolumeModel.setCurrentVolumeRatio(mRadioVolumeModel.getLowestVolume());
		/*
		 * 有可能刚点击返回键，执行onDestroy，声音渐变降低，此时再次打开应用，需要删除之前的关闭声音消息。
		 * 而重新创建Activity后RadioHandler操作不了之前的消息队列，所以需要将声音操作独立出去。
		 */
		mRadioVolumeModel.removeMessages(RadioConstants.FADE_DOWM);
		mRadioVolumeModel.removeMessages(RadioConstants.CLOSE_RADIO_VOLUME);
		mIRadioPlayModel.playRadio(RadioStatus.currentType, RadioStatus.currentFrequency);
		mRadioVolumeModel.fadeUpVolume();
	}

	/** 停止播放 */
	private void stopPlay() {
		mRadioHandler.removeMessages(RadioConstants.START_PLAY);
		mRadioVolumeModel.fadeDownVolume();
		// 音量降为0后发送closeRadioVol请求
		int fade_down_duration = (int) ((1f / RadioVolumeModel.volume_step_sub)
				* RadioVolumeModel.fade_down_delayMillis);
		radioOff(fade_down_duration);
	}

	/**
	 * 收音机声音打开
	 * 
	 * @param delayMillis
	 *            延迟时长
	 */
	private void radioOn(long delayMillis) {
		mRadioVolumeModel.removeMessages(RadioConstants.CLOSE_RADIO_VOLUME);
		mRadioVolumeModel.removeMessages(RadioConstants.OPEN_RADIO_VOLUME);
		mRadioVolumeModel.sendEmptyMessageDelayed(RadioConstants.OPEN_RADIO_VOLUME, delayMillis);
	}

	/**
	 * 收音机声音关闭
	 * 
	 * @param delayMillis
	 *            延迟时长
	 */
	private void radioOff(long delayMillis) {
		mRadioVolumeModel.removeMessages(RadioConstants.OPEN_RADIO_VOLUME);
		mRadioVolumeModel.removeMessages(RadioConstants.CLOSE_RADIO_VOLUME);
		mRadioVolumeModel.sendEmptyMessageDelayed(RadioConstants.CLOSE_RADIO_VOLUME, delayMillis);
	}

	/**
	 * 当搜索上下一个强信号台时发送消息
	 * 
	 * @param isFinish
	 *            是否手动播放某个频点（搜索到上下一个强信号台会自动播放）
	 * @param frequency
	 *            显示频点
	 */
	private void sendSearchNearStrongRadioResultMessage(boolean isFinish, int frequency) {
		mRadioHandler.obtainMessage(RadioConstants.MSG_SEARCH_NEAR_STRONG_RADIO, frequency, 0, isFinish).sendToTarget();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_search:// 点击搜索按钮
			SearchButton();
			break;
		case R.id.btn_previous:// 点击搜索上一个强信号台
			if (!isTouchable) {
				mISearchNearStrongRadioModel.searchPreviousStrongRadio();
			}
			break;
		case R.id.btn_next:// 点击搜索下一个强信号台
			if (!isTouchable) {
				mISearchNearStrongRadioModel.searchNextStrongRadio();
			}
			break;
		case R.id.btn_band:// 点击波段按钮
			// 应对选项卡切换过快发生Tab和Fragment不对应的问题
			// mRadioHandler.removeMessages(RadioConstants.MSG_SWITCH_FRAGMENT);
			// mRadioHandler.sendMessageDelayed(mRadioHandler.obtainMessage(RadioConstants.MSG_SWITCH_FRAGMENT,
			// RadioStatus.currentType == RadioConstants.TYPE_FM ?
			// RadioConstants.TYPE_AM : RadioConstants.TYPE_FM,
			// -1), 10);
			bandSwitch();
			break;
		case R.id.btn_sound:// 跳转音效设置界面
			gotoSound();
			break;
		case R.id.btn_switch:// 点击开关,退回主界面
			// mIRadioSwitchModel.switchOnOff(!isOpened());
			finishActivityAndBackHome();
			break;
		case R.id.ib_music:// 点击多媒体音乐按钮
			FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_MUSIC, null);
			mISwitchFMAMModel.jumpBackPlayMin();
			break;
		case R.id.ib_video:// 点击视频按钮
			FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_VIDEO, null);
			mISwitchFMAMModel.jumpBackPlayMin();
			break;
		case R.id.ib_bt_music:// 点击蓝牙音乐按钮
			Bundle bundle = new Bundle();
			bundle.putInt(CommonConstants.KEY_GOTO_BT, CommonConstants.GOTO_BT_MUSIC);
			FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_BT, bundle);
			mISwitchFMAMModel.jumpBackPlayMin();
			break;
		case R.id.ll_radar_distance:// 近远程
			longOrShortRange();
			break;
		// case R.id.tv_radar_track:// 单声道立体音
		// if
		// (tv_radar_track.getText().toString().equals(getString(R.string.single_track)))
		// {
		// tv_radar_track.setText(R.string.double_track);
		// } else {
		// tv_radar_track.setText(R.string.single_track);
		// }
		// break;
		default:
			break;
		}
	}

	/**
	 * 远近程切换
	 */
	public void longOrShortRange() {
		if (RadioStatus.rangeType == RadioConstants.RANGE_LONG) {
			iv_radar_distance.setVisibility(View.INVISIBLE);
			tv_radar_distance.setText(R.string.short_distance);
			RadioStatus.rangeType = RadioConstants.RANGE_SHORT;
			PreferencesUtil.getInstance().setLongOrShortRange(getContext(), RadioStatus.rangeType);
			Logger.logD("RadioFragment-----------------------shortRange");
		} else {
			iv_radar_distance.setVisibility(View.VISIBLE);
			tv_radar_distance.setText(R.string.long_distance);
			RadioStatus.rangeType = RadioConstants.RANGE_LONG;
			PreferencesUtil.getInstance().setLongOrShortRange(getContext(), RadioStatus.rangeType);
			Logger.logD("RadioFragment-----------------------longRange");
		}
	}

	/**
	 * 切换波段 应对选项卡切换过快发生Tab和Fragment不对应的问题
	 */
	public void bandSwitch() {
		mRadioHandler.removeMessages(RadioConstants.MSG_SWITCH_FRAGMENT);
		mRadioHandler.sendMessageDelayed(mRadioHandler.obtainMessage(RadioConstants.MSG_SWITCH_FRAGMENT,
				RadioStatus.currentType == RadioConstants.TYPE_FM ? RadioConstants.TYPE_AM : RadioConstants.TYPE_FM,
				-1), 10);
	}

	/**
	 * 单声道立体声
	 */
	private void radarTrackShow(int status) {
		if (ll_radar_distance.getVisibility() == View.VISIBLE) {
			if (status == 1) {
				tv_radar_track.setText(R.string.double_track);
				Logger.logD("RadioFragment-----------------------double_track");
			} else {
				tv_radar_track.setText(R.string.single_track);
				Logger.logD("RadioFragment-----------------------single_track");
			}
		}
	}

	/**
	 * 第一次进入，如果没有数据，就搜索
	 */
	private void firstSearch() {
		if (!hasAMOrFM()) {
			SearchButton();
		}
		firstGetInto = false;
	}

	/**
	 * 搜索
	 */
	public void SearchButton() {
		switch (RadioStatus.currentType) {
		case RadioConstants.TYPE_FM:
			mISearchAllFMModel.searchAllFM();// 搜索所有FM电台
			break;
		case RadioConstants.TYPE_AM:
			mISearchAllAMModel.searchAllAM();// 搜索所有AM电台
			break;
		default:
			break;
		}
	}

	/**
	 * 判断是否有数据
	 * 
	 * @return
	 */
	private boolean hasAMOrFM() {
		if (RadioStatus.currentType == RadioConstants.TYPE_FM) {
			return FMChannelDBManager.getInstance(getContext()).hasFMChannels();
		} else {
			return AMChannelDBManager.getInstance(getContext()).hasAMChannels();
		}
	}

	/**
	 * 是否显示上面左侧按钮
	 */
	public void isShowRadar(Boolean isShow) {
		if (isShow) {
			int rememberRangeType = PreferencesUtil.getInstance().getLongOrShortRange(getContext());
			Logger.logD("RadioFragment-----------------------rememberRangeType=========" + rememberRangeType);
			ll_radar_distance.setVisibility(View.VISIBLE);
			tv_radar_track.setVisibility(View.VISIBLE);
			if (rememberRangeType == RadioConstants.RANGE_LONG) {
				iv_radar_distance.setVisibility(View.VISIBLE);
				tv_radar_distance.setText(R.string.long_distance);
				Logger.logD("RadioFragment-----------------------isShowRadar---long_distance");
			} else {
				iv_radar_distance.setVisibility(View.INVISIBLE);
				tv_radar_distance.setText(R.string.short_distance);
				Logger.logD("RadioFragment-----------------------isShowRadar---short_distance");
			}
			Logger.logD("RadioFragment-----------------------showRadar");
		} else {
			ll_radar_distance.setVisibility(View.GONE);
			tv_radar_track.setVisibility(View.GONE);
			Logger.logD("RadioFragment-----------------------hideRadar");
		}

	}

	/** 广播接收器 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Logger.logD("RadioFragment-----------------------receiver broadcast, action = " + action);
			if (Intent.ACTION_SHUTDOWN.equals(action)) {// 关机广播
				// 停止播放
				mRadioHandler.sendEmptyMessage(RadioConstants.STOP_PLAY);
			} else if (RadioConstants.ACTION_RADIO_STERO_INFO.equals(action)) {
				int status = intent.getIntExtra("status", 1);
				RadioStatus.radarTrackStatus = status;
				mRadioHandler.obtainMessage(RadioConstants.MSG_RADIO_STERO_INFO, status, 0).sendToTarget();
			}
		}
	};

	/** 音频焦点监听器 */
	private OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:// 暂时失去AudioFocus，但是可以继续播放（如导航时），不过要降低音量。
				Logger.logD("RadioFragment-----------------------AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
				// RadioStatus.hasFocus = false;
				// 设置混音最低比例
				mRadioVolumeModel.setLowestVolume(AppUtil.calNavMixLowestRatio(getContext()));
				mRadioVolumeModel.fadeDownVolume();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:// 暂时失去了AudioFocus,但很快会重新得到焦点（如来电时），必须停止Audio的播放，但是因为可能会很快再次获得AudioFocus，这里可以不释放Media资源。
				Logger.logD("RadioFragment-----------------------AUDIOFOCUS_LOSS_TRANSIENT");
				RadioStatus.hasFocus = false;
				mRadioVolumeModel.setLowestVolume(0);
				mRadioHandler.removeMessages(RadioConstants.STOP_PLAY);
				mRadioHandler.sendEmptyMessage(RadioConstants.STOP_PLAY);
				break;
			case AudioManager.AUDIOFOCUS_LOSS:// 失去AudioFocus，并将会持续很长的时间（如播放音乐或视频时）
				Logger.logD("RadioFragment-----------------------AUDIOFOCUS_LOSS");
				RadioStatus.hasFocus = false;
				mRadioVolumeModel.setLowestVolume(0);
				mRadioHandler.removeMessages(RadioConstants.STOP_PLAY);
				mRadioHandler.sendEmptyMessage(RadioConstants.STOP_PLAY);
				// 一失去永久焦点就释放音频焦点
				abandonAudioFocus();
				break;
			case AudioManager.AUDIOFOCUS_GAIN:// 获得AudioFocus
				Logger.logD("RadioFragment-----------------------AUDIOFOCUS_GAIN");
				RadioStatus.hasFocus = true;
				// 设置当前音频永久焦点拥有者为收音机
				// AudioFocusModel.getInstance().setAudioFocusOwner(AudioFocusModel.OWNER_RADIO);
				if (!isOpened() || RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER
						|| RadioStatus.isSearchingFM || RadioStatus.isSearchingAM) {// 如果没有打开，或者正在搜索
					return;
				}
				if (mRadioVolumeModel.getLowestVolume() > 0) {// 说明是AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK，拉高声音即可
					mRadioVolumeModel.fadeUpVolume();
				} else {
					mRadioHandler.removeMessages(RadioConstants.START_PLAY);
					mRadioHandler.sendEmptyMessage(RadioConstants.START_PLAY);
				}
				break;
			default:
				break;
			}
		}
	};

	/** 申请音频焦点 */
	private int requestAudioFocus() {
		// if
		// (AudioFocusModel.getInstance().isAudioFocusBelong(AudioFocusModel.OWNER_RADIO))
		// {// 音频焦点已经属于收音机
		// Logger.logD("RadioFragment-----------------------已经拥有音频永久焦点，无需重复申请");
		// RadioStatus.hasFocus = true;
		// return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
		// }
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
		}
		int audioFocusState = mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);// 获取音频永久焦点
		RadioStatus.hasFocus = true;
		if (audioFocusState == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {// 申请音频永久焦点成功
			// 设置音频永久焦点拥有者为收音机
			// AudioFocusModel.getInstance().setAudioFocusOwner(AudioFocusModel.OWNER_RADIO);
			Logger.logD("RadioFragment-----------------------requestAudioFocus success");
		} else {// 申请音频永久焦点失败
			Logger.logE("RadioFragment-----------------------requestAudioFocus fail");
		}
		// 注册实体按键监听器
		ProtocolUtil.getInstance(getContext()).registerKeyPressListener(mIKeyPressInterface);
		return audioFocusState;
	}

	/** 释放音频焦点 */
	private int abandonAudioFocus() {
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
		}
		int audioFocusState = mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
		if (audioFocusState == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {// 释放音频永久焦点成功
			Logger.logD("RadioFragment-----------------------abandonAudioFocus success");
			RadioStatus.hasFocus = false;
			// 释放实体按键监听器
			ProtocolUtil.getInstance(getContext()).unregisterKeyPressListener(mIKeyPressInterface);
		} else {// 释放音频永久焦点失败
			Logger.logE("RadioFragment-----------------------abandonAudioFocus fail");
		}
		return audioFocusState;
	}

	/** 中控实体按钮监听 */
	private android.os.IKeyPressInterface mIKeyPressInterface = new android.os.IKeyPressInterface.Stub() {

		public void onKeyPressed(int keyCode, int mode) {
			switch (keyCode) {
			case CommonConstants.KEY_PREV:// 按键上一曲
				Logger.logD("KEY_PREV------------------------上一个强信号台, keyCode = " + keyCode + " mode = " + mode);
				mRadioHandler.removeMessages(RadioConstants.MSG_RADIO_PREVIOUS);
				mRadioHandler.removeMessages(RadioConstants.MSG_RADIO_NEXT);
				mRadioHandler.removeMessages(RadioConstants.MSG_RADIO_SWITCH);
				mRadioHandler.sendEmptyMessage(RadioConstants.MSG_RADIO_PREVIOUS);
				break;
			case CommonConstants.KEY_NEXT:// 按键下一曲
				Logger.logD("KEY_NEXT------------------------下一个强信号台, keyCode = " + keyCode + " mode = " + mode);
				mRadioHandler.removeMessages(RadioConstants.MSG_RADIO_PREVIOUS);
				mRadioHandler.removeMessages(RadioConstants.MSG_RADIO_NEXT);
				mRadioHandler.removeMessages(RadioConstants.MSG_RADIO_SWITCH);
				mRadioHandler.sendEmptyMessage(RadioConstants.MSG_RADIO_NEXT);
				break;

			case CommonConstants.KEY_RADIO_SWITCH:// Radio按键
				Logger.logD(
						"KEY_RADIO_SWITCH------------------------Radio按键, keyCode = " + keyCode + " mode = " + mode);
				mRadioHandler.removeMessages(RadioConstants.MSG_RADIO_PREVIOUS);
				mRadioHandler.removeMessages(RadioConstants.MSG_RADIO_NEXT);
				mRadioHandler.removeMessages(RadioConstants.MSG_RADIO_SWITCH);
				mRadioHandler.sendEmptyMessage(RadioConstants.MSG_RADIO_SWITCH);
				break;
			default:
				break;
			}
		}

		public String onGetAppInfo() {// 返回各应用名字标记
			return "radio";
		}

	};

	@Override
	public void onDestroy() {
		Logger.logD("RadioFragment-----------------------onDestroy");
		// 移除所有消息
		mRadioHandler.removeCallbacksAndMessages(null);
		// 停止播放，声音渐渐关闭
		mRadioHandler.sendEmptyMessage(RadioConstants.STOP_PLAY);
		// 释放音频焦点
		abandonAudioFocus();
		// 注销
		unregister();

		super.onDestroy();
	}

	@Override
	public void unregister() {
		getContext().unregisterReceiver(mReceiver);

		mISwitchFMAMModel.removeISwitchFMAMCallback(this);

		mIRadioSwitchModel.removeIRadioSwitchCallback(this);

		mISearchNearStrongRadioModel.removeISearchNearStrongRadioCallback(this);

		mRadioInfoReceiver.removeIRadioInfoCallback(this);
		mRadioInfoReceiver.unregisterRadioInfoReceiver();
	}

	@Override
	public void onRadioSwitchOn() {
		btn_switch.setText(R.string.close);
		mRadioHandler.removeMessages(RadioConstants.MSG_SEARCH_NEAR_STRONG_RADIO);
		mRadioHandler.removeMessages(RadioConstants.START_PLAY);
		mRadioHandler.sendEmptyMessage(RadioConstants.START_PLAY);
	}

	@Override
	public void onRadioSwitchOff() {
//		btn_switch.setText(R.string.open);
		mRadioHandler.removeMessages(RadioConstants.MSG_SEARCH_NEAR_STRONG_RADIO);
		mRadioVolumeModel.setLowestVolume(0);
		mRadioHandler.removeMessages(RadioConstants.STOP_PLAY);
		mRadioHandler.sendEmptyMessage(RadioConstants.STOP_PLAY);
	}

	@Override
	public void onSearchNearStrongRadioResult(boolean isFinish, int frequency) {
		sendSearchNearStrongRadioResultMessage(isFinish, frequency);
	}

	@Override
	public void onRadioInfoSearchNearStrongRadioResult(boolean isFinish, int frequency) {
		sendSearchNearStrongRadioResultMessage(isFinish, frequency);
	}

	@Override
	public void onSwitchFMAMPrepare(int radioType, boolean resetFragment) {
		if (resetFragment) {
			switchFragment(radioType);
		}
	}

	@Override
	public void beginSwitchFMToFM() {// FM切换到FM回调
	}

	@Override
	public void beginSwitchFMToAM() {// FM切换到AM回调
	}

	@Override
	public void beginSwitchAMToFM() {// AM切换到FM回调
	}

	@Override
	public void beginSwitchAMToAM() {// AM切换到AM回调
	}

	@Override
	public void beginSwitchFMToFMWhenSearchNearStrongRadio() {// 搜索上下一个强信号台时，FM切换到FM回调
	}

	@Override
	public void beginSwitchFMToAMWhenSearchNearStrongRadio() {// 搜索上下一个强信号台时，FM切换到AM回调
		// 停止动态刷新底部显示
		mRadioHandler.removeMessages(RadioConstants.MSG_SEARCH_NEAR_STRONG_RADIO);
	}

	@Override
	public void beginSwitchAMToFMWhenSearchNearStrongRadio() {// 搜索上下一个强信号台时，AM切换到FM回调
		// 停止动态刷新底部显示
		mRadioHandler.removeMessages(RadioConstants.MSG_SEARCH_NEAR_STRONG_RADIO);
	}

	@Override
	public void beginSwitchAMToAMWhenSearchNearStrongRadio() {// 搜索上下一个强信号台时，AM切换到AM回调
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (isTouchable) {
			int i = event.getAction();
			switch (v.getId()) {
			case R.id.btn_previous:
				if (i == MotionEvent.ACTION_DOWN) {
					mIStepRadioModel.isStepStart(true);
					mIStepRadioModel.stepBack();
					mRadioHandler.removeMessages(RadioConstants.MSG_CANCLE_LONGCLICK);
				} else if (i == MotionEvent.ACTION_UP) {
					mIStepRadioModel.isStepStart(false);
					mIStepRadioModel.stepPlayRadio();
					// 步进状态下，3秒无操作就取消步进功能
					mRadioHandler.removeMessages(RadioConstants.MSG_CANCLE_LONGCLICK);
					mRadioHandler.sendEmptyMessageDelayed(RadioConstants.MSG_CANCLE_LONGCLICK, 3000);
				}
				break;
			case R.id.btn_next:
				if (i == MotionEvent.ACTION_DOWN) {
					mIStepRadioModel.isStepStart(true);
					mIStepRadioModel.stepForward();
					mRadioHandler.removeMessages(RadioConstants.MSG_CANCLE_LONGCLICK);
				} else if (i == MotionEvent.ACTION_UP) {
					mIStepRadioModel.isStepStart(false);
					mIStepRadioModel.stepPlayRadio();
					mRadioHandler.removeMessages(RadioConstants.MSG_CANCLE_LONGCLICK);
					mRadioHandler.sendEmptyMessageDelayed(RadioConstants.MSG_CANCLE_LONGCLICK, 3000);
				}
				break;
			}
		}
		return false;
	}

	@Override
	public boolean onLongClick(View v) {
		if (isFirstLongClick) {
			isFirstLongClick = false;
			isTouchable = true;
			Logger.logD("RadioFragment-----------------上一个、下一个按钮进入步进模式");
		}
		return false;
	}

	@Override
	public void stopSearchWhenSwitch() {
		// TODO Auto-generated method stub
		
	}
}
