package com.semisky.ym_multimedia.music.utils;

public class MusicPlayStateSaver {
    private static MusicPlayStateSaver instance;
    private boolean isPrepared;// 音乐是否准备完成，随时可播放
    private boolean fromFileManager;// 是否从文件管理器跳转音乐播放
    private String musicUriFromFileManager;// 从文件管理器传来的音乐路径

    private MusicPlayStateSaver() {
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new MusicPlayStateSaver();
        }
    }

    public static synchronized MusicPlayStateSaver getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public void setPrepared(boolean isPrepared) {
        this.isPrepared = isPrepared;
    }

    public boolean isFromFileManager() {
        return fromFileManager;
    }

    public void setFromFileManager(boolean fromFileManager) {
        this.fromFileManager = fromFileManager;
    }

    public String getMusicUriFromFileManager() {
        return musicUriFromFileManager;
    }

    public void setMusicUriFromFileManager(String musicUriFromFileManager) {
        this.musicUriFromFileManager = musicUriFromFileManager;
    }
}
