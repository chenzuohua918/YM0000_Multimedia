package com.semisky.ym_multimedia.radio.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.semisky.ym_multimedia.radio.bean.Channel;
import com.semisky.ym_multimedia.radio.utils.RadioConstants;

/**
 * AM电台数据库管理类
 * 
 * @author Anter
 * 
 */
public class AMChannelDBManager {
	private static AMChannelDBManager instance;
	private Context mContext;

	public static synchronized AMChannelDBManager getInstance(Context context) {
		if (instance == null) {
			instance = new AMChannelDBManager(context);
		}
		return instance;
	}

	public AMChannelDBManager(Context context) {
		this.mContext = context;
	}

	/**
	 * 是否数据库中有该频道
	 * 
	 * @return
	 */
	public boolean hasAMChannels() {
		Cursor cursor = mContext.getContentResolver().query(
				DBConfiguration.TableAMConfiguration.CONTENT_URI, null, null,
				null, null);
		boolean result = false;
		if (cursor != null) {
			result = cursor.getCount() != 0;
		}
//		cursor.close();
		return result;
	}

	/**
	 * 查询频道数据
	 * 
	 * @param channelFrequency
	 * @return
	 */
	public Channel queryAMChannel(int channelFrequency) {
		Cursor cursor = mContext.getContentResolver().query(
				ContentUris.withAppendedId(
						DBConfiguration.TableAMConfiguration.CONTENT_URI,
						ChannelContentProvider.AM_ITEM_CODE), null,
				DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY + "=?",
				new String[] { String.valueOf(channelFrequency) }, null);
		Channel channel = null;
		if (cursor != null) {
			while (cursor.moveToNext()) {
				channel = new Channel();
				channel.setChannelFrequency(cursor.getInt(cursor
						.getColumnIndex(DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY)));
				channel.setChannelSignal(cursor.getInt(cursor
						.getColumnIndex(DBConfiguration.TableAMConfiguration.CHANNEL_SIGNAL)));
				channel.setChannelType(RadioConstants.TYPE_AM);
				break;
			}
		}
//		cursor.close();
		return channel;
	}

	/**
	 * 是否已经收藏了该频道
	 * 
	 * @param channelFrequency
	 * @return
	 */
	public boolean isChannelInDB(int channelFrequency) {
		return queryAMChannel(channelFrequency) != null;
	}

	/**
	 * 查询频道数据
	 * 
	 * @return
	 */
	public Cursor getAMChannelsCursor() {
		return mContext.getContentResolver().query(
				DBConfiguration.TableAMConfiguration.CONTENT_URI, null, null,
				null, null);
	}

	/**
	 * 查询频道数据
	 * 
	 * @return
	 */
	public List<Channel> queryAMChannels() {
		List<Channel> channels = new ArrayList<Channel>();
		Cursor cursor = mContext.getContentResolver().query(
				DBConfiguration.TableAMConfiguration.CONTENT_URI, null, null,
				null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				Channel channel = new Channel();
				channel.setChannelFrequency(cursor.getInt(cursor
						.getColumnIndex(DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY)));
				channel.setChannelSignal(cursor.getInt(cursor
						.getColumnIndex(DBConfiguration.TableAMConfiguration.CHANNEL_SIGNAL)));
				channel.setChannelType(RadioConstants.TYPE_AM);
				channels.add(channel);
			}
		}
//		cursor.close();
		return channels;
	}

	/**
	 * 更新频道信息
	 * 
	 * @param channel
	 * @return
	 */
	public boolean updateAMChannel(Channel channel) {
		if (!isChannelInDB(channel.getChannelFrequency())) {// 如果不在表里面，更新无从谈起
			return false;
		}

		ContentValues values = new ContentValues();
		values.put(DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY,
				String.valueOf(channel.getChannelFrequency()));
		values.put(DBConfiguration.TableAMConfiguration.CHANNEL_SIGNAL,
				channel.getChannelSignal());
		int result = mContext.getContentResolver().update(
				ContentUris.withAppendedId(
						DBConfiguration.TableAMConfiguration.CONTENT_URI,
						ChannelContentProvider.AM_ITEM_CODE), values,
				DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY + "=?",
				new String[] { String.valueOf(channel.getChannelFrequency()) });
		return result != -1;
	}

	/**
	 * 插入频道信息
	 * 
	 * @param channelName
	 * @param channelFrequency
	 * @param channelSignal
	 */
	public void insertAMChannel(String channelName, int channelFrequency,
			int channelSignal) {
		if (isChannelInDB(channelFrequency)) {// 如果已经插入，则只更新
			Channel channel = new Channel();
			channel.setChannelFrequency(channelFrequency);
			channel.setChannelSignal(channelSignal);
			updateAMChannel(channel);
		} else {
			ContentValues values = new ContentValues();
			values.put(DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY,
					String.valueOf(channelFrequency));
			values.put(DBConfiguration.TableAMConfiguration.CHANNEL_SIGNAL,
					channelSignal);
			mContext.getContentResolver().insert(
					DBConfiguration.TableAMConfiguration.CONTENT_URI, values);
		}
	}

	/**
	 * 插入频道信息
	 * 
	 * @param channel
	 */
	public void insertAMChannel(Channel channel) {
		ContentValues values = new ContentValues();
		values.put(DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY,
				channel.getChannelFrequency());
		values.put(DBConfiguration.TableAMConfiguration.CHANNEL_SIGNAL,
				channel.getChannelSignal());
		if (isChannelInDB(channel.getChannelFrequency())) {// 如果已经插入，则只更新
			updateAMChannel(channel);
		} else {
			mContext.getContentResolver().insert(
					DBConfiguration.TableAMConfiguration.CONTENT_URI, values);
		}
	}

	/**
	 * 删除所有频道
	 */
	public void deleteAllAMChannels() {
		mContext.getContentResolver().delete(
				DBConfiguration.TableAMConfiguration.CONTENT_URI, null, null);
	}

	/**
	 * 根据频率删除频道
	 * 
	 * @param frequency
	 */
	public void deleteAMChannel(int frequency) {
		mContext.getContentResolver().delete(
				ContentUris.withAppendedId(
						DBConfiguration.TableAMConfiguration.CONTENT_URI,
						ChannelContentProvider.AM_ITEM_CODE),
				DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY + "=?",
				new String[] { String.valueOf(frequency) });
	}
}
