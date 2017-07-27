package com.semisky.ym_multimedia.radio.dao;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * 电台数据提供者
 * 
 * @author Anter
 * 
 */
public class ChannelContentProvider extends ContentProvider {
	private static final UriMatcher uriMatcher;
	public static final int FM_LIST_CODE = 1;
	public static final int FM_ITEM_CODE = 2;
	public static final int AM_LIST_CODE = 3;
	public static final int AM_ITEM_CODE = 4;
	private SQLiteOpenHelper helper;
	private static Map<String, String> fmColumnMap = new HashMap<String, String>();
	private static Map<String, String> amColumnMap = new HashMap<String, String>();

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		uriMatcher.addURI(DBConfiguration.AUTHORITY, "fm_channels",
				FM_LIST_CODE);
		uriMatcher.addURI(DBConfiguration.AUTHORITY, "fm_channels/#",
				FM_ITEM_CODE);

		uriMatcher.addURI(DBConfiguration.AUTHORITY, "am_channels",
				AM_LIST_CODE);
		uriMatcher.addURI(DBConfiguration.AUTHORITY, "am_channels/#",
				AM_ITEM_CODE);

		fmColumnMap.put(DBConfiguration.TableFMConfiguration._ID,
				DBConfiguration.TableFMConfiguration._ID);
		fmColumnMap.put(DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY,
				DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY);
		fmColumnMap.put(DBConfiguration.TableFMConfiguration.CHANNEL_SIGNAL,
				DBConfiguration.TableFMConfiguration.CHANNEL_SIGNAL);

