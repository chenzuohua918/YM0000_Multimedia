package com.semisky.ym_multimedia.radio.dao;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * ContentProvider属性常量
 * 
 * @author Anter
 * @date 2017-1-4
 * 
 */
public class DBConfiguration {
	public static final String AUTHORITY = "com.semisky.ym_multimedia.radio.dao.database";
	public static final String DATABASE_NAME = "channel.db";
	public static final int DATABASE_VERSION = 1;

	/*
	 * FM电台数据库常量
	 */
	public static class TableFMConfiguration implements BaseColumns {
		public static final String TABLE_NAME = "fm_channels";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ DBConfiguration.AUTHORITY + "/" + TABLE_NAME);
		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.semisky.fmChannelList";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.semisky.fmChannelItem";
		public static final String CHANNEL_FREQUENCY = "frequency";
		public static final String CHANNEL_SIGNAL = "signal";
		public static final String DEFAULT_SORT_ORDER = "signal Desc";
		public static final int CHANNEL_LIMIT = 18;// 查询数据条数限制
	}

	/*
	 * AM电台数据库常量
	 */
	public static class TableAMConfiguration implements BaseColumns {
		public static final String TABLE_NAME = "am_channels";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ DBConfiguration.AUTHORITY + "/" + TABLE_NAME);
		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.semisky.amChannelList";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.semisky.amChannelItem";
		public static final String CHANNEL_FREQUENCY = "frequency";
		public static final String CHANNEL_SIGNAL = "signal";
		public static final String DEFAULT_SORT_ORDER = "signal Desc";
		public static final int CHANNEL_LIMIT = 18;// 查询数据条数限制
	}
}
