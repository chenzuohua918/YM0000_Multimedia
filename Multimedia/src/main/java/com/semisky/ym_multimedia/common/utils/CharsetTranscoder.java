package com.semisky.ym_multimedia.common.utils;

import java.io.File;
import java.io.FileInputStream;

import org.mozilla.universalchardet.UniversalDetector;

import android.text.TextUtils;

/**
 * 字符转码器
 * 
 * @author Anter
 * 
 */
public class CharsetTranscoder {
	/** 获取文件编码格式 */
	public static String getFileIncode(File targetFile) {
		if (targetFile == null || !targetFile.exists()
				|| targetFile.isDirectory()) {
			return null;
		}

		byte[] buf = new byte[4096];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(targetFile);

			// (1)新建字符编码检测器
			UniversalDetector detector = new UniversalDetector(null);

			// (2)检测文件
			int nread;
			while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
				detector.handleData(buf, 0, nread);
			}

			// (3)检测完成
			detector.dataEnd();

			// (4)获取字符编码格式
			String encoding = detector.getDetectedCharset();
			if (encoding == null) {
				// 默认UTF-8
				encoding = "UTF-8";
			}

			// (5)重置
			detector.reset();

			fis.close();
			return encoding;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/** 获取文件编码格式 */
	public static String getFileIncode(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return null;
		}

		return getFileIncode(new File(filePath));
	}

	/** 将乱码转换为正常显示信息 */
	public static String getDefaultEncodeString(String charset) {
		if (TextUtils.isEmpty(charset)) {
			return charset;
		}

		try {
			return android.os.ProtocolManager.getInstance()
					.getDefaultEncodeString(charset);
		} catch (Exception e) {
		}
		return charset;
	}
}
