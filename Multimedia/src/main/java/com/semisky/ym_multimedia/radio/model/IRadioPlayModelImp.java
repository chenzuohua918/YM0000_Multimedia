package com.semisky.ym_multimedia.radio.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.radio.utils.RadioUtil;
import com.semisky.ym_multimedia.radio.utils.PreferencesUtil;
import com.semisky.ym_multimedia.radio.utils.ProtocolUtil;
import com.semisky.ym_multimedia.radio.utils.RadioConstants;
import com.semisky.ym_multimedia.radio.utils.RadioStatus;
import com.semisky.ym_multimedia.radio.utils.Toaster;
import com.semisky.ym_multimedia.radio.utils.RadioStatus.SearchNearStrongChannel;

/**
 * 播放频点的Model实现类
 * 
 * @author Anter
 * 
 */
public class IRadioPlayModelImp implements IRadioPlayModel {
	private Context mContext;
	private static IRadioPlayModelImp instance;
	private List<IRadioPlayCallback> callbacks;

	public IRadioPlayModelImp(Context context) {
		this.mContext = context;
		callbacks = new ArrayList<IRadioPlayCallback>();
	}

	public static IRadioPlayModelImp getInstance(Context context) {
		if (instance == null) {
			instance = new IRadioPlayModelImp(context);
		}
		return instance;
	}

	@Override
	public void addIRadioPlayCallback(IRadioPlayCallback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}
	}

	@Override
	public void removeIRadioPlayCallback(IRadioPlayCallback callback) {
		callbacks.remove(callback);
	}

	@Override
	public void playRadio(int targetType, int frequency) {
		switch (targetType) {
		case RadioConstants.TYPE_FM:
			if (!RadioUtil.inFMFrequencyRange(frequency)) {
				Toaster.makeText(mContext, R.string.invalid_fm_frequency);
				return;
			}
			// if (RadioStatus.isSearchingInterrupted) {//
			// 没有在RadioInfoReceiver被置false，说明搜索所有FM被中断了
			// EventBus.getDefault().post(
			// new SearchFMResultEvent(frequency,
			// SearchFMState.INTERRUPT));
			// }
			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {
				// 如果正在搜索上下一个强信号台时，播放频点会中断搜索，而且不会发出65535结束码，所以要自己重置变量
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				PreferencesUtil.getInstance().setLatestRadioType(mContext,
						RadioConstants.TYPE_FM);
				switch (RadioStatus.currentType) {
				case RadioConstants.TYPE_FM:
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, frequency, RadioConstants.TYPE_FM);
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).fmSearch(frequency);
					break;
				case RadioConstants.TYPE_AM:
//					PreferencesUtil.getInstance().setLatestRadioType(mContext,
//							RadioConstants.TYPE_FM);
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, RadioStatus.searchNearShowingFrequency,
							RadioConstants.TYPE_AM);
					RadioStatus.currentType = RadioConstants.TYPE_FM;
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).fmSearch(
							RadioStatus.currentFrequency);
					break;
				default:
					break;
				}
			} else {
				PreferencesUtil.getInstance().setLatestRadioType(mContext,
						RadioConstants.TYPE_FM);
				switch (RadioStatus.currentType) {
				case RadioConstants.TYPE_FM:
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, frequency, RadioConstants.TYPE_FM);
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).fmSearch(frequency);
					break;
				case RadioConstants.TYPE_AM:
//					PreferencesUtil.getInstance().setLatestRadioType(mContext,
//							RadioConstants.TYPE_FM);
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, RadioStatus.currentFrequency,
							RadioConstants.TYPE_AM);
					RadioStatus.currentType = RadioConstants.TYPE_FM;
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).fmSearch(
							RadioStatus.currentFrequency);
					break;
				default:
					break;
				}
			}
			for (IRadioPlayCallback callback : callbacks) {
				if (callback != null) {
					callback.onRadioPlay(RadioConstants.TYPE_FM, frequency);
				}
			}
			break;
		case RadioConstants.TYPE_AM:
			if (!RadioUtil.inAMFrequencyRange(frequency)) {
				Toaster.makeText(mContext, R.string.invalid_am_frequency);
				return;
			}
			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {
				// 如果正在搜索上下一个强信号台时，播放频点会中断搜索，而且不会发出65535结束码，所以要自己重置变量
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				PreferencesUtil.getInstance().setLatestRadioType(mContext,
						RadioConstants.TYPE_AM);
				switch (RadioStatus.currentType) {
				case RadioConstants.TYPE_FM:
//					PreferencesUtil.getInstance().setLatestRadioType(mContext,
//							RadioConstants.TYPE_AM);
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, RadioStatus.searchNearShowingFrequency,
							RadioConstants.TYPE_FM);
					RadioStatus.currentType = RadioConstants.TYPE_AM;
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).amSearch(
							RadioStatus.currentFrequency);
					break;
				case RadioConstants.TYPE_AM:
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, frequency, RadioConstants.TYPE_AM);
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).amSearch(frequency);
					break;
				default:
					break;
				}
			} else {
				PreferencesUtil.getInstance().setLatestRadioType(mContext,
						RadioConstants.TYPE_AM);
				switch (RadioStatus.currentType) {
				case RadioConstants.TYPE_FM:
//					PreferencesUtil.getInstance().setLatestRadioType(mContext,
//							RadioConstants.TYPE_AM);
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, RadioStatus.currentFrequency,
							RadioConstants.TYPE_FM);
					RadioStatus.currentType = RadioConstants.TYPE_AM;
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).amSearch(
							RadioStatus.currentFrequency);
					break;
				case RadioConstants.TYPE_AM:
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, frequency, RadioConstants.TYPE_AM);
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).amSearch(frequency);
					break;
				default:
					break;
				}
			}
			for (IRadioPlayCallback callback : callbacks) {
				if (callback != null) {
					callback.onRadioPlay(RadioConstants.TYPE_AM, frequency);
				}
			}
			break;
		default:
			break;
		}
	}

}
