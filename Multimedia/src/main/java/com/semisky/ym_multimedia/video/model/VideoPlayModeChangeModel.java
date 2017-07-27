package com.semisky.ym_multimedia.video.model;

import android.content.Context;

import com.semisky.ym_multimedia.multimedia.utils.PreferencesUtil;
import com.semisky.ym_multimedia.video.utils.VideoConstants;

/**
 * 视频播放模式切换Model
 *
 * @author Anter
 */
public class VideoPlayModeChangeModel {
    private static VideoPlayModeChangeModel instance;
    private Context mContext;
    private OnVideoPlayModeChangeListener onVideoPlayModeChangeListener;

    private VideoPlayModeChangeModel(Context context) {
        this.mContext = context;
    }

    private static synchronized void syncInit(Context context) {
        if (instance == null) {
            instance = new VideoPlayModeChangeModel(context);
        }
    }

    public static VideoPlayModeChangeModel getInstance(Context context) {
        if (instance == null) {
            syncInit(context);
        }
        return instance;
    }

    public void setOnVideoPlayModeChangeListener(OnVideoPlayModeChangeListener
                                                         onVideoPlayModeChangeListener) {
        this.onVideoPlayModeChangeListener = onVideoPlayModeChangeListener;
    }

    /**
     * 切换视频播放模式
     */
    public void changePlayMode() {
        switch (PreferencesUtil.getInstance().getVideoPlayMode()) {
            case VideoConstants.MODE_CIRCLE_FOLDER:// 文件夹循环
                PreferencesUtil.getInstance().setVideoPlayMode(VideoConstants.MODE_CIRCLE_SINGLE);
                if (onVideoPlayModeChangeListener != null) {
                    onVideoPlayModeChangeListener.onVideoPlayModeChanged(VideoConstants
                            .MODE_CIRCLE_SINGLE);
                }
                break;
            case VideoConstants.MODE_CIRCLE_SINGLE:// 单曲循化
                PreferencesUtil.getInstance().setVideoPlayMode(VideoConstants.MODE_RANDOM);
                if (onVideoPlayModeChangeListener != null) {
                    onVideoPlayModeChangeListener.onVideoPlayModeChanged(VideoConstants
                            .MODE_RANDOM);
                }
                break;
            case VideoConstants.MODE_RANDOM:// 随机播放
                PreferencesUtil.getInstance().setVideoPlayMode(VideoConstants.MODE_CIRCLE_ALL);
                if (onVideoPlayModeChangeListener != null) {
                    onVideoPlayModeChangeListener.onVideoPlayModeChanged(VideoConstants
                            .MODE_CIRCLE_ALL);
                }
                break;
            case VideoConstants.MODE_CIRCLE_ALL:// 全部循环
                PreferencesUtil.getInstance().setVideoPlayMode(VideoConstants.MODE_CIRCLE_FOLDER);
                if (onVideoPlayModeChangeListener != null) {
                    onVideoPlayModeChangeListener.onVideoPlayModeChanged(VideoConstants
                            .MODE_CIRCLE_FOLDER);
                }
                break;
            default:
                break;
        }
    }

    public interface OnVideoPlayModeChangeListener {
        void onVideoPlayModeChanged(int mode);
    }
}
