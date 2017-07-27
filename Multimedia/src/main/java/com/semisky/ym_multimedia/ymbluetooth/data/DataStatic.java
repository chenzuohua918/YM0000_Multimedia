package com.semisky.ym_multimedia.ymbluetooth.data;

import android.bluetooth.BluetoothDevice;

/**
 * Created by luoyin on 16/10/18.
 */
public class DataStatic {
    public static BluetoothDevice mCurrentBT;
    public static BluetoothDevice mNewBT;
    public static int mBTConnectState = 0;
    //通话界面相关的标志
    public static int mDialogType = 0;
//    public static int mLastDialogType = 0;
    public static int mLastCallType = 0;
    public static int mLastCallType3 = 0;
    public static boolean mIsNeedSmallDialog = false;
    public static boolean mCheckStart = false;
    public static boolean mIsHidden = false;
    public static boolean mIsOperateByVehicle = true;
    //ACC状态
    public static boolean mAccOff = false;
    //当前音量
    public static float mCurrentVolume = 0;
    //处于蓝牙相关界面
    public static boolean sIsBluetoothUI = false;
    //当前界面
    public static int sCurrentIF = 0;

    public static final String mCurrentPackage = "com.semisky.ym_multimedia";
}
