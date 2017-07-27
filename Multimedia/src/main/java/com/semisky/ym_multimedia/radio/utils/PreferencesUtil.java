package com.semisky.ym_multimedia.radio.utils;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences管理类
 * 
 * @author Administrator
 * @date 2016-10-26
 * 
 */
public class PreferencesUtil {
	private static WeakReference<PreferencesManager> mReference;

	public static synchronized PreferencesManager getInstance() {
		if (mReference == null || mReference.get() == null) {
			mReference = new WeakReference<PreferencesManager>(new PreferencesManager());
		}
		return mReference.get();
	}

	public static class PreferencesManager {
		private static final String FILE_NAME = "YM_Radio";

		private SharedPreferences getSP(Context context) {
			return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		}

		// 保存最近Radio信息

		public void saveLatestRadioInfo(Context context, int frequency, int type) {
			setLatestRadioFrequency(context, frequency, type);
			setLatestRadioType(context, type);
		}

		// Radio类型（FM/AM）

		public int getLatestRadioType(Context context) {
			return getSP(context).getInt("latestRadioType", RadioConstants.TYPE_FM);
		}

		public boolean setLatestRadioType(Context context, int type) {
			return getSP(context).edit().putInt("latestRadioType", type).commit();
		}

		// Radio频段

		public int getLatestRadioFrequency(Context context) {
			int type = getLatestRadioType(context);
			if (type == RadioConstants.TYPE_FM) {// FM
				return getLatestFMRadioFrequency(context);
			} else {// AM
				return getLatestAMRadioFrequency(context);
			}
		}

		public boolean setLatestRadioFrequency(Context context, int frequency, int type) {
			if (type == RadioConstants.TYPE_FM) {// FM
				return setLatestFMRadioFrequency(context, frequency);
			} else {// AM
				return setLatestAMRadioFrequency(context, frequency);
			}
		}

		public int getLatestFMRadioFrequency(Context context) {
			return getSP(context).getInt("latestFMRadioFrequency", RadioConstants.DEFAULT_FM_FREQUENCY);
		}

		public boolean setLatestFMRadioFrequency(Context context, int frequency) {
			return getSP(context).edit().putInt("latestFMRadioFrequency", frequency).commit();
		}

		public int getLatestAMRadioFrequency(Context context) {
			return getSP(context).getInt("latestAMRadioFrequency", RadioConstants.DEFAULT_AM_FREQUENCY);
		}

		public boolean setLatestAMRadioFrequency(Context context, int frequency) {
			return getSP(context).edit().putInt("latestAMRadioFrequency", frequency).commit();
		}

		public int getMemoryFragmentFlag(Context context) {
			return getSP(context).getInt("memoryFragmentFlag", RadioConstants.TYPE_FM);
		}

		public boolean setMemoryFragmentFlag(Context context, int flag) {
			return getSP(context).edit().putInt("memoryFragmentFlag", flag).commit();
		}

		// 远近程
		public int getLongOrShortRange(Context context) {
			return getSP(context).getInt("rangeFlag", RadioConstants.RANGE_LONG);
		}

		public boolean setLongOrShortRange(Context context, int flag) {
			return getSP(context).edit().putInt("rangeFlag", flag).commit();
		}
	}
}
