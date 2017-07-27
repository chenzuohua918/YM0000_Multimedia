package com.semisky.ym_multimedia.radio.model;

import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.radio.utils.PreferencesUtil;
import com.semisky.ym_multimedia.radio.utils.RadioConstants;
import com.semisky.ym_multimedia.radio.utils.RadioStatus;
import com.semisky.ym_multimedia.radio.utils.SettingsUtil;
import com.semisky.ym_multimedia.radio.utils.Toaster;
import com.semisky.ym_multimedia.radio.utils.RadioStatus.SearchNearStrongChannel;

import android.content.Context;

/**
 * @ClassName: IStepRadioModelImp
 * @Description: 收音机步进Model实现类
 * @author: WAYSlDE
 */
public class IStepRadioModelImp implements IStepRadioModel {
	private Context mContext;
	private static IStepRadioModelImp instance;
	private boolean isStart = false;
	private IRadioPlayModel mIRadioPlayModel;
//	Thread forwardThread = new Thread(new StepForwardThread());
//	Thread backThread = new Thread(new StepBackThread());

	public IStepRadioModelImp(Context context) {
		this.mContext = context;
	}

	public static synchronized IStepRadioModelImp getInstance(Context context) {
		if (instance == null) {
			instance = new IStepRadioModelImp(context);
		}
		return instance;
	}

	@Override
	public void isStepStart(boolean isTrue) {
		this.isStart = isTrue;
	}

	@Override
	public void stepForward() {
		if (RadioStatus.hasFocus && !RadioStatus.isSearchingFM && !RadioStatus.isSearchingAM) {// 符合这些条件才能步进搜索
			if (!SettingsUtil.getInstance().isRadioLatestOpened(mContext)) {// 开关没打开
				Toaster.makeText(mContext.getApplicationContext(), R.string.message_open_first);
				return;
			}
			new Thread(new StepForwardThread()).start();
		}
	}

	@Override
	public void stepBack() {
		if (RadioStatus.hasFocus && !RadioStatus.isSearchingFM && !RadioStatus.isSearchingAM) {// 符合这些条件才能步进搜索
			if (!SettingsUtil.getInstance().isRadioLatestOpened(mContext)) {// 开关没打开
				Toaster.makeText(mContext.getApplicationContext(), R.string.message_open_first);
				return;
			}
			new Thread(new StepBackThread()).start();
		}
	}

	@Override
	public void stepPlayRadio() {
		mIRadioPlayModel = IRadioPlayModelImp.getInstance(mContext);
		mIRadioPlayModel.playRadio(RadioStatus.currentType,
				PreferencesUtil.getInstance().getLatestRadioFrequency(mContext));

	}

	private void stepForwardRadio() {
		switch (RadioStatus.currentType) {
		case RadioConstants.TYPE_FM:
//			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 如果正在搜索上下一个强FM台，停止
//				Logger.logD("searching near strong FM radio, click previous strong radio button to stop");
//				RadioStatus.currentFrequency -= RadioConstants.FM_STEP;
//				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
//				PreferencesUtil.getInstance().saveLatestRadioInfo(mContext, RadioStatus.currentFrequency,
//						RadioConstants.TYPE_FM);
//				Logger.logD("IStepRadioModelImp---------------------"+RadioStatus.currentFrequency);
//				break;
//			}

			if (RadioStatus.currentFrequency == RadioConstants.FMMAX) {// 已是最大FM频点
				// 从最小FM频点开始
				RadioStatus.currentFrequency = RadioConstants.FMMIN;
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				PreferencesUtil.getInstance().saveLatestRadioInfo(mContext, RadioConstants.FMMIN,
						RadioConstants.TYPE_FM);
				Logger.logD("IStepRadioModelImp---------------------"+RadioStatus.currentFrequency);
			}else{
				RadioStatus.currentFrequency += RadioConstants.FM_STEP;
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				PreferencesUtil.getInstance().saveLatestRadioInfo(mContext, RadioStatus.currentFrequency,
						RadioConstants.TYPE_FM);
				Logger.logD("IStepRadioModelImp---------------------"+RadioStatus.currentFrequency);
			}
			break;
		case RadioConstants.TYPE_AM:
//			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 如果正在搜索上下一个强AM台，停止
//				Logger.logD("searching near strong AM radio, click previous strong radio button to stop");
//				RadioStatus.currentFrequency -= RadioConstants.AM_STEP;
//				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
//				PreferencesUtil.getInstance().saveLatestRadioInfo(mContext, RadioStatus.currentFrequency,
//						RadioConstants.TYPE_AM);
//				Logger.logD("IStepRadioModelImp---------------------"+RadioStatus.currentFrequency);
//				break;
//			}

			if (RadioStatus.currentFrequency == RadioConstants.AMMAX) {// 已是最大AM频点
				// 从最小AM频点开始
				RadioStatus.currentFrequency = RadioConstants.AMMIN;
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				PreferencesUtil.getInstance().saveLatestRadioInfo(mContext, RadioConstants.AMMIN,
						RadioConstants.TYPE_AM);
				Logger.logD("IStepRadioModelImp---------------------"+RadioStatus.currentFrequency);
			}else{
				RadioStatus.currentFrequency += RadioConstants.AM_STEP;
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				PreferencesUtil.getInstance().saveLatestRadioInfo(mContext, RadioStatus.currentFrequency,
						RadioConstants.TYPE_AM);
				Logger.logD("IStepRadioModelImp---------------------"+RadioStatus.currentFrequency);
			}
			break;
		default:
			break;
		}
	}

