package com.semisky.ym_multimedia.common.utils;

/**
 * 公用常量类
 * 
 * @author Anter
 * 
 */
public class CommonConstants {
	// 切换Fragment标识
	public static final int FLAG_PHOTO = 0;
	public static final int FLAG_MUSIC = 1;
	public static final int FLAG_VIDEO = 2;
	public static final int FLAG_RADIO = 3;
	public static final int FLAG_BT = 4;

	public static final String KEY_GOTO_BT = "gotoBtTag".intern();// 键
	public static final int GOTO_BT_DEFAULT = 0;// 跳转蓝牙默认
	public static final int GOTO_BT_CENTERCONTROL = 1;// 中控跳转蓝牙
	public static final int GOTO_BT_MUSIC = 2;// 跳转蓝牙音乐

	// 中控按键key值
	public static final int KEY_PREV = 8;// 上一曲
	public static final int KEY_NEXT = 9;// 下一曲
	public static final int KEY_RADIO_SWITCH = 13;// Radio按键
	
	public static final int KEY_MODE_CLICK = 0;// 中控按钮短按模式
	public static final int KEY_MODE_LONG_CLICK = 1;// 中控按钮长按模式
}
