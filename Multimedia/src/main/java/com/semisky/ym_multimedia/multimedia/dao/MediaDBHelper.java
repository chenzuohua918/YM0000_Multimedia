package com.semisky.ym_multimedia.multimedia.dao;

import com.semisky.ym_multimedia.common.utils.Logger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MediaDBHelper extends SQLiteOpenHelper {

	public MediaDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Logger.logD("MediaDBHelper-----------------------onCreate(), 创建数据库，新建表");
		// photo table
		String sql_photo = "create table "
				+ DBConfiguration.PhotoConfiguration.TABLE_NAME + "("
				+ DBConfiguration.PhotoConfiguration._ID
				+ " integer primary key autoincrement, "
				+ DBConfiguration.USB_FLAG + " integer, "
				+ DBConfiguration.FILE_FLAG + " integer, "
				+ DBConfiguration.PhotoConfiguration.PHOTO_URI + " text, "
				+ DBConfiguration.PhotoConfiguration.PHOTO_FOLDER_URI + " text"
				+ ")";
		db.execSQL(sql_photo);
		// music table
		String sql_music = "create table "
				+ DBConfiguration.MusicConfiguration.TABLE_NAME + "("
				+ DBConfiguration.MusicConfiguration._ID
				+ " integer primary key autoincrement, "
				+ DBConfiguration.USB_FLAG + " integer, "
				+ DBConfiguration.FILE_FLAG + " integer, "
				+ DBConfiguration.MusicConfiguration.MUSIC_URI + " text, "
				+ DBConfiguration.MusicConfiguration.MUSIC_FOLDER_URI + " text"
				+ ")";
		db.execSQL(sql_music);
		// lyric table
		String sql_lyric = " create table "
				+ DBConfiguration.LyricConfiguration.TABLE_NAME + "("
				+ DBConfiguration.LyricConfiguration._ID
				+ " integer primary key autoincrement, "
				+ DBConfiguration.USB_FLAG + " integer, "
				+ DBConfiguration.FILE_FLAG + " integer, "
				+ DBConfiguration.LyricConfiguration.LYRIC_URI + " text, "
				+ DBConfiguration.LyricConfiguration.LYRIC_NAME + " text" + ")";
		db.execSQL(sql_lyric);
		// video table
		String sql_video = " create table "
				+ DBConfiguration.VideoConfiguration.TABLE_NAME + "("
				+ DBConfiguration.LyricConfiguration._ID
				+ " integer primary key autoincrement, "
				+ DBConfiguration.USB_FLAG + " integer, "
				+ DBConfiguration.FILE_FLAG + " integer, "
				+ DBConfiguration.VideoConfiguration.VIDEO_URI + " text, "
				+ DBConfiguration.VideoConfiguration.VIDEO_FOLDER_URI + " text"
				+ ")";
		db.execSQL(sql_video);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Logger.logD("MediaDBHelper-----------------------onUpgrade()");
	}
}
