package com.semisky.ym_multimedia.ymbluetooth.func;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;

import com.semisky.ym_multimedia.ymbluetooth.dialog.BTPairDialog;
import com.semisky.ym_multimedia.ymbluetooth.dialog.BTProgressDialog;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;
//import android.view.BottomBarManager;
import android.view.View;

import com.broadcom.bt.avrcp.BluetoothAvrcpBrowseItem;
import com.broadcom.bt.avrcp.BluetoothAvrcpController;
import com.broadcom.bt.hfdevice.BluetoothCallStateInfo;
import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventBTService;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventCallLog;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventContacts;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventMain;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventMusic;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventPair;
import com.semisky.ym_multimedia.ymbluetooth.avr.AvrcpCommandCallback;
import com.semisky.ym_multimedia.ymbluetooth.avr.AvrcpCommandManager;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.data.CallLogRecords;
import com.semisky.ym_multimedia.ymbluetooth.data.Contacts;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;
import com.semisky.ym_multimedia.ymbluetooth.data.MsgLoading;
import com.semisky.ym_multimedia.ymbluetooth.db.PhoneDBManager;
import com.semisky.ym_multimedia.ymbluetooth.dialog.BTClearDialog;
import com.ypy.eventbus.EventBus;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Created by luoyin on 16/10/18.
 */
public class FuncBTOperate implements AvrcpCommandCallback{
    private final String TAG = "FuncBTOperate";
    private final int VOLUME_MAX = 31;
    private final int RECONNECT_DELAY = 1900;
    private final int RECONNECT_A2DP_DELAY = 7000;
    private final int AVRCP_REDO_DELAY = 100;
    private final int AVRCP_REDO_TIMES = 0;
    private static FuncBTOperate instance;
    private ControlManager mControlManager;
    private Context mContext;
    private String mMusicName;
    private String mMediaAlbum;
    private String mMediaArtist;
//    private int mPauseBy = 0;
//    private boolean mActivePause = false;
    private int mIsAudioFocus = 0;
    private boolean mA2dpConnecting = false;
    private boolean mA2dpDisconnecting = false;
    private boolean mFadeDownClose = false;
    private boolean mFadeAbandonClose = false;

    public FuncBTOperate(Context context) {
        mContext = context;
        mControlManager = ControlManager.getInstance(context.getApplicationContext(), null);
    }

    public static FuncBTOperate getInstance(Context context) {
        if(instance == null) {
            instance = new FuncBTOperate(context);
        }
        return instance;
    }
    /**
     * 搜索蓝牙设备
     */
    public void searchBTDevices(){
        if (mControlManager.isEnabled() && !mControlManager.isDiscovering()){
//            mAdpBTDeviceList.clearBTDeviceList();
            //清空蓝牙设备列表
//            EventPair eventPair = new EventPair();
//            eventPair.setType(3);
//            eventPair.setBluetoothDevice(null);
//            eventPair.setState(true);
//            EventBus.getDefault().post(eventPair);
            notifyPair(null, 3);
            mControlManager.startDiscovery();
            //显示ProgressDialog
//            mProgressDialog = ProgressDialog.show(mContext, "搜索设备中...", "请稍后！", true, false);
        }
    }

    public void cancelSearchBTDevices(){
        if (mControlManager.isEnabled() && mControlManager.isDiscovering()){
            mControlManager.cancelDiscovery();
        }
    }

    /**
     * 停止自动连接
     */
    public void stopAutoConnBTDevice() {
        mAutoConnectHandler.removeMessages(0);
    }

    /**
     * 延时启动自动连接蓝牙设备
     */
    public void delayedConnectBTDevice(long delayMillis){
        //延时15s启动自动连接
        mAutoConnectHandler.removeMessages(0);
        mAutoConnectHandler.sendEmptyMessageDelayed(0, delayMillis);
    }

