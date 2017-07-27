package com.semisky.ym_multimedia.ymbluetooth.fragment;

import android.content.Context;
import android.os.Bundle;

import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.semisky.ym_multimedia.BaseFragment;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventDial;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.dialog.BTCallDialog;
import com.semisky.ym_multimedia.ymbluetooth.func.ControlManager;
import com.semisky.ym_multimedia.ymbluetooth.func.FuncBTOperate;
import com.ypy.eventbus.EventBus;

/**
 * Created by luoyin on 16/9/26.
 */
public class BTDial extends BaseFragment {
    private final String TAG = "BTDial";
    private Context mContext;
    private View mRootView;
    private TextView mPhoneNumTV;
    private ImageView mIV;
    private String mPhoneNum = "";

    private Button[] mKeyNumIVs = new Button[10];
    private int[] mKeyNumIDs = {R.id.iv_dial_0, R.id.iv_dial_1, R.id.iv_dial_2,
            R.id.iv_dial_3, R.id.iv_dial_4, R.id.iv_dial_5, R.id.iv_dial_6,
            R.id.iv_dial_7, R.id.iv_dial_8, R.id.iv_dial_9};

    private int[] mKeySymbolIDs = {R.id.iv_dial_call, R.id.iv_dial_star,
            R.id.iv_dial_hash, R.id.iv_dial_del, R.id.iv_dial_plus};

    private ControlManager mControlManager;
    private FuncBTOperate mFuncBTOperate;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden && mFuncBTOperate != null){
            refreshBTStateText();
            //设置标题
            mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.nl_bt_title, R.string.ym_bt_dail, -1);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.ym_bt_func_dial, null);
        }
//        if (savedInstanceState != null) {
//            // Restore the fragment's state here
//            mPhoneNum = savedInstanceState.getString("dialnumber");
//        }
        initViews();
        initData();
        refreshBTStateText();
        return mRootView;
    }

    public void onEventMainThread(EventDial eventDial) {
        BtLogger.d(TAG, "onEventMainThread－getMethod = " + eventDial.getMethod());
        switch (eventDial.getMethod()){
            case 0:
                clearData();
                break;
        }
    }

    private void initViews() {
        mPhoneNumTV = (TextView) mRootView.findViewById(R.id.tv_dial_phone_num);
        for (int i = 0; i < mKeyNumIDs.length; i++) {
            mKeyNumIVs[i] = (Button) mRootView.findViewById(mKeyNumIDs[i]);
            mKeyNumIVs[i].setOnClickListener(mNumOCListener);
        }
        for (int j = 0; j < mKeySymbolIDs.length; j++) {
            mRootView.findViewById(mKeySymbolIDs[j]).setOnClickListener(mSymbolOCListener);
        }
        //长按全删
        mRootView.findViewById(R.id.iv_dial_del).setOnLongClickListener(ol);
    }

    private void initData() {
        mControlManager = ControlManager.getInstance(mContext, null);
        mFuncBTOperate = FuncBTOperate.getInstance(mContext);
        //设置标题
        mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.nl_bt_title, R.string.ym_bt_dail, -1);
    }

    private void refreshBTStateText(){
        if(mPhoneNumTV == null){
            return;
        }
        //输入框显示提示
        if (mFuncBTOperate != null && mControlManager != null
                && !mControlManager.isEnabled()){
            //蓝牙未打开
            mPhoneNumTV.setText(R.string.ym_bt_please_open);
        } else if (mFuncBTOperate != null && mControlManager != null
                && mControlManager.getConnectionState(DataStatic.mCurrentBT) == BluetoothHfDevice.STATE_CONNECTED){
            //蓝牙已连接
            mPhoneNumTV.setText(R.string.ym_bt_state_connected);
        } else if (mFuncBTOperate != null && mControlManager != null
                && mControlManager.getConnectionState(DataStatic.mCurrentBT) != BluetoothHfDevice.STATE_CONNECTED){
            //蓝牙未连接
            mPhoneNumTV.setText(R.string.ym_bt_state_not_connected);
        } else {
            //其他状态
            mPhoneNumTV.setText(R.string.ym_bt_please_open);
        }
    }

    private View.OnLongClickListener ol = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View view) {
            clearData();
            return false;
        }
    };

    private void clearData(){
        BtLogger.e(TAG, "clearData");
        mPhoneNum = "";
        //清空数据时显示状态
        refreshBTStateText();
    }

    private View.OnClickListener mNumOCListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mKeyNumIDs.length; i++) {
                if(v.getId() == mKeyNumIDs[i]){
                    addPhoneNum(""+i);
                }
            }
        }
    };

    private View.OnClickListener mSymbolOCListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.iv_dial_star:
                    addPhoneNum("*");
                    break;
                case R.id.iv_dial_hash:
                    addPhoneNum("#");
                    break;
                case R.id.iv_dial_plus:
                    addPhoneNum("+");
                    break;
                case R.id.iv_dial_del:
                    mPhoneNum = mPhoneNum.substring(0, mPhoneNum.length()>0?mPhoneNum.length()-1:0);
                    mPhoneNumTV.setText(mPhoneNum);
                    //删除到没号码时显示状态
                    if("".equals(mPhoneNum)){
                        refreshBTStateText();
                    }
                    break;
                case R.id.iv_dial_call:
