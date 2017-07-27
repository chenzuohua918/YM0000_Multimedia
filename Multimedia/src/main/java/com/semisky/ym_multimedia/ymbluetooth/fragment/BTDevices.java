package com.semisky.ym_multimedia.ymbluetooth.fragment;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.semisky.ym_multimedia.BaseFragment;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.broadcom.bt.avrcp.BluetoothAvrcpController;
import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventPair;
import com.semisky.ym_multimedia.ymbluetooth.adapter.AdpBTDeviceList;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;
import com.semisky.ym_multimedia.ymbluetooth.func.ControlManager;
import com.semisky.ym_multimedia.ymbluetooth.func.FuncBTOperate;
import com.semisky.ym_multimedia.ymbluetooth.widget.VerticalTextView;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by luoyin on 16/9/26.
 */
public class BTDevices extends BaseFragment {
    private final String TAG = "BTDevices";
    private Context mContext;
    private View mRootView;
    private ListView mListView;
    private AdpBTDeviceList mAdpBTDeviceList;
    private VerticalTextView mRightBarTV;

    private View mYMConnectBtn;
    private View mYMDisconnectBtn;
    private View mYMDeleteBtn;

    private ControlManager mControlManager;
    private FuncBTOperate mFuncBTOperate;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden && mFuncBTOperate != null){
            //设置标题
            mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.nl_bt_title, R.string.ym_bt_list, -1);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRightBarStatus(false);
    }

    public void onEventMainThread(EventPair eventPair) {
        BtLogger.d(TAG, "onEventMainThread－pair = " + eventPair.getType());
        switch (eventPair.getType()){
            case 0:
                //显示列表
                showBTDeviceList();
                break;
            case 1:
                //添加设备
                mBTDeviceSet.add(eventPair.getBluetoothDevice());
                showBTDeviceList();
//                syncListItemState(eventPair.getBluetoothDevice(), eventPair.getState());
                break;
            case 2:
                //同步颜色需要延迟200毫秒，避免UI数据未加载完成的bug
                Message msg = new Message();
                msg.what = 0;
                msg.obj = eventPair;
                mUIDelayHandler.sendMessageDelayed(msg, 300);
                break;
            case 3:
                //清空蓝牙列表
                initBTDeviceSet();
                mBTDeviceList.clear();
                //清空列表时去掉高亮单元
                mAdpBTDeviceList.setBTDeviceList(mBTDeviceList);
                break;
            case 4:
                //蓝牙未开启,不能搜索蓝牙
                mRightBarTV.setEnabled(false);
                break;
            case 5:
                //蓝牙已开启,可以搜索蓝牙
                mRightBarTV.setEnabled(true);
                setRightBarStatus(false);
                break;
            case 6:
                //刷新列表
                mAdpBTDeviceList.notifyDataSetChanged();
                break;
            case 7:
                //蓝牙搜索中
//                mRightBarTV.setEnabled(false);
                setRightBarStatus(true);
                break;
            case 8:
                //配对完成高亮蓝牙设备并第一位显示
                position = 0;
                mAdpBTDeviceList.setmHighlightItem(0);
                break;
        }
    }

    private void setRightBarStatus(boolean searching){
        if(searching){
            mRightBarTV.setTextColor(mContext.getResources().getColor(R.color.ym_bt_gray));
            mRightBarTV.setBackgroundResource(R.drawable.ym_bt_selector_rightbar_two);
            mRightBarTV.setVText(mContext.getResources().getText(R.string.nl_bt_cancel_searching));
        }else{
            mRightBarTV.setTextColor(mContext.getResources().getColorStateList(R.color.ym_bt_color_radiobutton));
            mRightBarTV.setBackgroundResource(R.drawable.ym_bt_selector_rightbar_one);
            mRightBarTV.setVText(mContext.getResources().getText(R.string.nl_bt_search_devices));
        }
    }

    private void initBTDeviceSet(){
        mBTDeviceSet.clear();
        Iterator<BluetoothDevice> iterator = mControlManager.getBondedDevices().iterator();
        while(iterator.hasNext()) {
            mBTDeviceSet.add(iterator.next());
        }
    }

    private Handler mUIDelayHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        //改变蓝牙状态后只用刷新一下列表即可
