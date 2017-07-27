package com.semisky.ym_multimedia.ymbluetooth.func;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;

import com.broadcom.bt.hfdevice.BluetoothCallStateInfo;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;

import java.io.File;
import java.util.Set;

/**
 *
 * Created by DaiShiHao on 2015/1/4 0004.
 */
public class ControlManager {

    private final String TAG = "ControlManager";
    private static ControlManager instance;

    private FuncBTManager funcBTManager;
    private static Handler uiHandler;

    private Handler threadHandler;
    private Context mContext;

    private final int GET_PHONEBOOK_MSG = 0x11;
    private final int GET_COLLECTIONS_MSG = 0x12;
    private boolean MuteMic = true;   //默认mic是静掉


    private ControlManager(Context context){
        mContext = context;
        funcBTManager = new FuncBTManager(mContext, null);
    }

    public static synchronized ControlManager getInstance(Context context, Handler handler){
        if(instance == null){
            instance = new ControlManager(context);
        }	
//       uiHandler = handler;
        return instance;
    }
    
    public void setHandler(Handler handler){
    	uiHandler = handler;
    }
    

    /**
     *判断是否存在蓝牙设备
     * @return true if bluetooth device is existed,
     * return false if not .
     */
    public boolean hasBluetoothDevice(){
        return funcBTManager.hasBluetoothDevice();
    }

    /**
     * 判断蓝牙设备是否开启
     * @return true if enabled or return false if not.
     */
    public boolean isEnabled(){
        return funcBTManager.isEnabled();
    }

    /**
     * 打开蓝牙
     * @return true on success or false on error.
     */
    public boolean enableBT(){
        return  funcBTManager.enableBT();
    }

    /**
     * 关闭蓝牙
     * @return true on success or false on error.
     */
    public boolean disableBT(){
        return funcBTManager.disableBT();
    }

    /**
     * 获取蓝牙名称
     * @return btName
     */
    public String getBTName(){
        return funcBTManager.getBTName();
    }

    /**
     * 设置蓝牙的名称
     * @param newName The new name for BT.
     */
    public void setBTName(String newName){
         funcBTManager.setBTName(newName);
    }

    /*SCAN_MODE_NONE = 20        无功能状态 : 查询扫描和页面扫描都失效, 该状态下蓝牙模块既不能扫描其它设备, 也不可见;
      SCAN_MODE_CONNECTABLE = 21 扫描状态 :   查询扫描失效, 页面扫描有效, 该状态下蓝牙模块可以扫描其它设备, 从可见性来说只对已配对的蓝牙设备可见, 
                                              只有配对的设备才能主动连接本设备;
      SCAN_MODE_CONNECTABLE_DISCOVERABLE = 23  可见状态 :  查询扫描和页面扫描都有效;
   */
    public void setScanMode(int mode,String newName){
         funcBTManager.setScanMode(mode, newName);
    }

    public void setScanMode(int mode){
        funcBTManager.setScanMode(mode);
    }
    /**
     * 获取蓝牙的Mac地址
     * @return btAddress
     */
    public String getBTAddress(){
        return funcBTManager.getBTAddress();
    }

    /**
     * 开始搜索设备
     * @return true on success or false on error.
     */
    public boolean startDiscovery(){
        return funcBTManager.startDiscovery();
    }

    /**
     * 取消搜索
     * @return true on success or false on error.
     */
    public boolean cancelDiscovery(){
        return funcBTManager.cancelDiscovery();
    }

    /**
     * 判断是否在搜索
     * @return true if is discovering or false if not.
     */
    public boolean isDiscovering(){
        return funcBTManager.isDiscovering();
    }

    public void clearContactsVCF(){
        //清除电话簿
//        context.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, null, null);
        deleteFile(OUT_FILE_PULL_PB);
    }

    public void deleteVCFFiles(){
        BtLogger.e(TAG, "deleteVCFFiles");
        deleteFile(OUT_FILE_PULL_PB);
        deleteFile(OUT_FILE_PULL_CCH);
    }

    private static final String PATH_SDCARD = "/storage/emulated/0";
    private static final String OUT_FILE_PULL_PB = PATH_SDCARD + "/pb.vcf";
    private static final String OUT_FILE_PULL_OCH = PATH_SDCARD + "/och.vcf";
    private static final String OUT_FILE_PULL_ICH = PATH_SDCARD + "/ich.vcf";
    private static final String OUT_FILE_PULL_MCH = PATH_SDCARD + "/mch.vcf";
    private static final String OUT_FILE_PULL_CCH = PATH_SDCARD + "/cch.vcf";
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    public void setmBCStateInfo(BluetoothCallStateInfo mBCStateInfo) {
        funcBTManager.setmBCStateInfo(mBCStateInfo);
    }

    public BluetoothCallStateInfo getCallStateInfo(BluetoothDevice device){
        return funcBTManager.getCallStateInfo(device);
    }

    public int getAudioState(BluetoothDevice device) {
        return funcBTManager.getAudioState(device);

    }

    public void callStateResponse(int status, int callSetupState, int numActive, int numHeld, String number, int addrType) {
        funcBTManager.callStateResponse(status, callSetupState, numActive, numHeld, number, addrType);
    }

    public void setBtPhoneStatus(int status) {
        funcBTManager.setBtPhoneStatus(status);
    }

    /**
     * 连接
     * @param device 蓝牙设备
     * @return true on success or false on failed.
     */
    public boolean connect(BluetoothDevice device){
        return funcBTManager.connect(device);
    }