//                    Toast.makeText(mContext, "拨打电话！", Toast.LENGTH_SHORT).show();
                    BtLogger.d(TAG, "拨号界面－拨打" + mPhoneNumTV.getText().toString());
//                    dialNumber(mPhoneNumTV.getText().toString());
                    //蓝牙已连接才能拨打电话
                    if (mFuncBTOperate != null && mControlManager != null
                            && mControlManager.getConnectionState(DataStatic.mCurrentBT) == BluetoothHfDevice.STATE_CONNECTED){
                        //拨打电话
                        BTCallDail();
                    }
                    break;
            }
        }
    };

    private void addPhoneNum(String input){
        if(mPhoneNum.length()<100){
            mPhoneNum += input;
            mPhoneNumTV.setText(mPhoneNum);
        }
    }

    public void BTCallDail(){
        BtLogger.d(TAG, "方控－拨打" + mPhoneNumTV.getText().toString());
        BTCallDialog btCallDialog = BTCallDialog.getInstance(null);
        //蓝牙通话界面未弹出才响应拨号
        if(!btCallDialog.isShowing()){
            //如果拨号盘为空则拨打上一个号码
            if("".equals(mPhoneNum)){
                if(mFuncBTOperate != null){
                    if("".equals(mFuncBTOperate.getDialNumber())){
                        //默认拨打通话记录的第一条记录
                        mPhoneNum = mFuncBTOperate.getFirstCallLog();
                        mFuncBTOperate.setDialNumber(mPhoneNum);
                    }else{
                        mPhoneNum = mFuncBTOperate.getDialNumber();
                    }
                    //获取到的号码不为空字符才设置显示
                    if(!"".equals(mPhoneNum)){
                        mPhoneNumTV.setText(mPhoneNum);
                    }
                }
                //直接重播电话，这个是直接拨打上次呼出的号码，不呼出刚呼入或未接的
//                mControlManager.redial();
            } else {
                dialNumber(mPhoneNum);
            }
        }
    }

    /**
     * 拨打电话方法
     * @param number
     */
    private void dialNumber(String number){
        //号码不为null或空则执行拨号处理
        if(number != null && !number.equals("")){
            //拨打
            mControlManager.dial(number);
        }
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        outState.putString("dialnumber", mPhoneNum);
//        super.onSaveInstanceState(outState);
//    }

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
        return R.string.ym_bt_dail;
    }

    @Override
    public void setListener() {}

    @Override
    public void register() {}

    @Override
    public void unregister() {}
}
