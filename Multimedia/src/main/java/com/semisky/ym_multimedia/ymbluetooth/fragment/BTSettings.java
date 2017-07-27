package com.semisky.ym_multimedia.ymbluetooth.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.semisky.ym_multimedia.BaseFragment;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventCallLog;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.func.ControlManager;
import com.semisky.ym_multimedia.ymbluetooth.func.FuncBTOperate;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;
import com.ypy.eventbus.EventBus;

/**
 * Created by luoyin on 16/9/26.
 */
public class BTSettings extends BaseFragment implements CompoundButton.OnCheckedChangeListener{
    private final String TAG = "BTSettings";
    private Context mContext;
    private View mRootView;
    private TextView mBtName;
    private TextView mBtPwd;

    private CheckBox mBtSwitch;
    private CheckBox mBtAutoConn;
    private CheckBox mBtAutoAnswer;

    private ControlManager mControlManager;
    private FuncBTOperate mFuncBTOperate;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            if(mFuncBTOperate != null){
                //设置标题
                mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.nl_bt_title, R.string.ym_bt_settings, -1);
            }
            if(mControlManager != null && mBtName != null) {
                String btSelfName = mControlManager.getBTName();
                mBtName.setText(btSelfName);
            }
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
    }

    public void onEventMainThread(EventCallLog eventCallLog) {
        switch (eventCallLog.getMethod()){
            case 0:
                break;
            case 1:
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        BtLogger.d(TAG, "BTCallLog-onCreateView");
        mContext = getActivity().getApplicationContext();
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.ym_bt_func_settings, null);
        }
        initViews();
        initData();
        return mRootView;
    }

    private void initViews() {
        mBtName = (TextView) mRootView.findViewById(R.id.tv_settings_bt_name);
        mBtPwd = (TextView) mRootView.findViewById(R.id.tv_settings_bt_pwd);

        mBtSwitch = (CheckBox) mRootView.findViewById(R.id.tv_settings_bt_switch);
        mBtAutoConn = (CheckBox) mRootView.findViewById(R.id.tv_settings_bt_auto_connect);
        mBtAutoAnswer = (CheckBox) mRootView.findViewById(R.id.tv_settings_bt_auto_answer);
        mBtSwitch.setOnCheckedChangeListener(this);
        mBtAutoConn.setOnCheckedChangeListener(this);
        mBtAutoAnswer.setOnCheckedChangeListener(this);

    }

    private void initData() {
        mControlManager = ControlManager.getInstance(mContext, null);
        mFuncBTOperate = FuncBTOperate.getInstance(mContext);
        //设置标题
        mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.nl_bt_title, R.string.ym_bt_settings, -1);

        //同步按钮状态
        mBtSwitch.setChecked(mFuncBTOperate.getBTSwitchStatus());
        mBtAutoConn.setChecked(mFuncBTOperate.getBTAutoConnStatus());
        mBtAutoAnswer.setChecked(mFuncBTOperate.getBTAutoAnswerStatus());

        String btSelfName = mControlManager.getBTName();
        mBtName.setText(btSelfName);
        mBtPwd.setText("0000");
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.tv_settings_bt_switch:
                //蓝牙已开启，按钮关闭，则关闭蓝牙
                if(mControlManager.isEnabled() && !b){
                    mControlManager.disableBT();
                }
                //蓝牙未开启，按钮打开，则打开蓝牙
                if(!mControlManager.isEnabled() && b){
                    mControlManager.enableBT();
                }
                break;
            case R.id.tv_settings_bt_auto_connect:
                mFuncBTOperate.setBTAutoConnStatus(b);
                if(b){
                    //打开开关，1秒后自动连接，避免频繁操作响应
                    mFuncBTOperate.delayedConnectBTDevice(1000);
                }else{
                    //停止自动连接
                    mFuncBTOperate.stopAutoConnBTDevice();
                }
                break;
            case R.id.tv_settings_bt_auto_answer:
                mFuncBTOperate.setBTAutoAnswerStatus(b);
                break;
        }
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
        return R.string.ym_bt_settings;
    }

    @Override
    public void setListener() {}

    @Override
    public void register() {}

    @Override
    public void unregister() {}
}
