package com.semisky.ym_multimedia.radio.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.semisky.ym_multimedia.radio.bean.Channel;

/**
 * Cursor排序
 * 
 * @author Anter
 * @date 2017-1-4
 * 
 */
public class SortCursor extends CursorWrapper implements Comparator<Channel> {
	private Cursor mCursor;
	private List<Channel> channels = new ArrayList<Channel>();
	private int mPos = 0;

	public SortCursor(Cursor cursor) {
		super(cursor);
		this.mCursor = cursor;
	}

	/**
	 * 按columnName进行排序
	 * 
	 * @param cursor
	 * @param columnName
	 */
	public SortCursor(Cursor cursor, String columnName) {
		super(cursor);
		this.mCursor = cursor;
		if (mCursor != null && mCursor.getCount() > 0) {
			int i = 0;
			int column = cursor.getColumnIndexOrThrow(columnName);
			for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor
					.moveToNext(), i++) {
				Channel channel = new Channel();
				channel.setChannelFrequency(Integer.valueOf(cursor
						.getString(column)));
				channel.setOrder(i);
				channels.add(channel);
			}
		}
		Collections.sort(channels, this);
	}

	@Override
	public int compare(Channel lhs, Channel rhs) {
		int lFrequency = lhs.getChannelFrequency();
		int rFrequency = rhs.getChannelFrequency();
		if (lFrequency < rFrequency) {
			return -1;
		} else if (lFrequency > rFrequency) {
			return 1;
		} else {
			return 0;
		}
	}

	public boolean moveToPosition(int position) {
		if (position >= 0 && position < channels.size()) {
			mPos = position;
			int order = channels.get(position).getOrder();
			return mCursor.moveToPosition(order);
		}
		if (position < 0) {
			mPos = -1;
		}
		if (position >= channels.size()) {
			mPos = channels.size();
		}
		return mCursor.moveToPosition(position);
	}

	public boolean moveToFirst() {
		return moveToPosition(0);
	}

	public boolean moveToLast() {
		return moveToPosition(getCount() - 1);
	}

	public boolean moveToNext() {
		return moveToPosition(mPos + 1);
	}

	public boolean moveToPrevious() {
		return moveToPosition(mPos - 1);
	}

	public boolean move(int offset) {
		return moveToPosition(mPos + offset);
	}

	public int getPosition() {
		return mPos;
	}

}
