package com.semisky.ym_multimedia.radio.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.radio.dao.AMChannelDBManager;
import com.semisky.ym_multimedia.radio.dao.FMChannelDBManager;
import com.semisky.ym_multimedia.radio.utils.PreferencesUtil;
import com.semisky.ym_multimedia.radio.utils.ProtocolUtil;
import com.semisky.ym_multimedia.radio.utils.RadioConstants;
import com.semisky.ym_multimedia.radio.utils.RadioStatus;
import com.semisky.ym_multimedia.radio.utils.RadioStatus.SearchNearStrongChannel;

/**
 * 接收频点信息反馈（包含搜索所有电台和搜索上下一个强信号台）
 * 
 * @author Anter
 * 
 */
public class RadioInfoReceiver {
	private Context mContext;
	private static RadioInfoReceiver instance;
	private List<IRadioInfoCallback> callbacks;

	public RadioInfoReceiver(Context context) {
		this.mContext = context;
		callbacks = new ArrayList<IRadioInfoCallback>();
	}

	public static RadioInfoReceiver getInstance(Context context) {
		if (instance == null) {
			instance = new RadioInfoReceiver(context);
		}
		return instance;
	}

	public void registerRadioInfoReceiver() {
		ProtocolUtil.getInstance(mContext).registerRadioInfoReceiver(
				radioInfoReceiver);
	}

	public void unregisterRadioInfoReceiver() {
		ProtocolUtil.getInstance(mContext).unregisterRadioInfoReceiver(
				radioInfoReceiver);
	}