		amColumnMap.put(DBConfiguration.TableAMConfiguration._ID,
				DBConfiguration.TableAMConfiguration._ID);
		amColumnMap.put(DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY,
				DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY);
		amColumnMap.put(DBConfiguration.TableAMConfiguration.CHANNEL_SIGNAL,
				DBConfiguration.TableAMConfiguration.CHANNEL_SIGNAL);
	}

	@Override
	public boolean onCreate() {
		helper = new ChannelDBHelper(getContext(),
				DBConfiguration.DATABASE_NAME, null,
				DBConfiguration.DATABASE_VERSION);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase database = helper.getReadableDatabase();
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

		Cursor cursor = null;
		String orderBy = null;
		switch (uriMatcher.match(uri)) {
		case FM_LIST_CODE:// 查整个
			// 设置查询的表
			builder.setTables(DBConfiguration.TableFMConfiguration.TABLE_NAME);
			// 设置投影映射
			builder.setProjectionMap(fmColumnMap);
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = DBConfiguration.TableFMConfiguration.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			cursor = builder
					.query(database,
							null,
							null,
							null,
							null,
							null,
							orderBy,
							String.valueOf(DBConfiguration.TableFMConfiguration.CHANNEL_LIMIT));
			break;
		case FM_ITEM_CODE:// 根据条件查询
			// 设置查询的表
			builder.setTables(DBConfiguration.TableFMConfiguration.TABLE_NAME);
			// 设置投影映射
			builder.setProjectionMap(fmColumnMap);
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = DBConfiguration.TableFMConfiguration.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			cursor = builder
					.query(database,
							projection,
							selection,
							selectionArgs,
							null,
							null,
							orderBy,
							String.valueOf(DBConfiguration.TableFMConfiguration.CHANNEL_LIMIT));
			break;
		case AM_LIST_CODE:// 查整个
			// 设置查询的表
			builder.setTables(DBConfiguration.TableAMConfiguration.TABLE_NAME);
			// 设置投影映射
			builder.setProjectionMap(amColumnMap);
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = DBConfiguration.TableAMConfiguration.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			cursor = builder
					.query(database,
							null,
							null,
							null,
							null,
							null,
							orderBy,
							String.valueOf(DBConfiguration.TableAMConfiguration.CHANNEL_LIMIT));
			break;
		case AM_ITEM_CODE:// 根据条件查询
			// 设置查询的表
			builder.setTables(DBConfiguration.TableAMConfiguration.TABLE_NAME);
			// 设置投影映射
			builder.setProjectionMap(amColumnMap);
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = DBConfiguration.TableAMConfiguration.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			cursor = builder
					.query(database,
							projection,
							selection,
							selectionArgs,
							null,
							null,
							orderBy,
							String.valueOf(DBConfiguration.TableAMConfiguration.CHANNEL_LIMIT));
			break;
		default:
			throw new IllegalArgumentException("fail to query, Unknown URI "
					+ uri.toString());
		}

		if (cursor != null) {
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case FM_LIST_CODE:
			return DBConfiguration.TableFMConfiguration.CONTENT_TYPE_DIR;
		case FM_ITEM_CODE:
			return DBConfiguration.TableFMConfiguration.CONTENT_TYPE_ITEM;
		case AM_LIST_CODE:
			return DBConfiguration.TableAMConfiguration.CONTENT_TYPE_DIR;
		case AM_ITEM_CODE:
			return DBConfiguration.TableAMConfiguration.CONTENT_TYPE_ITEM;
		default:
			throw new RuntimeException("unknown uri " + uri.toString());
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase database = helper.getWritableDatabase();
		Uri insertUri = null;
		long rowId = 0;

		switch (uriMatcher.match(uri)) {
		case FM_LIST_CODE:
			// case FM_ITEM_CODE:
			rowId = database.insert(
					DBConfiguration.TableFMConfiguration.TABLE_NAME, null,
					values);
			if (rowId > 0) {
				insertUri = ContentUris
						.withAppendedId(
								DBConfiguration.TableFMConfiguration.CONTENT_URI,
								rowId);
			}
			break;
		case AM_LIST_CODE:
			// case AM_ITEM_CODE:
			rowId = database.insert(
					DBConfiguration.TableAMConfiguration.TABLE_NAME, null,
					values);
			if (rowId > 0) {
				insertUri = ContentUris
						.withAppendedId(
								DBConfiguration.TableAMConfiguration.CONTENT_URI,
								rowId);
			}
			break;
		default:
			throw new IllegalArgumentException("fail to insert, Unknown URI "
					+ uri.toString());
		}

		getContext().getContentResolver().notifyChange(insertUri, null);
		database.close();
		return insertUri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase database = helper.getWritableDatabase();
		int count = -1;

		switch (uriMatcher.match(uri)) {
		case FM_LIST_CODE:
			count = database
					.delete(DBConfiguration.TableFMConfiguration.TABLE_NAME,
							null, null);
			break;
		case FM_ITEM_CODE:
			count = database.delete(
					DBConfiguration.TableFMConfiguration.TABLE_NAME, selection,
					selectionArgs);
			break;
		case AM_LIST_CODE:
			count = database
					.delete(DBConfiguration.TableAMConfiguration.TABLE_NAME,
							null, null);
			break;
		case AM_ITEM_CODE:
			count = database.delete(
					DBConfiguration.TableAMConfiguration.TABLE_NAME, selection,
					selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("fail to delete, Unknown URI "
					+ uri.toString());
		}

		getContext().getContentResolver().notifyChange(uri, null);
		database.close();
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase database = helper.getWritableDatabase();
		int count = -1;

		switch (uriMatcher.match(uri)) {
		case FM_LIST_CODE:
			count = database.update(
					DBConfiguration.TableFMConfiguration.TABLE_NAME, values,
					null, null);
			break;
		case FM_ITEM_CODE:
			count = database.update(
					DBConfiguration.TableFMConfiguration.TABLE_NAME, values,
					selection, selectionArgs);
			break;
		case AM_LIST_CODE:
			count = database.update(
					DBConfiguration.TableAMConfiguration.TABLE_NAME, values,
					null, null);
			break;
		case AM_ITEM_CODE:
			count = database.update(
					DBConfiguration.TableAMConfiguration.TABLE_NAME, values,
					selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("fail to update, Unknown URI "
					+ uri.toString());
		}

		getContext().getContentResolver().notifyChange(uri, null);
		database.close();
		return count;
	}
}
