package com.semisky.ym_multimedia.music.bean;

/**
 * 歌曲实体类
 * 
 * @author Anter
 * 
 */
public class MusicInfo {
	// 路径
	private String uri;
	// 歌曲名（包含后缀）
	private String displayName;
	// 歌曲名（不包含后缀）
	private String title;
	// 演唱者
	private String artist;
	// 专辑
	private String album;
	// 时长
	private int duration;

	public MusicInfo() {

	}

	public MusicInfo(String uri, String displayName, String title,
			String artist, String album, int duration) {
		super();
		this.uri = uri;
		this.displayName = displayName;
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.duration = duration;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

}
