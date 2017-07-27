package com.semisky.ym_multimedia.radio.model;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.radio.dao.AMChannelDBManager;
import com.semisky.ym_multimedia.radio.event.SearchAMResultEvent;
import com.semisky.ym_multimedia.radio.event.SearchAMResultEvent.SearchAMState;
import com.semisky.ym_multimedia.radio.utils.ProtocolUtil;
import com.semisky.ym_multimedia.radio.utils.RadioConstants;
import com.semisky.ym_multimedia.radio.utils.RadioStatus;
import com.semisky.ym_multimedia.radio.utils.RadioStatus.SearchNearStrongChannel;
import com.ypy.eventbus.EventBus;

/**
 * 搜索所有AM电台的Model实现类
 * 
 * @author Anter
 * 
 */
public class ISearchAllAMModelImp implements ISearchAllAMModel {
	private Context mContext;
	private static ISearchAllAMModelImp instance;
	private SearchHandler mHandler;

	private static final int MSG_TIME_OUT = 0;

	public ISearchAllAMModelImp(Context context) {
		this.mContext = context;
		mHandler = new SearchHandler(this);
	}

	public static ISearchAllAMModelImp getInstance(Context context) {
		if (instance == null) {
			instance = new ISearchAllAMModelImp(context);
		}
		return instance;
	}

	private static class SearchHandler extends Handler {
		private static WeakReference<ISearchAllAMModelImp> mReference;

		public SearchHandler(ISearchAllAMModelImp modelImp) {
			mReference = new WeakReference<ISearchAllAMModelImp>(modelImp);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mReference.get() == null) {
				return;
			}
			
			switch (msg.what) {
			case MSG_TIME_OUT:// 超时
				if (RadioStatus.isSearchingAM) {// 为true说明没有正常结束搜索，此时手动停止
					Logger.logD("Search AM timeout");
					RadioStatus.isSearchingAM = false;
					// dismiss dialog
					mReference.get().notifyObserversSearchAllAMTimeout(
							RadioConstants.SEARCH_OVER_CODE);
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void searchAllAM() {
		if (RadioStatus.searchNearState == SearchNearStrongChannel.NEITHER) {
			RadioStatus.isSearchingAM = true;
			// 如果RadioInfoReceiver那没有置为false，则说明是用户中断了搜索
			RadioStatus.isSearchingInterrupted = true;
			// 清空列表
			AMChannelDBManager.getInstance(mContext).deleteAllAMChannels();
			notifyObserversListClear();
			// 开始搜索
			ProtocolUtil.getInstance(mContext).searchAllAM();
			mHandler.removeMessages(MSG_TIME_OUT);
			mHandler.sendEmptyMessageDelayed(MSG_TIME_OUT,
					RadioConstants.DURATION_SEARCH_TIME_OUT);
		}
	}

	@Override
	public void notifyObserversListClear() {
		EventBus.getDefault().post(
				new SearchAMResultEvent(RadioConstants.SEARCH_OVER_CODE,
						SearchAMState.CLEARLIST));
	}

	@Override
	public void notifyObserversSearchAllAMFinish(int frequency) {
		EventBus.getDefault().post(
				new SearchAMResultEvent(frequency, SearchAMState.FINISH));
	}

	@Override
	public void notifyObserversSearchAllAMUnFinish(int frequency) {
		EventBus.getDefault().post(
				new SearchAMResultEvent(frequency, SearchAMState.UNFINISH));
	}

	@Override
	public void notifyObserversSearchAllAMInterrupt(int frequency) {
		EventBus.getDefault().post(
				new SearchAMResultEvent(frequency, SearchAMState.INTERRUPT));
	}

	@Override
	public void notifyObserversSearchAllAMTimeout(int frequency) {
		EventBus.getDefault().post(
				new SearchAMResultEvent(frequency, SearchAMState.TIMEOUT));
	}

}
