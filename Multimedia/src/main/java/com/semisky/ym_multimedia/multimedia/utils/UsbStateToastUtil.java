package com.semisky.ym_multimedia.multimedia.utils;

import com.semisky.ym_multimedia.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 吐司工具类
 * 
 * @author Anter
 * 
 */
public class UsbStateToastUtil {
	private static final boolean OPEN_TOAST = true;

	@SuppressLint("InflateParams")
	private static void makeUsbToast(Context context, int resid) {
		View contentView = LayoutInflater.from(context).inflate(
				R.layout.layout_usb_toast, null);
		TextView tv_usb_toast = (TextView) contentView
				.findViewById(R.id.tv_usb_toast);
		tv_usb_toast.setText(resid);
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(contentView);
		toast.show();
	}

	/** 弹出U盘插入吐司 */
	public static void showUsbMountedToast(Context context) {
		if (OPEN_TOAST) {
			makeUsbToast(context, R.string.usb_in);
		}
	}

	/** 弹出U盘拔出吐司 */
	public static void showUsbUnMountedToast(Context context) {
		if (OPEN_TOAST) {
			makeUsbToast(context, R.string.usb_out);
		}
	}
}