	private void stepBackRadio() {
		switch (RadioStatus.currentType) {
		case RadioConstants.TYPE_FM:
//			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 如果正在搜索上下一个强FM台，停止
//				Logger.logD("searching near strong FM radio, click previous strong radio button to stop");
//				RadioStatus.currentFrequency -= RadioConstants.FM_STEP;
//				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
//				PreferencesUtil.getInstance().saveLatestRadioInfo(mContext, RadioStatus.currentFrequency,
//						RadioConstants.TYPE_FM);
//				Logger.logD("IStepRadioModelImp---------------------"+RadioStatus.currentFrequency);
//				break;
//			}

			if (RadioStatus.currentFrequency == RadioConstants.FMMIN) {// 已是最小FM频点
				// 从最大FM频点开始
				RadioStatus.currentFrequency = RadioConstants.FMMAX;
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				PreferencesUtil.getInstance().saveLatestRadioInfo(mContext, RadioConstants.FMMAX,
						RadioConstants.TYPE_FM);
				Logger.logD("IStepRadioModelImp---------------------"+RadioStatus.currentFrequency);
			}else{
				RadioStatus.currentFrequency -= RadioConstants.FM_STEP;
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				PreferencesUtil.getInstance().saveLatestRadioInfo(mContext, RadioStatus.currentFrequency,
						RadioConstants.TYPE_FM);
				Logger.logD("IStepRadioModelImp---------------------"+RadioStatus.currentFrequency);
			}
			break;
		case RadioConstants.TYPE_AM:
//			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 如果正在搜索上下一个强AM台，停止
//				Logger.logD("searching near strong AM radio, click previous strong radio button to stop");
//				RadioStatus.currentFrequency -= RadioConstants.AM_STEP;
//				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
//				PreferencesUtil.getInstance().saveLatestRadioInfo(mContext, RadioStatus.currentFrequency,
//						RadioConstants.TYPE_AM);
//				Logger.logD("IStepRadioModelImp---------------------"+RadioStatus.currentFrequency);
//				break;
//			}

			if (RadioStatus.currentFrequency == RadioConstants.AMMIN) {// 已是最小AM频点
				// 从最大AM频点开始
				RadioStatus.currentFrequency = RadioConstants.AMMAX;
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				PreferencesUtil.getInstance().saveLatestRadioInfo(mContext, RadioConstants.AMMAX,
						RadioConstants.TYPE_AM);
				Logger.logD("IStepRadioModelImp---------------------"+RadioStatus.currentFrequency);
			}else{
				RadioStatus.currentFrequency -= RadioConstants.AM_STEP;
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				PreferencesUtil.getInstance().saveLatestRadioInfo(mContext, RadioStatus.currentFrequency,
						RadioConstants.TYPE_AM);
				Logger.logD("IStepRadioModelImp---------------------"+RadioStatus.currentFrequency);
			}
			break;
		default:
			break;
		}
	}

	class StepForwardThread implements Runnable {

		@Override
		public void run() {
			while (isStart) {
				stepForwardRadio();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();// 异常处理
					throw new RuntimeException(e);
				}
			}
		}

	}

	class StepBackThread implements Runnable {

		@Override
		public void run() {
			while (isStart) {
				stepBackRadio();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();// 异常处理
					throw new RuntimeException(e);
				}
			}
		}

	}
}
