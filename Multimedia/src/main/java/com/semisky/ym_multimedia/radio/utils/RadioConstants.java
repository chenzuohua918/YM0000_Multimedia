package com.semisky.ym_multimedia.radio.utils;

/**
 * 全局常量
 * 
 * @author Anter
 * @date 2016-10-26
 * 
 */
public class RadioConstants {
	// Fragment标识
	public static final int FLAG_FM_FRAGMENT = 0;
	public static final int FLAG_AM_FRAGMENT = 1;
	// 频道类型
	public static final int TYPE_FM = 0;
	public static final int TYPE_AM = 1;
	// 远近程
	public static final int RANGE_LONG = 0;
	public static final int RANGE_SHORT = 1;
	// FM倍数
	public static final float FM_MULTIPLE = 100f;
	// FM步进
	public static final int FM_STEP = 10;
	// AM步进
	public static final int AM_STEP = 9;

	// FM/AM频率范围
	public static final int FMMAX = 10800;
	public static final int FMMIN = 8750;
	public static final int AMMAX = 1629;
	public static final int AMMIN = 531;
	// 默认频道
	public static final int DEFAULT_FM_FREQUENCY = FMMIN;
	public static final int DEFAULT_AM_FREQUENCY = AMMIN;
	// 搜索超时时间
	public static final int DURATION_SEARCH_TIME_OUT = 30000;
	// 搜索结束码
	public static final int SEARCH_OVER_CODE = 65535;

	// handler msg
	public static final int START_PLAY = 0;
	public static final int STOP_PLAY = 1;
	public static final int OPEN_RADIO_VOLUME = 2;
	public static final int CLOSE_RADIO_VOLUME = 3;
	public static final int FADE_DOWM = 4;
	public static final int FADE_UP = 5;
	public static final int MSG_RADIO_PREVIOUS = 6;
	public static final int MSG_RADIO_NEXT = 7;
	public static final int MSG_SEEK_DEC = 8;
	public static final int MSG_SEEK_INC = 9;
	public static final int MSG_SEARCH_NEAR_STRONG_RADIO = 10;
	public static final int MSG_SWITCH_FRAGMENT = 11;
	public static final int MSG_RADIO_STERO_INFO = 12;
	public static final int MSG_RADIO_SWITCH = 13; //Radio按键切换FM/AM
	public static final int MSG_CANCLE_LONGCLICK = 14; //3秒无操作取消步进功能
	public static final int MSG_CANCLE_SEARCH_TOTAL_SHOW = 15; //3秒取消显示总数
	

	// 广播action
	public static final String ACTION_FM_SEARCH_RESULT = "com.semisky.action.FM_SEARCH_RESULT";// 搜索到信号频段时发来广播
	public static final String ACTION_KEYEVENT_RADIO = "com.semisky.keyevent.RADIO";// 实体Radio按钮按下发来广播
	public static final String ACTION_RADIO_STERO_INFO = "com.semisky.RADIO_STERO_INFO";// 单声道立体音
}
