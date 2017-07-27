package com.semisky.ym_multimedia.video.bean;

/**
 * 视频实体类
 * 
 * @author Anter
 * 
 */
public class VideoInfo {
	// 路径
	private String uri;
	// 视频名（包含后缀）
	private String displayName;
	// 视频名（不包含后缀）
	private String title;
	// 总时长
	private int duration;
	// 视频宽度
	private int width;
	// 视频高度
	private int height;
	
	public VideoInfo() {
		
	}

	public VideoInfo(String uri, String displayName, String title,
			int duration, int width, int height) {
		super();
		this.uri = uri;
		this.displayName = displayName;
		this.title = title;
		this.duration = duration;
		this.width = width;
		this.height = height;
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

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}
