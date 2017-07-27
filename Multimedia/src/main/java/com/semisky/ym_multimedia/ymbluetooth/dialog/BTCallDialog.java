package com.semisky.ym_multimedia.ymbluetooth.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.broadcom.bt.hfdevice.BluetoothClccInfo;
import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventBTService;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;
import com.semisky.ym_multimedia.ymbluetooth.func.ControlManager;
import com.semisky.ym_multimedia.ymbluetooth.func.FuncBTOperate;
import com.semisky.ym_multimedia.ymbluetooth.widget.VerticalTextView;

import java.util.List;

/**
 * Created by luoyin on 16/10/17.
 */
public class BTCallDialog extends Dialog {
    private final String TAG = "BTCallDialog";
    private static BTCallDialog instance;
    private Context mContext;
    private FuncBTOperate mFuncBTOperate;
    private ControlManager mControlManager;

    private String mFirstCallNumber, mSecondCallNumber, mFirstTimeText, mSecondTimeText;
    private String mFirstCallName, mSecondCallName;

//    private int mDialogType = 0;
//    private int mLastDialogType = 0;
//    private boolean mIsNeedSmallDialog = false;
//    private boolean mCheckStart = false;
//    private boolean mIsHidden = false;
//    private boolean mIsPickupByVehicle = false;

    //私密模式切换
    private ImageButton mPrivateModeTV;
    //麦克风静音
    private ImageButton mMuteSwitchBTN;
    //来电扬声器开关
    private ImageButton mSpeakerSwitchBTN;
    //拨号键盘挂断按键
    private ImageButton mKeyHangupBTN;

    //第一个大来电去电弹框控件
    private View mFirstDialogV, mDailMainV;
    private Button mFirstHangupBTN, mFirstRejectBTN, mFirstPickupBTN;
    private TextView mFirstNameTV, mFirstNumberTV, mFirstTimeTV;
//    private CheckBox mFirstKeypadCB;
    //第二个插播弹框控件
    private View mSecDialogV;
    private Button mSecRejectBTN, mSecPickupBTN;
    private TextView mSecNumberTV, mSecTimeTV;
    //第三个切换通话弹框
    private View mSwitchDialogV;
    private View mSwitchTop1V;
    private View mSwitchTop2V;
    private Button mSwitchRejectBTN1, mSwitchPickupBTN1;
    private TextView mSwitchNumberTV1, mSwitchTimeTV1;
    private Button mSwitchRejectBTN2, mSwitchPickupBTN2;
    private TextView mSwitchNumberTV2, mSwitchTimeTV2;
    //第四个小来电弹框
    private View mSmallinDialogV;
    private ImageButton mSmallinRejectBTN, mSmallinPickupBTN;
    private TextView mSmallinNumberTV, mSmallinNameTV, mSmallinTimeTV;
    //第五个小去电／接通弹框
    private View mSmalloutDialogV, mSmalloutTopV;
    private ImageButton mSmalloutHangupBTN;
    private TextView mSmalloutNumberTV, mSmalloutTimeTV;
    //第六个小来电弹框
    private View mKidinDialogV;
    private Button mKidinRejectBTN, mKidinPickupBTN;
    private TextView mKidinNumberTV, mKidinTimeTV;
    //第七个小去电／接通弹框
    private View mKidoutDialogV, mKidoutTopV;
    private boolean mIsClick = false;
    private Button mKidoutHangupBTN;
    private TextView mKidoutNumberTV, mKidoutTimeTV;

    public BTCallDialog(Context context) {
        super(context, R.style.DialogStyle);
        mContext = context;
        mFuncBTOperate = FuncBTOperate.getInstance(mContext);
        mControlManager = ControlManager.getInstance(mContext, null);
        initViews();
    }

    public static BTCallDialog getInstance(Context context) {
        if(instance == null) {
            instance = new BTCallDialog(context);
            instance.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            instance.setCanceledOnTouchOutside(true);
        }
        return instance;
    }

