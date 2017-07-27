package com.semisky.ym_multimedia.multimedia.utils;

/**
 * 多媒体常量
 * 
 * @author Anter
 * 
 */
public class MultimediaConstants {
	// 当调用intern()方法时，会先检查字符串池中是否存在这个字符串，如果存在则返回这个字符串的引用，否则将这个字符串添加进字符串池中，然后返回这个字符串的引用
	public static final String PATH_USB1 = "/storage/usb0".intern();
	public static final String PATH_USB2 = "/storage/usb1".intern();

	public static final int FLAG_USB1 = 0;
	public static final int FLAG_USB2 = 1;

	public static final int ENJOY_MUSIC = 0;
	public static final int ENJOY_VIDEO = 1;

	public static final String SKIPTAG_PHOTO = "picture".intern();
	public static final String SKIPTAG_MUSIC = "music".intern();
	public static final String SKIPTAG_VIDEO = "video".intern();
	public static final String SKIPTAG_RADIO = "radio".intern();
	public static final String SKIPTAG_BLUETOOTH = "bluetooth".intern();
}
