package com.semisky.ym_multimedia.radio.test;

import com.semisky.ym_multimedia.common.utils.AppUtil;
import com.semisky.ym_multimedia.radio.fragment.RadioFragment;
import com.semisky.ym_multimedia.radio.model.IRadioSwitchModel;
import com.semisky.ym_multimedia.radio.model.IRadioSwitchModelImp;
import com.semisky.ym_multimedia.radio.model.ISearchAllAMModel;
import com.semisky.ym_multimedia.radio.model.ISearchAllAMModelImp;
import com.semisky.ym_multimedia.radio.model.ISearchAllFMModel;
import com.semisky.ym_multimedia.radio.model.ISearchAllFMModelImp;
import com.semisky.ym_multimedia.radio.model.ISearchNearStrongRadioModel;
import com.semisky.ym_multimedia.radio.model.ISearchNearStrongRadioModelImp;
import com.semisky.ym_multimedia.radio.model.ISwitchFMAMModel;
import com.semisky.ym_multimedia.radio.model.ISwitchFMAMModelImp;
import com.semisky.ym_multimedia.radio.utils.RadioConstants;
import com.semisky.ym_multimedia.radio.utils.RadioStatus;


import android.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TestBroadcastReceiver extends BroadcastReceiver {
	public static final String SEMISKY_RA_SEARCH_ALL = "SEMISKY_RA_SEARCH_ALL"; // 搜索
	public static final String SEMISKY_RA_SEARCH_PREVIOUS = "SEMISKY_RA_SEARCH_PREVIOUS";// 上一个
	public static final String SEMISKY_RA_SEARCH_NEXT = "SEMISKY_RA_SEARCH_NEXT";// 下一个
	public static final String SEMISKY_RA_BAND = "SEMISKY_RA_BAND";// 波段
	public static final String SEMISKY_RA_SWITCH = "SEMISKY_RA_SWITCH";// 开关
	public static final String SEMISKY_RA_RADAR = "SEMISKY_RA_RADAR";// 远近程

	private ISearchNearStrongRadioModel mISearchNearStrongRadioModel;// 搜索上下一个强信号台Model
	private ISearchAllFMModel mISearchAllFMModel; // 搜索所有FM
	private ISearchAllAMModel mISearchAllAMModel; // 搜索所有AM
	private ISwitchFMAMModel mISwitchFMAMModel; // 波段切换
	private IRadioSwitchModel mIRadioSwitchModel;// 开关Model
	private Context mContext;
	private static final String TAG = "TEST_BROADCAST_RECEIVER";
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context.getApplicationContext();
		mISearchNearStrongRadioModel = ISearchNearStrongRadioModelImp.getInstance(mContext);
		mIRadioSwitchModel = IRadioSwitchModelImp.getInstance(mContext);
		mISearchAllFMModel = ISearchAllFMModelImp.getInstance(mContext);
		mISearchAllAMModel = ISearchAllAMModelImp.getInstance(mContext);
		mISwitchFMAMModel = ISwitchFMAMModelImp.getInstance(mContext);
		String action = intent.getAction();
		Log.d(TAG,intent.getAction());
		if (SEMISKY_RA_SEARCH_ALL.equals(action)) {
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
		} else if (SEMISKY_RA_SEARCH_PREVIOUS.equals(action)) {
			mISearchNearStrongRadioModel.searchPreviousStrongRadio();
		} else if (SEMISKY_RA_SEARCH_NEXT.equals(action)) {
			mISearchNearStrongRadioModel.searchNextStrongRadio();
		} else if (SEMISKY_RA_BAND.equals(action)) {
			Log.d(TAG,"switch band");
			mISwitchFMAMModel.switchRadioType(RadioStatus.currentType == RadioConstants.TYPE_FM ? RadioConstants.TYPE_AM : RadioConstants.TYPE_FM, true);
		} else if (SEMISKY_RA_SWITCH.equals(action)) {
			mIRadioSwitchModel.switchOnOff(false);
			AppUtil.backHome(mContext);
		}
		// else if (SEMISKY_RA_RADAR.equals(action)) {
		// mRadioFragment.longOrShortRange();
		// }
	}
	// }
}
