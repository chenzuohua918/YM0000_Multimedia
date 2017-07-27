package com.semisky.ym_multimedia.radio.utils;

import java.text.DecimalFormat;

public class RadioUtil {
	/**
	 * 频点是否在规定范围内
	 * 
	 * @param frequency
	 * @return
	 */
	public static boolean inFrequencyRange(int frequency) {
		return inFMFrequencyRange(frequency) || inAMFrequencyRange(frequency);
	}

	/**
	 * 频点是否在当前类型频点范围内
	 * 
	 * @param frequency
	 * @return
	 */
	public static boolean inCurrentFrequencyRange(int frequency) {
		boolean result = false;
		switch (RadioStatus.currentType) {
		case RadioConstants.TYPE_FM:
			result = inFMFrequencyRange(frequency);
			break;
		case RadioConstants.TYPE_AM:
			result = inAMFrequencyRange(frequency);
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * 频点是否在FM规定范围内
	 * 
	 * @param frequency
	 * @return
	 */
	public static boolean inFMFrequencyRange(int frequency) {
		return frequency >= RadioConstants.FMMIN
				&& frequency <= RadioConstants.FMMAX;
	}

	/**
	 * 频点是否在AM规定范围内
	 * 
	 * @param frequency
	 * @return
	 */
	public static boolean inAMFrequencyRange(int frequency) {
		return frequency >= RadioConstants.AMMIN
				&& frequency <= RadioConstants.AMMAX;
	}

	/**
	 * 保留两位小数
	 * 
	 * @param frequency
	 * @return
	 */
	public static String formatFloatFrequency(float frequency) {
		return new DecimalFormat(".00").format(frequency);
	}
}
