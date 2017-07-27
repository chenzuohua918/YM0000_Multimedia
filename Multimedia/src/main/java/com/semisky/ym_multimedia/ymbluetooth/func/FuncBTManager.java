package com.semisky.ym_multimedia.ymbluetooth.func;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AppAudioPolicy;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;

import com.broadcom.bt.hfdevice.BluetoothCallStateInfo;
import com.broadcom.bt.hfdevice.BluetoothClccInfo;
import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.broadcom.bt.hfdevice.BluetoothPhoneBookInfo;
import com.broadcom.bt.hfdevice.IBluetoothHfDeviceEventHandler;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventBTService;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventDial;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;
import com.semisky.ym_multimedia.ymbluetooth.service.PbapService;
import com.ypy.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//added by zzy

/**
 * 封装蓝牙电话各种操作的类，提供接口 Created by DaiShiHao on 2014/12/25 0025.
 */
public class FuncBTManager implements ServiceListener {

    private final String TAG = "FuncBTManager";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothHfDevice bluetoothHfDevice;
    private HfDeviceEventHandler hfDeviceEventHandler;
    private final int VOLUME_MAX = 31;
    private Context mContext;
    private List<List<BluetoothPhoneBookInfo>> list = new ArrayList<List<BluetoothPhoneBookInfo>>();

    private Handler handler;
    private Handler threadHandler;
    private MyHandlerThread myHandlerThread;
    private final int THREAD_HANDLER_MSG = 0x99;

    private int mNumberType = 0;

    /* Ringtone control. */
    private static final int RING_STATE_IDLE = 0;
    private static final int RING_STATE_RINGING = 1;
    private int mRingState = RING_STATE_IDLE;
    private int mInBandRingStatus = BluetoothHfDevice.INBAND_STATE_OFF;
    private Ringtone mRingtone = null;
    private A2dpProfile mA2dpProfile;
    private boolean mOnCLCC = false;

    public FuncBTManager(Context context, Handler handler) {
        mContext = context;
        //只要创建蓝牙管理就开启同步服务
        Intent startServer = new Intent(mContext, PbapService.class);
        mContext.startService(startServer);  //启动电话簿同步服务
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.handler = handler;
        if (!BluetoothHfDevice.getProxy(context, this)) {
            BtLogger.d(TAG, "get proxy failed!");
        }

        if (myHandlerThread == null) {
            myHandlerThread = new MyHandlerThread("myHandlerThread");
            myHandlerThread.start();
        }
        if (threadHandler == null) {
            threadHandler = new Handler(myHandlerThread.getLooper(),
                    myHandlerThread);
        }
        //initA2dpService();
        mA2dpProfile = new A2dpProfile(context);
    }

