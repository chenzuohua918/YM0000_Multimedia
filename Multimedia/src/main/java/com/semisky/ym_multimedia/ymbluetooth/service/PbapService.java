package com.semisky.ym_multimedia.ymbluetooth.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;
import android.view.View;

import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.broadcom.bt.pbap.BluetoothAttributeMask;
import com.broadcom.bt.pbap.BluetoothPbapClient;
import com.broadcom.bt.pbap.IBluetoothPbapClientEventHandler;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventBTService;
import com.semisky.ym_multimedia.ymbluetooth.avr.AvrcpCommandCallback;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.data.CallLogRecords;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;
import com.semisky.ym_multimedia.ymbluetooth.data.MsgLoading;
import com.semisky.ym_multimedia.ymbluetooth.dialog.BTCallDialog;
import com.semisky.ym_multimedia.ymbluetooth.func.CallLogUtils;
import com.semisky.ym_multimedia.ymbluetooth.func.ContactsUtils;
import com.semisky.ym_multimedia.ymbluetooth.func.ControlManager;
import com.semisky.ym_multimedia.ymbluetooth.func.FuncBTOperate;
import com.ypy.eventbus.EventBus;

/**
 * Created by luoyin on 16/10/13.
 */
public class PbapService extends Service implements IBluetoothPbapClientEventHandler {
    private static final String TAG = "PbapService";
    private static ControlManager mControlManager;
    private BluetoothPbapClient mPbapClient;
//    private static final String PATH_SDCARD = "/storage/emulated/0";
//    private static final String OUT_FILE_PULL_PB = PATH_SDCARD + "/pb.vcf";
//    private static final String OUT_FILE_PULL_OCH = PATH_SDCARD + "/och.vcf";
//    private static final String OUT_FILE_PULL_ICH = PATH_SDCARD + "/ich.vcf";
//    private static final String OUT_FILE_PULL_MCH = PATH_SDCARD + "/mch.vcf";
//    private static final String OUT_FILE_PULL_CCH = PATH_SDCARD + "/cch.vcf";
    private static final String ATTR_MASK_NO_PHOTO = "4294967287";
    private Context mContext;
    private BTCallDialog mBTCallDialog;

    private FuncBTOperate mFuncBTOperate;

