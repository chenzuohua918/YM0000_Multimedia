package com.semisky.ym_multimedia.radio.utils;

public class RadioStatus {
	public static boolean hasFocus = true;// 是否获取了AudioFocus焦点
	public static SearchNearStrongChannel searchNearState = SearchNearStrongChannel.NEITHER;// 搜索邻近强信号台状态（默认既不往前也不往后搜索）
	public static int currentType = RadioConstants.TYPE_FM;// FM或者AM
	public static int rangeType = RadioConstants.RANGE_LONG;// 远程或者近程
	public static int radarTrackStatus = 1;// 单声道或者立体声    0：单声道 1：立体声
	public static int currentFrequency = RadioConstants.FMMIN;// 当前频道值
	public static int searchNearShowingFrequency = RadioConstants.FMMIN;// 搜索上下一个强信号台时显示的频点
	public static boolean isSearchingFM = false;// 正在搜索FM
	public static boolean isSearchingAM = false;// 正在搜索AM
	public static boolean isSearchingInterrupted = false;// 搜索所有FM或AM是否被打断了

	public enum SearchNearStrongChannel {// 搜索邻近的一个强信号台
		PREVIOUS, // 上一个
		NEXT, // 下一个
		NEITHER // 都不是
	}
}
