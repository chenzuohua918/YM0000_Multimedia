package com.semisky.ym_multimedia.radio.model;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.radio.dao.FMChannelDBManager;
import com.semisky.ym_multimedia.radio.event.SearchFMResultEvent;
import com.semisky.ym_multimedia.radio.event.SearchFMResultEvent.SearchFMState;
import com.semisky.ym_multimedia.radio.utils.ProtocolUtil;
import com.semisky.ym_multimedia.radio.utils.RadioConstants;
import com.semisky.ym_multimedia.radio.utils.RadioStatus;
import com.semisky.ym_multimedia.radio.utils.RadioStatus.SearchNearStrongChannel;
import com.ypy.eventbus.EventBus;

/**
 * 搜索所有FM电台的Model实现类
 * 
 * @author Anter
 * 
 */
public class ISearchAllFMModelImp implements ISearchAllFMModel {
	private Context mContext;
	private static ISearchAllFMModelImp instance;
	private SearchHandler mHandler;

	private static final int MSG_TIME_OUT = 0;

	public ISearchAllFMModelImp(Context context) {
		this.mContext = context;
		mHandler = new SearchHandler(this);
	}

	public static ISearchAllFMModelImp getInstance(Context context) {
		if (instance == null) {
			instance = new ISearchAllFMModelImp(context);
		}
		return instance;
	}

	private static class SearchHandler extends Handler {
		private static WeakReference<ISearchAllFMModelImp> mReference;

		public SearchHandler(ISearchAllFMModelImp modelImp) {
			mReference = new WeakReference<ISearchAllFMModelImp>(modelImp);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mReference.get() == null) {
				return;
			}
			
			switch (msg.what) {
			case MSG_TIME_OUT:// 超时
				if (RadioStatus.isSearchingFM) {// 为true说明没有正常结束搜索，此时手动停止
					Logger.logD("Search FM timeout");
					RadioStatus.isSearchingFM = false;
					// dismiss dialog
					mReference.get().notifyObserversSearchAllFMTimeout(
							RadioConstants.SEARCH_OVER_CODE);
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void searchAllFM() {
		if (RadioStatus.searchNearState == SearchNearStrongChannel.NEITHER) {
			RadioStatus.isSearchingFM = true;
			// 如果RadioInfoReceiver那没有置为false，则说明是用户中断了搜索
			RadioStatus.isSearchingInterrupted = true;
			// 清空列表
			FMChannelDBManager.getInstance(mContext).deleteAllFMChannels();
			notifyObserversListClear();
			// 开始搜索
			ProtocolUtil.getInstance(mContext).searchAllFM();
			mHandler.removeMessages(MSG_TIME_OUT);
			mHandler.sendEmptyMessageDelayed(MSG_TIME_OUT,
					RadioConstants.DURATION_SEARCH_TIME_OUT);
		}
	}

	private void removeTimeoutMessage() {
		mHandler.removeMessages(MSG_TIME_OUT);
	}

	@Override
	public void notifyObserversListClear() {
		EventBus.getDefault().post(
				new SearchFMResultEvent(RadioConstants.SEARCH_OVER_CODE,
						SearchFMState.CLEARLIST));
	}

	@Override
	public void notifyObserversSearchAllFMFinish(int frequency) {
		removeTimeoutMessage();
		EventBus.getDefault().post(
				new SearchFMResultEvent(frequency, SearchFMState.FINISH));
	}

	@Override
	public void notifyObserversSearchAllFMUnFinish(int frequency) {
		removeTimeoutMessage();
		EventBus.getDefault().post(
				new SearchFMResultEvent(frequency, SearchFMState.UNFINISH));
	}

	@Override
	public void notifyObserversSearchAllFMInterrupt(int frequency) {
		removeTimeoutMessage();
		EventBus.getDefault().post(
				new SearchFMResultEvent(frequency, SearchFMState.INTERRUPT));
	}

	@Override
	public void notifyObserversSearchAllFMTimeout(int frequency) {
		removeTimeoutMessage();
		EventBus.getDefault().post(
				new SearchFMResultEvent(frequency, SearchFMState.TIMEOUT));
	}

}
