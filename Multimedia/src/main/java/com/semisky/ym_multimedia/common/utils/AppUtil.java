package com.semisky.ym_multimedia.common.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.SoundEffectConstants;
import android.view.View;

public class AppUtil {
	/** 判断某个界面是否在前台 */
	public static boolean isForeground(Context context, String className) {
		if (context == null || TextUtils.isEmpty(className)) {
			return false;
		}

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = activityManager
				.getRunningTasks(1);
		if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
			ComponentName cpn = runningTaskInfos.get(0).topActivity;
			if (className.equals(cpn.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/** 跳转应用（进入主Activity） */
	public static void startActvity(Context context, String packageName) {
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(
				packageName);
		if (intent != null) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} else {
			Logger.logE("Unknow packageName!");
		}
	}

	/** 跳转应用（进入对应Activity） */
	public static void startActivity(Context context, String packageName,
			String className) {
		Intent intent = new Intent();
		intent.setClassName(packageName, className);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/** 跳转应用（进入对应Activity） */
	public static void startActivity(Context context, String packageName,
			String className, String firstKey, String firstValue) {
		Intent intent = new Intent();
		intent.setClassName(packageName, className);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(firstKey, firstValue);
		context.startActivity(intent);
	}

	/** 跳转Activity */
	public static void startActivity(Context context, Class<?> cls) {
		Intent intent = new Intent(context, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/** 跳转Activity */
	public static void startActivity(Context context, Class<?> cls,
			String firstKey, String firstValue) {
		Intent intent = new Intent(context, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(firstKey, firstValue);
		context.startActivity(intent);
	}

	/** 回到主界面 */
	public static void backHome(Context context) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		context.startActivity(intent);
	}

	/** 点击音 */
	public static void playClickSound(View view) {
		view.playSoundEffect(SoundEffectConstants.CLICK);
	}

	/** 四舍五入取整 */
	public static int roundOff(float f) {
		return new BigDecimal(f).setScale(0, BigDecimal.ROUND_HALF_UP)
				.intValue();
	}

	/**
	 * 保留一位小数
	 * 
	 * @param decimal
	 * @return
	 */
	public static float formatOneDecimal(float decimal) {
		return Float.parseFloat(new DecimalFormat(".0").format(decimal));
	}

	/** 计算导航混音最低比例 */
	public static float calNavMixLowestRatio(Context context) {
		int ratio = Settings.System.getInt(context.getContentResolver(),
				"semisky_car_navmixing", 7);// 默认7
		return (88 - (ratio - 1) * 8) / 100f;
	}
}