    private Handler mRegHfEventHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    BtLogger.e(TAG, "mRegHfEventHandler-bluetoothHfDevice="+bluetoothHfDevice);
                    if(!registerEvent()){
                        mRegHfEventHandler.removeMessages(0);
                        mRegHfEventHandler.sendEmptyMessageDelayed(0, 300);
                    }
                    break;
            }
        }
    };

    private boolean registerEvent(){
        boolean isReg = false;
        try {
            if (bluetoothHfDevice != null) {
                hfDeviceEventHandler = new HfDeviceEventHandler();
            }
            isReg = bluetoothHfDevice.registerEventHandler(hfDeviceEventHandler);
        } catch (NullPointerException exception) {
        } finally {
        }
        BtLogger.e(TAG, "registerEvent-isReg="+isReg);
        return isReg;
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        BtLogger.w(TAG, "lianjie－bluetoothHfDevice＝" + bluetoothHfDevice);
        if (bluetoothHfDevice == null) {
            bluetoothHfDevice = (BluetoothHfDevice) proxy;
        }
        //注册电话响应
        registerEvent();
        if (bluetoothHfDevice.getPeerFeatures().isInBandToneSupported()) {  //查询连接的手机是否推送铃声
            BtLogger.d(TAG, "==>funbox: isInBandToneSupported");
        } else {
            BtLogger.d(TAG, "==>funbox: is not InBandToneSupported");
        }
        BtLogger.w(TAG, "onService OVER－bluetoothHfDevice＝"+bluetoothHfDevice);
    }

    @Override
    public void onServiceDisconnected(int profile) {
        //蓝牙异常断开
        bluetoothHfDevice = null;
        BtLogger.w(TAG, "onServiceDisconnected－bluetoothHfDevice＝"+bluetoothHfDevice);
    }

    /**
     * 判断是否存在蓝牙设备
     *
     * @return true if bluetooth device is existed, return false if not .
     */
    public boolean hasBluetoothDevice() {
        return mBluetoothAdapter != null;
    }

    /**
     * 判断蓝牙设备是否开启
     *
     * @return true if enabled or return false if not.
     */
    public boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * 打开蓝牙
     *
     * @return true on success or false on error.
     */
    public boolean enableBT() {
        return mBluetoothAdapter.enable();
    }

    /**
     * 关闭蓝牙
     *
     * @return true on success or false on error.
     */
    public boolean disableBT() {
        return mBluetoothAdapter.disable();
    }

    /**
     * 获取蓝牙名称
     *
     * @return btName
     */
    public String getBTName() {
        return mBluetoothAdapter.getName();
    }

    /**
     * 设置蓝牙的名称
     *
     * @param newName The new name for BT.
     */
    public void setBTName(String newName) {
        if (!TextUtils.isEmpty(newName))
            mBluetoothAdapter.setName(newName);
    }

    public void setScanMode(int mode, String newName) {
        BtLogger.d(TAG, "setScanMode = " + mode + ",newName = " + newName);
        mBluetoothAdapter.setScanMode(mode, -1);
        setBTName(newName);
    }

    public void setScanMode(int mode) {
        BtLogger.d(TAG, "setScanMode = " + mode);
        mBluetoothAdapter.setScanMode(mode, -1);
    }

    /**
     * 获取已配对的设备
     *
     * @return 已配对设备的Set集合
     */
    public Set<BluetoothDevice> getBondedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    public boolean connect(BluetoothDevice device) {
        //connectA2dp(device);
//        setPriority(device, BluetoothProfile.PRIORITY_AUTO_CONNECT);
        return bluetoothHfDevice.connect(device);
    }

    public BluetoothCallStateInfo getCallStateInfo(BluetoothDevice device) {
        BtLogger.d(TAG, "getCallStateInfo device = " + device);
        if (device != null && bluetoothHfDevice != null) {
            return bluetoothHfDevice.getCallStateInfo(device);
        } else {
            return null;
        }

    }

    public int getAudioState(BluetoothDevice device) {
        BtLogger.d(TAG, "getAudioState device = " + device);
        if(bluetoothHfDevice != null) {
            return bluetoothHfDevice.getAudioState(device);
        }else{
            return 0;
        }
    }

    private void setStreamVolume(float volume){
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if(audioManager != null){
            BtLogger.d(TAG, "setStreamVolume-mgr-volume=" + volume);
            audioManager.setStreamVolume(AudioManager.STREAM_RING/*STREAM_MUSIC*/, (int) (volume * VOLUME_MAX), 0);
        }
    }

    private void RingPlay() {
        // RingStop() checks to see if we are already playing.
        RingStop();
        BtLogger.d(TAG, "RingPlay()");
        //Uri alertUri = RingtoneManager.getActualDefaultRingtoneUri(bydcontext, RingtoneManager.TYPE_RINGTONE);
        //Uri alertUri = Uri.parse("/system/media/audio/ringtones/CASSIOPEIA.ogg");
        Uri alertUri = Uri.parse("/system/media/audio/ringtones/Ring_Synth_04.ogg");
        BtLogger.d(TAG, "uri = " + alertUri.toString());

        mRingtone = RingtoneManager.getRingtone(mContext, alertUri);
        if (mRingtone != null) {
            mRingtone.setStreamType(RingtoneManager.TYPE_RINGTONE);
            //响铃时最大化音量
            setStreamVolume(1);
            mRingtone.play();
            mRingState = RING_STATE_RINGING;
        }
    }
    /**
     * Stops ring audio.
     */
    private void RingStop() {
        BtLogger.d(TAG, "RingStop() mRingState = " + mRingState);

        if (mRingState == RING_STATE_RINGING) {
            if (mRingtone.isPlaying()) {
//                float mixRatio = Settings.System.getInt(mContext.getContentResolver(),"semisky_car_navmixing", 7);
//                float lowestVolume = (88 - (mixRatio - 1) * 8) / 100f;
                //停止响铃时最小化音量
//                setStreamVolume(lowestVolume);
                BtLogger.d(TAG, "setStreamVolume---铃声静音=DataStatic.mCurrentVolume="+DataStatic.mCurrentVolume);
                //铃声停止就设置静音，避免影响蓝牙A2DP
                setStreamVolume(DataStatic.mCurrentVolume);
                mRingtone.stop();
            }
        }
        mRingState = RING_STATE_IDLE;
    }

    private static final int IFLY_TURN_ON = 1;        //讯飞已经启动
    private static final int IFLY_TURN_OFF = 2;       //讯飞已经关闭
    private static int mIflyStatus = IFLY_TURN_ON;    //默认讯飞是打开的


    public void setBtPhoneStatus(int status) {
        //打开关闭麦克风
        android.os.ProtocolManager.getInstance().setMicState(status);// state 1: open  ;  0: close;
        BtLogger.d(TAG, "setBtPhoneStatus: status = " + status + ",mIflyStatus = " + mIflyStatus);
        android.os.ProtocolManager.getInstance().setBtPhoneStatus(status);
        android.os.ProtocolManager.getInstance().setAppStatus("CallActivity", status);
        if (status == 1) {   //启动蓝牙电话
            if (mIflyStatus == IFLY_TURN_ON) {   //1
                mIflyStatus = IFLY_TURN_OFF;
                android.os.AppAudioPolicy.getInstance().turnOffIfly(mContext, AppAudioPolicy.APP_BT_PHONE);
            }
        } else {
            if (mIflyStatus == IFLY_TURN_OFF) {   //2
                mIflyStatus = IFLY_TURN_ON;
                android.os.AppAudioPolicy.getInstance().turnOnIfly(mContext, AppAudioPolicy.APP_BT_PHONE);
            }
        }
    }

    public void muteForDepop(int delayMs){
//        boolean isBluetooth = btMusicIsForeground();
//        if(!isBluetooth){
//            return;
//        }
//        int btPhoneStatus = android.os.ProtocolManager.getInstance().getBTPhoneStatus();
//        if(btPhoneStatus == 1){
//            BtLogger.e(TAG, "静音-muteForDepop-delayMs="+delayMs);
//            android.os.ProtocolManager.getInstance().muteForDepop(delayMs);
//        }
//        BtLogger.e(TAG, "静音-muteForDepop-delayMs="+delayMs);
//        android.os.ProtocolManager.getInstance().muteForDepop(delayMs);
    }

    public void muteForDepop2(int delayMs){
        BtLogger.e(TAG, "静音-muteForDepop2-delayMs="+delayMs);
        android.os.ProtocolManager.getInstance().muteForDepop(delayMs);
    }


    public boolean disconnect(BluetoothDevice device) {
//        disconnectA2dp(device);
//        setPriority(device, BluetoothProfile.PRIORITY_ON);
        //断开蓝牙时清空通话状态数据
        mBCStateInfo = null;
        return bluetoothHfDevice.disconnect(device);
    }

    public boolean setPriority(BluetoothDevice device, int priority) {
        BtLogger.e(TAG, "设置＊＊＊＊＊＊＊＊＊＊＊setPriority device = " + device + ",priority = " + priority);
        return bluetoothHfDevice.setPriority(device, priority);
    }

    public int getPriority(BluetoothDevice device) {
        BtLogger.w(TAG, "getPriority－bluetoothHfDevice＝"+bluetoothHfDevice);
        int priority = 0;
        if(bluetoothHfDevice != null){
            priority = bluetoothHfDevice.getPriority(device);
        }
        BtLogger.d(TAG,"getPriority device = "+device+",priority = "+priority);
        return priority;
    }

    public int getConnectionState(BluetoothDevice device) {
        int state = 0;
        if(bluetoothHfDevice != null){
            state = bluetoothHfDevice.getConnectionState(device);
        }
        return state;
    }

    public boolean removeBond(Class btClass, BluetoothDevice btDevice)
            throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    public boolean createBond(Class btClass, BluetoothDevice btDevice)
            throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    public boolean connectAudio() {
        return bluetoothHfDevice.connectAudio();
    }

    public boolean disconnectAudio() {
        boolean disconnect = false;
        if (bluetoothHfDevice != null) {
            disconnect = bluetoothHfDevice.disconnectAudio();
        }
        return disconnect;
    }

	/*
     * public int getAudioState(){ return bluetoothHfDevice.getAudioState() }
	 */

    /**
     * 获取蓝牙的Mac地址
     *
     * @return btAddress
     */
    public String getBTAddress() {
        return mBluetoothAdapter.getAddress();
    }

    public int getConnectionState() {
        return mBluetoothAdapter.getConnectionState();
    }

    /**
     * 开始搜索设备
     *
     * @return true on success or false on error.
     */
    public boolean startDiscovery() {
        return mBluetoothAdapter.startDiscovery();
    }

    /**
     * 取消搜索
     *
     * @return true on success or false on error.
     */
    public boolean cancelDiscovery() {
        return mBluetoothAdapter.cancelDiscovery();
    }

    /**
     * 判断是否在搜索
     *
     * @return true if is discovering or false if not.
     */
    public boolean isDiscovering() {
        return mBluetoothAdapter.isDiscovering();
    }

    public boolean dial(String num) {
        BluetoothCallStateInfo bcStateInfo = getCallStateInfo(DataStatic.mCurrentBT);
        int callSetupState = bcStateInfo.getCallSetupState();
        int numActive = bcStateInfo.getNumActiveCall();
        int numHeld = bcStateInfo.getNumHeldCall();
        BtLogger.e(TAG, "bt dialing.....callSetupState="+callSetupState);
        BtLogger.e(TAG, "bt dialing.....numActive="+numActive);
        BtLogger.e(TAG, "bt dialing.....numHeld="+numHeld);
        //只有处于挂断状态才能拨打电话
        if (!TextUtils.isEmpty(num) && callSetupState == 6 && numActive == 0 && numHeld == 0) {
            //车机拨打静音800ms,静音300ms不够
            muteForDepop2(800);
            return bluetoothHfDevice.dial(num);
        }
        return false;
    }

    public boolean redial() {
        if (!bluetoothHfDevice.redial()) {
            BtLogger.e(TAG, "redialing failed as device got disconnected..");
            return false;
        }
        return true;
    }

    public boolean hangup() {
        boolean hangup = false;
        BtLogger.e(TAG, "hangup－－－挂断");
        if (bluetoothHfDevice != null && DataStatic.mCurrentBT != null
                && getConnectionState(DataStatic.mCurrentBT) == BluetoothHfDevice.STATE_CONNECTED) {
            BtLogger.e(TAG, "hangup－－－bluetoothHfDevice＝"+bluetoothHfDevice);
            hangup = bluetoothHfDevice.hangup();
        }
        //如果处于通话状态时挂断就恢复BtPhoneStatus
        if(android.os.ProtocolManager.getInstance().getBTPhoneStatus() == 1){
            //车机挂断静音300ms
            muteForDepop2(800);
            setBtPhoneStatus(0);
        }
        return hangup;
    }

    public boolean answer() {
        //车机接听静音300ms
        muteForDepop2(800);
        return bluetoothHfDevice.answer();
    }

    public boolean hold(int holdType) {
        return bluetoothHfDevice.hold(holdType);
    }

    public boolean sendDTMFcode(char dtmfCode) {
        return bluetoothHfDevice.sendDTMFcode(dtmfCode);
    }

    public boolean isNotInit() {
        return bluetoothHfDevice == null;
    }

    class HfDeviceEventHandler implements IBluetoothHfDeviceEventHandler {

        @Override
        public void onConnectionStateChange(int errCode, BluetoothDevice remoteDevice,
                                            int newState, int prevState, int disconnectReason) {

            BtLogger.d(TAG, "lianjie HfDeviceEventHandler errCode" + errCode + "newState" + newState + "prevState" + prevState + "disconnectReason" + disconnectReason);

            if (newState == 0) {
                //蓝牙异常断开
            } else if (newState == 2) {
                //蓝牙连接
            }

        }

        @Override
        public void onAudioStateChange(int newState, int prevState) {
            //凯翼项目在这里有加，具体作用未知
            RingStop();
        }

        @Override
        public void onIndicatorsUpdate(int[] indValue) {

        }

        @Override
        public void onCallStateChange(int status, int callSetupState, int numActive, int numHeld, String number, int addrType) {
            BtLogger.e(TAG, "通话状态变化=onCallStateChange() status = " + status);
            callStateResponse(status, callSetupState, numActive, numHeld, number, addrType);
        }

        @Override
        public void onVRStateChange(int status, int vrState) {

        }

        @Override
        public void onVolumeChange(int volType, int volume) {

        }

        @Override
        public synchronized void onPhoneBookReadRsp(int status,
                                                    List<BluetoothPhoneBookInfo> phoneNum) {
            BtLogger.e(TAG, "onPhoneBookReadRsp = " + status + ",onPhoneBookReadRsp = " + phoneNum);
            if (BluetoothHfDevice.NO_ERROR == status) {
                String num = "";
                try {
                    num = phoneNum.get(0).getContactNumber();
                } catch (IndexOutOfBoundsException exception) {
                }
                BtLogger.d(TAG, "onPhoneBookReadRsp()" + "status:" + status + ",num = " + num);
            }
        }

        @Override
        public void onSubscriberInfoRsp(int status, String number, int addrType) {
            BtLogger.d(TAG, "onSubscriberInfoRsp()" + number);
        }

        @Override
        public void onOperatorSelectionRsp(int status, int mode,
                                           String operatorName) {

        }

        @Override
        public void onExtendedErrorResult(int errorResultCode) {

        }

        @Override
        public void onCLCCRsp(int status, List<BluetoothClccInfo> clcc) {
            mOnCLCC = false;
            onCLCCRspMethod(status, clcc);
        }

        @Override
        public void onVendorAtRsp(int status, String atRsp) {

        }

        @Override
        public void onRingEvent() {
            BtLogger.d(TAG, "onRingEvent-->>zzy mInBandRingStatus =" + mInBandRingStatus + ";mRingState = " + mRingState); /* Telechips' Remark */
            if (mInBandRingStatus == BluetoothHfDevice.INBAND_STATE_OFF && mRingState == RING_STATE_IDLE) {
                RingPlay();
            } else if (mInBandRingStatus == BluetoothHfDevice.INBAND_STATE_ON) {
                RingStop();
            }

        }

        @Override
        public void onInBandRingStatusEvent(int inBandRingStatus) {
            BtLogger.e(TAG, "onInBandRingStatusEvent status = " + inBandRingStatus);
            mInBandRingStatus = inBandRingStatus;
        }

        @Override
        public void onBIAStatus(int status) {

        }

        @Override
        public void onNRECEvent(int nrecState) {
            // isEnableNREC = bluetoothHFDevice.getDeviceNRECState(bluetoothDevice);
            BtLogger.d(TAG,"onNRECEvent nrecState : "+nrecState);
          /*  Message msg = Message.obtain();
            msg.what = GUI_UPDATE_NREC_STATUS;
            msg.arg1 = isEnableNREC == true? 1:0;
            viewUpdateHandler.sendMessage(msg);
            */
        }
    }

    public void insertPbook(List<BluetoothPhoneBookInfo> infos) {

    }

    private class MyHandlerThread extends HandlerThread implements
            Handler.Callback {

        public MyHandlerThread(String name) {
            super(name);
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case THREAD_HANDLER_MSG:
                    break;
            }
            return true;
        }
    }

    public boolean connectA2dp(BluetoothDevice device) {
        return mA2dpProfile.connect(device);
    }

    public boolean disconnectA2dp(BluetoothDevice device) {
        return mA2dpProfile.disconnect(device);
    }

    public int getConnectionA2dpState(BluetoothDevice device) {
        return mA2dpProfile.getConnectionA2dpState(device);
    }

    public boolean isA2dpPlaying(BluetoothDevice device) {
        return mA2dpProfile.isA2dpPlaying(device);
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
        mA2dpProfile.setPreferred(device, preferred);
    }

    /**
     * 通知服务
     * @param method
     */
    public void notifyBTService(int method){
        EventBTService eventBTService = new EventBTService();
        eventBTService.setMethod(method);
        Message msg = new Message();
        msg.what = 0;
        msg.obj = eventBTService;
        mHandler.sendMessage(msg);
//        EventBus.getDefault().post(eventBTService);
    }

    public void notifyBTService(int method, List<BluetoothClccInfo> clcc){
        EventBTService eventBTService = new EventBTService();
        eventBTService.setMethod(method);
        eventBTService.setClcc(clcc);
        Message msg = new Message();
        msg.what = 0;
        msg.obj = eventBTService;
        mHandler.sendMessage(msg);
//        EventBus.getDefault().post(eventBTService);
    }

    public void notifyBTService(int method, int callType, String number, String callState){
        EventBTService eventBTService = new EventBTService();
        eventBTService.setMethod(method);
        eventBTService.setCallType(callType);
        eventBTService.setCallState(callState);
        eventBTService.setNumber(number);
        Message msg = new Message();
        msg.what = 0;
        msg.obj = eventBTService;
        mHandler.sendMessage(msg);
//        EventBus.getDefault().post(eventBTService);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    EventBus.getDefault().post((EventBTService) msg.obj);
                    break;
            }
        }
    };

    private void onCLCCRspMethod(int status, List<BluetoothClccInfo> clcc) {
        BtLogger.e(TAG, "onCLCCRspMethod = " + status + ",isPendingClcc = " + clcc);
        if (BluetoothHfDevice.NO_ERROR == status) {
            String number = "";
            try {
                if(clcc.size()>1){
                    //第三方来电，三方通话中都会进入这里
                    notifyBTService(3, clcc);
                } else if(clcc.size()>0){
                    number = clcc.get(0).getCallNumber();
                    notifyBTService(2, clcc);
                }
            } catch (IndexOutOfBoundsException exception) {
            }
            BtLogger.d(TAG, "onCLCCRsp()" + "status:" + status + ",num = " + number);
        }
    }

    public boolean getCLCC() {
        //直接显示通话弹框
        notifyBTService(4);
        mOnCLCC = true;
        BtLogger.e(TAG, "getCLCC");
        loopGetCLCC();
        return true;
    }

    private void loopGetCLCC(){
        mCLCCHandler.removeMessages(0);
        bluetoothHfDevice.getCLCC();
        mCLCCHandler.sendEmptyMessageDelayed(0, 1000);
    }

    private int mClccRetryTimes = 0;
    private Handler mCLCCHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    BtLogger.e(TAG, "mCLCCHandler-mOnCLCC="+mOnCLCC);
                    if(mOnCLCC && mClccRetryTimes < 3){
                        mClccRetryTimes++;
                        loopGetCLCC();
                    }else{
                        mOnCLCC = false;
                        mClccRetryTimes = 0;
                    }
                    break;
            }
        }
    };

    private boolean getBTAutoAnswerStatus(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("BTSettingStatus", 0);
        return sharedPreferences.getBoolean("BTAutoAnswerStatus", false);
    }

    private BluetoothCallStateInfo mBCStateInfo;

    public void setmBCStateInfo(BluetoothCallStateInfo mBCStateInfo) {
        this.mBCStateInfo = mBCStateInfo;
    }

    public void callStateResponse(int status, int callSetupState, int numActive, int numHeld, String number, int addrType){
        if (BluetoothHfDevice.NO_ERROR == status) {
            BtLogger.d(TAG, "callSetupState" + callSetupState);
            BtLogger.d(TAG, "numActive" + numActive);
            BtLogger.d(TAG, "numHeld" + numHeld);
            BtLogger.d(TAG, "number" + number);
            BtLogger.d(TAG, "addrType" + addrType);
            //没连接成功直接不进入
            if(getConnectionState(DataStatic.mCurrentBT) != BluetoothHfDevice.STATE_CONNECTED){
                return;
            }
            //有之前状态信息,并且状态无变化
            if(mBCStateInfo != null
                    && mBCStateInfo.getCallSetupState() == callSetupState
                    && mBCStateInfo.getNumActiveCall() == numActive
                    && mBCStateInfo.getNumHeldCall() == numHeld){
                //只有第三方切换时会反复处理
                if (callSetupState == 6 && numActive == 1 && numHeld == 1) { // 接通
                    BtLogger.e(TAG, "第三方接通");
                    //三方通话需要用到clcc判断当前那个电话
                    getCLCC();
                }
                return;
            }
            mBCStateInfo = getCallStateInfo(DataStatic.mCurrentBT);
//            mBCStateInfo = new BluetoothCallStateInfo(numActive, callSetupState, numHeld);
            BtLogger.e(TAG, "电话人名解析正常");
            if ((callSetupState == 2 || callSetupState == 3) && numActive == 0) { // 去电
                BtLogger.e(TAG, "去电");
                //去电获取号码
                getCLCC();
                //拨号前关闭讯飞
                setBtPhoneStatus(1);
                //打电话前静音500毫秒(必须放在setBtPhoneStatus之后)
                muteForDepop(1000);
                //弹出去电弹框
//                notifyBTService(4, CallLog.Calls.OUTGOING_TYPE, null, "正在拨通...");
            } else if (callSetupState == 6 && numActive == 0 && numHeld == 0) { // 挂断
                BtLogger.e(TAG, "挂断");
                RingStop();
                notifyBTService(15);
                if(android.os.ProtocolManager.getInstance().getBTPhoneStatus() == 1){
                    //挂电话后静音1秒(必须放在setBtPhoneStatus之前)
                    muteForDepop(1200);
                }
                //以前是讯飞，现在是按键，不还原，按键会乱掉
                setBtPhoneStatus(0);
            } else if (callSetupState == 6 && numActive == 0 && numHeld == 1) { // 保持通话
                BtLogger.e(TAG, "保持");
                getCLCC();
            } else if (callSetupState == 6 && numActive == 1 && numHeld == 1) { // 接通
                BtLogger.e(TAG, "第三方接通");
                //三方通话需要用到clcc判断当前那个电话
                getCLCC();
            } else if (callSetupState == 6 && numActive == 1 && numHeld == 0) { // 接通
                //单通话时再次打开麦克风等通话通道
                setBtPhoneStatus(1);
                //没有号码就获取
                getCLCC();
                BtLogger.e(TAG, "单通话接通");
                //改变电话弹框界面状态
//                notifyBTService(12, CallLog.Calls.OUTGOING_TYPE, null, "00:00");
                RingStop();
            } else if (callSetupState == 4 && numActive == 0) { // 来电
                BtLogger.e(TAG, "来电");
                //拨号前关闭讯飞
                setBtPhoneStatus(1);
                //打电话前静音500毫秒(必须放在setBtPhoneStatus之后)
                muteForDepop(1000);
                //弹出来电弹框
//                notifyBTService(4, CallLog.Calls.MISSED_TYPE, number, "来电");
                getCLCC();
                //来电自动接听
                if(getBTAutoAnswerStatus()){
                    answer();
                }
            }
            // 多方来电
            if (callSetupState == 5 && numActive == 1) {   //第三方来电
                BtLogger.e(TAG, "第三方来电－"+number);
                getCLCC();
                //弹出插播来电弹框
//                notifyBTService(2, CallLog.Calls.MISSED_TYPE, number, "插播来电...");
            } else if (callSetupState == 5 && numActive == 0 && numHeld == 0) {
                //iphone手机已接通对方挂断，这时第三方未接通；android手机第三方接通也是走这里numHeld = 1
                //第三方来电时挂断原通话走这里
                BtLogger.e(TAG, "挂断三方通话中的原通话－"+number);
//                notifyBTService(14);
                getCLCC();
            } else if (callSetupState == 5 && numActive == 0 && numHeld == 1) {
                //接通第三方来电状态
            }
        }
    }

    /**
     * 判断某个界面是否在前台
     *
     * @param context
     * @param className
     *            某个界面名称
     */
    public boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
//            if (className.equals(cpn.getClassName())) {
//                return true;
//            }
            String currentActivity = cpn.getClassName();
            BtLogger.e(TAG, "mIsNeedSmallDialog-currentActivity=" + currentActivity);
            if(currentActivity != null && currentActivity.contains(className)){
                return true;
            }
        }
        return false;
    }

    public void notifyDial(int method){
        BtLogger.d(TAG, "notifyDial");
        EventDial eventDial = new EventDial();
        eventDial.setMethod(method);
        EventBus.getDefault().post(eventDial);
    }
}
