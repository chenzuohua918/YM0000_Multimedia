package com.semisky.ym_multimedia.multimedia.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.multimedia.dao.MediaScanner;
import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateToastUtil;

/**
 * 静态广播接收器
 * 
 * @author Anter
 * 
 */
public class MultimediaReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {// 插上U盘
			// 弹吐司
			UsbStateToastUtil.showUsbMountedToast(context
					.getApplicationContext());
			// 获取U盘挂载路径
			String usbPath = intent.getData().getPath();
			if (!TextUtils.isEmpty(usbPath)) {
				Logger.logD("MultimediaReceiver---" + usbPath + "---Mounted");

				if (usbPath.equals(MultimediaConstants.PATH_USB1)) {
					// 通知所有观察者USB1接口有U盘插入
					UsbStateManager.getInstance().notifyAllObserversUsbMounted(
							context.getApplicationContext(),
							MultimediaConstants.FLAG_USB1);
					// 通知MediaScanner类USB1口插入U盘
					MediaScanner.getInstance(context.getApplicationContext())
							.onUsbMounted(MultimediaConstants.FLAG_USB1);
				} else if (usbPath.equals(MultimediaConstants.PATH_USB2)) {
					// 通知所有观察者USB2接口有U盘插入
					UsbStateManager.getInstance().notifyAllObserversUsbMounted(
							context.getApplicationContext(),
							MultimediaConstants.FLAG_USB2);
					// 通知MediaScanner类USB2口插入U盘
					MediaScanner.getInstance(context.getApplicationContext())
							.onUsbMounted(MultimediaConstants.FLAG_USB2);
				}
			}
		} else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {// 拔出U盘
			// 弹吐司
			UsbStateToastUtil.showUsbUnMountedToast(context
					.getApplicationContext());
			// 获取拔出U盘路径
			String usbPath = intent.getData().getPath();
			if (!TextUtils.isEmpty(usbPath)) {
				Logger.logD("MultimediaReceiver---" + usbPath + "---UnMounted");

				if (usbPath.equals(MultimediaConstants.PATH_USB1)) {
					// 通知MediaScanner类USB1口U盘拔出
					MediaScanner.getInstance(context.getApplicationContext())
							.onUsbUnMounted(MultimediaConstants.FLAG_USB1);
					// 通知所有观察者USB1接口U盘拔出
					UsbStateManager.getInstance()
							.notifyAllObserversUsbUnMounted(
									MultimediaConstants.FLAG_USB1);
				} else if (usbPath.equals(MultimediaConstants.PATH_USB2)) {
					// 通知MediaScanner类USB2口U盘拔出
					MediaScanner.getInstance(context.getApplicationContext())
							.onUsbUnMounted(MultimediaConstants.FLAG_USB2);
					// 通知所有观察者USB2接口U盘拔出
					UsbStateManager.getInstance()
							.notifyAllObserversUsbUnMounted(
									MultimediaConstants.FLAG_USB2);
				}
			}
		}
	}

}
