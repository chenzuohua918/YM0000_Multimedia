package com.semisky.ym_multimedia.multimedia.dao;

import android.provider.BaseColumns;

/**
 * 多媒体数据库配置
 * 
 * @author Anter
 * 
 */
public class DBConfiguration {
	public static final String DATABASE_NAME = "YM_Multimedia.db";
	public static final int DATABASE_VERSION = 1;

	public static final String USB_FLAG = "usbFlag";// U盘标识
	public static final String FILE_FLAG = "fileFlag";// 文件类型

	public static final int FLAG_PHOTO = 0;
	public static final int FLAG_MUSIC = 1;
	public static final int FLAG_LYRIC = 2;
	public static final int FLAG_VIDEO = 3;

	/** 图片数据库配置 */
	public static class PhotoConfiguration implements BaseColumns {
		public static final String TABLE_NAME = "photos";// 表名
		public static final String PHOTO_URI = "photoUri";// 图片路径
		public static final String PHOTO_FOLDER_URI = "photoFolderUri";// 所属文件夹路径
		public static final String DEFAULT_SORT_ORDER = PHOTO_URI
				+ " COLLATE LOCALIZED ASC";// 排序方式（本地语言）
	}

	/** 音乐数据库配置 */
	public static class MusicConfiguration implements BaseColumns {
		public static final String TABLE_NAME = "musics";// 表名
		public static final String MUSIC_URI = "musicUri";// 音乐路径
		public static final String MUSIC_FOLDER_URI = "musicFolderUri";// 所属文件夹路径
		public static final String DEFAULT_SORT_ORDER = MUSIC_URI
				+ " COLLATE LOCALIZED ASC";// 排序方式（本地语言）
	}

	/** 歌词数据库配置 */
	public static class LyricConfiguration implements BaseColumns {
		public static final String TABLE_NAME = "lyrics";// 表名
		public static final String LYRIC_URI = "lyricUri";// 歌词路径
		public static final String LYRIC_NAME = "lyricName";// 歌词名（不包含后缀）
	}

	/** 视频数据库配置 */
	public static class VideoConfiguration implements BaseColumns {
		public static final String TABLE_NAME = "videos";// 表名
		public static final String VIDEO_URI = "videoUri";// 音乐路径
		public static final String VIDEO_FOLDER_URI = "videoFolderUri";// 所属文件夹路径
		public static final String DEFAULT_SORT_ORDER = VIDEO_URI
				+ " COLLATE LOCALIZED ASC";// 排序方式（本地语言）
	}
}
