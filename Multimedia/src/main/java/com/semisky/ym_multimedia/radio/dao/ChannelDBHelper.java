package com.semisky.ym_multimedia.radio.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.semisky.ym_multimedia.common.utils.Logger;

public class ChannelDBHelper extends SQLiteOpenHelper {

	public ChannelDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Logger.logD("Create channel dbs");
		String createFMChannelTableSQL = "create table "
				+ DBConfiguration.TableFMConfiguration.TABLE_NAME + "("
				+ DBConfiguration.TableFMConfiguration._ID
				+ " integer primary key autoincrement, "
				+ DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY
				+ " integer, "
				+ DBConfiguration.TableFMConfiguration.CHANNEL_SIGNAL
				+ " integer" + ")";
		String createAMChannelTableSQL = "create table "
				+ DBConfiguration.TableAMConfiguration.TABLE_NAME + "("
				+ DBConfiguration.TableAMConfiguration._ID
				+ " integer primary key autoincrement, "
				+ DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY
				+ " integer, "
				+ DBConfiguration.TableAMConfiguration.CHANNEL_SIGNAL
				+ " integer" + ")";
		db.execSQL(createFMChannelTableSQL);
		db.execSQL(createAMChannelTableSQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Logger.logD("Channel db upgrade");
	}

}