//                        if(mAdpBTDeviceList != null){
//                            mAdpBTDeviceList.notifyDataSetChanged();
//                        }
                        showBTDeviceList();
                        break;
                }
            }
        };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.ym_bt_func_devices, null);
        }
        initViews();
        initDeviceList();
        initData();
        return mRootView;
    }

    private void initViews() {
        mListView = (ListView) mRootView.findViewById(R.id.lv_device_list);
        //右边条响应
//        mRootView.findViewById(R.id.ll_right_bar).setOnClickListener(mOCListener);
        mRightBarTV = (VerticalTextView) mRootView.findViewById(R.id.tv_right_bar);
        mRightBarTV.setOnClickListener(mOCListener);

        mYMConnectBtn = mRootView.findViewById(R.id.ym_device_connect);
        mYMDisconnectBtn = mRootView.findViewById(R.id.ym_device_disconnect);
        mYMDeleteBtn = mRootView.findViewById(R.id.ym_device_delete);
        mYMConnectBtn.setOnClickListener(mOnYMMenuCListener);
        mYMDisconnectBtn.setOnClickListener(mOnYMMenuCListener);
        mYMDeleteBtn.setOnClickListener(mOnYMMenuCListener);
    }

    //储存所有蓝牙设备
    private List<BluetoothDevice> mBTDeviceList = new ArrayList<BluetoothDevice>();
    //用于过滤重复的设备信息
    private Set<BluetoothDevice> mBTDeviceSet = new HashSet<BluetoothDevice>();

    private void initDeviceList() {
        mAdpBTDeviceList = new AdpBTDeviceList(mContext, mBTDeviceList);
        mListView.setAdapter(mAdpBTDeviceList);
        mListView.setOnItemClickListener(mOIClistener);
//        mListView.setOnItemLongClickListener(mOILClistener);
    }

    private void initData() {
        //蓝牙控制类
        mControlManager = ControlManager.getInstance(mContext, null);
        mFuncBTOperate = FuncBTOperate.getInstance(mContext);
        //设置标题
        mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.nl_bt_title, R.string.ym_bt_list, -1);
        //初始化右边文字
//        mRightBarTV.setTextColor(mContext.getResources().getColor(R.color.ym_bt_color_search_buttom));
//        mRightBarTV.setBackgroundResource(R.drawable.ym_bt_selector_rightbar_one);
        mRightBarTV.setVText(mContext.getResources().getText(R.string.nl_bt_search_devices));
        BtLogger.d(TAG, "initData");
        //设置蓝牙永久可见
