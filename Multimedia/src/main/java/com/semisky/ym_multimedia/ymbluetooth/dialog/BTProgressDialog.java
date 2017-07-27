package com.semisky.ym_multimedia.ymbluetooth.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;

/**
 * Created by luoyin on 16/10/17.
 */
public class BTProgressDialog extends Dialog {
    private final String TAG = "BTProgressDialog";
    private static BTProgressDialog instance;
    private TextView mRateTV, mPromptTV;

    public BTProgressDialog(Context context) {
        super(context, R.style.DialogStyle);
        setCustomDialog();
    }

    public static BTProgressDialog getInstance(Context context) {
        if(instance == null) {
            instance = new BTProgressDialog(context);
            instance.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            instance.setCanceledOnTouchOutside(true);
        }
        return instance;
    }

    private void setCustomDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.ym_bt_dialog_progress, null);
        mRateTV = (TextView) mView.findViewById(R.id.tv_rate);
        mPromptTV = (TextView) mView.findViewById(R.id.tv_prompt);
        //这句是添加背景灰色
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount=0.9f;
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND | WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        super.setContentView(mView);
    }

    public void setText(int promptId, String rate){
        BtLogger.e(TAG, "setText-rate");
        if (promptId != 0) {
            mPromptTV.setText(promptId);
        }

        if(rate != null) {
            mRateTV.setText(rate);
        }
    }
}
