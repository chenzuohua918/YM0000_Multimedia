package com.semisky.ym_multimedia.multimedia.model;

import com.semisky.ym_multimedia.music.utils.OnMusicInfosChangeListener;
import com.semisky.ym_multimedia.photo.utils.OnPhotoInfosChangeListener;
import com.semisky.ym_multimedia.video.utils.OnVideoInfosChangeListener;

import android.database.Cursor;

public interface MediaDataModel {
	/** Photo */

	void addPhotoUri(int usbFlag, String photoUri);// 添加图片信息

	Cursor queryAllPhoto();// 查询所有图片

	Cursor queryPhotoIncludeFolder(String folderUri);// 获取该文件夹下的所有图片（包含含有图片文件的文件夹）

	Cursor queryPhotoExcludeFolder(String folderUri);// 获取该文件夹下的所有图片（不包含含有图片文件的文件夹）

	void queryPhotoDirectUnder(int usbFlag, String folderUri);// 获取该目录下的直属的图片文件及包含图片文件的文件夹（相同的文件夹只添加一次）

	String getPreviousPlayPhotoUri();// 获取上一张要预览的图片Uri

	String getNextPlayPhotoUri();// 获取下一张要预览的图片Uri

	int getPhotoPosition(int usbFlag, String photoUri);// 获取图片在当前列表的位置

	void deletePhotoUri(int usbFlag);// 删除图片信息

	void setOnPhotoInfosChangeListener(OnPhotoInfosChangeListener listener);

	/** Music */

	void addMusicUri(int usbFlag, String musicUri);// 添加音乐信息

	Cursor queryAllMusic();// 查询所有音乐

	Cursor queryMusicIncludeFolder(String folderUri);// 获取该文件夹下的所有音乐（包含含有音乐文件的文件夹）

	Cursor queryMusicExcludeFolder(String folderUri);// 获取该文件夹下的所有音乐（不包含含有音乐文件的文件夹）

	void queryMusicDirectUnder(int usbFlag, String folderUri);// 获取该目录下的直属的音乐文件及包含音乐文件的文件夹（相同的文件夹只添加一次）

	String getMusicPosition(String musicUri);// 获取该音乐文件在它直属的文件夹中的位置（不包含含有音乐文件的文件夹）

	String getPreviousPlayMusicUriByPlayMode();// 获取上一首要播放的歌曲Uri

	String getNextPlayMusicUriByPlayMode(boolean fromUser);// 获取下一首要播放的歌曲Uri

	int getMusicPosition(int usbFlag, String musicUri);// 获取音乐在当前列表的位置

	void deleteMusicUri(int usbFlag);// 删除音乐信息

	void setOnMusicInfosChangeListener(OnMusicInfosChangeListener listener);

	/** Lyric */

	void addLyricUri(int usbFlag, String lyricUri);// 添加歌词信息

	String getLyricUri(String musicUri);// 获取该音乐文件匹配的歌词文件Uri

	void deleteLyricUri(int usbFlag);// 删除歌词信息

	/** Video */

	void addVideoUri(int usbFlag, String videoUri);// 添加视频信息

	Cursor queryAllVideo();// 查询所有视频

	Cursor queryVideoIncludeFolder(String folderUri);// 获取该文件夹下的所有视频（包含含有视频文件的文件夹）

	Cursor queryVideoExcludeFolder(String folderUri);// 获取该文件夹下的所有视频（不包含含有视频文件的文件夹）

	void queryVideoDirectUnder(int usbFlag, String folderUri);// 获取该目录下的直属的视频文件及包含视频文件的文件夹（相同的文件夹只添加一次）

	String getPreviousPlayVideoUriByPlayMode();// 获取上一个要播放的视频Uri

	String getNextPlayVideoUriByPlayMode(boolean fromUser);// 获取下一个要播放的视频Uri

	int getVideoPosition(int usbFlag, String videoUri);// 获取视频在当前列表的位置

	void deleteVideoUri(int usbFlag);// 删除视频信息

	void setOnVideoInfosChangeListener(OnVideoInfosChangeListener listener);

	/** Common */

	void deleteAllMediaUri(int usbFlag);// 删除所有多媒体信息
}
