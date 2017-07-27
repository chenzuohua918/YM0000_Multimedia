package com.semisky.ym_multimedia.ymbluetooth.func;

import android.content.Context;

/**
 * Created by luoyin on 16/11/8.
 */
public class FuncBase {
	private final String TAG = "FuncBase";
	private static FuncBase instance;
	private Context mContext;

	public FuncBase(Context context) {
		mContext = context;
	}

	public static FuncBase getInstance(Context context) {
		if (instance == null) {
			instance = new FuncBase(context);
		}
		return instance;
	}

	public int dp2px(float dipValue) {
		final float scale = mContext.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public int px2dp(float pxValue) {
		final float scale = mContext.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

}
