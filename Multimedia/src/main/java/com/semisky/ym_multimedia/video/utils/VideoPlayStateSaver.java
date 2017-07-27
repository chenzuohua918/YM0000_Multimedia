package com.semisky.ym_multimedia.video.utils;

public class VideoPlayStateSaver {
    private static VideoPlayStateSaver instance;
    private boolean fromFileManager;// 是否从文件管理器跳转视频播放
    private String videoUriFromFileManager;// 从文件管理器传来的视频路径

    private VideoPlayStateSaver() {
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new VideoPlayStateSaver();
        }
    }

    public static synchronized VideoPlayStateSaver getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    public boolean isFromFileManager() {
        return fromFileManager;
    }

    public void setFromFileManager(boolean fromFileManager) {
        this.fromFileManager = fromFileManager;
    }

    public String getVideoUriFromFileManager() {
        return videoUriFromFileManager;
    }

    public void setVideoUriFromFileManager(String videoUriFromFileManager) {
        this.videoUriFromFileManager = videoUriFromFileManager;
    }
}