    private Ringtone mRingtone = null;
    /* Ringtone control. */
    private static final int RING_STATE_IDLE = 0;
    private static final int RING_STATE_RINGING = 1;
    private int mRingState = RING_STATE_IDLE;
    private long mConnectTime;
    private long mCloseTime;
    private boolean mSyncTimeout = false;


    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEvent(EventBTService eventBTService) {
        BtLogger.e(TAG, "onEvent---service-"+eventBTService.getMethod());
        switch (eventBTService.getMethod()){
            case 0:
                //同步联系人or通话记录
                startGetRecordsPhoneBook();
                break;
            case 1:
                //自动连接蓝牙设备
//                mFuncBTOperate.pollConnectBTDevice();
                //高亮连接中蓝牙设备
                highlightCurrentBT();
                break;
            case 2:
                //来去电时间点
                mConnectTime = System.currentTimeMillis();
                //单通话状态
                showSingleCallDialog(eventBTService);
                break;
            case 3:
                //显示切换三方通话对话框
                showSwitchCallDialog(eventBTService);
                break;
            case 4:
                //直接显示通话弹框
                showCallDialog();
                break;
            case 5:
                //撤销单通弹框
                dismissBTCallDialog();
                break;
            case 6:
                initBTMusic();
                break;
            case 7:
                initBTMusic();
                playBTMusic(DataStatic.mCurrentBT);
                break;
            case 8:
                initBTMusic();
                pauseBTMusic(DataStatic.mCurrentBT);
                break;
            case 9:
                initBTMusic();
                nextBTMusic(DataStatic.mCurrentBT);
                break;
            case 10:
                initBTMusic();
                lastBTMusic(DataStatic.mCurrentBT);
                break;
            case 11:
                //挂断时间点
                mCloseTime = System.currentTimeMillis();
                BtLogger.e(TAG, "mCloseTime---mConnectTime-==="+(mCloseTime - mConnectTime));
                //接通
                if(mCloseTime - mConnectTime < 1000){
                    mHangupHandler.sendEmptyMessageDelayed(0, 1000);
                } else {
                    //执行挂断操作
                    hangupCall();
                }
                break;
            case 12:
                break;
            case 13:
                break;
            case 14:
                break;
            case 15:
                //收到挂断广播才关闭对话框
                dismissBTCallDialog();
                //挂断电话再同步保存到通话记录中
                //保存第一个电话数据
                syncFirstBTCallLog();
                //保存第三方来电数据
                syncSecondBTCallLog();
                break;
            case 16:
                //私密模式切换UI响应
                changePrivateMode(eventBTService.isPrivateMode());
                break;
            case 17:
                //同步完成0.3秒后断开pbap连接
                mDisHandler.sendEmptyMessageDelayed(0, 300);
//                disconnect();
                break;
            case 18:
                //同步电话号码数据，用于保存通话记录
                //初始化单通电话数据
                if(mFirstCallLogRecords == null) {
                    mFirstCallLogRecords = syncCallLogInfo(eventBTService);
                }
                //来电状态有变化需要随时同步
                if(eventBTService.getCallType() != 0) {
                    BtLogger.e(TAG, "mFirstCallLogRecords=" + eventBTService.getCallType());
                    mFirstCallLogRecords.setType(eventBTService.getCallType());
                }
                break;
            case 19:
                //同步电话号码数据，用于保存通话记录
                //初始化第三方来电数据
                if(mSecondCallLogRecords == null) {
                    mSecondCallLogRecords = syncCallLogInfo(eventBTService);
                }
                //来电状态有变化需要随时同步
                if(eventBTService.getCallType() != 0) {
                    BtLogger.e(TAG, "mSecondCallLogRecords=" + eventBTService.getCallType());
                    mSecondCallLogRecords.setType(eventBTService.getCallType());
                }
                break;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = this.getApplicationContext();
        mControlManager = ControlManager.getInstance(mContext, null);
        mFuncBTOperate = FuncBTOperate.getInstance(mContext);
        initCallDialog(this);
        BtLogger.d(TAG, "onStartCommand");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                startGetRecordsPhoneBook();
//            }
//        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    private Handler mHangupHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hangupCall();
        }
    };

//    private Handler mUIHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case 0:
//                    mFuncBTOperate.dismissBTProgressDialog();
//                    break;
//                case 1:
//                    //同步联系人通话记录时弹出加载框，阻止操作（1350个联系人读取耗时约210秒）
//                    mFuncBTOperate.showBTProgressDialog("同步通讯录，请等待!", "同步通讯录失败！", 250000);
//                    break;
//            }
//        }
//    };

