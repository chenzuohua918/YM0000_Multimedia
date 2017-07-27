package com.semisky.ym_multimedia.ymbluetooth.receiver;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;

import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;

import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;
import com.semisky.ym_multimedia.ymbluetooth.data.MsgLoading;
import com.semisky.ym_multimedia.ymbluetooth.func.Constants;
import com.semisky.ym_multimedia.ymbluetooth.func.ControlManager;
import com.semisky.ym_multimedia.ymbluetooth.func.FuncBTOperate;

/**
 * Created by luoyin on 16/10/13.
 */
public class BTBCReceiver extends BroadcastReceiver {
    private final String TAG = "BTBCReceiver";
    private Context mContext;
    private ControlManager mControlManager;
    private FuncBTOperate mFuncBTOperate;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context.getApplicationContext();
        mControlManager = ControlManager.getInstance(mContext, null);
        mFuncBTOperate = FuncBTOperate.getInstance(mContext);
        String action = intent.getAction();
        BtLogger.d(TAG, "BTBCReceiver蓝牙广播action = " + action);
        //蓝牙通话切换不用重练，否则弹框的数据会闪现一下
        if (!action.equals(BluetoothHfDevice.ACTION_AUDIO_STATE_CHANGED) && !action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            //反复获取之前连接的设备
            //重新开机则将之前连接成功的蓝牙设备传给当前设备
            //首次进入时需要获取当前蓝牙(在蓝牙连接成功广播中会将DataStatic.mCurrentBT赋值，所以无需处理)
//            DataStatic.mCurrentBT = mFuncBTOperate.getConnectDevice();
        }
//        dealCallDialog(intent);
        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
            //如果蓝牙已经开启，SP为关闭，则关闭蓝牙
            if (mControlManager.isEnabled() && !mFuncBTOperate.getBTSwitchStatus()) {
                //关闭蓝牙
                mControlManager.disableBT();
            }
            //如果蓝牙未开启，SP为打开，ACC为on，则打开蓝牙
            if (!mControlManager.isEnabled() && mFuncBTOperate.getBTSwitchStatus() && !DataStatic.mAccOff) {
                //启动蓝牙
                mControlManager.enableBT();
            }
            //开机设置蓝牙永久可见
            mControlManager.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
            mControlManager.setBTName("karry_test");
        }
        //静音按钮操作引起蓝牙音量恢复错误，通过静音广播调整到正确值
        if(action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)){
            switch (intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1)){
                case AudioManager.RINGER_MODE_NORMAL:
                    mFuncBTOperate.restoreVolume();
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    break;
            }
        }
        if(action.equals("com.semisky.ACTION_ACC_STATUS")){
            BtLogger.e(TAG, "acc status: " + intent.getIntExtra("status", -1));
            switch (intent.getIntExtra("status", -1)){
                //acc off
                case 0:
                    DataStatic.mAccOff = true;
                    //断开蓝牙
                    mFuncBTOperate.closeBTMusic();
                    mFuncBTOperate.notifyPair(null, 3);
                    mControlManager.disableBT();
                    break;
                //acc on
                case 1:
                    DataStatic.mAccOff = false;
                    //重连
//                    connectBTDevice();
                    mControlManager.enableBT();
                    break;
            }
        }
        //###以下为蓝牙连接同步相关广播处理
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            //＊＊＊发现设备
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            BtLogger.e(TAG, "发现设备名: " + device.getName());
            BtLogger.d(TAG, "发现设备地址: " + device.getAddress());
            //设备名不能为null
            if(device.getName() != null && !"null".equals(device.getName())){
                //添加发现的设备
                mFuncBTOperate.notifyPair(device, 1);
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            //禁用蓝牙搜索
            mFuncBTOperate.notifyPair(null, 7);
            //＊＊＊扫描开始
            //弹出加载框
//            mFuncBTOperate.showBTProgressDialog(R.string.searching_bt_devices, R.string.searching_timeout, 120000, true);
            //设置蓝牙永久可见
//            mControlManager.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            //＊＊＊扫描结束
            mFuncBTOperate.notifyPair(null, 0);
            BtLogger.w(TAG, "广播去弹框");
            //去掉加载框
//            mFuncBTOperate.dismissBTSearchDialog();
            //高亮蓝牙搜索
            mFuncBTOperate.notifyPair(null, 5);
            //自动连接设备
            mFuncBTOperate.notifyBTService(1);
        } else if (BluetoothHfDevice.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
            //＊＊＊连接状态变化
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            int state = mControlManager.getConnectionState(device);
            /////////////////////
            BtLogger.e(TAG, "BTBCReceiver－连接－状态变化state = " + state);
            int bondState = device.getBondState();
            BtLogger.e(TAG, "BTBCReceiver－连接－bondState = " + bondState);

            //广播处理完成后要重置新连接设备相关参数(这个必须放在connectNewBTDevice之前，否则会立刻重连)
            mFuncBTOperate.resetNewBtStatus(device);
            switch (state) {
                case BluetoothHfDevice.STATE_CONNECTED:
//                    mControlManager.setBTName("罗隐--车机");
                    BtLogger.d(TAG, "BTBCReceiver连接状态－成功=" + device);
                    BtLogger.e(TAG, "＊＊＊＊＊设备切换－－:" + DataStatic.mCurrentBT + ">>>>>>>>" + device);
                    BtLogger.d(TAG, "BTBCReceiver连接状态－mBTConnectState=" + DataStatic.mBTConnectState);
                    if(DataStatic.mAccOff == true){
                        //如果acc off时响应了连接则直接断开蓝牙
                        //断开蓝牙
                        mFuncBTOperate.disconnectBT(device);
                        return;
                    }
                    mFuncBTOperate.a2dpConnecting();
                    //连接成功则不重复执行连接操作
                    if(DataStatic.mBTConnectState != BluetoothHfDevice.STATE_CONNECTED){
                        DataStatic.mBTConnectState = BluetoothHfDevice.STATE_CONNECTED;
                    }else{
                        return;
                    }
                    if(device != null){
                        BtLogger.e(TAG, "连接设备Priority=" + mControlManager.getPriority(device));
                        //设备同步到连接列表，避免自动连接成功却不在列表中显示的bug
                        mFuncBTOperate.notifyPair(device, 1);
                        if(DataStatic.mCurrentBT == null) {
                            BtLogger.e(TAG, "首次连接成功－－－1");
                            //连接成功，服务同步电话数据(以下这句需要用到DataStatic.mCurrentBT，必须先赋值当前设备)
                            DataStatic.mCurrentBT = device;
                            syncClearAndPullAllCallData();
                            //同步过后再将蓝牙优先级设置为自动(同步完成后再将当前设备优先级提升至1000)
                            mControlManager.setPriority(device, BluetoothProfile.PRIORITY_AUTO_CONNECT);
                        } else if(!DataStatic.mCurrentBT.getAddress().equals(device.getAddress())){
                            //切换设备才进此处
                            BtLogger.e(TAG, "未改变之前之前连接设备Priority=" + mControlManager.getPriority(DataStatic.mCurrentBT));
                            //连接成功，服务同步电话数据(以下这句需要用到DataStatic.mCurrentBT，必须先赋值当前设备)
                            DataStatic.mCurrentBT = device;
                            syncClearAndPullAllCallData();
                            //同步过后再将蓝牙优先级设置为自动(同、步完成后再将当前设备优先级提升至1000)
                            mControlManager.setPriority(device, BluetoothProfile.PRIORITY_AUTO_CONNECT);
                        } else {
                            BtLogger.e(TAG, "设备重连成功");
                            //直接连接成功,无需同步数据
                            mFuncBTOperate.notifyMain(0, true);
                        }
                        //保存当前连接设备地址
                        mFuncBTOperate.setSPBTAddress(device.getAddress());
                        //改变相应设备颜色
                        mFuncBTOperate.notifyPairAndMainS(device, true);
                        //蓝牙连接则初始化蓝牙音乐相关数据
                        mFuncBTOperate.notifyBTService(6);
                    }
                    //关闭加载弹框
                    mFuncBTOperate.dismissmBTProgressDialog();
                    //关闭配对弹框
                    mFuncBTOperate.dismissmBTPairDialog();
                    break;
                case BluetoothHfDevice.STATE_DISCONNECTED:
                    //断开蓝牙则恢复A2DP连接状态
                    mFuncBTOperate.setmA2dpDisconnecting(false);
                    mFuncBTOperate.setmA2dpConnecting(false);
                    //连接中刷新列表状态
                    mFuncBTOperate.notifyPair(null, 6);
                    //关闭加载弹框
                    mFuncBTOperate.dismissmBTProgressDialog();
                    //测试发现必须重复执行断开操作(不重复执行断开操作作废)
                    if(DataStatic.mBTConnectState != BluetoothHfDevice.STATE_DISCONNECTED){
                        DataStatic.mBTConnectState = BluetoothHfDevice.STATE_DISCONNECTED;
                    }
                    //同步设备列表颜色
                    mFuncBTOperate.notifyPairAndMainS(device, false);
                    //如果断开的设备是当前连接的设备则做相应的处理
                    if(DataStatic.mCurrentBT != null
                            && DataStatic.mCurrentBT.getAddress().equals(device.getAddress())){
                        //如果有新设备则清空当前设备并连接
                        if(DataStatic.mNewBT != null) {
                            //当前设备已断开,清空当前设备
//                            mFuncBTOperate.setSPBTAddress("0");
                            if(mControlManager.getConnectionState(DataStatic.mCurrentBT)
                                    == BluetoothHfDevice.STATE_DISCONNECTED){
//                                DataStatic.mCurrentBT = null;
                                BtLogger.e(TAG, "断开恢复优先级-！getPriority＝" + mControlManager.getPriority(DataStatic.mCurrentBT));
                                //新配对请求仅降低当前设备连接优先级，避免自动连接冲突（无需主动降低优先级，断开后自动变-1）
//                                mControlManager.setPriority(DataStatic.mCurrentBT, BluetoothProfile.PRIORITY_ON);
                            }
                            //连接新设备
                            mFuncBTOperate.connectNewBTDevice();
                        }
                    }
                    BtLogger.d(TAG, "BTBCReceiver断开－reconnectBTDevice");
                    //收到蓝牙断开广播就响应自动连接
                    if(mFuncBTOperate != null && DataStatic.mAccOff == false
                            && mFuncBTOperate.getBTAutoConnStatus()){
                        //延时15秒再次连接
                        mFuncBTOperate.delayedConnectBTDevice(15000);
                    }
                    break;
                case BluetoothHfDevice.STATE_CONNECTING:
                    //连接中刷新列表状态
                    mFuncBTOperate.notifyPair(null, 6);
                    String btSelfName = mControlManager.getBTName();
                    //连接弹框
//                    mFuncBTOperate.showmBTProgressDialog(mContext, R.string.bt_connecting, btSelfName);
                    break;
            }
        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            //＊＊＊绑定状态变化
            int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            int bondState2 = device.getBondState();
            int state = mControlManager.getConnectionState(device);
            BtLogger.e(TAG, "BTBCReceiver－绑定－状态变化－连接状态= " + state);
            BtLogger.e(TAG, "BTBCReceiver－绑定－状态变化－绑定状态= " + bondState);
            BtLogger.e(TAG, "BTBCReceiver－绑定－状态变化－绑定状态222= " + bondState2);
        } else if (BluetoothDevice.ACTION_PAIRING_CANCEL.equals(action)) {
            //＊＊＊取消配对
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            BtLogger.e(TAG, "BTBCReceiver取消配对");
        } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
            //＊＊＊配对请求
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //没什么作用（无需主动降低优先级，断开后自动变-1）
//            mControlManager.setPriority(device, BluetoothProfile.PRIORITY_ON);

            //蓝牙被动断开，同时有设备主动连接车机
            //当设备需要配对时,就将设备设置为null
            //保存当前连接设备地址
