package com.semisky.ym_multimedia.ymbluetooth.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.semisky.ym_multimedia.MainActivity;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;

/**
 * Created by luoyin on 16/10/17.
 */
public class BTPairDialog extends Dialog {
    private final String TAG = "BTPairDialog";
    private static BTPairDialog instance;
    private Button mExitBTN;
    private TextView mTitleTV, mNameTagTV, mNameTV, mPwdTagTV, mPwdTV;
    private Activity mActivity;

    public BTPairDialog(Context context) {
        super(context, R.style.DialogStyle);
        mActivity = (context instanceof Activity) ? (Activity)context : null;
        setCustomDialog();
    }

    public static BTPairDialog getInstance(Context context) {
        if(instance == null) {
            instance = new BTPairDialog(context);
            instance.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            instance.setCanceledOnTouchOutside(true);
            instance.setOnCancelButtonListener();
        }
        return instance;
    }

    private void setCustomDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.ym_bt_dialog_pair, null);
        mTitleTV = (TextView) mView.findViewById(R.id.tv_title);
        mNameTagTV = (TextView) mView.findViewById(R.id.tv_bt_name_tag);
        mNameTV = (TextView) mView.findViewById(R.id.tv_bt_name);
        mPwdTagTV = (TextView) mView.findViewById(R.id.tv_bt_pwd_tag);
        mPwdTV = (TextView) mView.findViewById(R.id.tv_bt_pwd);
        mExitBTN = (Button) mView.findViewById(R.id.iv_bt_exit);
        //这句是添加背景灰色
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount=0.9f;
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND | WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        super.setContentView(mView);
    }

//    public void setText(String t1, String t2){
//        BtLogger.e(TAG, "setCallType-" + t1 + "-" + t2);
//        if(t1 != null){
//            mTitleTV.setText(t1);
//        }
//        if(t2 != null) {
//            mContentTV.setText(t2);
//        }
//    }

    public void setText(int titleId, int nameTagId, String name, int pwdTagId, String pwd){
        BtLogger.e(TAG, "setText-");
        if(titleId != 0){
            mTitleTV.setText(titleId);
        }
        if(nameTagId != 0) {
            mNameTagTV.setText(nameTagId);
        }
        if(name != null) {
            mNameTV.setText(name);
        }
        if(pwdTagId != 0) {
            mPwdTagTV.setText(pwdTagId);
        }
        if(pwd != null) {
            mPwdTV.setText(pwd);
        }
        mExitBTN.setText(R.string.ym_bt_exit);
    }


    /**
     * 被叫挂断响应
     * @param listener
     */
    public void setOnCancelListener(View.OnClickListener listener){
//        mCancelBTN.setOnClickListener(listener);
    }

    public void setOnCancelButtonListener(){
        mExitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                BtLogger.e(TAG, "instance-"+instance);
                BtLogger.e(TAG, "BTPairDialog.this.getOwnerActivity()-"+BTPairDialog.this.getOwnerActivity());
                BtLogger.e(TAG, "instance.getOwnerActivity()-"+instance.getOwnerActivity());
                if(instance != null && instance.getOwnerActivity() != null){
                    instance.getOwnerActivity().finish();
                }
                BtLogger.e(TAG, "mActivity="+mActivity);
                if(mActivity != null){
                    BtLogger.e(TAG, "mActivity-fininsh="+mActivity);
                    mActivity.finish();
                    //这个需要置空，否则下次关闭时变量就不对了
                    instance = null;
                    BtLogger.e(TAG, "mActivity-fininsh-over="+mActivity);
                }
            }
        });
    }
}
