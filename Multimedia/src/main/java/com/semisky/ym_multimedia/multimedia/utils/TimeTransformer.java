package com.semisky.ym_multimedia.multimedia.utils;

import java.util.Locale;

/**
 * 时间转换工具
 * 
 * @author Anter
 * 
 */
public class TimeTransformer {
	/**
	 * 将时间转换为时钟格式
	 * 
	 * @param time
	 *            时长（毫秒）
	 * @return
	 */
	public static String getMusicFormatTime(int time) {
		if (time <= 0)
			return "00:00";
		time /= 1000;
		long min = time / 60 % 60;
		long second = time % 60;
		return String.format(Locale.getDefault(), "%02d:%02d", min, second);
	}

	/**
	 * 将时间转换为时钟格式
	 * 
	 * @param time
	 *            时长（毫秒）
	 * @return
	 */
	public static String getVideoFormatTime(int time) {
		if (time <= 0)
			return "0:00:00";
		time /= 1000;
		long min = time / 60 % 60;
		long hour = time / 60 / 60;
		long second = time % 60;
		return String.format(Locale.getDefault(), "%01d:%02d:%02d", hour, min,
				second);
	}
}
