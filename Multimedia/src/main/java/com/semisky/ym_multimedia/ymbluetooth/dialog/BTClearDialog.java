package com.semisky.ym_multimedia.ymbluetooth.dialog;

import android.app.Dialog;
import android.content.Context;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.semisky.ym_multimedia.R;

/**
 * Created by luoyin on 16/10/17.
 */
public class BTClearDialog extends Dialog {
    private final String TAG = "BTClearDialog";
    private static BTClearDialog instance;
    private Button mOkBTN, mConfirmBTN, mCancelBTN;
    private TextView mTitleTV, mContentTV;

    public BTClearDialog(Context context) {
        super(context, R.style.DialogStyle);
        setCustomDialog();
    }

    public static BTClearDialog getInstance(Context context) {
        if(instance == null) {
            instance = new BTClearDialog(context);
            instance.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            instance.setCanceledOnTouchOutside(true);
        }
        return instance;
    }

    private void setCustomDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.ym_bt_dialog_clear, null);
        mTitleTV = (TextView) mView.findViewById(R.id.tv_title);
        mContentTV = (TextView) mView.findViewById(R.id.tv_content);
        mOkBTN = (Button) mView.findViewById(R.id.iv_bt_ok);
        mOkBTN.setVisibility(View.GONE);
        mConfirmBTN = (Button) mView.findViewById(R.id.iv_bt_confirm);
        mCancelBTN = (Button) mView.findViewById(R.id.iv_bt_cancel);
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

    public void setText(int titleId, int promptId){
        BtLogger.e(TAG, "setText-id");
        if(titleId != 0){
            mTitleTV.setText(titleId);
        }
        if(promptId != 0) {
            mContentTV.setText(promptId);
        }
        mOkBTN.setText(R.string.ym_bt_ok);
        mConfirmBTN.setText(R.string.ym_bt_confirm);
        mCancelBTN.setText(R.string.ym_bt_cancel);
    }

    /**
     * 拨出挂断响应
     * @param listener
     */
    public void setOnOkListener(View.OnClickListener listener){
        mOkBTN.setOnClickListener(listener);
    }

    /**
     * 被叫接听响应
     * @param listener
     */
    public void setOnConfirmListener(View.OnClickListener listener){
        mConfirmBTN.setOnClickListener(listener);
    }

    /**
     * 被叫挂断响应
     * @param listener
     */
    public void setOnCancelListener(View.OnClickListener listener){
        mCancelBTN.setOnClickListener(listener);
    }

    public void setSingleButton(boolean singleBtn){
        if(singleBtn){
            mConfirmBTN.setVisibility(View.GONE);
            mCancelBTN.setVisibility(View.GONE);
            mOkBTN.setVisibility(View.VISIBLE);
        }else{
            mConfirmBTN.setVisibility(View.VISIBLE);
            mCancelBTN.setVisibility(View.VISIBLE);
            mOkBTN.setVisibility(View.GONE);
        }
    }
}