	public void addIRadioInfoCallback(IRadioInfoCallback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}
	}

	public void removeIRadioInfoCallback(IRadioInfoCallback callback) {
		callbacks.remove(callback);
	}

	/**
	 * 接收频道搜索信息接口（不在主线程中）
	 */
	private android.os.IRadioInfoInterface radioInfoReceiver = new android.os.IRadioInfoInterface.Stub() {

		@Override
		public void onRadioInfoChanged(final int frequency, int strength) {
			Logger.logD("onRadioInfoChanged frequency---" + frequency
					+ ",strength---" + strength);
			if (frequency == -1 || frequency == RadioConstants.SEARCH_OVER_CODE) {// 无强信号频段（搜索结束时会发）
				Logger.logD("receive radio:frequency = " + frequency);
				if (RadioStatus.isSearchingFM) {// 搜索所有FM频道结束
					Logger.logD("receive radio:search fm finish");
					RadioStatus.isSearchingFM = false;
					RadioStatus.isSearchingInterrupted = false;
					// dismiss dialog
					ISearchAllFMModelImp.getInstance(mContext)
							.notifyObserversSearchAllFMFinish(frequency);
				} else if (RadioStatus.isSearchingAM) {// 搜索所有AM频道结束
					Logger.logD("receive radio:search am finish");
					RadioStatus.isSearchingAM = false;
					RadioStatus.isSearchingInterrupted = false;
					// dismiss dialog
					ISearchAllAMModelImp.getInstance(mContext)
							.notifyObserversSearchAllAMFinish(frequency);
				} else {// 既没在搜索所有FM频道，也没在搜索所有AM频道，说明是在搜索上下一个强信号台
					switch (RadioStatus.searchNearState) {
					case PREVIOUS:// 搜索上一个强信号台
						Logger.logD("receive radio:no previous strong channel");
						RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
						// 无上一个强信号台，置为最小频点
						// 保存频道信息
						switch (RadioStatus.currentType) {
						case RadioConstants.TYPE_FM:
							RadioStatus.currentFrequency = RadioConstants.FMMIN;
							PreferencesUtil.getInstance().saveLatestRadioInfo(
									mContext, RadioConstants.FMMIN,
									RadioConstants.TYPE_FM);
							// 通知Fragment和Activity刷新频点显示
							for (IRadioInfoCallback callback : callbacks) {
								if (callback != null) {
									callback.onRadioInfoSearchNearStrongRadioResult(
											true, RadioConstants.FMMIN);
								}
							}
							break;
						case RadioConstants.TYPE_AM:
							RadioStatus.currentFrequency = RadioConstants.AMMIN;
							PreferencesUtil.getInstance().saveLatestRadioInfo(
									mContext, RadioConstants.AMMIN,
									RadioConstants.TYPE_AM);
							// 通知Fragment和Activity刷新频点显示
							for (IRadioInfoCallback callback : callbacks) {
								if (callback != null) {
									callback.onRadioInfoSearchNearStrongRadioResult(
											true, RadioConstants.AMMIN);
								}
							}
							break;
						default:
							break;
						}
						break;
					case NEXT:// 搜索下一个强信号台
						Logger.logD("receive radio:no next strong channel");
						RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
						// 无下一个强信号台，置为最大频点
						// 保存频道信息
						switch (RadioStatus.currentType) {
						case RadioConstants.TYPE_FM:
							RadioStatus.currentFrequency = RadioConstants.FMMAX;
							PreferencesUtil.getInstance().saveLatestRadioInfo(
									mContext, RadioConstants.FMMAX,
									RadioConstants.TYPE_FM);
							// 通知Fragment和Activity刷新频点显示
							for (IRadioInfoCallback callback : callbacks) {
								if (callback != null) {
									callback.onRadioInfoSearchNearStrongRadioResult(
											true, RadioConstants.FMMAX);
								}
							}
							break;
						case RadioConstants.TYPE_AM:
							RadioStatus.currentFrequency = RadioConstants.AMMAX;
							PreferencesUtil.getInstance().saveLatestRadioInfo(
									mContext, RadioConstants.AMMAX,
									RadioConstants.TYPE_AM);
							// 通知Fragment和Activity刷新频点显示
							for (IRadioInfoCallback callback : callbacks) {
								if (callback != null) {
									callback.onRadioInfoSearchNearStrongRadioResult(
											true, RadioConstants.AMMAX);
								}
							}
							break;
						default:
							break;
						}
						break;
					/**
					 * 因为弹框一旦消失，会立即播放一个频点，播放频点会中断搜索，并且不会发65535中断码，所以这块代码无效
					 */
					// case NEITHER://
					// 既不搜索上一个强信号台也不搜索下一个强信号台（出现情况：搜索所有FM或者AM信号台的时候，用户手动关闭了搜索弹框，表示中断搜索。但是并没有中断命令，所以选择不接收，但是还是会发送65535过来）
					// Logger.logD("receive radio:search finish after user interrupt");
					// // 即使用户打断了搜索所有FM或AM，实际还是在搜索，只是不接收而已
					// RadioStatus.isSearchingInterrupted = false;
					// break;
					default:
						break;
					}
				}
			} else {// 有效结果反馈
				Logger.logD("receive radio:frequency = " + frequency
						+ "; strength = " + strength);
				if (RadioStatus.isSearchingFM) {// 正在搜索FM
					if (strength <= 0) {
						Logger.logD("receive FM radio:" + frequency
								+ " strength <= 0,do not add to db");
					} else {
						Logger.logD("receive radio:add fm channel to db");
						// 添加FM数据库
						FMChannelDBManager.getInstance(mContext)
								.insertFMChannel("", frequency, strength);
					}
					// 信号不管为不为0都显示一遍
					ISearchAllFMModelImp.getInstance(mContext)
							.notifyObserversSearchAllFMUnFinish(frequency);
				} else if (RadioStatus.isSearchingAM) {// 正在搜索AM
					if (strength <= 0) {
						Logger.logD("receive AM radio:" + frequency
								+ " strength <= 0,do not add to db");
					} else {
						Logger.logD("receive radio:add am channel to db");
						// 添加AM数据库
						AMChannelDBManager.getInstance(mContext)
								.insertAMChannel("", frequency, strength);
					}
					// 信号为0不为0都显示一遍
					ISearchAllAMModelImp.getInstance(mContext)
							.notifyObserversSearchAllAMUnFinish(frequency);
				} else {
					if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 说明是在搜索上下一个强信号台
						Logger.logD("receive radio:searching near strong channel, frequency = "
								+ frequency);
						if (strength > 0) {// 信号强度大于0才有效，搜索成功
							Logger.logD("receive radio:finally, result frequency = "
									+ frequency);
							RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
							RadioStatus.currentFrequency = frequency;
							// 保存频道信息
							PreferencesUtil.getInstance().saveLatestRadioInfo(
									mContext, frequency,
									RadioStatus.currentType);
							// 通知Fragment和Activity刷新频点显示
							for (IRadioInfoCallback callback : callbacks) {
								if (callback != null) {
									callback.onRadioInfoSearchNearStrongRadioResult(
											true, frequency);
								}
							}
						} else {
							// 通知Fragment和Activity刷新频点显示
							for (IRadioInfoCallback callback : callbacks) {
								if (callback != null) {
									callback.onRadioInfoSearchNearStrongRadioResult(
											false, frequency);
								}
							}
						}
						RadioStatus.searchNearShowingFrequency = frequency;
					}
				}
			}
		}

	};

}