    private Handler mAutoConnectHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    autoConnectBTDevice();
                    break;
            }
        }
    };

    private void autoConnectBTDevice() {
        int easylink = 0;
        if (android.os.ProtocolManager.getInstance() != null) {
            easylink = android.os.ProtocolManager.getInstance().getAppStatus("EC");
        }
        //未初始化，ACC为off状态，手机互联状态，都直接不自动连接
        if (mControlManager == null || mControlManager.isNotInit()
                || DataStatic.mAccOff || easylink == 1) {
            return;
        }
        //自动连接之前连过的蓝牙设备
        BluetoothDevice connectDevice = getConnectDevice();
        BtLogger.e(TAG, "自动连接-保存连接蓝牙设备-connectDevice＝" + connectDevice);
        BtLogger.e(TAG, "自动连接-当前连接蓝牙设备－DataStatic.mCurrentBT=" + DataStatic.mCurrentBT);
        //自动连接判断：是之前连接的设备,而且有自动连接优先级才算,避免自行断开后重启自动连接
        if (connectDevice != null
                && mControlManager.getPriority(connectDevice) == BluetoothProfile.PRIORITY_AUTO_CONNECT) {
            connectBTDevice(connectDevice);
        }
    }




    public void setMyNumber(String number){
        if (number != null && !number.equals("")) {
            setMyNum(number);
        }
    }

    public void clearMyNumber(){
        setMyNum("");
    }

    public void setMyNum(String number){
        BtLogger.e(TAG, "setMyNum-number=" + number);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("MyPhoneInfo", 0);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putString("number", number);
        mEditor.commit();
    }

    public String getMyNumber(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("MyPhoneInfo", 0);
        return sharedPreferences.getString("number", "");
    }

    public void setDialNumber(String number){
        if (number != null && !number.equals("")) {
            setDialNum(number);
        }
    }

    public void clearDialNumber(){
        setDialNum("");
    }

    private void setDialNum(String number){
        BtLogger.e(TAG, "setDialNum-number=" + number);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("DialPhoneInfo", 0);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putString("number", number);
        mEditor.commit();
    }

    public String getDialNumber(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("DialPhoneInfo", 0);
        return sharedPreferences.getString("number", "");
    }

    /**
     * SP保存自动连接蓝牙地址
     * @param btAddress
     */
    public void setSPBTAddress(String btAddress){
        BtLogger.e(TAG, "---－setSPBTAddress＝" + btAddress);
        if (btAddress != null && !btAddress.equals("")) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("ConnectDevice", 0);
            SharedPreferences.Editor mEditor = sharedPreferences.edit();
            mEditor.putString("btAddress", btAddress);
            mEditor.commit();
        }
    }

    /**
     * 获取SP自动连接蓝牙地址
     * @return
     */
    public String getSPBTAddress(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("ConnectDevice", 0);
        return sharedPreferences.getString("btAddress", "");
    }

    /**
     * 保存蓝牙开关状态
     * @param status
     */
    public void setBTSwitchStatus(boolean status){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("BTSettingStatus", 0);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putBoolean("BTSwitchStatus", status);
        mEditor.commit();
    }

    /**
     * 获取蓝牙开关状态
     * @return
     */
    public boolean getBTSwitchStatus(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("BTSettingStatus", 0);
        return sharedPreferences.getBoolean("BTSwitchStatus", false);
    }

    /**
     * 保存自动连接开关状态
     * @param status
     */
    public void setBTAutoConnStatus(boolean status){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("BTSettingStatus", 0);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putBoolean("BTAutoConnStatus", status);
        mEditor.commit();
    }

    /**
     * 获取自动连接开关状态
     * @return
     */
    public boolean getBTAutoConnStatus(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("BTSettingStatus", 0);
        return sharedPreferences.getBoolean("BTAutoConnStatus", false);
    }

    /**
     * 保存自动接听开关状态
     * @param status
     */
    public void setBTAutoAnswerStatus(boolean status){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("BTSettingStatus", 0);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putBoolean("BTAutoAnswerStatus", status);
        mEditor.commit();
    }

    /**
     * 获取自动接听开关状态
     * @return
     */
    public boolean getBTAutoAnswerStatus(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("BTSettingStatus", 0);
        return sharedPreferences.getBoolean("BTAutoAnswerStatus", false);
    }

    public void refreshCallDialog(){
        mControlManager.setmBCStateInfo(null);
        refreshCallState();
    }

    private int mAudioState;
    public void refreshCallState(){
        //以下这段是为了在通话时刷新私密状态用
        if(mControlManager == null || DataStatic.mCurrentBT == null){
            return;
        }
        //音频状态
        int audioState = mControlManager.getAudioState(DataStatic.mCurrentBT);
        //音频状态有变化才执行处理
        if(mAudioState != audioState){
            switch (audioState) {
                case BluetoothHfDevice.STATE_DISCONNECTED:
                    //隐私模式，在这里同步界面变化
                    notifyBTService(16, true);
                    break;
                case BluetoothHfDevice.STATE_CONNECTED:
                    //车机模式
                    notifyBTService(16, false);
                    break;
            }
        }
        mAudioState = audioState;

        //获取蓝牙通话数据
        BluetoothCallStateInfo bcStateInfo = mControlManager.getCallStateInfo(DataStatic.mCurrentBT);
        BtLogger.e(TAG, "通话数据为＊＊＊＊bcStateInfo = " + bcStateInfo);
        //同步蓝牙通话弹框
        if(bcStateInfo != null){
            BtLogger.e(TAG, "getConnectDevice－getPhoneNumber＝" + bcStateInfo.getPhoneNumber()
                    +"－getCallSetupState＝" + bcStateInfo.getCallSetupState());
            BtLogger.e(TAG, "getConnectDevice－getNumActiveCall＝" + bcStateInfo.getNumActiveCall()
                    +"－getNumOfCalls＝" + bcStateInfo.getNumOfCalls());
            int status = 255;
            int callSetupState = bcStateInfo.getCallSetupState();
            int numActive = bcStateInfo.getNumActiveCall();
            int numHeld = bcStateInfo.getNumHeldCall();
            String number = bcStateInfo.getPhoneNumber();
            int addrType = -1;
            mControlManager.callStateResponse(status, callSetupState, numActive, numHeld, number, addrType);
        }
    }

    /**
     * 获取自动连接的设备
     * @return
     */
    public BluetoothDevice getConnectDevice() {
        BtLogger.e(TAG, "－getConnectDevice＝" + mControlManager);
        //获取能够自动连接的设备，只能是一个设备，也就是最后一次连接的设备
        Set<BluetoothDevice> bondedDevices = mControlManager.getBondedDevices();
        BtLogger.e(TAG, "获取之前连接的设备－bondedDevices＝" + bondedDevices);
        if (bondedDevices == null) {
            return null;
        }
        for (BluetoothDevice device : bondedDevices) {
            //获取SP保存的蓝牙设备
            String spbtAddress = getSPBTAddress();
            BtLogger.e(TAG, "SP保存蓝牙地址-spbtAddress = " + spbtAddress);
            if (spbtAddress.equals(device.getAddress())) {
                BtLogger.e(TAG, "保存的设备为＊＊＊＊＊＊＊＊AutoConnectDevice = " + device.toString());
                return device;
            }
        }
        return null;
    }

    /**
     * 连接蓝牙设备
     * @param btDevice
     */
    public void connectBTDevice(BluetoothDevice btDevice) {
        if(btDevice == null){
            return;
        }
        DataStatic.mNewBT = btDevice;
        BtLogger.d(TAG, "当前连接设备－DataStatic.mCurrentBT: " + DataStatic.mCurrentBT);
        int state = 0;
        if (DataStatic.mCurrentBT != null) {
            state = mControlManager.getConnectionState(DataStatic.mCurrentBT);
        }
        //无当前连接设备，当前连接设备不处于连接状态，当前设备与连接设备相同，都执行连接操作
        if (DataStatic.mCurrentBT == null
                || state != BluetoothHfDevice.STATE_CONNECTED
                || DataStatic.mCurrentBT.getAddress().equals(btDevice.getAddress())){
            connectNewBTDevice();
        }else{
            BtLogger.e(TAG, "断开之前的设备－name: " + DataStatic.mCurrentBT.getName());
            //断开连接前断开pbap
            notifyBTService(17);
            //断开之前的蓝牙设备连接
            disconnectBT(DataStatic.mCurrentBT);
            //断开当前设备后通知配对界面
            notifyPairAndMainS(DataStatic.mCurrentBT, false);
        }
    }

    public void resetNewBtStatus(BluetoothDevice device) {
        if(DataStatic.mNewBT != null
                && DataStatic.mNewBT.getAddress().equals(device.getAddress())){
            if(mControlManager.getConnectionState(DataStatic.mNewBT)
                    == BluetoothHfDevice.STATE_DISCONNECTED || mControlManager.getConnectionState(DataStatic.mNewBT)
                    == BluetoothHfDevice.STATE_CONNECTED){
                //新连接处于断开或连接成功就清空
                DataStatic.mNewBT = null;
                isConnectingNewBT = false;
            }
        }
    }

    boolean isConnectingNewBT = false;
    protected Handler mConnectingTimeoutHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    isConnectingNewBT = false;
                    break;
            }
        }
    };

    public void connectNewBTDevice(){
        if(DataStatic.mNewBT == null || isConnectingNewBT){
            return;
        }
        isConnectingNewBT = true;
        //避免重复连接标志，连接三秒后重置
        mConnectingTimeoutHandler.removeMessages(0);
        mConnectingTimeoutHandler.sendEmptyMessageDelayed(0, 3000);
        //连接完成后置空新设备
//        DataStatic.mNewBT = null;
        int state = mControlManager.getConnectionState(DataStatic.mNewBT);
        int bondState = DataStatic.mNewBT.getBondState();
        BtLogger.d(TAG, "新连接设备－state可用蓝牙连接状态: " + state);
        BtLogger.d(TAG, "新连接设备－bond 状态: " + bondState);
        //createBond会引起ACTION_DISCOVERY_FINISHED广播
        if(state ==  BluetoothHfDevice.STATE_CONNECTED) {
            if (bondState == BluetoothDevice.BOND_BONDED) {
                BtLogger.d(TAG, "新连接－绑定成功: " + DataStatic.mNewBT.getAddress());
                //设备同步到连接列表，避免自动连接成功却不在列表中显示的bug
                notifyPair(DataStatic.mNewBT, 1);
                //处于连接中
                notifyPairAndMainS(DataStatic.mNewBT, true);
                //无须同步联系人时启用菜单
                notifyMain(0, true);
                BtLogger.e(TAG, "还原高亮菜单位置－－－2");
            }else if (bondState == BluetoothDevice.BOND_NONE){
                BtLogger.d(TAG, "连接－绑定异常: closeConnect" + DataStatic.mNewBT.getAddress());
                //断开重连新设备
                disconnectBT(DataStatic.mNewBT);
            }else if (bondState == BluetoothDevice.BOND_BONDING){
                //连接成功，但状态是绑定中，异常状态
                BtLogger.d(TAG, "连接－绑定中: 异常" + DataStatic.mNewBT.getAddress());
                //断开连接前断开pbap
                notifyBTService(17);
                //统一，只要断开连接就响应主界面等
                notifyPairAndMainS(DataStatic.mNewBT, false);
            }
        } else if (state ==  BluetoothHfDevice.STATE_DISCONNECTED){
            if (bondState == BluetoothDevice.BOND_BONDED) {
                BtLogger.d(TAG, "未连接－绑定成功: " + DataStatic.mNewBT);
                //手机端做了断开或取消配对后会进入这个位置
                mControlManager.connect(DataStatic.mNewBT);
            }else if (bondState == BluetoothDevice.BOND_BONDING){
                BtLogger.d(TAG, "未连接－绑定中: 异常－" + DataStatic.mNewBT.getAddress());
//                mControlManager.removeBond(btDevice.getClass(), btDevice);//?
                mControlManager.connect(DataStatic.mNewBT);
            }else if (bondState == BluetoothDevice.BOND_NONE) {
                BtLogger.d(TAG, "未连接－未绑定：" + DataStatic.mNewBT.getAddress());
                //连接必须用connect方法，否则setPriority逻辑不正确
                mControlManager.connect(DataStatic.mNewBT);//sure
            }
        }
    }

    public void notifyPairAndMainS(BluetoothDevice bluetoothDevice, boolean state){
        //同步主页和通话状态等
        notifyMainAndService(bluetoothDevice, state);
        //刷新配对界面设备列表状态
        notifyPair(null, 2);
    }

    public void notifyPair(BluetoothDevice bluetoothDevice, int type){
        EventPair eventPair = new EventPair();
        eventPair.setType(type);
        eventPair.setBluetoothDevice(bluetoothDevice);
        //添加发现的设备
        EventBus.getDefault().post(eventPair);
    }

    public void notifyMainAndService(BluetoothDevice bluetoothDevice, boolean state){
        if(state){
            // 连接成功
            BtLogger.d(TAG, "连接成功 DataStatic.mCurrentBT= " + bluetoothDevice.getName());
            //连接成功则立即高亮音乐
//            notifyMain(3, false);
        }else{
            // 断开连接
            //通知电话相关界面清空list数据(断开时不能操作联系人和通话记录菜单，所以无须清理)
//            notifyCallData(1);
            //如果通话中则挂断电话
            notifyBTService(11);
            //断开需要同步弹框界面（之前的11分开成11＋15了）
            notifyBTService(15);
            //取消清除弹框
            dismissmBTClearDialog();
            //断开则清空蓝牙音乐界面数据
//            notifyMusic(0, 0, "", "", "");
            //蓝牙断开则关闭蓝牙音乐相关通道
            closeBTMusic();
            //每次断开就自动连接蓝牙
//            reConnectBTDevice();
            BtLogger.e(TAG, "禁用菜单位置－－－3-state-"+state);
            //通知主界面变化
            notifyMain(0, state);
            BtLogger.e(TAG, "还原高亮菜单位置－－－3");
        }
        //连接成功与否
        notifyMain(1, state, -1, mMusicName);
    }

    /**
     * 通知服务
     * @param method
     */
    public void notifyBTService(int method){
        EventBTService eventBTService = new EventBTService();
        eventBTService.setMethod(method);
        EventBus.getDefault().post(eventBTService);
    }

    public void notifyBTService(int method, boolean privateMode){
        EventBTService eventBTService = new EventBTService();
        eventBTService.setMethod(method);
        eventBTService.setPrivateMode(privateMode);
        EventBus.getDefault().post(eventBTService);
    }

    public void notifyBTService(int method, String number, int callType){
        EventBTService eventBTService = new EventBTService();
        eventBTService.setMethod(method);
        eventBTService.setNumber(number);
        eventBTService.setCallType(callType);
        EventBus.getDefault().post(eventBTService);
    }

    /**
     * 通知音乐界面
     * @param method
     */
    public void notifyMusic(int method, int playStatus, String title, String album, String artist){
        EventMusic eventMusic = new EventMusic();
        eventMusic.setMethod(method);
        eventMusic.setPlayStatus(playStatus);
        eventMusic.setTitle(title);
        eventMusic.setAlbum(album);
        eventMusic.setArtist(artist);
        EventBus.getDefault().post(eventMusic);
    }

    public void notifyMusic(int method, int playStatus){
        notifyMusic(method, playStatus, null, null, null);
        boolean isBluetooth = mControlManager.btMusicIsForeground();
        //播放状态变为播放就渐变提升音量
        if(method == 1 && isBluetooth && playStatus == BluetoothAvrcpController.PLAY_STATUS_PLAYING){
            fadeUpVolume();
        }
    }

    public void notifyMain(int method, boolean enadled){
        BtLogger.e(TAG, "主页变化－－:" + method + ">>>>>>>>" + enadled);
        EventMain eventMain = new EventMain();
        eventMain.setMethod(method);
        eventMain.setEnabled(enadled);
        EventBus.getDefault().post(eventMain);
    }

    public void notifyMain(int method, boolean enabled, long playStatus, String musicName){
        if(method == 1){
            syncBottomBar(enabled, playStatus, musicName);
        }
        EventMain eventMain = new EventMain();
        eventMain.setMethod(method);
        eventMain.setEnabled(enabled);
        eventMain.setPlayStatus(playStatus);
        eventMain.setMusicName(musicName);
        EventBus.getDefault().post(eventMain);
    }

    private void syncBottomBar(boolean enabled, long playStatus, String musicName){
//        String bottomStr = "";
//        //设置左下角蓝牙信息(按理说应该是pause，实际stop就是pause)
//        //为避免左下角标修改错乱,仅在播放状态才修改launcher的左下角信息
//        if(playStatus == BluetoothAvrcpController.PLAY_STATUS_PLAYING
//                && DataStatic.mCurrentVolume > 0
//                && !"".equals(musicName.trim())){
////            mBTInfoTV.setText(musicName);
////            mBottomBar.setFirstText(musicName);
//            bottomStr = musicName;
//            BottomBarManager bottomBarManager = BottomBarManager.getInstance(mContext);
//            bottomBarManager.putCurrentIcon(android.view.BottomBar.ICON_BLUETOOTH);
//            bottomBarManager.putSecondText("");
//            bottomBarManager.putThirdText("");
//            BtLogger.e(TAG, "mContext.getPackageName()=" + mContext.getPackageName());
//            bottomBarManager.putPackageName("com.semisky.ym_multimedia.ymbluetooth.bluetooth");
//            bottomBarManager.putClassName("com.semisky.ym_multimedia.ymbluetooth.bluetooth.BluetoothFragment");
//            bottomBarManager.putFirstText(bottomStr);
//            BtLogger.e(TAG, "syncBottomBTInfo－bottomStr=" + bottomStr);
//            //后台改变需要发送更新广播给launcher
//            mContext.sendBroadcast(new Intent("action_change_bottombar_text"));
//        }else if(DataStatic.mCurrentBT == null || !enabled){
//            if(mControlManager != null) {
//                String btSelfName = mControlManager.getBTName();
//                BtLogger.e(TAG, "syncBottomBTInfo－btSelfName="+btSelfName);
//                if (btSelfName != null) {
////                    mBTInfoTV.setText(btSelfName);
////                    mBottomBar.setFirstText(btSelfName);
//                    //未连接蓝牙,自身名字
//                    bottomStr = btSelfName;
//                }
//                //蓝牙后台运行时断开同步左下角蓝牙信息为自身名字
//                BottomBarManager bottomBarManager = BottomBarManager.getInstance(mContext);
//                if("com.semisky.ym_multimedia.ymbluetooth.bluetooth.BluetoothFragment".equals(bottomBarManager.getClassName())
//                        &&"com.semisky.ym_multimedia.ymbluetooth.bluetooth".equals(bottomBarManager.getPackageName())){
//                    bottomBarManager.putFirstText(bottomStr);
//                }
//            }
//        }else{
////            mBTInfoTV.setText(DataStatic.mCurrentBT.getName());
////            mBottomBar.setFirstText(DataStatic.mCurrentBT.getName());
//            bottomStr = DataStatic.mCurrentBT.getName();
//            //蓝牙后台运行时断开同步左下角蓝牙信息为连接中蓝牙设备的名字
//            BottomBarManager bottomBarManager = BottomBarManager.getInstance(mContext);
//            if("com.semisky.ym_multimedia.ymbluetooth.bluetooth.BluetoothFragment".equals(bottomBarManager.getClassName())
//                    &&"com.semisky.ym_multimedia.ymbluetooth.bluetooth".equals(bottomBarManager.getPackageName())){
//                bottomBarManager.putFirstText(bottomStr);
//            }
//        }
    }
    /**
     * 同步电话界面数据
     * @param method
     */
    public void notifyCallData(int method) {
        BtLogger.d(TAG, "同步电话数据 notifyCallData");
        notifyContactsList(method);
        notifyCallLogList(method);
    }

    public void notifyContactsList(int method){
        BtLogger.d(TAG, "同步联系人列表数据");
        EventContacts eventContacts = new EventContacts();
        eventContacts.setMethod(method);
        EventBus.getDefault().post(eventContacts);
    }

    public void notifyCallLogList(int method){
        BtLogger.d(TAG, "同步通话记录列表数据");
        EventCallLog eventCallLog = new EventCallLog();
        eventCallLog.setMethod(method);
        EventBus.getDefault().post(eventCallLog);
    }

    private BTAvrcpCtrlEventHandler mBTAvrcpControll;
    BluetoothAvrcpController avrcp;
    long avrcpState = BluetoothAvrcpController.PLAY_STATUS_ERROR;

    public long getAvrcpState() {
        return avrcpState;
    }

    long avrcpLastState = BluetoothAvrcpController.PLAY_STATUS_ERROR;
    int mLossType = 0;
    int mAudioFocusType = 0;
    private boolean mIsLoss = false;
    private int[] supportElementAttr = {
            (int) BluetoothAvrcpController.MEDIA_ATTRIBUTE_TITLE,
            (int) BluetoothAvrcpController.MEDIA_ATTRIBUTE_ARTIST,
            (int) BluetoothAvrcpController.MEDIA_ATTRIBUTE_ALBUM,
            (int) BluetoothAvrcpController.MEDIA_ATTRIBUTE_TRACK_NUM,
            (int) BluetoothAvrcpController.MEDIA_ATTRIBUTE_NUM_TRACKS,
            (int) BluetoothAvrcpController.MEDIA_ATTRIBUTE_GENRE,
            (int) BluetoothAvrcpController.MEDIA_ATTRIBUTE_PLAYING_TIME };

    public void initBTMusic() {
        //如果是易联且音量不为最高就直接提升音量
        int easylink = android.os.ProtocolManager.getInstance().getAppStatus("EC");
        if(easylink == 1){
            fadeUpVolume();
        }
        boolean isBluetooth = mControlManager.btMusicIsForeground();
        //acc off-on蓝牙后于易联启动会不连接a2dp
        if(isBluetooth || easylink == 1) {
            //申请音频焦点
//            requestAudioFocus();
            //到蓝牙界面必须恢复音量（因为界面切换时需要延时500毫秒才能判断准确，否则当前界面会被误判为上一个界面）
            mAudioGainHandler.removeMessages(0);
            mAudioGainHandler.sendEmptyMessageDelayed(0, 500);

            BtLogger.e(TAG, "DataStatic.mCurrentBT===="+DataStatic.mCurrentBT);
            //只有连接设备才初始化，避免avrcp初始化错误
            if(DataStatic.mCurrentBT == null){
                return;
            }
            BtLogger.e(TAG, "initBTMusic");
//            connectA2dp(DataStatic.mCurrentBT);
            //初始化音乐时重置音乐信息和播放状态
//            if (mAvrcpCallback == null) {
//                BtLogger.e(TAG, "mAvrcpCallback==null");
//                mAvrcpCallback = this;

//                notifyMusic(0, 0, "", mContext.getResources().getString(R.string.please_open_player), "");
//                //同步播放按键
//                notifyMusic(1, (int) avrcpState);
//            }
            //每次初始化都获取一次ID3信息和播放状态
            //每次都获取会造成AVRCP无响应
//          getPlayerStatus(DataStatic.mCurrentBT);
            if (avrcp == null) {
                notifyMusic(0, 0, "", mContext.getResources().getString(R.string.ym_bt_please_open_player), "");
                //同步播放按键
                notifyMusic(1, (int) avrcpState);
                BtLogger.e(TAG, "avrcpavrcpavrcpavrcp==null");
                //执行以下语句即可调用onServiceConnected
                BluetoothAvrcpController.getProxy(mContext, serviceListener);
            } else if(avrcp.getConnectionState(DataStatic.mCurrentBT) != BluetoothProfile.STATE_CONNECTED){
                BtLogger.e(TAG, "avrcpavrcpavrcpavrcp==connect");
                //这个语句不执行也不影响获取ID3信息和控制暂停播放等逻辑
                avrcp.connect(DataStatic.mCurrentBT);
                //重新进入蓝牙音乐界面时显示歌曲信息
                getPlayerStatus(DataStatic.mCurrentBT);
            } else {
                //重新进入蓝牙音乐界面时显示歌曲信息
                getPlayerStatus(DataStatic.mCurrentBT);
            }
            BtLogger.e(TAG, "avrcp=" + avrcp);
        }
    }

    protected Handler mFadeUpHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    fadeUpVolume();
                    break;
            }
        }
    };

    public void abandonBTMusic(){
//        pausePlayer(DataStatic.mCurrentBT);
        BtLogger.e(TAG, "closeBTMusic-avrcpLastState=" + avrcpLastState);
        //FM,本地音乐串音
//        disconnectA2dp(DataStatic.mCurrentBT);
//        setStreamVolume(VOLUME_MAX); // 当失去音频焦点时，恢复音量
        fadeDownAbandonAudio();
        //不响应音频焦点
        abandonAudioFocus();
        //还原音频状态
        mIsAudioFocus = 0;
        //关蓝牙控制器
//        closeAvrcp();
        //断开控制时重置音乐状态
        notifyMusic(0, 0, "", mContext.getResources().getString(R.string.ym_bt_please_open_player), "");
        //同步播放按键
//        notifyMusic(1, (int) avrcpState);
        //播放按键在断开时恢复暂停/停止状态
        notifyMusic(1, (int) BluetoothAvrcpController.PLAY_STATUS_STOPPED);
    }

    public void closeBTMusic(){
//        pausePlayer(DataStatic.mCurrentBT);
        BtLogger.e(TAG, "closeBTMusic-avrcpLastState=" + avrcpLastState);
        //FM,本地音乐串音
        disconnectA2dp(DataStatic.mCurrentBT);
//        setStreamVolume(VOLUME_MAX); // 当失去音频焦点时，恢复音量
//        fadeDownCloseAudio();
        //不响应音频焦点
        abandonAudioFocus();
        //还原音频状态
        mIsAudioFocus = 0;
        //关蓝牙控制器
        closeAvrcp();
        //断开控制时重置音乐状态
        notifyMusic(0, 0, "", mContext.getResources().getString(R.string.ym_bt_please_open_player), "");
        //同步播放按键
//        notifyMusic(1, (int) avrcpState);
        //播放按键在断开时恢复暂停/停止状态
        notifyMusic(1, (int) BluetoothAvrcpController.PLAY_STATUS_STOPPED);
    }

    private void closeAvrcp(){
        BtLogger.e(TAG, "closeAvrcp");
        //当前也没为蓝牙时需要一个dialog？？？
        //断开蓝牙连接则关闭音乐控制器，下次从新创建
//            avrcp.disconnect(bluetoothDevice);
        if(avrcp != null){
            avrcp.unregisterEventHandler();
            avrcp.closeProxy();
            avrcp = null;
        }
        //保存当前播放音乐状态
//        avrcpState = BluetoothAvrcpController.PLAY_STATUS_STOPPED;
        if(mBTAvrcpControll != null) {
            mBTAvrcpControll.unregisterCallback();
            mBTAvrcpControll.clear();
            mBTAvrcpControll = null;
        }
    }

    protected Handler mBTDisconnectHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    disconnect((BluetoothDevice) msg.obj);
                    break;
                case 1:
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    removeBond(device);
                    break;
            }
        }
    };

    private void disconnect(BluetoothDevice device){
        //避免重复
        mBTDisconnectHandler.removeMessages(0);
        int a2dpState = mControlManager.getConnectionA2dpState(device);
        BtLogger.e(TAG, "disconnect-a2dpState="+a2dpState);
        //如果a2dp没有断开则延时100毫秒循环查询
        if (a2dpState != BluetoothProfile.STATE_DISCONNECTED ) { //0
            Message msg = new Message();
            msg.what = 0;
            msg.obj = device;
            mBTDisconnectHandler.sendMessageDelayed(msg, RECONNECT_DELAY);
        } else if(a2dpState == BluetoothProfile.STATE_CONNECTED ){
            //如果走到这不还没有断开a2dp则再断开一次a2dp
            //断开时关闭音乐及a2dp
            closeBTMusic();
            Message msg = new Message();
            msg.what = 0;
            msg.obj = device;
            mBTDisconnectHandler.sendMessageDelayed(msg, RECONNECT_DELAY);
        } else {
            mControlManager.disconnect(device);
        }
    }

    public void disconnectBT(BluetoothDevice device){
        closeBTMusic();
        disconnect(device);
        if(mControlManager != null){
            //清空通话状态
            mControlManager.setmBCStateInfo(null);
        }
    }

    public boolean removeBond(BluetoothDevice device){
        //避免重复
        mBTDisconnectHandler.removeMessages(1);
        int a2dpState = mControlManager.getConnectionA2dpState(device);
        BtLogger.e(TAG, "removeBond-a2dpState=" + a2dpState);
        //如果a2dp没有断开则延时100毫秒循环查询
        if (a2dpState != BluetoothProfile.STATE_DISCONNECTED ) { //0
            Message msg = new Message();
            msg.what = 1;
            msg.obj = device;
            mBTDisconnectHandler.sendMessageDelayed(msg, RECONNECT_DELAY);
            return false;
        } else if(a2dpState == BluetoothProfile.STATE_CONNECTED ){
            //如果走到这不还没有断开a2dp则再断开一次a2dp
            //断开时关闭音乐及a2dp
            closeBTMusic();
            Message msg = new Message();
            msg.what = 1;
            msg.obj = device;
            mBTDisconnectHandler.sendMessageDelayed(msg, RECONNECT_DELAY);
            return false;
        } else {
            try {
                return mControlManager.removeBond(device);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }


    BluetoothProfile.ServiceListener serviceListener = new BluetoothProfile.ServiceListener() {

        @Override
        public void onServiceDisconnected(int profile) {
            // TODO Auto-generated method stub
            BtLogger.d(TAG, "onServiceDisconnected()");
            //关闭界面
        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            // TODO Auto-generated method stub
            avrcp = (BluetoothAvrcpController) proxy;
            BtLogger.d(TAG, "onServiceConnected()-DataStatic.mCurrentBT="+DataStatic.mCurrentBT);
            mBTAvrcpControll = new BTAvrcpCtrlEventHandler(avrcp, DataStatic.mCurrentBT);
            avrcp.registerEventHandler(mBTAvrcpControll);
            if (DataStatic.mCurrentBT == null){
                //切换到蓝牙界面
            } else {
                //启动avrcp请求
                startAvrcpReq(DataStatic.mCurrentBT);
                //在这里获取一下状态蓝牙音乐状态等信息
                getPlayerStatus(DataStatic.mCurrentBT);
            }
        }
    };

    public void startAvrcpReq(BluetoothDevice bluetoothDevice){

        int targetFeatures = avrcp.getTargetFeatures(bluetoothDevice);
        BtLogger.d(TAG, "Target features: "
                + Integer.toBinaryString(targetFeatures));

        mBTAvrcpControll.processCmds(AvrcpCommandManager.CMD_LOAD_APP_SETTING_ATTR);
        mBTAvrcpControll.registerCallback(this);
    }

    class BTAvrcpCtrlEventHandler extends AvrcpCommandManager{
        public BTAvrcpCtrlEventHandler(BluetoothAvrcpController controller, BluetoothDevice device) {
            super(controller, device);
        }

        @Override
        public void onGetPlayStatusRsp(BluetoothDevice target, int songLength,
                                       int songPosition, byte playStatus, int status) {
            //无论有无变化都需要同步播放按键
            notifyMusic(1, playStatus);
            //同步左下角数据
            getElementAttributes(target);
            //执行相应变化则取消延时重复操作
            removeAvrcpHandler(playStatus);
            //播放状态相同或播放停止则不进入
            if(avrcpState == playStatus){
                return;
            }
            //音频丢失状态时不响应音乐关闭信息,过滤多个播放器时有停止状态发送的bug
            if(mIsLoss == true && playStatus == BluetoothAvrcpController.PLAY_STATUS_STOPPED){
                //这个位置如果执行会造成切换倒车时有PLAY_STATUS_STOPPED状态未赋值给avrcpState,造成startPlayer方法不执行play方法
//                return;
            }
            BtLogger.e(TAG, "播放状态变化onGetPlayStatusRsp() songLength:" + songLength
             + ", songPosition:" + songPosition + ", status:" + status + ", playStatus:" + playStatus);
            if (!BluetoothAvrcpController.isSuccess(status)) {
                //异常状态
                return;
//                stopPlayer(target);
            }
            BtLogger.e(TAG, "avrcpState 变化(2) " + avrcpState + " >>> " + (long) playStatus);
            avrcpState = playStatus;
            //如果是播放状态则获取音频焦点
//            if(playStatus == BluetoothAvrcpController.PLAY_STATUS_PLAYING){
//                requestAudioFocus();
//            }
//            getElementAttributes(target);
            //直接调用这句无效，需要调用上面的getElementAttributes
//            reloadMetaData();
        }

        @Override
        public void onPlaybackStatusChanged(BluetoothDevice target,
                                            byte playStatus) {
            BtLogger.e(TAG, "onPlaybackStatusChanged() avrcpState 变化(1) " + avrcpState + " >>> " + (long) playStatus);
            BtLogger.e(TAG, "mIsLoss ＝ " + mIsLoss);
            boolean hasPlayStatusChanged = (avrcpState == (long) playStatus) ? false: true;
            boolean hasPlayStatusRestarted = (avrcpState == BluetoothAvrcpController.PLAY_STATUS_STOPPED && playStatus == BluetoothAvrcpController.PLAY_STATUS_PLAYING) ? true: false;
            boolean hasPlayStatusContinued = (avrcpState == BluetoothAvrcpController.PLAY_STATUS_PAUSED && playStatus == BluetoothAvrcpController.PLAY_STATUS_PLAYING) ? true: false;
            //当音量低于最大音时需要渐变升音量
//            if(playStatus == BluetoothAvrcpController.PLAY_STATUS_PLAYING && mCurrentVolume < 1.0f){
//                fadeUpVolume();
//            }
            //无论有无变化都需要同步播放按键
            notifyMusic(1, playStatus);
            //同步左下角数据
            getElementAttributes(target);
            //执行相应变化则取消延时重复操作
            removeAvrcpHandler(playStatus);
            //播放状态没变化获取变为停止则不进入
            if(!hasPlayStatusChanged){
                return;
            }
            //音频丢失状态时不响应音乐关闭信息,过滤多个播放器时有停止状态发送的bug
            if(mIsLoss == true && playStatus == BluetoothAvrcpController.PLAY_STATUS_STOPPED){
                //这个位置如果执行会造成切换倒车时有PLAY_STATUS_STOPPED状态未赋值给avrcpState,造成startPlayer方法不执行play方法
//                return;
            }
            avrcpState = (long) playStatus;
            int parkingFlag = android.os.ProtocolManager.getInstance().getAppStatus("ParkingActivity");
            int mapFlag = android.os.ProtocolManager.getInstance().getAppStatus("MapActivity");
            BtLogger.e(TAG, "parkingFlag ＝ " + parkingFlag);
            BtLogger.e(TAG, "mapFlag ＝ " + mapFlag);
//            mPauseHandler.removeMessages(0);
            //如果是倒车或地图则暂停播放音频
            if(parkingFlag == 1){
                //记录倒车时音频播放
                if(avrcpState == BluetoothAvrcpController.PLAY_STATUS_PLAYING){
                    avrcpLastState = avrcpState;
                }
                //静音
                setStreamVolume(0);
//                pausePlayer(DataStatic.mCurrentBT);
                //延迟5秒二次暂停（4秒都不够），为适配华为ATH-AL00手机
                //这个处理会造成很多响应问题，华为手机经常性5秒内不想应控制
//                mPauseHandler.sendEmptyMessageDelayed(0, 8000);
                return;
            }
            //如果是播放状态则获取音频焦点
//            if(playStatus == BluetoothAvrcpController.PLAY_STATUS_PLAYING){
//                requestAudioFocus();
//            }
//            if (playStatus == BluetoothAvrcpController.PLAY_STATUS_PAUSED && mActivePause == false) {
//                mActivePause = true;
//            } else if (playStatus == BluetoothAvrcpController.PLAY_STATUS_PLAYING) {
//                mActivePause = true;
//            }
        }

        @Override
        public void onTrackChanged(BluetoothDevice target, long trackId) {
            // TODO Auto-generated method stub
            BtLogger.d(TAG, "onTrackChanged trackId = " + trackId);
            //切换歌曲改变歌曲信息
            getElementAttributes(target);
            // mSeekBar.setProgress(0);
            //同步播放按钮变化
//            getPlayerStatus(target);
        }


        @Override
        public void onPlayerAppSettingChanged(BluetoothDevice target,
                                              byte[] attribute, byte[] value) {
            //改变音量等设置信息
        }

        @Override
        public void onGetFolderItemsRsp(BluetoothDevice target, byte scope,
                                        BluetoothAvrcpBrowseItem[] items, int status) {
            if (!BluetoothAvrcpController.isSuccess(status)) {
                 BtLogger.d(TAG, "GetFolderItems response failed");
                return;
            }
            BtLogger.d(TAG, "onGetFolderItemsRsp");
            switch (scope) {
                case BluetoothAvrcpController.SCOPE_MEDIA_PLAYER_LIST:
                     BtLogger.d(TAG, "case:setting Media player list");
                    break;
                case BluetoothAvrcpController.SCOPE_NOW_PLAYING:
                    break;
            }
        }
    }

    private Handler mReplayHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    //不能判断是否处于蓝牙界面，因为后台播放也需要恢复
//                    boolean isBluetooth = mControlManager.btMusicIsForeground();

                    if(true){
                        autoStartPlayer(DataStatic.mCurrentBT);
                    }
                    break;
            }
        }
    };

    private void removeAvrcpHandler(int playStatus){
//        boolean pauseResponse = (avrcpState == BluetoothAvrcpController.PLAY_STATUS_PLAYING
//                && playStatus != BluetoothAvrcpController.PLAY_STATUS_PLAYING) ? true: false;
//        boolean playResponse = (avrcpState != BluetoothAvrcpController.PLAY_STATUS_PLAYING
//                && playStatus == BluetoothAvrcpController.PLAY_STATUS_PLAYING) ? true: false;
        BtLogger.e(TAG, "removeAvrcpHandler avrcpState 变化(1) " + avrcpState + " >>> " + (long) playStatus);
//
//        if(pauseResponse){
//            BtLogger.e(TAG, "removeAvrcpHandler 暂停 移除 mPauseHandler ");
//            mPauseHandler.removeMessages(0);
//        }
//        if(playResponse){
//            BtLogger.e(TAG, "removeAvrcpHandler 播放 移除 mPlayHandler ");
//            mPlayHandler.removeMessages(0);
//        }

        if(playStatus == BluetoothAvrcpController.PLAY_STATUS_PLAYING){
            if(mPlayHandler.hasMessages(0)){
                BtLogger.e(TAG, "removeAvrcpHandler 播放 移除 mPlayHandler ");
                mPlayHandler.removeMessages(0);
            }
        }else{
            if(mPauseHandler.hasMessages(0)){
                BtLogger.e(TAG, "removeAvrcpHandler 暂停 移除 mPauseHandler ");
                mPauseHandler.removeMessages(0);
            }
        }
    }

    private void avrcpPause(BluetoothDevice bluetoothDevice) {
        BtLogger.e(TAG, "avrcpPause----avrcp="+avrcp);
        if(avrcp != null && bluetoothDevice != null) {
            BtLogger.e(TAG, "avrcpPause------pause-bt="+bluetoothDevice);
            avrcp.pause(bluetoothDevice);
        } else {
            //初始化蓝牙音乐相关数据
            notifyBTService(6);
            //异常处于暂停时将状态改为暂停
            avrcpState = BluetoothAvrcpController.PLAY_STATUS_PAUSED;
            notifyMusic(1, (int) avrcpState);
        }
    }
    private void avrcpPlay(BluetoothDevice bluetoothDevice){
        BtLogger.e(TAG, "avrcpPlay--------------------avrcp="+avrcp);
        BtLogger.e(TAG, "avrcpPlay--------------------bluetoothDevice=" + bluetoothDevice);
        //在蓝牙音乐界面,并且蓝牙在连接的状态,加上一条：如果是因为AUDIOFOCUS_LOSS而暂停掉后获得audiofocus后接着播放
        if(avrcp != null && bluetoothDevice != null
                && mLossType != AudioManager.AUDIOFOCUS_LOSS) {
            //立马将上一个状态还原给当前状态,避免状态因时间差错乱
            BtLogger.e(TAG, "avrcpPlay------play-avrcpState="+avrcpState);
            avrcp.play(bluetoothDevice);
        } else {
            //初始化蓝牙音乐相关数据
            notifyBTService(6);
        }
    }

    private int mRepauseTimes = 0;
    protected Handler mPauseHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    if(mRepauseTimes < AVRCP_REDO_TIMES){
                        BtLogger.e(TAG, "mPauseHandler------循环暂停");
                        avrcpPause(DataStatic.mCurrentBT);
                        if(mPauseHandler.hasMessages(0)){
                            mPauseHandler.removeMessages(0);
                        }
                        mPauseHandler.sendEmptyMessageDelayed(0, AVRCP_REDO_DELAY);
                        mRepauseTimes++;
                    }else{
                        mRepauseTimes = 0;
                    }
                    break;
            }
        }
    };

    private int mReplayTimes = 0;
    protected Handler mPlayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    if(mReplayTimes < AVRCP_REDO_TIMES){
                        BtLogger.e(TAG, "mPlayHandler------循环播放");
                        avrcpPlay(DataStatic.mCurrentBT);
                        if(mPlayHandler.hasMessages(0)){
                            mPlayHandler.removeMessages(0);
                        }
                        mPlayHandler.sendEmptyMessageDelayed(0, AVRCP_REDO_DELAY);
                        mReplayTimes++;
                    }else{
                        mReplayTimes = 0;
                    }
                    break;
            }
        }
    };

    public void startPlayerBy(BluetoothDevice bluetoothDevice) {
        //播放操作前同步播放按键
        notifyMusic(1, (int) avrcpState);
        //同步记录的歌曲信息
        notifyMusic(0, 0, mMusicName, mMediaAlbum, mMediaArtist);
        //主动播放则直接还原Loss状态
        mLossType = 0;
        BtLogger.e(TAG, "startPlayerBy");
        startPlayer(bluetoothDevice);
    }

    public void startPlayerForLoss() {
        BtLogger.e(TAG, "startPlayerForLoss-mLossType=" + mLossType);
        if(mLossType == AudioManager.AUDIOFOCUS_LOSS){
            mLossType = 0;
            //避免重复
            mReplayHandler.removeMessages(0);
            //苹果手机至少延时1秒才能正常恢复播放(来电打断)
            mReplayHandler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    /**
     * 播放
     * @param bluetoothDevice
     */
    private void startPlayer(BluetoothDevice bluetoothDevice){
        BtLogger.e(TAG, "startPlayer--------------------avrcp="+avrcp);
        BtLogger.e(TAG, "startPlayer--------------------bluetoothDevice=" + bluetoothDevice);
        BtLogger.e(TAG, "startPlayer--------------------avrcpState="+avrcpState);
        BtLogger.e(TAG, "startPlayer--------------------mLossType=" + mLossType);
        //在蓝牙音乐界面,并且蓝牙在连接的状态,加上一条：如果是因为AUDIOFOCUS_LOSS而暂停掉后获得audiofocus后接着播放
        if(avrcp != null && bluetoothDevice != null
                && mLossType != AudioManager.AUDIOFOCUS_LOSS) {
            //立马将上一个状态还原给当前状态,避免状态因时间差错乱
            avrcpState = avrcpLastState;
            BtLogger.e(TAG, "startPlayer------play-avrcpState="+avrcpState);
            avrcp.play(bluetoothDevice);
            //获取音频焦点
//            requestAudioFocus();
            //播放音乐时才升音
//            fadeUpVolume();
            if(mPlayHandler.hasMessages(0)){
                mPlayHandler.removeMessages(0);
            }
            mPlayHandler.sendEmptyMessageDelayed(0, AVRCP_REDO_DELAY);
        } else {
            //初始化蓝牙音乐相关数据
            notifyBTService(6);
        }
    }

    private void autoStartPlayer(BluetoothDevice bluetoothDevice){
        BtLogger.e(TAG, "autoStartPlayer--------------------mLossType="+mLossType);
        BtLogger.e(TAG, "autoStartPlayer--------------------avrcpLastState="+avrcpLastState);
        BtLogger.e(TAG, "autoStartPlayer--------------------avrcpState="+avrcpState);
        int a2dpState = mControlManager.getConnectionA2dpState(bluetoothDevice);
        //如果不是主动暂停的
        if(avrcpLastState == BluetoothAvrcpController.PLAY_STATUS_PLAYING
                && a2dpState == BluetoothProfile.STATE_CONNECTED){
            startPlayer(bluetoothDevice);
        }
        //自动播放状态还原
        mIsLoss = false;
    }

    /**
     * 停止
     * @param bluetoothDevice
     */
    public void stopPlayer(BluetoothDevice bluetoothDevice) {
        BtLogger.d(TAG, "stopPlayer");
        //在蓝牙音乐界面,并且蓝牙在连接的状态,加上一条：如果是因为AUDIOFOCUS_LOSS而暂停掉后获得audiofocus后接着播放
        if(avrcp!=null && bluetoothDevice != null) {
            avrcp.stop(bluetoothDevice);
        }
    }

    /**
     * 暂停
     * @param bluetoothDevice
     */
    public void pausePlayer(BluetoothDevice bluetoothDevice) {
        BtLogger.e(TAG, "pausePlayer-----------------------======avrcpState="+avrcpState);
        BtLogger.e(TAG, "pausePlayer--------------------mLossType="+mLossType);
        if(avrcp != null && bluetoothDevice != null) {
            BtLogger.e(TAG, "pausePlayer------pause-bt="+bluetoothDevice);
            avrcp.pause(bluetoothDevice);
            if(mReplayHandler.hasMessages(0)){
                mReplayHandler.removeMessages(0);
            }
            if(mPauseHandler.hasMessages(0)){
                mPauseHandler.removeMessages(0);
            }
            mPauseHandler.sendEmptyMessageDelayed(0, AVRCP_REDO_DELAY);
        } else {
            //初始化蓝牙音乐相关数据
            notifyBTService(6);
            //异常处于暂停时将状态改为暂停
            avrcpState = BluetoothAvrcpController.PLAY_STATUS_PAUSED;
            notifyMusic(1, (int) avrcpState);
        }
    }

    public void pausePlayerBy(BluetoothDevice bluetoothDevice) {
        BtLogger.e(TAG, "pausePlayerBy");
        pausePlayer(bluetoothDevice);
    }

    /**
     * 下一曲
     * @param bluetoothDevice
     */
    public void nextMusic(BluetoothDevice bluetoothDevice) {
        BtLogger.d(TAG, "nextMusic");
        if(avrcp!=null && bluetoothDevice!= null) {
            avrcp.forward(bluetoothDevice);
//            fadeUpVolume();
        } else {
            //初始化蓝牙音乐相关数据
            notifyBTService(6);
        }
    }

    /**
     * 上一曲
     * @param bluetoothDevice
     */
    public void lastMusic(BluetoothDevice bluetoothDevice) {
        BtLogger.d(TAG, "lastMusic");
        if(avrcp!=null && bluetoothDevice!=null){
            avrcp.backward(bluetoothDevice);
//            fadeUpVolume();
        } else {
            //初始化蓝牙音乐相关数据
            notifyBTService(6);
        }
    }

    public void setmA2dpConnecting(boolean mA2dpConnecting) {
        this.mA2dpConnecting = mA2dpConnecting;
    }

    public void setmA2dpDisconnecting(boolean mA2dpDisconnecting) {
        this.mA2dpDisconnecting = mA2dpDisconnecting;
    }

    public void a2dpReConnecting(){
        //如果a2dp连接失败则再次连接
        if(mA2dpConnecting){
            mA2dpConnecting = false;
            BtLogger.d(TAG, "a2dpReConnecting -DataStatic.mCurrentBT= " + DataStatic.mCurrentBT);
//            notifyBTService(6);
//            connectA2dp(DataStatic.mCurrentBT);
        }
    }

    public void a2dpConnecting(){
        //LG手机连接A2DP有问题，必须延时3s才能连接成功，5s6s都试过，效果不好
        BtLogger.d(TAG, "connectA2dpConnecting2dp =**************************************************** " );
        //如果连接上蓝牙则连接A2DP
        mA2dpHandler.removeMessages(2);
        mA2dpHandler.sendEmptyMessageDelayed(2, RECONNECT_A2DP_DELAY);
    }

    protected void connectA2dp(BluetoothDevice bluetoothDevice) {
        BtLogger.d(TAG, "connectA2dpConnecting2dp-connectA2dp- =**************************************************** " );
        int easylink = android.os.ProtocolManager.getInstance().getAppStatus("EC");
        //蓝牙和易联界面连接a2dp
        if(true) {
            int a2dpState = mControlManager.getConnectionA2dpState(bluetoothDevice);
            BtLogger.d(TAG, "connectA2dp = " + a2dpState);
            BtLogger.d(TAG, "mA2dpConnecting = " + mA2dpConnecting);
            BtLogger.d(TAG, "mA2dpDisconnecting = " + mA2dpDisconnecting);
            BtLogger.d(TAG, "bluetoothDevice = " + bluetoothDevice);
            //连接就还原断开A2DP标志（不能主动还原这个状态，有些设备断开响应迟钝，如果在这个时候连接会出问题）
//            mA2dpDisconnecting = false;
            //因为延时处理的，所以这里需要判断当前状态，如果断开了就不能连接A2DP
            if(bluetoothDevice == null
                    || mControlManager.getConnectionState(bluetoothDevice) != BluetoothHfDevice.STATE_CONNECTED){
                return;
            }
            try {
                //避免重复
                mA2dpHandler.removeMessages(2);
                if (a2dpState == BluetoothProfile.STATE_DISCONNECTED && !mA2dpConnecting) { // 0
                    BtLogger.e(TAG, "a2dpState == BluetoothProfile.STATE_DISCONNECTED");
                    //只有当之前的蓝牙设备完全断开才连接
                    if(!mA2dpDisconnecting){
                        mA2dpConnecting = true;
                        mControlManager.connectA2dp(bluetoothDevice);
                    }
                    mA2dpHandler.sendEmptyMessageDelayed(2, RECONNECT_A2DP_DELAY);
                } else if (a2dpState == BluetoothProfile.STATE_CONNECTING && mA2dpConnecting) { // 1
                    //处于连接中则查询连接状态
                    mA2dpHandler.sendEmptyMessageDelayed(2, RECONNECT_A2DP_DELAY);
                } else if (a2dpState == BluetoothProfile.STATE_CONNECTED && mA2dpConnecting) { // 2
                    //已连接则还原连接状态标志
                    mA2dpConnecting = false;
                }
            } catch (final Exception e) {
                BtLogger.d(TAG, "connectA2dp--catch = " + e);
            } finally {
                BtLogger.d(TAG, "connectA2dp--finally = ");
            }
        }
    }
    private void disconnectA2dp(BluetoothDevice bluetoothDevice) {
        int a2dpState = mControlManager.getConnectionA2dpState(bluetoothDevice);
        BtLogger.e(TAG, "disconnectA2dp-a2dpState="+a2dpState);
        BtLogger.e(TAG, "disconnectA2dp-mA2dpDisconnecting="+mA2dpDisconnecting);
        BtLogger.e(TAG, "disconnectA2dp-bluetoothDevice="+bluetoothDevice);
        //断开就还原连接A2DP标志
//        mA2dpConnecting = false;
        try {
            //避免重复
            mA2dpHandler.removeMessages(3);
            if (a2dpState == BluetoothProfile.STATE_CONNECTED && !mA2dpDisconnecting) { //2
                //只有非连接状态才断开
                if(!mA2dpConnecting) {
                    mA2dpDisconnecting = true;
                    mControlManager.disconnectA2dp(bluetoothDevice);
                }
                mA2dpHandler.sendEmptyMessageDelayed(3, RECONNECT_A2DP_DELAY);
            } else if (a2dpState == BluetoothProfile.STATE_CONNECTING){ //1
                //延时200毫秒后再次断开
                mA2dpHandler.sendEmptyMessageDelayed(3, RECONNECT_A2DP_DELAY);
            } else if (a2dpState == BluetoothProfile.STATE_DISCONNECTING && mA2dpDisconnecting){ //3
                //如果处于断开中状态则循环查询连接状态
                mA2dpHandler.sendEmptyMessageDelayed(3, RECONNECT_A2DP_DELAY);
            } else if (a2dpState == BluetoothProfile.STATE_DISCONNECTED && mA2dpDisconnecting){ //0
                //延时100毫秒查询断开状态,如果已断开则
                mA2dpDisconnecting = false;
            }
        } catch (final Exception e) {
        }
    }

    private boolean isA2dpPlaying(BluetoothDevice bluetoothDevice) {
        boolean isA2dpPlaying = mControlManager.isA2dpPlaying(bluetoothDevice);
        BtLogger.d(TAG, "isA2dpPlaying() " + isA2dpPlaying);
        return isA2dpPlaying;
    }

    private boolean isMusicPlaying() {
        boolean isMusicPlaying;
        if(avrcpState == BluetoothAvrcpController.PLAY_STATUS_PLAYING) {
            isMusicPlaying = true;
        } else {
            isMusicPlaying = false;
        }
        BtLogger.d(TAG, "isMusicPlaying() " + isMusicPlaying);
        return isMusicPlaying;
    }

    private AudioManager mAudioManager;
    private void setStreamVolume(float volume) {
        if (mAudioManager == null) {
            BtLogger.e(TAG, "setStreamVolume---mAudioManager==null");
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        if(mAudioManager != null){
            BtLogger.d(TAG, "setStreamVolume---volume="+volume);
            mAudioManager.setStreamVolume(AudioManager.STREAM_RING/*STREAM_MUSIC*/, (int) (volume * VOLUME_MAX), 0);
        }
    }

    public void setStreamVolumePub(float volume) {
        setStreamVolume(volume);
    }

    @SuppressLint("NewApi")
    AudioManager.OnAudioFocusChangeListener afcl = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            mAudioFocusHandler.sendEmptyMessage(focusChange);
        }
    };

    private Handler mAudioFocusHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            BtLogger.e(TAG, "mAudioFocusHandler, msg.what = " + msg.what);
            BtLogger.e(TAG, "start-mIsLoss= " + mIsLoss);
            BtLogger.e(TAG, "start-avrcpLastState= " + avrcpLastState);
            BtLogger.e(TAG, "start-avrcpStatet= " + avrcpState);
            mAudioFocusType = msg.what;
            switch (msg.what) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    //方控响应注销
                    notifyMain(5, false);
                    BtLogger.e(TAG, "AUDIOFOCUS_LOSS");
                    //过滤重复执行,否则会出现连续倒车将上一个状态改为暂停
                    if(mIsLoss == false){
                        mIsLoss = true;
                        //记录被动暂停前的状态
                        avrcpLastState = avrcpState;
                    }
                    abandonBTMusic();
                    //长焦点被抢占时将播放状态改为停止,避免后台意外断开后自动连接引起自动继续播放蓝牙音乐
                    //避免暂停播放但播放状态未及时同步
                    avrcpState = BluetoothAvrcpController.PLAY_STATUS_PAUSED;
                    mLossType = msg.what;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    BtLogger.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    //导航播报，降音
                    fadeDownVolume();
                    mLossType = msg.what;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    BtLogger.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                    //方控响应注销
                    notifyMain(5, false);
                    //讯飞短暂获取
//                    if (isA2dpPlaying(DataStatic.mCurrentBT)) {
//                        setStreamVolume(31); // 当失去音频焦点时，恢复音量
//                    } else {
//                    }
                    //过滤重复执行,否则会出现连续倒车将上一个状态改为暂停
                    if(mIsLoss == false){
                        mIsLoss = true;
                        //记录被动暂停前的状态
                        avrcpLastState = avrcpState;
                    }
                    //倒车和打电话，降音至0
                    fadeDownAbandonAudio();
                    //如果不是主动暂停的
//                    if(mActivePause == false){
//                        pausePlayer(DataStatic.mCurrentBT);
                        //避免暂停播放但播放状态未及时同步
//                        avrcpState = BluetoothAvrcpController.PLAY_STATUS_PAUSED;
//                    }、
                    mLossType = msg.what;
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    int parkingFlag = android.os.ProtocolManager.getInstance().getAppStatus("ParkingActivity");
                    //如果还处于通话或倒车中
                    if(parkingFlag == 1 || android.os.ProtocolManager.getInstance().getBTPhoneStatus() == 1){
                        break;
                    }
                    BtLogger.e(TAG, "AUDIOFOCUS_GAIN-mLossType="+mLossType);
                    //旋钮切歌响应注册
                    notifyMain(4, false);
                    //自己获取到音频焦点
//                    fadeUpVolume();
                    if(mAudioGainHandler.hasMessages(0)){
                        mAudioGainHandler.removeMessages(0);
                    }
                    //因为有倒车界面判断，所以需要延时才判断准确
                    mAudioGainHandler.sendEmptyMessageDelayed(0, 500);
                    if(mLossType == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
                        //避免重复
//                        mReplayHandler.removeMessages(0);
                        //苹果手机至少延时800毫秒才能正常恢复播放(来电打断)
//                        mReplayHandler.sendEmptyMessageDelayed(0, 1000);
                    }
                    break;
                default:
                    BtLogger.e(TAG, "Unknown audio focus change code");
                    break;
            }
            BtLogger.e(TAG, "art-mIsLoss= " + mIsLoss);
            BtLogger.e(TAG, "art-avrcpLastState= " + avrcpLastState);
            BtLogger.e(TAG, "art-avrcpStatet= " + avrcpState);
        }
    };

    private Handler mAudioGainHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    fadeUpVolume();
                    break;
            }
        }
    };

    public void lossRegain(boolean isMenuMusic){
        // 只要进入蓝牙就抢焦点
        requestAudioFocus();
        //自己获取到音频焦点
        //重启音乐
        BtLogger.w(TAG, "lossRegain");
        notifyBTService(6);
        //这里恢复mIsLoss就会造成在蓝牙音乐正在恢复过程处于暂停状态时切换响应Loss保存暂停状态
        //如果不恢复，处于别的菜单时会暂停音乐，但切换到暂停的蓝牙音乐再切换别的应用恢复就会自动播放
//                        mIsLoss = false;
        //当不是蓝牙音乐菜单时才恢复Loss状态，原因就是上面两条注释
        if(!isMenuMusic){
            mIsLoss = false;
        }
        //如果蓝牙音乐处于播放中就将音量恢复(这里升音量就没有渐变效果)
//                        fadeUpVolume();
        //重新进入蓝牙音乐界面时显示歌曲信息
        if(DataStatic.mCurrentBT != null){
            getPlayerStatus(DataStatic.mCurrentBT);
        }
    }

    private Handler mVolumeHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    fadeDownVolume();
                    break;
                case 1:
                    fadeUpVolume();
                    break;
            }
        }
    };

    private static float max_vol_ratio = 1.0f;//0.75f;
    private void fadeUpVolume() {
        //倒车，通话，被抢占音频焦点都不得提升音量，避免串音
        int parkingFlag = android.os.ProtocolManager.getInstance().getAppStatus("ParkingActivity");
        int easylink = android.os.ProtocolManager.getInstance().getAppStatus("EC");
        if(parkingFlag == 1 || (AudioManager.AUDIOFOCUS_LOSS == mAudioFocusType && easylink != 1)
                || android.os.ProtocolManager.getInstance().getBTPhoneStatus() == 1){
            return;
        }
        BtLogger.d(TAG, "fadeUpVolume-parkingFlag="+parkingFlag);
        //停止升降音
        mVolumeHandler.removeMessages(0);
        mVolumeHandler.removeMessages(1);
        //这个不需要否则会造成先降音再渐变升音
//        float mixRatio = Settings.System.getInt(mContext.getContentResolver(),"semisky_car_navmixing", 7);
//        if(mCurrentVolume == 1.0f){
//            mCurrentVolume = (88 - (mixRatio - 1) * 8) / 100f;
//        }
        //逐渐增加音量
        DataStatic.mCurrentVolume += .05f;
        if(DataStatic.mCurrentVolume > max_vol_ratio){
            DataStatic.mCurrentVolume = max_vol_ratio;
        }
        setStreamVolume(DataStatic.mCurrentVolume);
        if (DataStatic.mCurrentVolume < max_vol_ratio) {
            mVolumeHandler.sendEmptyMessageDelayed(1, 80);
        }
    }

    public void restoreVolume(){
        setStreamVolume(DataStatic.mCurrentVolume);
    }

    private void fadeDownVolume() {
        BtLogger.d(TAG, "fadeDownVolume");
        //停止升降音
        mVolumeHandler.removeMessages(0);
        mVolumeHandler.removeMessages(1);
        float mixRatio = Settings.System.getInt(mContext.getContentResolver(),"semisky_car_navmixing", 7);
        float lowestVolume = (88 - (mixRatio - 1) * 8) / 100f * max_vol_ratio;
        //逐渐降低音量
        DataStatic.mCurrentVolume -= .05f;
        if(DataStatic.mCurrentVolume <= lowestVolume){
            DataStatic.mCurrentVolume = lowestVolume;
        }
        int btPhoneStatus = android.os.ProtocolManager.getInstance().getBTPhoneStatus();
        //非通话状态才降音,避免将铃声给降音了
        if(btPhoneStatus == 1){
            setStreamVolume(1);
        } else {
            setStreamVolume(DataStatic.mCurrentVolume);
            //这个需要放在设置最低音量后执行
            if(DataStatic.mCurrentVolume == lowestVolume){
                if(mFadeDownClose){
                    mFadeDownClose = false;
                    disconnectA2dp(DataStatic.mCurrentBT);
                }
                if(mFadeAbandonClose){
                    BtLogger.d(TAG, "setStreamVolume---静音=00");
                    mFadeAbandonClose = false;
                    //改为a2dp通道静音即可
                    DataStatic.mCurrentVolume = 0;
                    setStreamVolume(DataStatic.mCurrentVolume);
                }
            }
        }
        if (DataStatic.mCurrentVolume > lowestVolume) {
            mVolumeHandler.sendEmptyMessageDelayed(0, 80);
        }
    }

    public void requestAudioFocusResume() {
        BtLogger.w(TAG, "requestAudioFocusResume-mIsAudioFocus=" + mIsAudioFocus);
        if (mAudioManager == null) {
            BtLogger.e(TAG, "requestAudioFocusResume-mAudioManager==null");
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        if(mAudioManager != null && mIsAudioFocus == 0) {
            BtLogger.e(TAG, "requestAudioFocusResume-执行=" + mAudioManager);
            //蓝牙断开，音乐界面播放时调用
            mIsAudioFocus = mAudioManager.requestAudioFocus(afcl, AudioManager.STREAM_RING/*STREAM_MUSIC*/, AudioManager.AUDIOFOCUS_GAIN);
            mAudioFocusType = 0;
            if (mIsAudioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {// 申请音频永久焦点成功
                BtLogger.e(TAG, "BT_MUSIC-----------------------requestAudioFocus success");
            } else {// 申请音频永久焦点失败
                BtLogger.e(TAG, "BT_MUSIC-----------------------requestAudioFocus failed");
            }
        }
        //旋钮切歌响应注册
        notifyMain(4, false);
    }

    public void requestAudioFocus() {
        boolean isBluetooth = mControlManager.btMusicIsForeground();
        BtLogger.w(TAG, "requestAudioFocus-isBluetooth=" + isBluetooth);
        if(isBluetooth){
            requestAudioFocusResume();
        }
    }

    private void fadeDownCloseAudio() {
        mFadeDownClose = true;
        fadeDownVolume();
    }

    private void fadeDownAbandonAudio() {
        mFadeAbandonClose = true;
        fadeDownVolume();
    }

    private void abandonAudioFocus() {
        if (mAudioManager == null) {
            BtLogger.e(TAG, "abandonAudioFocus-mAudioManager==null");
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        BtLogger.d(TAG, "abandonAudioFocus");
        if(mAudioManager != null){
            //界面销毁时调用
            int abandonAudioFocusState = mAudioManager.abandonAudioFocus(afcl);
            if (abandonAudioFocusState == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {// 释放音频永久焦点成功
                BtLogger.e(TAG, "BT_MUSIC-------------------abandonAudioFocus success");
            } else {// 释放音频永久焦点失败
                BtLogger.e(TAG, "BT_MUSIC---------------abandonAudioFocus fail");
            }
        }
        //方控响应注销
        notifyMain(5, false);
    }

    public void getPlayerStatus(BluetoothDevice bluetoothDevice){
//        btnPause.setEnabled(true);
//        btnNext.setEnabled(true);
//        btnPrev.setEnabled(true);
        BtLogger.d(TAG, "getPlayerStatus");
        try {
            avrcp.getPlayStatus(bluetoothDevice);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void getElementAttributes(BluetoothDevice bluetoothDevice){
        BtLogger.d(TAG, "getElementAttributes-avrcpState=" + avrcpState);
        try {
            //如果是播放状态则获取音频焦点
            if(avrcpState == BluetoothAvrcpController.PLAY_STATUS_PLAYING){
                requestAudioFocus();
            }
            avrcp.getElementAttributes(bluetoothDevice, supportElementAttr);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    long mDuration;
    public void clearID3Infos(){
        mMediaAlbum = mContext.getResources().getString(R.string.ym_bt_please_open_player);
        mMediaArtist = "";
        mMusicName = "";
    }

    public void reloadMetaData(){
        // TODO: Need to update txtPlayername here.
        // txtPlayer.setText("Current Player Name");
        if (mBTAvrcpControll != null && mMusicName != mBTAvrcpControll.getMediaTitle()) {
            BtLogger.e(TAG, "reloadMetaData-avrcpState=" + avrcpState);
            String title = mBTAvrcpControll.getMediaTitle();
            String mediaAlbum = mBTAvrcpControll.getMediaAlbum();
            String mediaArtist = mBTAvrcpControll.getMediaArtist();
            if(!(title.equals("")||title==null)){
//                titleTextView.setText(title);
//                mediaAlbumTextView.setText(mediaAlbum);
//                mediaArtistTextView.setText(mediaArtist);
            }
            //如果是播放状态则获取音频焦点
            if(avrcpState == BluetoothAvrcpController.PLAY_STATUS_PLAYING){
                requestAudioFocus();
            }
            if(title == null || "".equals(title)){
                mediaAlbum = mContext.getResources().getString(R.string.ym_bt_please_open_player);
//                avrcpState = BluetoothAvrcpController.PLAY_STATUS_STOPPED;
            }
            //由请打开播放器状态变为歌曲信息时直接再次播放，解决播放按钮点击只获取歌曲信息而不播放的bug
            //效果不达标，只要连接就会全自动播放
//            if(mContext.getResources().getString(R.string.please_open_player).equals(mMediaAlbum)){
//                //二次执行播放音乐
//                startPlayerBy(DataStatic.mCurrentBT);
//            }
            mMediaAlbum = mediaAlbum;
            mMediaArtist = mediaArtist;
            mMusicName = title;
            BtLogger.e(TAG, "title:" + title + "mediaAlbum:" + mediaAlbum
                    + "mediaArtist:" + mediaArtist);
            notifyMusic(0, 0, title, mediaAlbum, mediaArtist);
            //同步播放按键
            notifyMusic(1, (int) avrcpState);
            //第二个参数表示蓝牙是否连接，如果进入这里应该是连接状态
            notifyMain(1, true, avrcpState, title);
        }

        try {
            mDuration = mBTAvrcpControll.getMediaDuration();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    //清空列表弹框
    private BTClearDialog mBTClearDialog;

    public void showmBTClearDialog(Context context, int title, int prompt, View.OnClickListener listener){
        showmBTClearDialog(context, false, title, prompt, listener);
    }

    public void showmBTClearDialog(Context context, boolean singleBtn,int title, int prompt, View.OnClickListener listener){
        mBTClearDialog = BTClearDialog.getInstance(context);
        mBTClearDialog.setText(title, prompt);
        mBTClearDialog.setOnOkListener(listener);
        mBTClearDialog.setOnConfirmListener(listener);
        mBTClearDialog.setOnCancelListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissmBTClearDialog();
                //点击禁用还原
                notifyContactsList(3);
            }
        });
        mBTClearDialog.setSingleButton(singleBtn);
        mBTClearDialog.show();
    }

    public void dismissmBTClearDialog(){
        if(mBTClearDialog != null){
            mBTClearDialog.dismiss();
        }
    }

    //配对提示弹框
    private BTPairDialog mBTPairDialog;
    public void showmBTPairDialog(Context context, int titleId, int nameTagId, String name, int pwdTagId, String pwd){
        mBTPairDialog = BTPairDialog.getInstance(context);
        mBTPairDialog.setText(titleId, nameTagId, name, pwdTagId, pwd);
//        mBTPairDialog.setOnCancelListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dismissmBTPairDialog();
//            }
//        });
        mBTPairDialog.show();
    }

    public void dismissmBTPairDialog(){
        if(mBTPairDialog != null){
            mBTPairDialog.dismiss();
        }
    }

    //加载进度弹框
    private BTProgressDialog mBTProgressDialog;
    public void showmBTProgressDialog(Context context, int promptId, String rate){
        //处于蓝牙界面才弹出加载框
        if(DataStatic.sIsBluetoothUI){
            mBTProgressDialog = BTProgressDialog.getInstance(context);
            mBTProgressDialog.setText(promptId, rate);
            mBTProgressDialog.show();
        }
    }

    public void dismissmBTProgressDialog(){
        if(mBTProgressDialog != null){
            mBTProgressDialog.dismiss();
        }
    }

    public void sendCurrentPosition(Activity activity, int first, int second, int third){
//        Intent intent = new Intent();
//        intent.setAction("com.semisky.nl.currentPage");
//        intent.putExtra("FirstPage", activity.getResources().getString(first));
//        if(second != -1) {
//            intent.putExtra("SecondPage", activity.getResources().getString(second));
//        }
//        if(third != -1){
//            intent.putExtra("thirdPage", activity.getResources().getString(third));
//        }
//        activity.sendBroadcast(intent);
    }

    //***以下为保存联系人数据
    public void saveContacts(List<Contacts> contactsList){
        PhoneDBManager phoneDBHelper = new PhoneDBManager(mContext);
        phoneDBHelper.saveContacts(contactsList);
    }

    public List<Contacts> getContacts(){
        List<Contacts> contactsList;
        PhoneDBManager phoneDBHelper = new PhoneDBManager(mContext);
        contactsList = phoneDBHelper.getContactsList();
        return contactsList;
    }

    public void deleteContacts(){
        PhoneDBManager phoneDBHelper = new PhoneDBManager(mContext);
        phoneDBHelper.deleteAllContacts();
        //同步联系人界面
        notifyContactsList(0);
    }

    //***查找联系人
    public String getContactsNumByName(String name) {
        BtLogger.d(TAG, "getContactsNumByName = " + name);
        PhoneDBManager phoneDBHelper = new PhoneDBManager(mContext);
        String number = phoneDBHelper.getContactsNumByName(name);
        return number;
    }

    public String getContactsNameByNumber(String number) {
        BtLogger.d(TAG, "getContactsNameByNumber = " + number);
        PhoneDBManager phoneDBHelper = new PhoneDBManager(mContext);
        String name = phoneDBHelper.getContactsNameByNumber(number);
        return name;
    }

    //***以下为保存通话记录数据
    public void saveCallLogRecords(List<CallLogRecords> callLogRecordsList){
        PhoneDBManager phoneDBHelper = new PhoneDBManager(mContext);
        phoneDBHelper.saveCallLogRecords(callLogRecordsList);
    }

    public void insertCallLogRecords(CallLogRecords callLogRecords){
        PhoneDBManager phoneDBHelper = new PhoneDBManager(mContext);
        phoneDBHelper.insertCallLogRecords(callLogRecords);
    }

    public List<CallLogRecords> getCallLogRecords(){
        List<CallLogRecords> callLogRecordsList;
        PhoneDBManager phoneDBHelper = new PhoneDBManager(mContext);
        callLogRecordsList = phoneDBHelper.getCallLogRecordsList();
        return callLogRecordsList;
    }

    public List<CallLogRecords> getCallLogRecordsByType(int type){
        List<CallLogRecords> callLogRecordsList;
        PhoneDBManager phoneDBHelper = new PhoneDBManager(mContext);
        callLogRecordsList = phoneDBHelper.getCallLogRecordsListByType(type);
        return callLogRecordsList;
    }

    public String getFirstCallLog(){
        List<CallLogRecords> callLogRecordsList = getCallLogRecords();
        if(callLogRecordsList.size() > 0){
            return callLogRecordsList.get(0).getNumber();
        }else{
            return "";
        }
    }

    public void deleteAllCallLogRecords(){
        PhoneDBManager phoneDBHelper = new PhoneDBManager(mContext);
        phoneDBHelper.deleteAllCallLogRecords();
        //同步通话记录界面
        notifyCallLogList(0);
    }

    public void deleteSingleCallLogRecord(String number){
        PhoneDBManager phoneDBHelper = new PhoneDBManager(mContext);
        phoneDBHelper.deleteSingleCallLogRecord(number);
        //同步通话记录界面
        notifyCallLogList(0);
    }

    //以下为重连处理
    public void reConnectA2dp(){
        BtLogger.d(TAG, "2秒后重连reConnectA2dp");
        //重连A2dp
        mA2dpHandler.sendEmptyMessageDelayed(0, 2000);
    }

    protected Handler mA2dpHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    //初始化蓝牙音乐相关数据
                    notifyBTService(6);
                    break;
                case 1:
                    if((!isA2dpPlaying(DataStatic.mCurrentBT) || !isStatusPlaying(avrcpState))&& (avrcp != null)) { //如果没有播放才能播放,avrcp应该初始化好
                        startPlayer(DataStatic.mCurrentBT);
                    }
                    break;
                case 2:
                    //循环连接
                    connectA2dp(DataStatic.mCurrentBT);
                    break;
                case 3:
                    //循环关闭
                    disconnectA2dp(DataStatic.mCurrentBT);
                    break;
            }
        }
    };

    private boolean isStatusPlaying(long playStatus) {
        return (playStatus == BluetoothAvrcpController.PLAY_STATUS_PLAYING ||
                playStatus == BluetoothAvrcpController.PLAY_STATUS_FWD_SEEK ||
                playStatus == BluetoothAvrcpController.PLAY_STATUS_REV_SEEK) ? true: false;
    }

    public void showStatus(int resId){
//        Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
    }

    private static final String PATH_SDCARD = "/storage/emulated/0";
    public static final String OUT_FILE_PULL_PB = PATH_SDCARD + "/pb.vcf";
    private static final String OUT_FILE_PULL_OCH = PATH_SDCARD + "/och.vcf";
    private static final String OUT_FILE_PULL_ICH = PATH_SDCARD + "/ich.vcf";
    private static final String OUT_FILE_PULL_MCH = PATH_SDCARD + "/mch.vcf";
    public static final String OUT_FILE_PULL_CCH = PATH_SDCARD + "/cch.vcf";

    public boolean fileIsExists(String path){
        try{
            File f=new File(path);
            if(!f.exists()){
                return false;
            }

        }catch (Exception e) {
            // TODO: handle exception
            return false;
        }
        return true;
    }

    @Override
    public void onCommandCompleted(int status) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCommandFailure(int errStatus) {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onInitCompleted() {
        // TODO Auto-generated method stub
        //如果播放器之前被唤起过就会走这里，不开启也会进这里
        BtLogger.d(TAG, "onInitCompleted()");
        //手机播放器被唤起，会走这里，但直接调用getPlayerStatus得不到数据
        //直接开始播放，在状态变化回调中处理(必须在获取播放状态之前)
//        mFuncBTOperate.startPlayerBy(DataStatic.mCurrentBT);
        //以下两个都无效
//        mFuncBTOperate.getPlayerStatus(DataStatic.mCurrentBT);
//        mFuncBTOperate.reloadMetaData();

    }

    @Override
    public void onLoadAttributesFailure() {
        BtLogger.d(TAG, "onLoadAttributesFailure()");
        //直接开始播放，在状态变化回调中处理(必须在获取播放状态之前)
//        mFuncBTOperate.startPlayerBy(DataStatic.mCurrentBT);
        //以下两个都无效
//        mFuncBTOperate.getPlayerStatus(DataStatic.mCurrentBT);
//        mFuncBTOperate.reloadMetaData();
        //avrcp初始化失败则重新初始化(不能加)
//        initBTMusic();
    }

    @Override
    public void onLoadMetataCompleted() {
        BtLogger.d(TAG, "onLoadMetataCompleted()");
        //调用getElementAttributes方法则会走这里
        // mPosition = 0;
        reloadMetaData();
    }

    @Override
    public void onLoadAppAttrsLoaded() {
    }
}
