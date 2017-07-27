package com.semisky.ym_multimedia.ymbluetooth.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 永久获得焦点TextView
 * @author Administrator
 *
 */
public class MarqueeTextView extends TextView {

	public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MarqueeTextView(Context context) {
		super(context);
	}
	
	@Override
	public boolean isFocused() {
		return true;
	}
	
	

}
