package com.semisky.ym_multimedia.ymbluetooth.EventMsg;

/**
 * Created by luoyin on 16/10/18.
 */
public class EventMain {
    private int method;
    private boolean enabled;
    private long playStatus;
    private String musicName;

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getPlayStatus() {
        return playStatus;
    }

    public void setPlayStatus(long playStatus) {
        this.playStatus = playStatus;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }
}