    private void highlightCurrentBT(){
        if(DataStatic.mCurrentBT != null) {
            int state = mControlManager.getConnectionState(DataStatic.mCurrentBT);
            int bondState = DataStatic.mCurrentBT.getBondState();
            if (state == BluetoothHfDevice.STATE_CONNECTED) {
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    //设备同步到连接列表，避免自动连接成功却不在列表中显示的bug
                    mFuncBTOperate.notifyPair(DataStatic.mCurrentBT, 1);
                    //刷新配对界面设备列表状态
                    mFuncBTOperate.notifyPair(null, 2);
                }
            }
        }
    }

    private void initCallDialog(Context context){
        mBTCallDialog = BTCallDialog.getInstance(context);
        mBTCallDialog.setOnRejectListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFuncBTOperate.notifyBTService(11);
            }
        });
        mBTCallDialog.setOnHangupListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFuncBTOperate.notifyBTService(11);
            }
        });
        mBTCallDialog.setOnPickupListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mControlManager.answer();
            }
        });

        //插播电话相关注册
        mBTCallDialog.setOnThreeRejectListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_second_reject:
                    case R.id.btn_kidin_reject:
                        // 挂断当前来电/挂断held通话
                        mControlManager.hold(BluetoothHfDevice.HANGUP_HELD);
                        break;
                    case R.id.btn_switch_reject1:
                        //挂断当前通话，切换held通话
                    case R.id.btn_switch_reject2:
                    case R.id.btn_kidout_hangup:
                        //挂断当前通话，切换held通话
                        mControlManager.hold(BluetoothHfDevice.HANGUP_ACTIVE_ACCEPT_HELD);
                        break;
                }
                // 挂断之前通话，接听当前来电
                // mControlManager.hold(BluetoothHfDevice.HANGUP_ACTIVE_ACCEPT_HELD);
            }
        });
        mBTCallDialog.setOnThreePickupListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 保留之前通话，接听当前来电
                mControlManager.hold(BluetoothHfDevice.SWAP_CALLS);
            }
        });

        mBTCallDialog.setOnPrivateModeChangeListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BtLogger.e(TAG, "setOnPrivateModeChangeListener=" + view.getTag().toString());
                if ("false".equals(view.getTag().toString())) {
                    //切换私密模式
                    mControlManager.disconnectAudio();
                } else {
                    //还原私密模式
                    mControlManager.connectAudio();
                }
            }
        });

        mBTCallDialog.setOnMuteSwitchListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BtLogger.e(TAG, "setOnMuteSwitchListener=" + view.getTag().toString());
                if ("false".equals(view.getTag().toString())) {
                    //关闭麦克风
                    android.os.ProtocolManager.getInstance().setMicState(0);// state 1: open  ;  0: close;
                    view.setTag(true);
                } else {
                    //打开麦克风
                    android.os.ProtocolManager.getInstance().setMicState(1);// state 1: open  ;  0: close;
                    view.setTag(false);
                }
            }
        });

        mBTCallDialog.setOnSpeakerSwitchListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BtLogger.e(TAG, "setOnSpeakerSwitchListener=" + view.getTag().toString());
                if ("false".equals(view.getTag().toString())) {
                    //关闭铃声
                    mFuncBTOperate.setStreamVolumePub(0);
                    view.setTag(true);
                } else {
                    //打开铃声
                    mFuncBTOperate.setStreamVolumePub(1);
                    view.setTag(false);
                }
            }
        });
    }

    private void dismissBTCallDialog(){
        if(mBTCallDialog != null){
            mBTCallDialog.dismiss();
            mBTCallDialog.stopTime1();
            mBTCallDialog.stopTime2();
        }
    }

    private void changePrivateMode(boolean isPrivate){
        if(mBTCallDialog != null) {
            mBTCallDialog.changePrivateModeState(isPrivate);
        }
    }

    private void showCallDialog(){
        if(mBTCallDialog != null){
            mBTCallDialog.show();
        }
    }

    private void showSingleCallDialog(EventBTService eventBTService){
        if(mBTCallDialog != null){
            mBTCallDialog.switchSingleCall(eventBTService);
        }
    }

    private void showSwitchCallDialog(EventBTService eventBTService){
        if(mBTCallDialog != null){
            mBTCallDialog.switch3Calls(eventBTService);
        }
    }

    private void hangupCall(){
//        mControlManager.setBtPhoneStatus(0);
        BtLogger.e(TAG, "hangupCall");
        mControlManager.hangup();
    }

    private CallLogRecords mFirstCallLogRecords = null;
    private CallLogRecords mSecondCallLogRecords = null;
    private CallLogRecords syncCallLogInfo(EventBTService eventBTService) {
        BtLogger.e(TAG, "getCallLogInfos－" + eventBTService.getNumber());
        CallLogRecords callLogRecords = new CallLogRecords();
        callLogRecords.setDateTime(System.currentTimeMillis());
        //如有保存null号码等bug，这条语句可以移到同步通话状态那个位置去，暂时不用
        if (eventBTService.getNumber() != null) {
            callLogRecords.setNumber(eventBTService.getNumber());
        }
        return callLogRecords;
    }

    /**
     * 同步拨打电话数据到通话记录
     */
    private void syncSecondBTCallLog(){
        if(mSecondCallLogRecords != null){
            BtLogger.e(TAG, "syncSecondBTCallLog－" + mSecondCallLogRecords.getNumber());
            //保存到通话记录数据库
            mFuncBTOperate.insertCallLogRecords(mSecondCallLogRecords);
            //同步到通话记录界面数据
            mFuncBTOperate.notifyCallLogList(0);
            mSecondCallLogRecords = null;
        }
    }

    /**
     * 同步拨打电话数据到通话记录
     */
    private void syncFirstBTCallLog(){
        if(mFirstCallLogRecords != null){
            //保存到通话记录数据库
            mFuncBTOperate.insertCallLogRecords(mFirstCallLogRecords);
            //同步到通话记录界面数据
            mFuncBTOperate.notifyCallLogList(0);
            mFirstCallLogRecords = null;
        }
    }

    /**
     * 获取通话记录
     */
    public void startGetRecordsPhoneBook() {
        //isRecordsPBTableNull = mControlManager.isRecordsPBTableNull();
        BtLogger.e(TAG, "startGetRecordsPhoneBook-DataStatic.mCurrentBT="+DataStatic.mCurrentBT);
        if (DataStatic.mCurrentBT != null && DataStatic.mCurrentBT.getAddress() != null) {
            BtLogger.e(TAG, "开始获取通话记录");
            String btAddress = DataStatic.mCurrentBT.getAddress();
            if(connect(btAddress)){
                //同步联系人进度
                mFuncBTOperate.showmBTProgressDialog(mContext, R.string.ym_bt_downloading_pb, "0%");
            }
        }
    }

    private boolean connect(String bdaddr) {
        BtLogger.e(TAG, "传入蓝牙地址bdaddr: " + bdaddr);
        if (bdaddr != null) {
            if (mPbapClient != null) {
                BluetoothDevice currpbapServer = mPbapClient.getPbapServer();
                BtLogger.e(TAG, "某个蓝牙地址: currpbapServer: " + currpbapServer.getAddress());
                if (!bdaddr.equalsIgnoreCase(currpbapServer.getAddress())) {
                    BtLogger.e(TAG, "Pbap地址不同");
                    mPbapClient.finish();
                    mPbapClient = null;
                }
            }
            if (mPbapClient == null){
                bdaddr = bdaddr.toUpperCase();
                BluetoothDevice pbapServer;
                if(bdaddr == null){
                    return false;
                }else{
                    pbapServer = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(bdaddr);
                }
                if (pbapServer == null) {
                    return false;
                }else{
                    mPbapClient = new BluetoothPbapClient(this, pbapServer, this);
                }
                if (mPbapClient == null) {
                    return false;
                }
            }
            BtLogger.e(TAG, "mPbapClient 连接 ＝"+mPbapClient);
            mPbapClient.connect();
            //启动联系人授权超时
            syncContactsTimeout();
            return true;
        } else {
            BtLogger.e(TAG, "onConnect(): Invalid bdaddr: " + bdaddr);
            return false;
        }
    }

    private void noContactsPermissions(){
        //清空所有数据
        mFuncBTOperate.deleteAllCallLogRecords();
        mFuncBTOperate.deleteContacts();
        mFuncBTOperate.notifyCallLogList(0);
        mFuncBTOperate.notifyContactsList(0);
        //同步联系人完成时启用菜单
        mFuncBTOperate.notifyMain(0, true);
        BtLogger.e(TAG, "还原高亮菜单位置－－－5");
        //点击禁用还原
        mFuncBTOperate.notifyContactsList(3);
        //去掉加载弹框
        mFuncBTOperate.dismissmBTProgressDialog();
    }

    private void syncContactsTimeout(){
        mSyncTimeout = false;
        mSyncContactsHandler.removeMessages(0);
        mSyncContactsHandler.sendEmptyMessageDelayed(0, 12000);
    }

    private Handler mSyncContactsHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    BtLogger.e(TAG, "权限无-超时");
                    noContactsPermissions();
                    mSyncTimeout = true;
                    break;
            }
        }
    };

    private Handler mDisHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            disconnect();
        }
    };

    private boolean disconnect() {
        BtLogger.e(TAG, "Pbap disconnect");
        // Check if the client is already created
        if (mPbapClient != null) {
            mPbapClient.disconnect();
            return true;
        } else {
            return false;
        }
    }

    private void pullPhonebook(String bdaddr, String path, String maxListCountStr,
                               String listStartOffsetStr, String vcardVersionStr, String attrMaskStr,
                               String outFilepath) {
        if (mPbapClient != null) {
            int maxListCount = BluetoothPbapClient.MAX_LIST_COUNT_NOT_SET;
            try {
                maxListCount = Integer.parseInt(maxListCountStr);
            } catch (Throwable t) {
                BtLogger.e(TAG, "Error parsing maxListCount", t);
            }

            int listStartOffset = BluetoothPbapClient.MAX_LIST_COUNT_NOT_SET;
            try {
                listStartOffset = Integer.parseInt(listStartOffsetStr);
            } catch (Throwable t) {
                BtLogger.e(TAG, "Error parsing listStartOffset", t);
            }

            byte vcardVersion = BluetoothPbapClient.DEFAULT_VCARD_VERSION;
            try {
                vcardVersion = Byte.parseByte(vcardVersionStr);
            } catch (Throwable t) {
                BtLogger.e(TAG, "Error parsing vcardVersion", t);
            }

            BluetoothAttributeMask mask = null;

            if (attrMaskStr != null && !attrMaskStr.equals("-1")) {
                try {
                    long attrMask = Long.parseLong(attrMaskStr);
                    mask = new BluetoothAttributeMask();
                    mask.parse(attrMask);
                } catch (Throwable t) {
                    BtLogger.e(TAG, "Error parsing attrMask", t);
                }
            }

            if (outFilepath != null && mask != null) {
                BtLogger.d(TAG, "pullPhonebook(): path=" + (path == null ? "(null)" : path) + ", vcardVersion="
                        + vcardVersion + ", maxlistCount=" + maxListCount + ", listStartOffset="
                        + listStartOffset + ", outFilepath=" + outFilepath);
                mPbapClient.pullPhonebook(path, mask, vcardVersion, maxListCount, listStartOffset, outFilepath);
            }
        }
    }