    private void initViews() {
        BtLogger.e(TAG, "initViews");
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.ym_bt_dialog_dail, null);
        mPrivateModeTV = (ImageButton) mView.findViewById(R.id.tv_private_mode);
        //初始化音频输出
//        mPrivateModeTV.setTextColor(mContext.getResources().getColor(R.color.white));
        mPrivateModeTV.setBackgroundResource(R.drawable.ym_bt_rightbar_normal);
        mMuteSwitchBTN = (ImageButton) mView.findViewById(R.id.btn_mute_switch);
        //初始化静音状态,默认没有静音
        mMuteSwitchBTN.setTag(false);
        mSpeakerSwitchBTN = (ImageButton) mView.findViewById(R.id.btn_speaker_switch);
        //扬声器默认打开
        mSpeakerSwitchBTN.setTag(true);
        mKeyHangupBTN = (ImageButton) mView.findViewById(R.id.ib_dial_hangup);
        //第一个来电去电弹框控件
        mFirstDialogV = mView.findViewById(R.id.rl_first_dialog);
        mFirstNameTV = (TextView) mView.findViewById(R.id.tv_first_name);
        mFirstNumberTV = (TextView) mView.findViewById(R.id.tv_first_number);
        mFirstTimeTV = (TextView) mView.findViewById(R.id.tv_first_time);
        mFirstHangupBTN = (Button) mView.findViewById(R.id.btn_first_hangup);
        mFirstHangupBTN.setVisibility(View.GONE);
        mFirstRejectBTN = (Button) mView.findViewById(R.id.btn_first_reject);
        mFirstPickupBTN = (Button) mView.findViewById(R.id.btn_first_pickup);
        //第二重弹框（来电插播）控件
        mSecDialogV = mView.findViewById(R.id.rl_second_dialog);
        mSecNumberTV = (TextView) mView.findViewById(R.id.tv_second_number);
        mSecTimeTV = (TextView) mView.findViewById(R.id.tv_second_time);
        mSecRejectBTN = (Button) mView.findViewById(R.id.btn_second_reject);
        mSecPickupBTN = (Button) mView.findViewById(R.id.btn_second_pickup);
        //第三个切换三方通话控件
        mSwitchDialogV = mView.findViewById(R.id.ll_switch_dialog);
        mSwitchTop1V = mView.findViewById(R.id.ll_switch_top1);
        mSwitchTop2V = mView.findViewById(R.id.ll_switch_top2);
        mSwitchNumberTV1 = (TextView) mView.findViewById(R.id.tv_switch_number1);
        mSwitchTimeTV1 = (TextView) mView.findViewById(R.id.tv_switch_time1);
        mSwitchRejectBTN1 = (Button) mView.findViewById(R.id.btn_switch_reject1);
        mSwitchPickupBTN1 = (Button) mView.findViewById(R.id.btn_switch_pickup1);
        mSwitchNumberTV2 = (TextView) mView.findViewById(R.id.tv_switch_number2);
        mSwitchTimeTV2 = (TextView) mView.findViewById(R.id.tv_switch_time2);
        mSwitchRejectBTN2 = (Button) mView.findViewById(R.id.btn_switch_reject2);
        mSwitchPickupBTN2 = (Button) mView.findViewById(R.id.btn_switch_pickup2);
        //第四重弹框－小来电弹框控件
        mSmallinDialogV = mView.findViewById(R.id.rl_smallin_dialog);
        mSmallinNumberTV = (TextView) mView.findViewById(R.id.tv_smallin_number);
        mSmallinNameTV = (TextView) mView.findViewById(R.id.tv_smallin_name);
        mSmallinTimeTV = (TextView) mView.findViewById(R.id.tv_smallin_time);
        mSmallinRejectBTN = (ImageButton) mView.findViewById(R.id.btn_smallin_reject);
        mSmallinPickupBTN = (ImageButton) mView.findViewById(R.id.btn_smallin_pickup);
        //第五重弹框－小去电／接通弹框控件
        mSmalloutDialogV = mView.findViewById(R.id.rl_smallout_dialog);
        mSmalloutNumberTV = (TextView) mView.findViewById(R.id.tv_smallout_number);
        mSmalloutTimeTV = (TextView) mView.findViewById(R.id.tv_smallout_time);
        mSmalloutHangupBTN = (ImageButton) mView.findViewById(R.id.btn_smallout_hangup);
        //上部响应，仅导航
        mSmalloutTopV = mView.findViewById(R.id.ll_smallout_top);
        mSmalloutTopV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtLogger.e(TAG, "mSmalloutTopV-mIsClick=true");
                int mapFlag = android.os.ProtocolManager.getInstance().getAppStatus("MapActivity");
                if(mapFlag == 1){
                    //导航时切换大框
                    mIsClick = true;
                }
            }
        });

        //第六重弹框－小来电弹框控件
        mKidinDialogV = mView.findViewById(R.id.rl_kidin_dialog);
        mKidinNumberTV = (TextView) mView.findViewById(R.id.tv_kidin_number);
        mKidinTimeTV = (TextView) mView.findViewById(R.id.tv_kidin_time);
        mKidinRejectBTN = (Button) mView.findViewById(R.id.btn_kidin_reject);
        mKidinPickupBTN = (Button) mView.findViewById(R.id.btn_kidin_pickup);
        //第七重弹框－小去电／接通弹框控件
        mKidoutDialogV = mView.findViewById(R.id.rl_kidout_dialog);
        mKidoutNumberTV = (TextView) mView.findViewById(R.id.tv_kidout_number);
        mKidoutTimeTV = (TextView) mView.findViewById(R.id.tv_kidout_time);
        mKidoutHangupBTN = (Button) mView.findViewById(R.id.btn_kidout_hangup);
        //上部响应，仅导航（这个是第三方通话的小弹框，点击呼出的是三方通话，呼出切换）
        mKidoutTopV = mView.findViewById(R.id.ll_kidout_top);
        mKidoutTopV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtLogger.e(TAG, "mKidoutTopV-mIsClick=true");
                int mapFlag = android.os.ProtocolManager.getInstance().getAppStatus("MapActivity");
                if(mapFlag == 1){
                    //导航时切换大框
                    mIsClick = true;
                }
            }
        });

        mDailMainV = mView.findViewById(R.id.rl_dial_main);
        mDailMainV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtLogger.e(TAG, "mKidoutTopV-mIsClick=true");
                int mapFlag = android.os.ProtocolManager.getInstance().getAppStatus("MapActivity");
                if(mapFlag == 1){
                    //导航时切换小框
                    mIsClick = false;
                }
            }
        });

        BtLogger.e(TAG, "initViews-mFirstDialogV="+mFirstDialogV);
        BtLogger.e(TAG, "initViews-mSecDialogV="+mSecDialogV);
        //初始化键盘
        initKeypadDialog(mView);
        super.setContentView(mView);
    }

    private void setCallType(int callType){
        BtLogger.e(TAG, "setCallType");
        switch (callType){
            case CallLog.Calls.OUTGOING_TYPE:
            case CallLog.Calls.INCOMING_TYPE:
                mFirstHangupBTN.setVisibility(View.VISIBLE);
                mFirstRejectBTN.setVisibility(View.GONE);
                mFirstPickupBTN.setVisibility(View.GONE);
                //显示键盘按钮
//                mFirstKeypadCB.setVisibility(View.VISIBLE);
                //私密模式按钮在接通时显示
//                mPrivateModeTV.setVisibility(View.VISIBLE);
                break;
            case CallLog.Calls.MISSED_TYPE:
                mFirstHangupBTN.setVisibility(View.GONE);
                mFirstRejectBTN.setVisibility(View.VISIBLE);
                mFirstPickupBTN.setVisibility(View.VISIBLE);
                //隐藏键盘按钮
//                mFirstKeypadCB.setVisibility(View.GONE);
                //私密模式在来电时隐藏
                mPrivateModeTV.setVisibility(View.GONE);
                break;
        }
    }

    private void setFirstCallInfos(String number, int callType){
        BtLogger.e(TAG, "setFirstCallInfos-" + number);
        //设置单通电话数据
        if(number != null){
            mFirstCallNumber = number;
            //同步第一个对话框状态，单按钮／双按钮
            setCallType(callType);
            getFirstName();
        }
    }

    private void saveFirstCallInfos(String number, int callType){
        BtLogger.e(TAG, "saveFirstCallInfos-" + number + "callType=" + callType);
        //设置单通电话数据
        if(number != null){
            //设置第二个号码就同步到保存的数据
            mFuncBTOperate.notifyBTService(18, number, callType);
            //和之前保存的号码不同则保存当前号码
            if(!number.equals(mFuncBTOperate.getDialNumber())){
                mFuncBTOperate.setDialNumber(number);
            }
        }
    }

    private void setSecondCallInfos(String number, int callType){
        BtLogger.e(TAG, "setSecondCallInfos-" + number);
        //设置第三方来电的数据
        if(number != null){
            mSecondCallNumber = number;
            getSecondName();
        }
    }

    private void saveSecondCallInfos(String number, int callType){
        BtLogger.e(TAG, "saveSecondCallInfos-" + number);
        //设置第三方来电的数据
        if(number != null){
            //设置第二个号码就同步到保存的数据
            mFuncBTOperate.notifyBTService(19, number, callType);
            //和之前保存的号码不同则保存当前号码
            if(!number.equals(mFuncBTOperate.getDialNumber())){
                mFuncBTOperate.setDialNumber(number);
            }
        }
    }

    private int timeusedinsec1 = 0;
    private boolean timeStop1 = true;
    private int timeusedinsec2 = 0;
    private boolean timeStop2 = true;
    private Handler mTimeHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    updateTime1();
                    break;
                case 2:
                    updateTime2();
                    break;
                default:
                    break;
            }
        }
    };

    private Handler mNumberHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    mFirstNameTV.setText(mFirstCallName);
                    mFirstNumberTV.setText(mFirstCallNumber);
                    mSmallinNumberTV.setText(mFirstCallNumber);
                    mSmallinNameTV.setText(mFirstCallName);
                    mSmalloutNumberTV.setText(mFirstCallName);
                    mSwitchNumberTV1.setText(mFirstCallName);
                    break;
                case 2:
                    mSecNumberTV.setText(mSecondCallName);
                    mSwitchNumberTV2.setText(mSecondCallName);
                    //三方通话小弹框显示第二个电话号码
                    mKidinNumberTV.setText(mSecondCallName);
                    mKidoutNumberTV.setText(mSecondCallName);
                    break;
                default:
                    break;
            }
        }
    };

    public void startTime1(){
        if(timeStop1){
            timeStop1 = false;
            updateTime1();
            BtLogger.e(TAG, "startTime1");
        }
    }

    public void stopTime1(){
        timeStop1 = true;
        timeusedinsec1 = 0;
    }
    private void updateTime1(){
        if(timeStop1){
            return;
        }
//        BtLogger.e(TAG, "updateTime1");
//        DateFormat.format("HH:mm", "");
        int minute = (int) (timeusedinsec1 / 60)%60;
        int second = (int) (timeusedinsec1 % 60);
        String mint ="00";
        String sec = "00";
        if (minute < 10)
            mint = "0" + minute;
        else
            mint = "" + minute;
        if (second < 10)
            sec = "0" + second;
        else
            sec = "" + second;
        mFirstTimeTV.setText(mint + ":" + sec);
        if(DataStatic.mDialogType == 4){
            mFirstTimeText = mint + ":" + sec;
            mSwitchTimeTV1.setText(mFirstTimeText);
        }
        mSmallinTimeTV.setText(mint + ":" + sec);
        mSmalloutTimeTV.setText(mint + ":" + sec);
        timeusedinsec1++;
        mTimeHandler.sendEmptyMessageDelayed(1, 1000);
    }

    private void startTime2(){
        if (timeStop2){
            timeStop2 = false;
            updateTime2();
            BtLogger.e(TAG, "startTime2");
        }
    }

    public void stopTime2(){
        timeStop2 = true;
        timeusedinsec2 = 0;
    }
    private void updateTime2(){
        if(timeStop2){
            return;
        }
//        BtLogger.e(TAG, "updateTime2");
//        DateFormat.format("HH:mm", "");
        int minute = (int) (timeusedinsec2 / 60)%60;
        int second = (int) (timeusedinsec2 % 60);
        String mint ="00";
        String sec = "00";
        if (minute < 10)
            mint = "0" + minute;
        else
            mint = "" + minute;
        if (second < 10)
            sec = "0" + second;
        else
            sec = "" + second;
        BtLogger.e(TAG, "updateTime2--time="+mint + ":" + sec);
        if(DataStatic.mDialogType == 3) {
            mSecondTimeText = mint + ":" + sec;
            mSwitchTimeTV2.setText(mSecondTimeText);
        }
        //三方通话单独显示第三方时的时间
        mKidinTimeTV.setText(mint + ":" + sec);
        mKidoutTimeTV.setText(mint + ":" + sec);
        timeusedinsec2++;
        mTimeHandler.sendEmptyMessageDelayed(2, 1000);
    }
    /**
     * 拨出挂断响应
     * @param listener
     */
    public void setOnHangupListener(View.OnClickListener listener){
        mKeyHangupBTN.setOnClickListener(listener);
        mFirstHangupBTN.setOnClickListener(listener);
        mSmalloutHangupBTN.setOnClickListener(listener);
    }
    /**
     * 被叫接听响应
     * @param listener
     */
    public void setOnRejectListener(View.OnClickListener listener){
        mFirstRejectBTN.setOnClickListener(listener);
        mSmallinRejectBTN.setOnClickListener(listener);
    }

    /**
     * 被叫挂断响应
     * @param listener
     */
    public void setOnPickupListener(View.OnClickListener listener){
        mFirstPickupBTN.setOnClickListener(listener);
        mSmallinPickupBTN.setOnClickListener(listener);
    }

    public void setOnThreeRejectListener(View.OnClickListener listener){
        mSecRejectBTN.setOnClickListener(listener);
        mSwitchRejectBTN1.setOnClickListener(listener);
        mSwitchRejectBTN2.setOnClickListener(listener);
        //独显第三方插播来电
        mKidinRejectBTN.setOnClickListener(listener);
        mKidoutHangupBTN.setOnClickListener(listener);
    }

    public void setOnThreePickupListener(View.OnClickListener listener){
        mSecPickupBTN.setOnClickListener(listener);
        mSwitchPickupBTN1.setOnClickListener(listener);
        mSwitchPickupBTN2.setOnClickListener(listener);
        //独显第三方插播来电
        mKidinPickupBTN.setOnClickListener(listener);
    }

    public void setOnPrivateModeChangeListener(View.OnClickListener listener){
        mPrivateModeTV.setOnClickListener(listener);
    }

    public void setOnMuteSwitchListener(View.OnClickListener listener){
        mMuteSwitchBTN.setOnClickListener(listener);
    }

    public void setOnSpeakerSwitchListener(View.OnClickListener listener){
        mSpeakerSwitchBTN.setOnClickListener(listener);
    }

    public void changePrivateModeState(boolean isPrivate){
        mPrivateModeTV.setTag(isPrivate);
        //弹框显示则静音500毫秒
        if(isShowing()){
            mControlManager.muteForDepop(900);
        }
        if(isPrivate){
//            mPrivateModeTV.setVText(mContext.getResources().getText(R.string.private_mode_off));
//            mPrivateModeTV.setTextColor(mContext.getResources().getColor(R.color.gray));
            mPrivateModeTV.setBackgroundResource(R.color.ym_bt_bg_blue_normal);
        }else{
//            mPrivateModeTV.setVText(mContext.getResources().getText(R.string.private_mode_on));
//            mPrivateModeTV.setTextColor(mContext.getResources().getColor(R.color.white));
            mPrivateModeTV.setBackgroundResource(R.color.ym_bt_bg_green_normal);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        DataStatic.mDialogType = 0;
//        DataStatic.mLastDialogType = 0;
        DataStatic.mIsNeedSmallDialog = false;
        DataStatic.mCheckStart = false;
        DataStatic.mIsHidden = false;
        DataStatic.mIsOperateByVehicle = true;
        //清空电话状态
        if(mControlManager != null){
            mControlManager.setmBCStateInfo(null);
        }
        //清空控件数据
        mFirstNameTV.setText("");
        mFirstNumberTV.setText("");
        mFirstTimeTV.setText("");
        mSecNumberTV.setText("");
        mSecTimeTV.setText("");
        mSwitchNumberTV1.setText("");
        mSwitchTimeTV1.setText("");
        mSwitchNumberTV2.setText("");
        mSwitchTimeTV2.setText("");
        mSmallinNumberTV.setText("");
        mSmallinNameTV.setText("");
        mSmallinTimeTV.setText("");
        mSmalloutNumberTV.setText("");
        mSmalloutTimeTV.setText("");
        //清空第三方独显时的数据
        mKidinNumberTV.setText("");
        mKidoutNumberTV.setText("");
        mKidinTimeTV.setText("");
        mKidoutTimeTV.setText("");
        dismissBTKeypadDialog();
        //恢复数字键盘弹框按钮状态
        if(!mKeypadDialogV.isShown()){
//            mFirstKeypadCB.setChecked(false);
        }
        //挂断通话后将所有数据清掉
        mFirstTimeText = null;
        mFirstCallNumber = null;
        mFirstCallName = null;
        mSecondTimeText = null;
        mSecondCallNumber = null;
        mSecondCallName = null;
        stopCheck();
        //清空数字键盘输入框数据
        mPhoneNum = "";
        mPhoneNumTV.setText(mPhoneNum);
        mIsClick = false;
    }

    /**
     * 启动倒车/地图轮询
     */
    private void startCheck(){
        if (!DataStatic.mCheckStart){
            DataStatic.mCheckStart = true;
            checkPackingOrMap();
            BtLogger.e(TAG, "startCheck");
        }
    }

    /**
     * 关闭倒车/地图轮询
     */
    private void stopCheck(){
        DataStatic.mCheckStart = false;
    }

    /**
     * 定时300ms倒车/地图轮询
     */
    private void checkPackingOrMap(){

        new Thread(){
            @Override
            public void run() {
                do {
                    if(DataStatic.mCheckStart == false){
                        break;
                    }
                    int parkingFlag = android.os.ProtocolManager.getInstance().getAppStatus("ParkingActivity");
                    int mapFlag = android.os.ProtocolManager.getInstance().getAppStatus("MapActivity");
                    int easylink = android.os.ProtocolManager.getInstance().getAppStatus("EC");
                    //易联界面则不显示弹框
//                    if(easylink == 1){
//                        setHidden(true);
//                    }else{
//                        setHidden(false);
//                    }
                    try {
                        boolean isChanged = false;
                        if(parkingFlag == 1 || (mapFlag == 1 && !mIsClick)){
                            if(!DataStatic.mIsNeedSmallDialog){
                                isChanged = true;
                                DataStatic.mIsNeedSmallDialog = true;
                            }
                        } else {
                            if(DataStatic.mIsNeedSmallDialog){
                                isChanged = true;
                                DataStatic.mIsNeedSmallDialog = false;
                            }
                        }
                        //当倒车和地图状态发生变化时立即刷新弹框状态
                        if(isChanged){
                            //刷新通话弹框
                            mFuncBTOperate.refreshCallDialog();
                            BtLogger.e(TAG, "mIsNeedSmallDialog=" + DataStatic.mIsNeedSmallDialog);
                            BtLogger.e(TAG, "mIsNeedSmallDialog-parkingFlag=" + parkingFlag);
                            BtLogger.e(TAG, "mIsNeedSmallDialog-mapFlag=" + mapFlag);
                        }
                        Thread.sleep(300);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while(true);
            }
        }.start();
    }

    public void switchSingleCall(EventBTService eventBTService){
        List<BluetoothClccInfo> clcc = eventBTService.getClcc();
        if(clcc.size() > 0){
            BluetoothClccInfo clccInfo = clcc.get(0);
            BtLogger.e(TAG, "switchSingleCall－clccInfo-state=" + clccInfo.getCallState());
            BtLogger.e(TAG, "switchSingleCall－clccInfo-direction=" + clccInfo.getCallDirection());
            BtLogger.e(TAG, "switchSingleCall－clccInfo-index=" + clccInfo.getCallIndex());
            BtLogger.e(TAG, "switchSingleCall－clccInfo-mode=" + clccInfo.getCallMode());
            BtLogger.e(TAG, "switchSingleCall－mDialogType=" + DataStatic.mDialogType);
//            BtLogger.e(TAG, "switchSingleCall－mLastDialogType=" + DataStatic.mLastDialogType);
            String myNumber = mFuncBTOperate.getMyNumber();
            String clccNumber = clccInfo.getCallNumber();
            BtLogger.e(TAG, "switchSingleCall－myNumber=" + myNumber);
            if(myNumber.equals(clccNumber)){
                return;
            }
            if (clccNumber == null) {
                clccNumber = mContext.getResources().getText(R.string.ym_bt_unknown).toString();
            }
            int callType = 0;
            if(clccInfo.getCallDirection() == BluetoothHfDevice.CALL_DIRECTION_OUTGOING){
                //去电
                callType = CallLog.Calls.OUTGOING_TYPE;
            } else if(clccInfo.getCallDirection() == BluetoothHfDevice.CALL_DIRECTION_INCOMING){
                //来电
                callType = CallLog.Calls.INCOMING_TYPE;
            }
            if(clccInfo.getCallState() == BluetoothHfDevice.CALL_SETUP_STATE_INCOMING) {
                DataStatic.mIsOperateByVehicle = false;
                DataStatic.mIsNeedSmallDialog = true;
                //来电状态
                callType = CallLog.Calls.MISSED_TYPE;
                mFirstTimeText = mContext.getResources().getString(R.string.ym_bt_incoming);
                if(clccInfo.getCallIndex() == 2) {
                    //切换第三方单通话
                    //停止定时器
                    stopTime1();
                }
            } else if(clccInfo.getCallState() == BluetoothHfDevice.CALL_SETUP_STATE_ALERTING) {
                DataStatic.mIsOperateByVehicle = true;
//                if(!mIsPickupByVehicle){
//                    //切换私密模式
//                    mControlManager.disconnectAudio();
//                    mIsPickupByVehicle = true;
//                }
                //去电未接状态
//                mFirstTimeText = mContext.getResources().getString(R.string.ym_bt_dialing);
                mFirstTimeText = "";
            } else if(clccInfo.getCallState() == BluetoothHfDevice.CALL_STATE_ACTIVE
                    || clccInfo.getCallState() == BluetoothHfDevice.CALL_STATE_HELD) {
                DataStatic.mIsNeedSmallDialog = false;
                //判断是否为车机端操作
                if(!DataStatic.mIsOperateByVehicle){
                    //只有来电时才处理私密状态
//                    if(DataStatic.mDialogType == 0 || DataStatic.mDialogType == 5
//                            //由三方通话来电未接切换为单方来电
//                            || (DataStatic.mDialogType == 1 && DataStatic.mLastDialogType == 7)
//                            //来电由小弹框变为大弹框
//                            || (DataStatic.mDialogType == 1 && DataStatic.mLastDialogType == 5)
//                            //单方来电手机接听
//                            || (DataStatic.mDialogType == 1 && DataStatic.mLastDialogType == 0)){
//                        BtLogger.e(TAG, "disconnectAudio-1－mDialogType=" + DataStatic.mDialogType);
//                        BtLogger.e(TAG, "disconnectAudio-1－mLastDialogType=" + DataStatic.mLastDialogType);
//                        //切换私密模式
//                        mControlManager.disconnectAudio();
//                        DataStatic.mIsOperateByVehicle = true;
//                    }
                    BtLogger.e(TAG, "disconnectAudio-1－mLastCallType=" + DataStatic.mLastCallType);
                    if(DataStatic.mLastCallType == CallLog.Calls.MISSED_TYPE){
                        //切换私密模式
                        mControlManager.disconnectAudio();
                        DataStatic.mIsOperateByVehicle = true;
                    }
                }
                //主动(车机端)挂断响应CALL_STATE_ACTIVE,被动(手机端)挂断响应CALL_STATE_HELD
                //瞬间显示空白,避免显示错误时间
                mFirstTimeText = "";
                //接通状态
                startTime1();
                if(clccInfo.getCallIndex() == 2) {
                    //切换第三方单通话
                    //将第二个的通话时间传给第一个,这个位置会重复进入多次,只能将不为0的值传递出去
                    if(timeusedinsec2 != 0){
                        timeusedinsec1 = timeusedinsec2;
                    }
                    BtLogger.e(TAG, "timeusedinsec1－=" + timeusedinsec1);
                    BtLogger.e(TAG, "timeusedinsec2－=" + timeusedinsec2);
                }
            }

            if(clccInfo.getCallIndex() == 1) {
//                String number = clccInfo.getCallNumber();
                //保存号码
                saveFirstCallInfos(clccNumber, callType);
            }
            if(DataStatic.mIsNeedSmallDialog){
                //来去电小弹框
                switch (callType){
                    case CallLog.Calls.OUTGOING_TYPE:
                    case CallLog.Calls.INCOMING_TYPE:
                        switchDialogType(6);
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        switchDialogType(5);
                        break;
                }
            }else{
                switchDialogType(1);
            }
            //刷新时间栏数据
            refreshTimeTVs();
            //同步号码
            setFirstCallInfos(clccNumber, callType);
            //因为没有第二个电话了，必须停time2
            stopTime2();
            //保存当前电话类型
            DataStatic.mLastCallType = callType;
        }
    }

    private void exchangeCallInfo(){

    }

    public void switch3Calls(EventBTService eventBTService){
        BtLogger.e(TAG, "switch3Calls－mDialogType=" + DataStatic.mDialogType);
        //到这个三方同就统一将计时打开，避免重启计时器未启动问题
        startTime1();
        //根据第三方来电的状态判断通话界面样式
        List<BluetoothClccInfo> clcc = eventBTService.getClcc();
        if(clcc.size() > 1){
            BluetoothClccInfo clccInfo1;
            BluetoothClccInfo clccInfo3;
            //需要判断哪个号码是第三方号码
            if(clcc.get(0).getCallIndex() == 1) {
                clccInfo1 = clcc.get(0);
                clccInfo3 = clcc.get(1);
            } else {
                clccInfo1 = clcc.get(1);
                clccInfo3 = clcc.get(0);
            }
            int callType1 = 0;
            int callType3 = 0;
            String clccNumber1 = clccInfo1.getCallNumber();
            String clccNumber3 = clccInfo3.getCallNumber();
            if (clccNumber1 == null) {
                clccNumber1 = mContext.getResources().getText(R.string.ym_bt_unknown).toString();
            }
            if (clccNumber3 == null) {
                clccNumber3 = mContext.getResources().getText(R.string.ym_bt_unknown).toString();
            }
            BtLogger.e(TAG, "switch3Calls－clccInfo1-number=" + clccNumber1);
            BtLogger.e(TAG, "switch3Calls－clccInfo3-number=" + clccNumber3);
            BtLogger.e(TAG, "switch3Calls－clccInfo1-direction=" + clccInfo1.getCallDirection());
            BtLogger.e(TAG, "switch3Calls－clccInfo3-direction=" + clccInfo3.getCallDirection());
            //必须先获取来去电状态，下一个状态判断如果时未接听才可以设置为－－MISSED_TYPE
            if(clccInfo1.getCallDirection() == BluetoothHfDevice.CALL_DIRECTION_OUTGOING){
                //去电
                callType1 = CallLog.Calls.OUTGOING_TYPE;
            } else if(clccInfo1.getCallDirection() == BluetoothHfDevice.CALL_DIRECTION_INCOMING){
                //来电
                callType1 = CallLog.Calls.INCOMING_TYPE;
            }
            if(clccInfo3.getCallDirection() == BluetoothHfDevice.CALL_DIRECTION_OUTGOING){
                //去电
                callType3 = CallLog.Calls.OUTGOING_TYPE;
            } else if(clccInfo3.getCallDirection() == BluetoothHfDevice.CALL_DIRECTION_INCOMING){
                //来电
                callType3 = CallLog.Calls.INCOMING_TYPE;
            }
            BtLogger.e(TAG, "switch3Calls－clccInfo3-state=" + clccInfo3.getCallState());
            BtLogger.e(TAG, "switch3Calls－clccInfo3-summary=" + clccInfo3.getSummary());
            //这个判断不靠谱，有时第三方来电未接通时，状态会是CALL_STATE_HELD
//            if(clccInfo3.getCallState() == BluetoothHfDevice.CALL_SETUP_STATE_WAITING){
            //上一个状态是单通话时就切来电
            if(DataStatic.mDialogType == 1 || clccInfo3.getCallState() == BluetoothHfDevice.CALL_SETUP_STATE_WAITING) {
                DataStatic.mIsOperateByVehicle = false;
                mSecondTimeText = mContext.getResources().getString(R.string.nl_bt_third_incoming);
                callType3 = CallLog.Calls.MISSED_TYPE;
                if(DataStatic.mIsNeedSmallDialog){
                    //小弹框来电
                    switchDialogType(7);
                }else{
                    //第三方来电
                    switchDialogType(2);
                }
            } else {
                if(clccInfo1.getCallState() == BluetoothHfDevice.CALL_STATE_ACTIVE) {
                    //第三方保持中
                    //只有三方接通时通话才计时
                    startTime2();
                    mSecondTimeText = mContext.getResources().getString(R.string.nl_bt_on_hold);
                    if(DataStatic.mIsNeedSmallDialog){
                        //小弹框通话中
                        switchDialogType(6);
                    }else{
                        switchDialogType(4);
                    }
                } else if(clccInfo1.getCallState() == BluetoothHfDevice.CALL_STATE_HELD) {
                    //判断是否为车机端操作
                    if(!DataStatic.mIsOperateByVehicle){
                        BtLogger.e(TAG, "disconnectAudio-2－mDialogType=" + DataStatic.mDialogType);
//                        BtLogger.e(TAG, "disconnectAudio-2－mLastDialogType=" + DataStatic.mLastDialogType);
                        //第三方来电和第三方来电小弹框时接听才处理私密
//                        if(DataStatic.mDialogType == 2 || DataStatic.mDialogType == 7){
//                            //切换私密模式
//                            mControlManager.disconnectAudio();
//                            DataStatic.mIsOperateByVehicle = true;
//                        }
                        BtLogger.e(TAG, "disconnectAudio-2－mLastCallType3=" + DataStatic.mLastCallType3);
                        if(DataStatic.mLastCallType3 == CallLog.Calls.MISSED_TYPE){
                            //切换私密模式
                            mControlManager.disconnectAudio();
                            DataStatic.mIsOperateByVehicle = true;
                        }
                    }
                    //第三方接通／通话中
                    //只有三方接通时通话才计时
                    startTime2();
                    mFirstTimeText = "保持";
                    if(DataStatic.mIsNeedSmallDialog){
                        //小弹框通话中
                        switchDialogType(8);
                    }else{
                        switchDialogType(3);
                    }
                }
            }
            //刷新时间栏数据
            refreshTimeTVs();
            //同步号码
            setFirstCallInfos(clccNumber1, callType1);
            setSecondCallInfos(clccNumber3, callType3);
            //保存号码
            saveFirstCallInfos(clccNumber1, callType1);
            saveSecondCallInfos(clccNumber3, callType3);
            //保存当前电话类型
            DataStatic.mLastCallType3 = callType3;
        }
    }

    public void setHidden(boolean hidden){
        if(DataStatic.mIsHidden != hidden){
            DataStatic.mIsHidden = hidden;
            //刷新通话弹框
            mFuncBTOperate.refreshCallDialog();
        }
    }

    public void switchDialogType(int type){
        BtLogger.e(TAG, "switchDialogType－type=" + type);
        if(DataStatic.mIsHidden){
            hide();
        }else{
            //弹框就清空号码
            if(mControlManager != null){
                mControlManager.notifyDial(0);
            }
            show();
        }
        startCheck();
        mFirstDialogV.setVisibility(View.GONE);
        mSecDialogV.setVisibility(View.GONE);
        mSwitchDialogV.setVisibility(View.GONE);
        mSmallinDialogV.setVisibility(View.GONE);
        mSmalloutDialogV.setVisibility(View.GONE);
        mPrivateModeTV.setVisibility(View.VISIBLE);
        mKidinDialogV.setVisibility(View.GONE);
        mKidoutDialogV.setVisibility(View.GONE);
        //这句是添加背景灰色
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount=0.9f;
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND | WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        //仅去电和来电小弹框可以进入，其他不响应
        switch (type){
            case 1:
            case 2:
            case 3:
            case 4:
            case 6:
            case 7:
            case 8:
                //大框来去电
                mFirstDialogV.setVisibility(View.VISIBLE);
                break;
//            case 2:
//                //隐藏按键弹框
//                dismissBTKeypadDialog();
//                //第三方来电
//                mFirstDialogV.setVisibility(View.VISIBLE);
//                mSecDialogV.setVisibility(View.VISIBLE);
////                mSecTimeTV.setText("插播来电");
//                break;
//            case 3:
//                //隐藏按键弹框
//                dismissBTKeypadDialog();
//                //第三方通话中
//                mSwitchDialogV.setVisibility(View.VISIBLE);
//                mSwitchPickupBTN1.setVisibility(View.VISIBLE);
//                mSwitchRejectBTN1.setVisibility(View.GONE);
//                mSwitchPickupBTN2.setVisibility(View.GONE);
//                mSwitchRejectBTN2.setVisibility(View.VISIBLE);
//                mSwitchTop1V.setBackgroundResource(R.drawable.ym_bt_intercut_calling_normal);
//                mSwitchTop2V.setBackgroundResource(R.drawable.ym_bt_intercut_calling_selected);
//
//                break;
//            case 4:
//                //三方通话切换原通话
//                mSwitchDialogV.setVisibility(View.VISIBLE);
//                mSwitchPickupBTN1.setVisibility(View.GONE);
//                mSwitchRejectBTN1.setVisibility(View.VISIBLE);
//                mSwitchPickupBTN2.setVisibility(View.VISIBLE);
//                mSwitchRejectBTN2.setVisibility(View.GONE);
//                mSwitchTop1V.setBackgroundResource(R.drawable.ym_bt_intercut_calling_selected);
//                mSwitchTop2V.setBackgroundResource(R.drawable.ym_bt_intercut_calling_normal);
//                break;
            case 5:
                //隐藏按键弹框
                dismissBTKeypadDialog();
                //来电小框
                mSmallinDialogV.setVisibility(View.VISIBLE);
                mPrivateModeTV.setVisibility(View.GONE);
//                getWindow().setBackgroundDrawable(new ColorDrawable(0));
                //以下两种方法都可以将背景设置为透明
                lp.dimAmount=0.0f;
                getWindow().setAttributes(lp);
                //以下这句可以将背景改为纯透明
//                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND | WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                break;
//            case 6:
//                //隐藏按键弹框
//                dismissBTKeypadDialog();
//                //去电小框
//                mSmalloutDialogV.setVisibility(View.VISIBLE);
//                mPrivateModeTV.setVisibility(View.GONE);
////                getWindow().setBackgroundDrawable(new ColorDrawable(0));
//                //以下两种方法都可以将背景设置为透明
//                lp.dimAmount=0.0f;
//                getWindow().setAttributes(lp);
//                //以下这句可以将背景改为纯透明
////                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND | WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                break;
//            case 7:
//                //隐藏按键弹框
//                dismissBTKeypadDialog();
//                //来电小框
//                mKidinDialogV.setVisibility(View.VISIBLE);
//                mPrivateModeTV.setVisibility(View.GONE);
////                getWindow().setBackgroundDrawable(new ColorDrawable(0));
//                //以下两种方法都可以将背景设置为透明
//                lp.dimAmount=0.0f;
//                getWindow().setAttributes(lp);
//                //以下这句可以将背景改为纯透明
////                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND | WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                break;
//            case 8:
//                //隐藏按键弹框
//                dismissBTKeypadDialog();
//                //去电小框
//                mKidoutDialogV.setVisibility(View.VISIBLE);
//                mPrivateModeTV.setVisibility(View.GONE);
////                getWindow().setBackgroundDrawable(new ColorDrawable(0));
//                //以下两种方法都可以将背景设置为透明
//                lp.dimAmount=0.0f;
//                getWindow().setAttributes(lp);
//                //以下这句可以将背景改为纯透明
////                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND | WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                break;
        }
        //恢复数字键盘弹框按钮状态
        if(!mKeypadDialogV.isShown()){
//            mFirstKeypadCB.setChecked(false);
        }
        //只有当对话框变化的时候才赋值
        if(DataStatic.mDialogType != type){
//            DataStatic.mLastDialogType = DataStatic.mDialogType;
            DataStatic.mDialogType = type;
        }
    }

    /**
     * 刷新文本信息
     */
    private void refreshTimeTVs(){
        //呼叫状态数据
        mFirstTimeTV.setText(mFirstTimeText);
        mSmallinTimeTV.setText(mFirstTimeText);
        mSmalloutTimeTV.setText(mFirstTimeText);
        //第三方来电文字
        mSecTimeTV.setText(mSecondTimeText);
        mKidinTimeTV.setText(mSecondTimeText);
        mKidoutTimeTV.setText(mSecondTimeText);
        //同步三方切换文字变化
        mSwitchTimeTV1.setText(mFirstTimeText);
        mSwitchTimeTV2.setText(mSecondTimeText);
    }

    private void getFirstName(){
        new Thread(){
            @Override
            public void run() {
                BtLogger.e(TAG, "第1个电话号码＝" + mFirstCallNumber);
                if (mFirstCallNumber == null) {
                    mFirstCallName = mContext.getResources().getText(R.string.ym_bt_unknown).toString();
                } else {
                    mFirstCallName = mFuncBTOperate.getContactsNameByNumber(mFirstCallNumber);
                    if (mFirstCallName == null) {
                        mFirstCallName = mFirstCallNumber;
                    }
                }
                BtLogger.e(TAG, "第1个电话名字＝" + mFirstCallName);
                Message msg = new Message();
                msg.what = 1;
                mNumberHandler.sendMessage(msg);
            }
        }.start();
    }
    
    private void getSecondName(){
        new Thread(){
            @Override
            public void run() {
                BtLogger.e(TAG, "第2个电话号码＝" + mSecondCallNumber);
                if (mSecondCallNumber == null) {
                    mSecondCallName = mContext.getResources().getText(R.string.ym_bt_unknown).toString();
                } else {
                    mSecondCallName = mFuncBTOperate.getContactsNameByNumber(mSecondCallNumber);
                    if (mSecondCallName == null) {
                        mSecondCallName = mSecondCallNumber;
                    }
                }
                BtLogger.e(TAG, "第2个电话名字＝" + mSecondCallName);
                Message msg = new Message();
                msg.what = 2;
                mNumberHandler.sendMessage(msg);
            }
        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        BtLogger.e(TAG, "call-onKeyDown-" + keyCode);
        //通话界面不响应返回键
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        BtLogger.e(TAG, "call-onKeyUp-" + keyCode);
        //通话界面不响应返回键
        switch (keyCode) {
            case  KeyEvent.KEYCODE_BACK:
                return true;
            //接听
            case KeyEvent.KEYCODE_F3:
                //第三方来电
                if(DataStatic.mDialogType == 2 || DataStatic.mDialogType == 7
                        || DataStatic.mDialogType == 3 || DataStatic.mDialogType == 4){
                    // 保留之前通话，接听当前来电
                    mControlManager.hold(BluetoothHfDevice.SWAP_CALLS);
                }else if(timeStop1 && (mFirstPickupBTN.isShown()
                        || mSmallinPickupBTN.isShown() || mKidinPickupBTN.isShown())){
                    //接听
                    mControlManager.answer();
                }
                break;
            //挂断
            case KeyEvent.KEYCODE_F4:
                //第三方来电
                if(DataStatic.mDialogType == 2 || DataStatic.mDialogType == 7){
                    //第三方来电时方控默认挂断第三方来电
                    // 挂断当前来电/挂断held通话
                    mControlManager.hold(BluetoothHfDevice.HANGUP_HELD);
                }else if(DataStatic.mDialogType == 3 || DataStatic.mDialogType == 4 || DataStatic.mDialogType == 8){
                    //挂断当前通话，切换held通话
                    mControlManager.hold(BluetoothHfDevice.HANGUP_ACTIVE_ACCEPT_HELD);
                }else{
                    //挂断
                    mFuncBTOperate.notifyBTService(11);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    //以下为数字弹框
    private View mKeypadDialogV;
    private TextView mPhoneNumTV;
    private String mPhoneNum = "";
    private Button[] mKeyNumIVs = new Button[12];
    private int[] mKeyNumIDs = {R.id.iv_dial_0, R.id.iv_dial_1, R.id.iv_dial_2,
            R.id.iv_dial_3, R.id.iv_dial_4, R.id.iv_dial_5, R.id.iv_dial_6,
            R.id.iv_dial_7, R.id.iv_dial_8, R.id.iv_dial_9, R.id.iv_dial_star,
            R.id.iv_dial_hash,};

    private void initKeypadDialog(View view) {
        //数字键盘弹框
        mKeypadDialogV = view.findViewById(R.id.ll_keypad_area);
//        mKeypadDialogV.setVisibility(View.VISIBLE);
        //拨号键盘相关控件
        mPhoneNumTV = (TextView) view.findViewById(R.id.tv_dial_phone_num);
        for (int i = 0; i < mKeyNumIDs.length; i++) {
            mKeyNumIVs[i] = (Button) view.findViewById(mKeyNumIDs[i]);
            mKeyNumIVs[i].setOnClickListener(mNumOCListener);
        }
    }

    private void showBTKeypadDialog(){
        mKeypadDialogV.setVisibility(View.VISIBLE);
    }
    private void dismissBTKeypadDialog(){
//        mKeypadDialogV.setVisibility(View.GONE);
    }

    private View.OnClickListener mNumOCListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mKeyNumIDs.length; i++) {
                if(v.getId() == mKeyNumIDs[i]){
                    switch (i){
                        case 10:
                            addPhoneNum("*");
                            break;
                        case 11:
                            addPhoneNum("#");
                            break;
                        default:
                            addPhoneNum(""+i);
                            break;
                    }
                }
            }
        }
    };

    private void addPhoneNum(String input){
        char key = input.charAt(0);
        mControlManager.sendDTMFcode(key);
        mPhoneNum += input;
        mPhoneNumTV.setText(mPhoneNum);
    }

    @Override
    public void onAttachedToWindow() {
        {
            Intent intent = new Intent("com.semisky.nl.currentPage");
            intent.putExtra("app_title", getContext().getString(R.string.ym_bt_dail));
            getContext().sendBroadcast(intent);
        }
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        {
            Intent intent = new Intent("com.semisky.nl.currentPage");
            intent.putExtra("app_title", getContext().getString(R.string.ym_bt_music));
            getContext().sendBroadcast(intent);
        }
        super.onDetachedFromWindow();
    }
}
