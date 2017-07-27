package com.semisky.ym_multimedia.video.utils;

public class VideoConstants {
	// 循环模式
	public static final int MODE_CIRCLE_FOLDER = 0;// 文件夹循环
	public static final int MODE_CIRCLE_SINGLE = 1;// 单曲循环
	public static final int MODE_RANDOM = 2;// 随机播放
	public static final int MODE_CIRCLE_ALL = 3;// 全部循环

	/** msg.what */

	// VideoHandler(VideoFragment)
	public static final int MSG_VIDEO_PLAY = 300;// 播放指定位置
	public static final int MSG_VIDEO_START = 301;// 开始播放
	public static final int MSG_VIDEO_PAUSE = 302;// 暂停播放
	public static final int MSG_VIDEO_STOP = 303;// 停止播放
	public static final int MSG_VIDEO_PREVIOUS = 304;// 上一个视频
	public static final int MSG_VIDEO_NEXT = 305;// 下一个视频
	public static final int MSG_VIDEO_UPDATE_PROGRESS = 306;// 更新播放进度
	public static final int MSG_VIDEO_GET_SPEED = 307;// 获取车速
	public static final int MSG_VIDEO_SUB = 308;// 快退
	public static final int MSG_VIDEO_PLUS = 309;// 快进
}
