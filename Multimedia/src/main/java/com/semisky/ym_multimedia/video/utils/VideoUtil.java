package com.semisky.ym_multimedia.video.utils;

import java.lang.reflect.Method;

import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

public class VideoUtil {
	/** 关闭倒车的RearCamera，释放视频资源 */
	public static void closedRearCamera() {
		// 直接调系统
		android.hardware.RearCamera.enableCM3Control();
		android.hardware.RearCamera.stopEarlyCamera();
		android.hardware.RearCamera.disableCM3Control();
	}

	/** 行车禁止播放视频开关是否打开 */
	public static boolean isSpeedWarningOpened(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
				"ym0000_Settings_car_running_video", 0) == 1;
	}
}