//            mFuncBTOperate.setSPBTAddress("0");
            if(mControlManager.getConnectionState(DataStatic.mCurrentBT)
                    == BluetoothHfDevice.STATE_DISCONNECTED){
                BtLogger.e(TAG, "断开恢复优先级-222-getPriority＝" + mControlManager.getPriority(DataStatic.mCurrentBT));
                //新配对请求仅降低当前设备连接优先级，避免自动连接冲突（无需主动降低优先级，断开后自动变-1）
//                mControlManager.setPriority(DataStatic.mCurrentBT, BluetoothProfile.PRIORITY_ON);
//                DataStatic.mCurrentBT = null;
            }
            BtLogger.e(TAG, "BTBCReceiver配对请求-mCurrentBT="+DataStatic.mCurrentBT);
//                syncClearAndPullAllCallData();
        } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            //＊＊＊蓝牙开关
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            BtLogger.d(TAG, "BT Open state = " + state);
            BtLogger.d(TAG, "BT DataStatic.mAccOff = " + DataStatic.mAccOff);
            if (BluetoothAdapter.STATE_TURNING_ON == state){
                //同步蓝牙开关状态
                mFuncBTOperate.setBTSwitchStatus(true);
                //如果ACC为off则关闭蓝牙
                if(DataStatic.mAccOff){
                    mControlManager.disableBT();
                }else{
                    //开机设置蓝牙永久可见
                    mControlManager.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                    mControlManager.setBTName("karry_change");
                    //自动连接蓝牙
                    mFuncBTOperate.delayedConnectBTDevice(600);
                    //高亮蓝牙搜索
                    mFuncBTOperate.notifyPair(null, 5);
                }
            }else if(BluetoothAdapter.STATE_TURNING_OFF == state){
                //同步蓝牙开关状态
                mFuncBTOperate.setBTSwitchStatus(false);
            }
        }

        //###以下为蓝牙电话相关广播处理
        int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
        //###隐私模式切换
        if (action.equals(BluetoothHfDevice.ACTION_AUDIO_STATE_CHANGED)) {
            int prevState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1);