//    private BluetoothPbapClient getClient(String bdaddr) {
//        if (bdaddr == null) {
//            BtLogger.d(TAG, "getClient(): Invalid bdaddr: " + bdaddr);
//            return null;
//        }
//        bdaddr = bdaddr.toUpperCase();
//        @SuppressWarnings("unused")
//        BluetoothDevice pbapServer = null;
//        try {
//            pbapServer = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(bdaddr);
//        } catch (Throwable t) {
//            BtLogger.e(TAG, "getClient(): Unable to create BluetoothDevice with address: " + bdaddr);
//            return null;
//        }
//
//        if (mPbapClient == null)
//            BtLogger.e(TAG, "getClient(): PBAP server not connected: " + bdaddr);
//
//        return mPbapClient;
//    }

    @Override
    public void onConnected(BluetoothDevice bluetoothDevice, boolean b) {
        BtLogger.e(TAG, "onConnected: " + b);
        //如果操作了授权就取消超时
        mSyncContactsHandler.removeMessages(0);
        //联系人同步授权超时,7秒
        if(mSyncTimeout){
            return;
        }
        if (b == true) {                   //手机端打开了‘同步通讯录’，iphone比较特殊，不管关闭还是打开success都是true
            //判断有无拉取vcf文件
            if(!mFuncBTOperate.fileIsExists(mFuncBTOperate.OUT_FILE_PULL_PB) || !mFuncBTOperate.fileIsExists(mFuncBTOperate.OUT_FILE_PULL_CCH)){
                //开启同步线程并拉取电话数据
//                syncDataThreadAndPullPB();
                if(!mFuncBTOperate.fileIsExists(mFuncBTOperate.OUT_FILE_PULL_CCH)) {
                    //拉取通话记录
                    pullCallLogVCF();
                }
                if(!mFuncBTOperate.fileIsExists(mFuncBTOperate.OUT_FILE_PULL_PB)) {
                    //拉取联系人
                    pullContactsVCF();
                }
            }
        } else {
            //手机端不允许‘同步通讯录’
            BtLogger.e(TAG, "权限无-不允许");
            noContactsPermissions();
        }
    }

    @Override
    public void onDisconnected(BluetoothDevice bluetoothDevice, boolean b) {
        BtLogger.e(TAG, "onDisconnected=" + b);
    }

    @Override
    public void onPathSet(BluetoothDevice bluetoothDevice, boolean b, String s) {

    }

    @Override
    public void onPullPhonebookCompleted(BluetoothDevice bluetoothDevice, String s, boolean b, int i, int i1, String s1) {
        BtLogger.e(TAG, "拉取VCF完成－s=" + s + ",s1=" + s1);
        Message msg = new Message();
        msg.what = 0;
        msg.obj = s1;
        mHandler.sendMessage(msg);
    }

    private void syncContacts(String filePath){
        BtLogger.e(TAG, "syncContacts");
        ContactsUtils contactsUtils = new ContactsUtils();
        try {
            contactsUtils.addVCFtoContacts(mContext, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synCallLogs(String filePath){
        BtLogger.e(TAG, "synCallLogs");
        CallLogUtils callLogUtils = new CallLogUtils();
        try {
            callLogUtils.addVCFtoCallLog(mContext, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler() {//2、绑定handler到CustomThread实例的Looper对象
        public void handleMessage(Message msg) {//3、定义处理消息的方法
            BtLogger.d(TAG, "线程msg.obj =" + msg.obj);
            if (mFuncBTOperate.OUT_FILE_PULL_PB.equals(msg.obj)) {
                syncContacts((String) msg.obj);
            } else {
//                                try {
//                                    Thread.sleep(2000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
                synCallLogs((String) msg.obj);
            }
        }
    };

//    private Thread mThread;
//    public void syncDataThreadAndPullPB() {
//        BtLogger.e(TAG, "开启同步线程-mThread="+mThread);
//        if(mThread == null) {
//            mThread = new Thread() {
//                @Override
//                public void run() {
//                    BtLogger.d(TAG, "线程循环？？？");
//                    Looper.prepare();//1、初始化Looper
//                    mHandler = new Handler() {//2、绑定handler到CustomThread实例的Looper对象
//                        public void handleMessage(Message msg) {//3、定义处理消息的方法
//                            BtLogger.d(TAG, "线程msg.obj =" + msg.obj);
//                            if (mFuncBTOperate.OUT_FILE_PULL_PB.equals(msg.obj)) {
//                                syncContacts((String) msg.obj);
//                            } else {
////                                try {
////                                    Thread.sleep(2000);
////                                } catch (InterruptedException e) {
////                                    e.printStackTrace();
////                                }
//                                synCallLogs((String) msg.obj);
//                            }
//                        }
//                    };
//                    Looper.loop();//4、启动消息循环
//                }
//            };
//            mThread.start();
//        }
//    }

    private void pullContactsVCF(){
        if (DataStatic.mCurrentBT != null && DataStatic.mCurrentBT.getAddress() != null) {
            String btAddress = DataStatic.mCurrentBT.getAddress();
            //拉取电话相关数据
            pullPhonebook(btAddress, BluetoothPbapClient.PB_PATH, "-1", "-1",
                    "0", ATTR_MASK_NO_PHOTO, mFuncBTOperate.OUT_FILE_PULL_PB);
        }
    }

    private void pullCallLogVCF(){
        if (DataStatic.mCurrentBT != null && DataStatic.mCurrentBT.getAddress() != null) {
            String btAddress = DataStatic.mCurrentBT.getAddress();

//        pullPhonebook(btAddress, BluetoothPbapClient.OCH_PATH, "-1", "-1",
//                "0", ATTR_MASK_NO_PHOTO, OUT_FILE_PULL_OCH);
//        pullPhonebook(btAddress, BluetoothPbapClient.ICH_PATH, "-1", "-1",
//                "0", ATTR_MASK_NO_PHOTO, OUT_FILE_PULL_ICH);
//        pullPhonebook(btAddress, BluetoothPbapClient.MCH_PATH, "-1", "-1",
//                "0", ATTR_MASK_NO_PHOTO, OUT_FILE_PULL_MCH);
            pullPhonebook(btAddress, BluetoothPbapClient.CCH_PATH, "-1", "-1",
                    "0", ATTR_MASK_NO_PHOTO, mFuncBTOperate.OUT_FILE_PULL_CCH);
        }
    }

    @Override
    public void onPullVcardListingCompleted(BluetoothDevice bluetoothDevice, String s, boolean b, int i, int i1, String s1) {
        BtLogger.d(TAG, "拉取onPullVcardListingCompleted－s=" + s + ",s1=" + s1);
    }

    @Override
    public void onPullVcardEntryEvent(BluetoothDevice bluetoothDevice, String s, boolean b, String s1) {
        BtLogger.d(TAG, "VVV--onPullVcardEntryEvent－s=" + s + ",s1=" + s1);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
            mRingtone.play();
            mRingState = RING_STATE_RINGING;
        }
    }

    /**
     * Stops ring audio.
     */
    public void RingStop() {
        BtLogger.d(TAG, "RingStop() mRingState = " + mRingState);

        if (mRingState == RING_STATE_RINGING) {
            if (mRingtone.isPlaying()) {
                mRingtone.stop();
            }
        }
        mRingState = RING_STATE_IDLE;
    }

    private void initBTMusic(){
        mFuncBTOperate.initBTMusic();
    }

    private void playBTMusic(BluetoothDevice bluetoothDevice){
        mFuncBTOperate.startPlayerBy(bluetoothDevice);
    }

    private void pauseBTMusic(BluetoothDevice bluetoothDevice){
        mFuncBTOperate.pausePlayerBy(bluetoothDevice);
    }

    private void nextBTMusic(BluetoothDevice bluetoothDevice){
        mFuncBTOperate.nextMusic(bluetoothDevice);
    }

    private void lastBTMusic(BluetoothDevice bluetoothDevice){
        mFuncBTOperate.lastMusic(bluetoothDevice);
    }

}
