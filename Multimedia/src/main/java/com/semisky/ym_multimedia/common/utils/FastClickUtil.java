package com.semisky.ym_multimedia.common.utils;

public class FastClickUtil {
	private static long lastClickTime;

	/** 是否响应点击事件 */
	public static boolean enableToResponseClick(int period) {
		long startTime = System.currentTimeMillis();
		long time = startTime - lastClickTime;
		if (time > 0 && time < period) {// 在规定时间毫秒内不管点击多少次都只响应一次点击事件
			return false;
		}
		lastClickTime = startTime;
		return true;
	}
}
