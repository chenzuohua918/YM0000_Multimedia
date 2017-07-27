package com.semisky.ym_multimedia.radio.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.radio.dao.DBConfiguration;
import com.semisky.ym_multimedia.radio.utils.RadioUtil;
import com.semisky.ym_multimedia.radio.utils.RadioStatus;

public class AMAdapter extends CursorAdapter {

	@SuppressWarnings("deprecation")
	public AMAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.item_number.setText(String.valueOf(cursor.getPosition() + 1));
		int frequency = cursor
				.getInt(cursor
						.getColumnIndex(DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY));
		holder.item_frequency.setText(String.valueOf(frequency));

		if (RadioStatus.currentFrequency == frequency) {// 当前选中
			view.setBackgroundResource(R.color.item_pressed_color);
		} else {// 没选中
			view.setBackgroundResource(R.drawable.item_selector);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(
				R.layout.frequency_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.item_number = (TextView) view.findViewById(R.id.item_number);
		holder.item_frequency = (TextView) view
				.findViewById(R.id.item_frequency);
		view.setTag(holder);
		return view;
	}

	static class ViewHolder {
		TextView item_number;
		TextView item_frequency;
	}

	/**
	 * 获取某频点的位置
	 * 
	 * @param frequency
	 * @return
	 */
	public int getPosition(int frequency) {
		if (RadioUtil.inAMFrequencyRange(frequency)) {
			Cursor cursor = getCursor();
			int pos = -1;
			cursor.moveToPosition(pos);
			while (cursor.moveToNext()) {
				pos++;
				if (frequency <= cursor
						.getInt(cursor
								.getColumnIndex(DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY))) {
					return cursor.getPosition();
				}
				if (pos == getCount() - 1) {
					return pos;
				}
			}
		}
		return 0;
	}
}
