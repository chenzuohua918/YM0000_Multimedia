package com.semisky.ym_multimedia.music.model;

import android.content.Context;

import com.semisky.ym_multimedia.multimedia.utils.PreferencesUtil;
import com.semisky.ym_multimedia.music.utils.MusicConstants;

/**
 * 音频播放模式切换Model
 *
 * @author Anter
 */
public class MusicPlayModeChangeModel {
    private static MusicPlayModeChangeModel instance;
    private Context mContext;
    private OnMusicPlayModeChangeListener onMusicPlayModeChangeListener;

    private MusicPlayModeChangeModel(Context context) {
        this.mContext = context;
    }

    private static synchronized void syncInit(Context context) {
        if (instance == null) {
            instance = new MusicPlayModeChangeModel(context);
        }
    }

    public static MusicPlayModeChangeModel getInstance(Context context) {
        if (instance == null) {
            syncInit(context);
        }
        return instance;
    }

    public void setOnMusicPlayModeChangeListener(OnMusicPlayModeChangeListener
                                                         onMusicPlayModeChangeListener) {
        this.onMusicPlayModeChangeListener = onMusicPlayModeChangeListener;
    }

    /**
     * 切换音频播放模式
     */
    public void changePlayMode() {
        switch (PreferencesUtil.getInstance().getMusicPlayMode()) {
            case MusicConstants.MODE_CIRCLE_FOLDER:// 文件夹循环
                PreferencesUtil.getInstance().setMusicPlayMode(MusicConstants.MODE_CIRCLE_SINGLE);
                if (onMusicPlayModeChangeListener != null) {
                    onMusicPlayModeChangeListener.onMusicPlayModeChanged(MusicConstants
                            .MODE_CIRCLE_SINGLE);
                }
                break;
            case MusicConstants.MODE_CIRCLE_SINGLE:// 单曲循化
                PreferencesUtil.getInstance().setMusicPlayMode(MusicConstants.MODE_RANDOM);
                if (onMusicPlayModeChangeListener != null) {
                    onMusicPlayModeChangeListener.onMusicPlayModeChanged(MusicConstants
                            .MODE_RANDOM);
                }
                break;
            case MusicConstants.MODE_RANDOM:// 随机播放
                PreferencesUtil.getInstance().setMusicPlayMode(MusicConstants.MODE_CIRCLE_ALL);
                if (onMusicPlayModeChangeListener != null) {
                    onMusicPlayModeChangeListener.onMusicPlayModeChanged(MusicConstants
                            .MODE_CIRCLE_ALL);
                }
                break;
            case MusicConstants.MODE_CIRCLE_ALL:// 全部循环
                PreferencesUtil.getInstance().setMusicPlayMode(MusicConstants.MODE_CIRCLE_FOLDER);
                if (onMusicPlayModeChangeListener != null) {
                    onMusicPlayModeChangeListener.onMusicPlayModeChanged(MusicConstants
                            .MODE_CIRCLE_FOLDER);
                }
                break;
            default:
                break;
        }
    }

    public interface OnMusicPlayModeChangeListener {
        void onMusicPlayModeChanged(int mode);
    }
}
