package com.semisky.ym_multimedia.ymbluetooth.tests;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;
import com.semisky.ym_multimedia.ymbluetooth.func.ControlManager;
import com.semisky.ym_multimedia.ymbluetooth.func.FuncBTOperate;

/**
 * 工厂测试接收器
 * 
 * @author Anter
 * 
 */
public class FactoryTestBroadcastReceiver extends BroadcastReceiver {
	private final String TAG = "FactoryTestBroadcastReceiver";
	private Context mContext;
	private ControlManager mControlManager;
	private FuncBTOperate mFuncBTOperate;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context.getApplicationContext();
		mControlManager = ControlManager.getInstance(mContext, null);
		mFuncBTOperate = FuncBTOperate.getInstance(mContext);

		//仅蓝牙界面及连接状态才响应厂测
		if(DataStatic.mCurrentBT != null && mControlManager != null
				&& mControlManager.getConnectionState(DataStatic.mCurrentBT) == BluetoothHfDevice.STATE_CONNECTED){
			String action = intent.getAction();
			boolean isBluetooth = mControlManager.btMusicIsForeground();
			if(DataStatic.sIsBluetoothUI){
				if (FactoryTestConstants.SEMISKEY_BT_MATCHING.equals(action)) {//蓝牙配对
					mFuncBTOperate.notifyMain(10, false);
				} else if (FactoryTestConstants.SEMISKEY_BT_MUSIC.equals(action)) {//蓝牙音乐
					mFuncBTOperate.notifyMain(9, false);
				} else if (FactoryTestConstants.SEMISKEY_CONTACTPERSON.equals(action)) {//联系人
					mFuncBTOperate.notifyMain(8, false);
				} else if (FactoryTestConstants.SEMISKEY_BTRECORD.equals(action)) {//通话记录
					mFuncBTOperate.notifyMain(7, false);
				} else if (FactoryTestConstants.SEMISKEY_BTCALL.equals(action)) {//蓝牙拨号
					String number = intent.getStringExtra("NUMBER");
					mFuncBTOperate.notifyMain(11, false, 0, number);
				} else if (FactoryTestConstants.SEMISKEY_BTMUSICNEXT.equals(action)) {//下一曲
					mFuncBTOperate.notifyBTService(9);
				} else if (FactoryTestConstants.SEMISKEY_BTMUSICPREVIOUS.equals(action)) {//上一曲
					mFuncBTOperate.notifyBTService(10);
				} else if (FactoryTestConstants.SEMISKEY_BT_PLAY.equals(action)) {//播放
					mFuncBTOperate.notifyBTService(7);
				} else if (FactoryTestConstants.SEMISKEY_BT_PAUSE.equals(action)) {//暂停
					mFuncBTOperate.notifyBTService(8);
				}
			}
		}
	}
}
