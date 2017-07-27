package com.semisky.ym_multimedia.music.utils;

public class MusicConstants {
	// 循环模式
	public static final int MODE_CIRCLE_FOLDER = 0;// 文件夹循环
	public static final int MODE_CIRCLE_SINGLE = 1;// 单曲循环
	public static final int MODE_RANDOM = 2;// 随机播放
	public static final int MODE_CIRCLE_ALL = 3;// 全部循环

	/** msg.what */

	// MusicPlayHandler(MusicPlayService)
	public static final int MSG_SERVICE_PLAY = 200;// 播放指定位置
	public static final int MSG_SERVICE_START = 201;// 开始播放
	public static final int MSG_SERVICE_PAUSE = 202;// 暂停播放
	public static final int MSG_SERVICE_STOP = 203;// 停止播放
	public static final int MSG_SERVICE_PREVIOUS = 204;// 上一曲
	public static final int MSG_SERVICE_NEXT = 205;// 下一曲
	public static final int MSG_SERVICE_UPDATE_PROGRESS = 206;// 更新播放进度
	public static final int MSG_SERVICE_FADE_UP = 207;// 音量渐变上升
	public static final int MSG_SERVICE_FADE_DOWN = 208;// 音量渐变下降
	public static final int MSG_SERVICE_SUB = 209;// 快退
	public static final int MSG_SERVICE_PLUS = 210;// 快进
}