//            BtLogger.e(TAG, "BTBCReceiver－音频－prevState = " + prevState);
            BtLogger.e(TAG, "BTBCReceiver－音频－状态变化state = " + state);
            switch (state) {
                case BluetoothHfDevice.STATE_DISCONNECTED:
                    //隐私模式，在这里同步界面变化
                    mFuncBTOperate.notifyBTService(16, true);
                    break;
                case BluetoothHfDevice.STATE_CONNECTED:
                    //车机模式
                    mFuncBTOperate.notifyBTService(16, false);
                    break;
            }
        }

        if(action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
            BtLogger.e(TAG, "BTBCReceiver－BluetoothA2dp-state = " + state);
            switch (state) {
                case BluetoothProfile.STATE_DISCONNECTED:  //0
//                    mFuncBTOperate.reConnectA2dp();
                    //还原断开连接状态，避免断开连接时这个状态错误
                    mFuncBTOperate.setmA2dpDisconnecting(false);
                    //如果是连接反馈连接失败则重连
//                    mFuncBTOperate.a2dpReConnecting();
                    //连接失败或断开A2DP后还原连接中标志
                    mFuncBTOperate.setmA2dpConnecting(false);
                    break;
                case BluetoothProfile.STATE_CONNECTED:     //2
                    //还原连接状态，避免连接时这个状态错误
                    mFuncBTOperate.setmA2dpConnecting(false);
                    boolean isBluetooth = mControlManager.btMusicIsForeground();
                    int easylink = android.os.ProtocolManager.getInstance().getAppStatus("EC");
                    if(isBluetooth || easylink == 1) {
                        //初始化蓝牙音乐相关数据
                        mFuncBTOperate.notifyBTService(6);
                        //不能在这里恢复，否则只要连接上a2dp就会恢复蓝牙音乐播放
//                        mFuncBTOperate.startPlayerForLoss();
                        //恢复播放蓝牙音乐
                        mFuncBTOperate.notifyMain(12, false);
                    }
                    break;
            }
        }

        //###易联相关广播接收处理(以下这部分无用)
        if (action.equals(Constants.BROADCAST_BT_CHECKSTATUS)) {  //蓝牙连接请求,EC启动时候发出
            if(DataStatic.mCurrentBT != null && mControlManager != null
                    && mControlManager.getConnectionState(DataStatic.mCurrentBT) == BluetoothHfDevice.STATE_CONNECTED){
            }else{
                Intent connected = new Intent();
                connected.setAction(Constants.BROADCAST_BT_OPENED);
                connected.putExtra("name", mControlManager.getBTName());
//            connected.putExtra("pin", "xxxx");
                context.sendBroadcast(connected);
            }
        } else if (action.equals(Constants.BROADCAST_BT_A2DP_ACQUIRE)) {    //蓝牙A2DP请求,进入屏幕映射时发出
            //蓝牙连接则初始化蓝牙音乐相关数据
//            mFuncBTOperate.notifyBTService(6);
            //延时1秒再连接a2dp,避免易联广播在还处于倒车界面时接收
            mEasyConnHandler.removeMessages(0);
            mEasyConnHandler.sendEmptyMessageDelayed(0, 600);
        } else if (action.equals(Constants.BROADCAST_BT_A2DP_RELEASE)) {    //蓝牙A2DP释放,A2DP释放失败
            //这个关闭会引起由易联切换到其他应用时A2DP断开bug（将close方法改为静音就不会了）
            mFuncBTOperate.abandonBTMusic();
        } else if (action.equals(Constants.BROADCAST_APP_QUIT)) {        //EC退出时发出，车机恢复蓝牙
        }
        //###易联相关广播接收处理(以上这部分无用)
    }

    private void syncClearAndPullAllCallData(){
        new Thread(){
            @Override
            public void run() {
                BtLogger.e(TAG, "清空所有数据");
                mFuncBTOperate.deleteContacts();
                mFuncBTOperate.deleteAllCallLogRecords();
                mControlManager.deleteVCFFiles();
                //清空本机号码
                mFuncBTOperate.clearMyNumber();
                //清空上次拨打电话号码
                mFuncBTOperate.clearDialNumber();
                //清除上个蓝牙设备的ID3信息
                mFuncBTOperate.clearID3Infos();
                //重新拉取联系人
                mFuncBTOperate.notifyBTService(0);
            }
        }.start();
    }

    private Handler mEasyConnHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //蓝牙连接则初始化蓝牙音乐相关数据
            mFuncBTOperate.notifyBTService(6);
        }
    };
}
