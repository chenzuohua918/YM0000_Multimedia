package com.semisky.ym_multimedia.ymbluetooth.fragment;

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

import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventCallLog;
import com.semisky.ym_multimedia.ymbluetooth.adapter.AdpCallLogList;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.data.CallLogRecords;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;
import com.semisky.ym_multimedia.ymbluetooth.func.ControlManager;
import com.semisky.ym_multimedia.ymbluetooth.func.FuncBTOperate;
import com.semisky.ym_multimedia.ymbluetooth.widget.VerticalTextView;
import com.ypy.eventbus.EventBus;

import java.util.List;

/**
 * Created by luoyin on 16/9/26.
 */
public class BTCallLog extends BaseFragment {
    private final String TAG = "BTCallLog";
    private Context mContext;
    private View mRootView;
    private VerticalTextView mRightBarTV;
    private ListView mListView;
    private AdpCallLogList mAdpCallLogList;
    private List<CallLogRecords> mCallLogRecordsList;

    private ControlManager mControlManager;
    private FuncBTOperate mFuncBTOperate;

    private View mYMChangeTypeBtn;
    private View mYMDelSelectBtn;
    private View mYMDelAllBtn;
    private View mYMDialBtn;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            if(mFuncBTOperate != null){
                //设置标题
                mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.nl_bt_title, R.string.ym_bt_call_log, -1);
            }
            if(mAdpCallLogList != null){
                mAdpCallLogList.notifyDataSetChanged();
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
        BtLogger.d(TAG, "BTCallLog-mRightBarTV＝"+mRightBarTV);
        BtLogger.d(TAG, "BTCallLog-mContext＝" + mContext);
        mRightBarTV.setVText(mContext.getResources().getText(R.string.nl_bt_clear_list));
    }

    private void refreshRightText(){
        mRightBarTV.setVText(mContext.getResources().getText(R.string.nl_bt_clear_list));
    }

    public void onEventMainThread(EventCallLog eventCallLog) {
        switch (eventCallLog.getMethod()){
            case 0:
                syncCallLogs();
                break;
            case 1:
                clearList();
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        BtLogger.d(TAG, "BTCallLog-onCreateView");
        mContext = getActivity().getApplicationContext();
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.ym_bt_func_call_log, null);
        }
        initViews();
        initData();
        syncCallLogs();
        return mRootView;
    }

    private void initViews() {
        mListView = (ListView) mRootView.findViewById(R.id.lv_call_log_list);
        mAdpCallLogList = new AdpCallLogList(mContext, mCallLogRecordsList);
        mListView.setAdapter(mAdpCallLogList);
        mListView.setOnItemClickListener(mOIClistener);
        //右边条响应
        mRootView.findViewById(R.id.ll_right_bar).setOnClickListener(mOCListener);
        mRightBarTV = (VerticalTextView) mRootView.findViewById(R.id.tv_right_bar);

        mYMChangeTypeBtn = mRootView.findViewById(R.id.ym_change_call_type);
        mYMDelSelectBtn = mRootView.findViewById(R.id.ym_delete_select);
        mYMDelAllBtn = mRootView.findViewById(R.id.ym_delete_all);
        mYMDialBtn = mRootView.findViewById(R.id.ym_call_log_dial);
        mYMChangeTypeBtn.setOnClickListener(mOnYMMenuCListener);
        mYMDelSelectBtn.setOnClickListener(mOnYMMenuCListener);
        mYMDelAllBtn.setOnClickListener(mOnYMMenuCListener);
        mYMDialBtn.setOnClickListener(mOnYMMenuCListener);
    }

    private int showType = 0;
    private View.OnClickListener mOnYMMenuCListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //未连接蓝牙则不响应
            if(mControlManager != null
                    && mControlManager.getConnectionState(DataStatic.mCurrentBT) != BluetoothHfDevice.STATE_CONNECTED){
                return;
            }
            switch (view.getId()){
                case R.id.ym_change_call_type:
                    showType = ++showType%4;
                    syncCallLogs();
                    break;
                case R.id.ym_delete_select:
                    mFuncBTOperate.showmBTClearDialog(getActivity(), R.string.nl_bt_cautions, R.string.ym_bt_delete_single_call_log, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mFuncBTOperate.showStatus(R.string.ym_bt_clear_list_msg);
                            clearList();
                            //删除单条通话记录
                            mDelSingleCLogHandler.sendEmptyMessage(0);
                            mFuncBTOperate.dismissmBTClearDialog();
                            syncCallLogs();
                        }
                    });
                    break;
                case R.id.ym_delete_all:
                    mFuncBTOperate.showmBTClearDialog(getActivity(), R.string.nl_bt_cautions, R.string.ym_bt_delete_all_call_log, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mFuncBTOperate.showStatus(R.string.ym_bt_clear_list_msg);
                            clearList();
                            //清理通话记录数据库
                            mDelAllCLogHandler.sendEmptyMessage(0);
                            mFuncBTOperate.dismissmBTClearDialog();
                        }
                    });
                    break;
                case R.id.ym_call_log_dial:
                    if(mCallLogRecordsList.size() > position) {
                        String dialNumber = mCallLogRecordsList.get(position).getNumber();
                        BtLogger.e(TAG, "通话记录界面－拨打" + dialNumber);
                        mControlManager.dial(dialNumber);
                    }
                    break;
            }
        }
    };

    private void initData() {
        mControlManager = ControlManager.getInstance(mContext, null);
        mFuncBTOperate = FuncBTOperate.getInstance(mContext);
        //设置标题
        mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.nl_bt_title, R.string.ym_bt_call_log, -1);
        refreshRightText();
    }

    private int position = 0;

    private AdapterView.OnItemClickListener mOIClistener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(true){
                position = i;
                mAdpCallLogList.setmHighlightItem(i);
                if(mAdpCallLogList != null){
                    mAdpCallLogList.notifyDataSetChanged();
                }
                return;
            }
            if(mCallLogRecordsList.size() > i) {
                String dialNumber = mCallLogRecordsList.get(i).getNumber();
                BtLogger.e(TAG, "通话记录界面－拨打" + dialNumber);
                mControlManager.dial(dialNumber);
            }
        }
    };

    private View.OnClickListener mOCListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mFuncBTOperate.showmBTClearDialog(getActivity(), R.string.nl_bt_cautions, R.string.ym_bt_delete_all_call_log, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mFuncBTOperate.showStatus(R.string.ym_bt_clear_list_msg);
                    clearList();
                    //清理通话记录数据库
                    mDelAllCLogHandler.sendEmptyMessage(0);
                    mFuncBTOperate.dismissmBTClearDialog();
                }
            });
        }
    };

    private Handler mDelAllCLogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    BtLogger.e(TAG, "清空通话记录");
                    mFuncBTOperate.deleteAllCallLogRecords();
                    break;
            }
        }
    };

    private Handler mDelSingleCLogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    BtLogger.e(TAG, "删除单条通话记录--position="+position);
                    if(position < mCallLogRecordsList.size()){
                        String number = mCallLogRecordsList.get(position).getNumber();;
                        mFuncBTOperate.deleteSingleCallLogRecord(number);
                    }
                    break;
            }
        }
    };

    private Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    BtLogger.d(TAG, "ReadPhoneBookThread start");
                    mCallLogRecordsList = mFuncBTOperate.getCallLogRecords();
                    BtLogger.d(TAG, "ReadPhoneBookThread end");
                    if(mCallLogRecordsList != null && mAdpCallLogList != null) {
                        mAdpCallLogList.setCallLogList(mCallLogRecordsList);
                    }
                    break;
            }
        }
    };

    private Handler mShowTypeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    BtLogger.d(TAG, "ReadPhoneBookThread start---showType="+showType);
                    mCallLogRecordsList = mFuncBTOperate.getCallLogRecordsByType(showType);
                    BtLogger.d(TAG, "ReadPhoneBookThread end");
                    if(mCallLogRecordsList != null && mAdpCallLogList != null) {
                        mAdpCallLogList.setCallLogList(mCallLogRecordsList);
                    }
                    break;
            }
        }
    };

    public void syncCallLogs() {
        //蓝牙打开,且连接当前设备才同步,否则清空
        if(mControlManager.isEnabled() && !mControlManager.isNotInit()
                && mControlManager.getConnectionState(DataStatic.mCurrentBT) == BluetoothHfDevice.STATE_CONNECTED) {
            BtLogger.d(TAG, "同步通话记录");
            mShowTypeHandler.sendEmptyMessage(0);
        }else{
            clearList();
        }
    }

    private void clearList(){
        if(mCallLogRecordsList != null && mAdpCallLogList != null){
            mCallLogRecordsList.clear();
            mAdpCallLogList.setCallLogList(mCallLogRecordsList);
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
        return R.string.ym_bt_call_log;
    }

    @Override
    public void setListener() {}

    @Override
    public void register() {}

    @Override
    public void unregister() {}
}
