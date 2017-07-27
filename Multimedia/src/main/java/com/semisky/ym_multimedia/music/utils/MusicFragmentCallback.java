package com.semisky.ym_multimedia.music.utils;

/**
 * MusicFragment回调Activity接口
 * 
 * @author Anter
 * 
 */
public interface MusicFragmentCallback {
	// MusicFragment请求Activity绑定MusicPlayService
	void requestBindMusicPlayService();

	// MusicFragment请求Activity解绑MusicPlayService
	void requestUnbindMusicPlayService();
}
