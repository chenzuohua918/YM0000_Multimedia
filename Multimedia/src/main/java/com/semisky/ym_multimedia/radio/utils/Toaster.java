package com.semisky.ym_multimedia.radio.utils;

import android.content.Context;
import android.widget.Toast;

public class Toaster {
	private static boolean OPEN_TOAST = true;

	public static void makeText(Context context, String text) {
		if (OPEN_TOAST) {
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
		}
	}

	public static void makeText(Context context, int resId) {
		if (OPEN_TOAST) {
			Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
		}
	}
}
