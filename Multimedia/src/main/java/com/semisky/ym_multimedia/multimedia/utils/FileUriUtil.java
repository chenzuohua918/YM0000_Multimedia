package com.semisky.ym_multimedia.multimedia.utils;

import java.io.File;

import android.text.TextUtils;

public class FileUriUtil {
	/** 通过Url获取文件的Title */
	public static String getFileTitle(String url) {
		if (TextUtils.isEmpty(url)) {
			return "";
		}

		String endUrl = url.substring(url.lastIndexOf(File.separator) + 1);
		return endUrl.substring(0, endUrl.lastIndexOf("."));
	}
}
