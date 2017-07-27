package com.semisky.ym_multimedia.common.utils;

import android.util.Log;

/**
 * Log工具类
 * 
 * @author Anter
 * 
 */
public class Logger {
	public static final String TAG = "Multimedia";
	public static final boolean DEBUG = true;

	public static void logD(String msg) {
		if (DEBUG) {
			Log.d(TAG, msg);
		}
	}

	public static void logI(String msg) {
		if (DEBUG) {
			Log.i(TAG, msg);
		}
	}

	public static void logE(String msg) {
		if (DEBUG) {
			Log.e(TAG, msg);
		}
	}

}
