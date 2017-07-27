package com.semisky.ym_multimedia.radio.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.radio.utils.PreferencesUtil;
import com.semisky.ym_multimedia.radio.utils.RadioConstants;
import com.semisky.ym_multimedia.radio.utils.RadioStatus;
import com.semisky.ym_multimedia.radio.utils.RadioStatus.SearchNearStrongChannel;

/**
 * 切换FM／AM的Model实现类
 * 
 * @author Anter
 * 
 */
public class ISwitchFMAMModelImp implements ISwitchFMAMModel {
	private Context mContext;
	private static ISwitchFMAMModelImp instance;
	private List<ISwitchFMAMCallback> callbacks;

	public ISwitchFMAMModelImp(Context context) {
		this.mContext = context;
		callbacks = new ArrayList<ISwitchFMAMCallback>();
	}

	public static ISwitchFMAMModelImp getInstance(Context context) {
		if (instance == null) {
			instance = new ISwitchFMAMModelImp(context);
		}
		return instance;
	}

	@Override
	public void addISwitchFMAMCallback(ISwitchFMAMCallback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}
	}

	@Override
	public void removeISwitchFMAMCallback(ISwitchFMAMCallback callback) {
		callbacks.remove(callback);
	}

	@Override
	public void switchRadioType(int radioType, boolean resetFragment) {
		switch (radioType) {
		case RadioConstants.TYPE_FM:// 切换到FM
			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER || RadioStatus.isSearchingAM||RadioStatus.isSearchingFM) {
				Logger.logD("ISwitchFMAMModelImp-------正在搜索AM，得先停止才能切换");
				switch (RadioStatus.currentType) {
				case RadioConstants.TYPE_AM:
					IRadioPlayModelImp.getInstance(mContext).playRadio(RadioConstants.TYPE_AM, RadioConstants.AMMIN);
					break;
				}
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				RadioStatus.isSearchingAM = false;
				RadioStatus.isSearchingFM = false;
				for (ISwitchFMAMCallback callback : callbacks) {
					if (callback != null) {
						callback.stopSearchWhenSwitch();
					}
				}
			} else {
				for (ISwitchFMAMCallback callback : callbacks) {
					if (callback != null) {
						callback.onSwitchFMAMPrepare(radioType, resetFragment);
					}
				}
				switch (RadioStatus.currentType) {
				case RadioConstants.TYPE_FM:
					for (ISwitchFMAMCallback callback : callbacks) {
						if (callback != null) {
							callback.beginSwitchFMToFM();
						}
					}
					break;
				case RadioConstants.TYPE_AM:
					RadioStatus.currentType = RadioConstants.TYPE_FM;
					RadioStatus.currentFrequency = PreferencesUtil.getInstance().getLatestFMRadioFrequency(mContext);
					for (ISwitchFMAMCallback callback : callbacks) {
						if (callback != null) {
							callback.beginSwitchAMToFM();
						}
					}
					IRadioPlayModelImp.getInstance(mContext).playRadio(RadioStatus.currentType,
							RadioStatus.currentFrequency);
					break;
				default:
					break;
				}
			}

			break;
		case RadioConstants.TYPE_AM:// 切换到AM
			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER || RadioStatus.isSearchingFM|| RadioStatus.isSearchingAM) {
				Logger.logD("ISwitchFMAMModelImp-------正在搜索FM，得先停止才能切换");
				switch (RadioStatus.currentType) {
				case RadioConstants.TYPE_FM:
					IRadioPlayModelImp.getInstance(mContext).playRadio(RadioConstants.TYPE_FM, RadioConstants.FMMIN);
					break;
				}
				for (ISwitchFMAMCallback callback : callbacks) {
					if (callback != null) {
						callback.stopSearchWhenSwitch();
					}
				}
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				RadioStatus.isSearchingFM = false;
				RadioStatus.isSearchingAM = false;
			} else {
				for (ISwitchFMAMCallback callback : callbacks) {
					if (callback != null) {
						callback.onSwitchFMAMPrepare(radioType, resetFragment);
					}
				}
				switch (RadioStatus.currentType) {
				case RadioConstants.TYPE_FM:
					RadioStatus.currentType = RadioConstants.TYPE_AM;
					RadioStatus.currentFrequency = PreferencesUtil.getInstance().getLatestAMRadioFrequency(mContext);
					for (ISwitchFMAMCallback callback : callbacks) {
						if (callback != null) {
							callback.beginSwitchFMToAM();
						}
					}
					IRadioPlayModelImp.getInstance(mContext).playRadio(RadioStatus.currentType,
							RadioStatus.currentFrequency);
					break;
				case RadioConstants.TYPE_AM:
					for (ISwitchFMAMCallback callback : callbacks) {
						if (callback != null) {
							callback.beginSwitchAMToAM();
						}
					}
					break;
				default:
					break;
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 切换到音乐视频蓝牙的时候，如果是正在搜索，则停止搜索并在返回的时候播放最小频点
	 */
	@Override
	public void jumpBackPlayMin() {
		if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER || RadioStatus.isSearchingFM
				|| RadioStatus.isSearchingAM) {
			switch (RadioStatus.currentType) {
			case RadioConstants.TYPE_FM:
				IRadioPlayModelImp.getInstance(mContext).playRadio(RadioStatus.currentType, RadioConstants.FMMIN);
				break;
			case RadioConstants.TYPE_AM:
				IRadioPlayModelImp.getInstance(mContext).playRadio(RadioStatus.currentType, RadioConstants.AMMIN);
				break;
			default:
				break;
			}
		}
		RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
		RadioStatus.isSearchingFM = false;
		RadioStatus.isSearchingAM = false;
	}
}
