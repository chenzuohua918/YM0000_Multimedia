package com.semisky.ym_multimedia.multimedia.utils;

public interface OnScanMediaFileListener {
	// 扫描开始
	void onScanStart(int usbFlag);

	// 每隔一段时间通知刷新媒体列表
	void onNotifyRefreshList(int usbFlag);

	// 扫描到第一个图片文件
	void onScannedFirstPhoto(int usbFlag, String photoUri);

	// 扫描到第一个音乐文件
	void onScannedFirstMusic(int usbFlag, String musicUri);

	// 扫描到第一个视频文件
	void onScannedFirstVideo(int usbFlag, String videoUri);

	// 扫描结束
	void onScanFinish(int usbFlag);
}
