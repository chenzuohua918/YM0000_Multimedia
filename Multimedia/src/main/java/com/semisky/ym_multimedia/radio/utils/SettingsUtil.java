package com.semisky.ym_multimedia.radio.utils;

import android.content.Context;
import android.provider.Settings;

/**
 * Settings数据库管理类
 * 
 * @author Anter
 * 
 */
public class SettingsUtil {
	private static SettingsUtil instance;

	public static synchronized SettingsUtil getInstance() {
		if (instance == null) {
			instance = new SettingsUtil();
		}
		return instance;
	}

	// 保存开关状态

	public void setRadioPlayState(Context context, boolean isOpened) {
		Settings.System.putInt(context.getContentResolver(), "radioPlayState",
				isOpened ? 1 : 0);
	}

	public boolean isRadioLatestOpened(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
				"radioPlayState", 1) == 1;
	}
}