    /**
     * 断开连接
     * @param device 蓝牙设备
     * @return true on success or false on failed.
     */
    public boolean disconnect(BluetoothDevice device){
        BtLogger.e(TAG, "disconnect-device=" + device);
        return funcBTManager.disconnect(device);
    }

    public boolean removeBond(BluetoothDevice device){
        BtLogger.e(TAG, "removeBond-device=" + device);
        try {
            return funcBTManager.removeBond(device.getClass(), device);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getPriority(BluetoothDevice device) {
        int priority = funcBTManager.getPriority(device);
        //BtLogger.d(TAG,"getPriority device = "+device+",priority = "+priority);
        return priority;
    }

    public boolean setPriority(BluetoothDevice device, int priority) {
        BtLogger.e(TAG, "设置设备＊＊＊＊＊＊Priority=" + priority);
        return funcBTManager.setPriority(device, priority);
    }

    public int getConnectionState(BluetoothDevice device) {
	int state = funcBTManager.getConnectionState(device);
        //BtLogger.d(TAG,"getConnectionState device = "+device+"state = "+state);
        return state;
    }
    public int getConnectionState() {
        return funcBTManager.getConnectionState();
    }
    /**A2dpProfile
     * 连接
     * @param device 蓝牙设备
     * @return true on success or false on failed.
     */
    public boolean connectA2dp(BluetoothDevice device){
        return funcBTManager.connectA2dp(device);
    }
    /**A2dpProfile
     * 断开连接
     * @param device 蓝牙设备
     * @return true on success or false on failed.
     */
    public boolean disconnectA2dp(BluetoothDevice device){
        return funcBTManager.disconnectA2dp(device);
    }
    /**A2dpProfile
     * 获取连接状态
     * @param device 蓝牙设备
     * @return
        STATE_DISCONNECTED  = 0;
        STATE_CONNECTING    = 1;
        STATE_CONNECTED     = 2;
        STATE_DISCONNECTING = 3;
     */
    public int getConnectionA2dpState(BluetoothDevice device){
        return funcBTManager.getConnectionA2dpState(device);
    }
    /**A2dpProfile
     * 判断是否在播放
     * @param device 蓝牙设备
     * @return
     */
    public boolean isA2dpPlaying(BluetoothDevice device){
        return funcBTManager.isA2dpPlaying(device);
    }
    public void setPreferred(BluetoothDevice device, boolean preferred) {
        funcBTManager.setPreferred(device,preferred);
    }
    /**
     * 蓝牙配对
     * @param btClass 反射类
     * @param device 蓝牙设备
     * @return true on success or false on failed.
     */
    public boolean createBond(Class btClass, BluetoothDevice device){
        try {
            return funcBTManager.createBond(btClass, device);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 蓝牙取消配对
     * @param btClass 反射类
     * @param device 蓝牙设备
     * @return true on success or false on failed.
     */
//    public boolean removeBond(Class btClass, BluetoothDevice device){
//        try {
//            return funcBTManager.removeBond(btClass, device);
//        }catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }
//
//    }

    /**
     * 获取已配对的设备
     * @return Set类型的集合
     */
    public Set<BluetoothDevice> getBondedDevices(){
        return funcBTManager.getBondedDevices();
    }








    
    /**
     * 拨打电话
     * @param num 要拨打的号码
     * @return true on success or false on failed.
     */
    public boolean dial(String num){
        DataStatic.mIsOperateByVehicle = true;
        //拨号则为车机操作
//        BTCallDialog.getInstance(mContext).setmIsPickupByVehicle(true);
        return funcBTManager.dial(num);
    }

    public boolean redial(){
        return funcBTManager.redial();
    }

    /**
     * 接听电话
     * @return true on success or false on failed.
     */
    public boolean answer(){
        DataStatic.mIsOperateByVehicle = true;
        return funcBTManager.answer();
    }

    /**
     * 挂断电话
     * @return true on success or false on failed.
     */
    public boolean hangup(){
        DataStatic.mIsOperateByVehicle = true;
        return funcBTManager.hangup();
    }
    
    
    public boolean hold(int holdType){
        DataStatic.mIsOperateByVehicle = true;
    	return funcBTManager.hold(holdType);
    }
    

    /**
     * 获取通话状态
     * @return true on success or false on failed.
     */
    public boolean getCLCC(){
        return funcBTManager.getCLCC();
    }

    /**
     * 通话状态下，点击数字按钮
     * @return true on success or false on failed.
     */
    public boolean sendDTMFcode(char dtmfCode){
        return funcBTManager.sendDTMFcode(dtmfCode);
    }


    public boolean isNotInit(){
        return  funcBTManager.isNotInit();
    }

    public boolean connectAudio(){
        return funcBTManager.connectAudio();
    }

    public boolean disconnectAudio(){
        BtLogger.e(TAG, "disconnectAudio-断开音频");
        //切换私密时静音300ms
        muteForDepop2(300);
        return funcBTManager.disconnectAudio();
    }

    /**
     * 读取数据库中电话本
     */
    public void getPhoneBook(){
       threadHandler.sendEmptyMessage(GET_PHONEBOOK_MSG);
    }

//    public boolean isForeground(Context context, String className) {
//        return DataStatic.sCurrentIF == 2;
//    }
    public boolean btMusicIsForeground() {
        return DataStatic.sIsBluetoothUI && DataStatic.sCurrentIF == 4;
    }

    public void notifyDial(int method){
        funcBTManager.notifyDial(method);
    }

    public void muteForDepop(int delayMs){
        funcBTManager.muteForDepop(delayMs);
    }

    public void muteForDepop2(int delayMs){
        funcBTManager.muteForDepop2(delayMs);
    }
}
