package com.semisky.ym_multimedia.music.service;

public interface MusicPlayCallback {
	// reset first
	void onResetFirst();

	// 开始准备
	void onPrepareStart(String musicUrl);

	// 音乐文件不存在
	void onMusicNotExist(String musicUrl);

	// Prepare出错
	void onPrepareError(String musicUrl);

	// 更新播放进度
	void onUpdatePlayProgress(int progress);

	// 开始播放
	void onMusicPlay(String musicUrl);

	// 定点播放
	void onSeekToProgress(int progress);

	// 暂停播放
	void onMusicPause();

	// 停止播放
	void onMusicStop();

	// 恢复播放界面显示
	void onRestorePlayState();
}