//        mControlManager.enableBT();
//        mControlManager.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
//        mControlManager.setBTName("Karry-LY");
        //如果蓝牙未打开则先打开蓝牙，如果已打开则开始搜索设备
        if (!mControlManager.isEnabled()) {
            mRightBarTV.setEnabled(false);
        }else{
            //蓝牙打开,可以搜索
            mRightBarTV.setEnabled(true);
        }
        //蓝牙打开则开启服务
        initBTDeviceSet();
    }

    private int position = 0;

    private AdapterView.OnItemClickListener mOIClistener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(true){
                position = i;
                mAdpBTDeviceList.setmHighlightItem(i);
                showBTDeviceList();
                return;
            }
            avoidRepeat();
            BtLogger.d(TAG, "点击单元：" +i+"-mCurrentBT："+ DataStatic.mCurrentBT);
            //点击则停止自动重连
            mFuncBTOperate.stopAutoConnBTDevice();
            //点击停止搜索
            mControlManager.cancelDiscovery();
            //列表处于配对中不响应
            if (!mAdpBTDeviceList.ismPairing()) {
                BtLogger.e(TAG, "点击－响应：" + mAdpBTDeviceList.ismPairing());
                BluetoothDevice bluetoothDevice = mBTDeviceList.get(i);
                //获取点击设备的状态
                int state = mControlManager.getConnectionState(bluetoothDevice);
                if(state == BluetoothHfDevice.STATE_CONNECTED){
                    mFuncBTOperate.showmBTClearDialog(getActivity(), R.string.nl_bt_cautions, R.string.ym_bt_unpair_and_disconnect, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //断开当前蓝牙设备
                            disconnectCurrentBTDevice();
                            mFuncBTOperate.dismissmBTClearDialog();
                        }
                    });
                } else {
                    BtLogger.d(TAG, "点击－连接设备：" + bluetoothDevice.getName());
                    mFuncBTOperate.connectBTDevice(bluetoothDevice);
                }
            }
        }
    };

    private AdapterView.OnItemLongClickListener mOILClistener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
            BluetoothDevice bluetoothDevice = mBTDeviceList.get(i);
            int state = mControlManager.getConnectionState(bluetoothDevice);
            if(state != BluetoothHfDevice.STATE_CONNECTED){
                mFuncBTOperate.showmBTClearDialog(getActivity(), R.string.nl_bt_cautions, R.string.ym_bt_pair_delete, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BluetoothDevice bluetoothDevice = mBTDeviceList.get(i);
                        BtLogger.e(TAG, "长按－i=" + i);
                        BtLogger.e(TAG, "长按－bluetoothDevice=" + bluetoothDevice);
                        //取消保存并从列表中删除
                        mBTDeviceSet.remove(bluetoothDevice);
                        BtLogger.e(TAG, "长按－set-remove=" + bluetoothDevice);
                        if(mControlManager.getBondedDevices().contains(bluetoothDevice)){
                            BtLogger.e(TAG, "长按－mc-remove=" + bluetoothDevice);
//                            mControlManager.getBondedDevices().remove(bluetoothDevice);
//                            mControlManager.removeBond(bluetoothDevice);
                            mFuncBTOperate.removeBond(bluetoothDevice);
                        }
                        showBTDeviceList();
                        mFuncBTOperate.dismissmBTClearDialog();
                    }
                });
            }
            return true;
        }
    };

    private void disconnectCurrentBTDevice(){
        BluetoothDevice bluetoothDevice = DataStatic.mCurrentBT;
        //断开时关闭音乐及a2dp
//                    mFuncBTOperate.closeBTMusic();
        //断开连接前断开pbap
        mFuncBTOperate.notifyBTService(17);
        //已连接设备被点击则断开
        //断开之前的蓝牙设备连接
        mFuncBTOperate.disconnectBT(bluetoothDevice);
        BtLogger.d(TAG, "点击－断开设备：" + bluetoothDevice.getName());
        //主动断开就直接降低设备优先级
        mControlManager.setPriority(bluetoothDevice, BluetoothProfile.PRIORITY_ON);
        //主动断开则清空保存的蓝牙设备
//                    mFuncBTOperate.setSPBTAddress("0");
        mFuncBTOperate.removeBond(bluetoothDevice);
        //断开当前设备后通知配对界面
        mFuncBTOperate.notifyPairAndMainS(bluetoothDevice, false);
        //主动断开当前设备则清空当前设备
        mFuncBTOperate.setSPBTAddress("0");
        if(mControlManager.getConnectionState(DataStatic.mCurrentBT)
                == BluetoothHfDevice.STATE_DISCONNECTED){
            DataStatic.mCurrentBT = null;
        }
        //这个修改会造成严重---蓝牙断不开
//        DataStatic.mCurrentBT = null;
        //直接清除vcf数据
        mFuncBTOperate.deleteContacts();
        mFuncBTOperate.deleteAllCallLogRecords();
        mControlManager.deleteVCFFiles();
    }

    private View.OnClickListener mOCListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //            mControlManager.disableBT();
            //如果蓝牙音乐在播放则提醒用户暂停蓝牙音乐

            if(DataStatic.mCurrentBT != null && mControlManager.getConnectionState(DataStatic.mCurrentBT) == BluetoothHfDevice.STATE_CONNECTED
                    && mFuncBTOperate.getAvrcpState() == BluetoothAvrcpController.PLAY_STATUS_PLAYING){
                mFuncBTOperate.showmBTClearDialog(getActivity(), true, R.string.nl_bt_cautions, R.string.nl_bt_search_warning, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mFuncBTOperate.dismissmBTClearDialog();
                    }
                });
            }else if(mControlManager.isDiscovering()){
                mFuncBTOperate.cancelSearchBTDevices();
            }else{
                setRightBarStatus(true);
                mFuncBTOperate.searchBTDevices();
            }
        }
    };

    private View.OnClickListener mOnYMMenuCListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            int position = mListView.getSelectedItemPosition();
            //位置不能超出蓝牙列表范围
            if(position >= mBTDeviceList.size()){
                return;
            }
            BtLogger.d(TAG, "position：" + position);
            BluetoothDevice bluetoothDevice = mBTDeviceList.get(position);
            BtLogger.d(TAG, "操作设备：" + bluetoothDevice.getName());
            BtLogger.d(TAG, "操作设备地址：" + bluetoothDevice);
            switch (view.getId()){
                case R.id.ym_device_connect:
                    if(bluetoothDevice != null){
                        if(DataStatic.mCurrentBT != null
                                && DataStatic.mCurrentBT.getAddress().equals(bluetoothDevice.getAddress())){
                        }else{
                            BtLogger.d(TAG, "连接：" + DataStatic.mCurrentBT);
                            mFuncBTOperate.connectBTDevice(bluetoothDevice);
                        }
                    }
                    break;
                case R.id.ym_device_disconnect:
                    if(bluetoothDevice != null){
                        if(DataStatic.mCurrentBT != null
                                && DataStatic.mCurrentBT.getAddress().equals(bluetoothDevice.getAddress())){
                            BtLogger.d(TAG, "断开：" + DataStatic.mCurrentBT);
                            mFuncBTOperate.showmBTClearDialog(getActivity(), R.string.nl_bt_cautions, R.string.ym_bt_unpair_and_disconnect, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //断开当前蓝牙设备
                                    disconnectCurrentBTDevice();
                                    mFuncBTOperate.dismissmBTClearDialog();
                                }
                            });
                        }else{
                        }
                    }
                    break;
                case R.id.ym_device_delete:
                    mFuncBTOperate.showmBTClearDialog(getActivity(), R.string.nl_bt_cautions, R.string.ym_bt_pair_delete, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            BluetoothDevice bluetoothDevice = mBTDeviceList.get(position);
                            BtLogger.e(TAG, "删除－bluetoothDevice=" + bluetoothDevice);
                            //取消保存并从列表中删除
                            mBTDeviceSet.remove(bluetoothDevice);
                            BtLogger.e(TAG, "删除－set-remove=" + bluetoothDevice);
                            if(mControlManager.getBondedDevices().contains(bluetoothDevice)){
                                BtLogger.e(TAG, "删除－mc-remove=" + bluetoothDevice);
//                            mControlManager.getBondedDevices().remove(bluetoothDevice);
//                            mControlManager.removeBond(bluetoothDevice);
                                mFuncBTOperate.removeBond(bluetoothDevice);
                            }
                            showBTDeviceList();
                            mFuncBTOperate.dismissmBTClearDialog();
                        }
                    });
                    break;
            }
        }
    };

    private void showBTDeviceList(){
        BtLogger.e(TAG, "showBTDeviceList");
        Iterator<BluetoothDevice> iterator = mBTDeviceSet.iterator();
        mBTDeviceList.clear();
        BluetoothDevice bluetoothDevice;
        while(iterator.hasNext()) {
            bluetoothDevice = iterator.next();
            if(mControlManager.getConnectionState(bluetoothDevice) == BluetoothHfDevice.STATE_CONNECTED
                    && DataStatic.mCurrentBT != null && bluetoothDevice.getAddress().equals(DataStatic.mCurrentBT.getAddress())){
                mBTDeviceList.add(0,bluetoothDevice);
            }else{
                mBTDeviceList.add(bluetoothDevice);
            }
        }
//                mBTDeviceSet.clear();
        BtLogger.e(TAG, "showBTDeviceList 刷新 。");
        mAdpBTDeviceList.setBTDeviceList(mBTDeviceList);
        //移动到首部
//        mListView.smoothScrollToPosition(0);
        mListView.setSelection(0);
    }

    private Handler mRepeatHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    mListView.setOnItemClickListener(mOIClistener);
                    break;
            }
        }
    };

    private void avoidRepeat(){
        mListView.setOnItemClickListener(null);
        mRepeatHandler.sendEmptyMessageDelayed(0, 500);
    }

    @Override
    public void initModel() {}

    @Override
    public void initLeftViews() {}

    @Override
    public void initRightViews() {}

    @Override
    public void initMiddleViews() {}

    @Override
    public int getSystemUITitleResId() {
        return R.string.ym_bt_list;
    }

    @Override
    public void setListener() {}

    @Override
    public void register() {}

    @Override
    public void unregister() {}
}
