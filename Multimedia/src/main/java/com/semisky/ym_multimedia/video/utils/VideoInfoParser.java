package com.semisky.ym_multimedia.video.utils;

import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import com.semisky.ym_multimedia.common.utils.CharsetTranscoder;
import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager;
import com.semisky.ym_multimedia.video.bean.VideoInfo;

import java.io.File;

/**
 * 视频信息解析类
 *
 * @author Anter
 */
public class VideoInfoParser {
    private static VideoInfoParser instance;
    private OnParseVideoInfoListener onParseVideoInfoListener;
    private VideoInfo playingVideoInfo;

    private VideoInfoParser() {
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new VideoInfoParser();
        }
    }

    public static VideoInfoParser getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    public void setOnParseVideoInfoListener(OnParseVideoInfoListener listener) {
        this.onParseVideoInfoListener = listener;
    }

    public VideoInfo getPlayingVideoInfo() {
        return playingVideoInfo;
    }

    public void setPlayingVideoInfo(VideoInfo info) {
        this.playingVideoInfo = info;
    }

    /**
     * 解析视频文件
     */
    public VideoInfo parseVideoInfo(String videoUri) {
        playingVideoInfo = null;
        Logger.logD("VideoInfoParser-----------------------视频解析开始");
        // 解析开始回调
        if (onParseVideoInfoListener != null) {
            onParseVideoInfoListener.onParseVideoInfoStart(videoUri);
        }

        if (TextUtils.isEmpty(videoUri)) {
            Logger.logE("VideoInfoParser-----------------------视频路径为空");
            return playingVideoInfo;
        }

        if (!UsbStateManager.getInstance().isFileBelongUsbMounted(videoUri)//
                // 判断歌曲所在的U盘是否挂载，因为有可能拔出U盘后new
                // File(videoUri).exists()依然为true
                || !new File(videoUri).exists()) {
            Logger.logE("VideoInfoParser-----------------------视频文件不存在");
            // 文件不存在回调
            if (onParseVideoInfoListener != null) {
                onParseVideoInfoListener.onParseVideoFileNotExist(videoUri);
            }
            return playingVideoInfo;
        }

        // 音视频解析工具对象
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            // 设置解析源
            mmr.setDataSource(videoUri);
        } catch (Exception e) {
            Logger.logE("VideoInfoParser-----------------------setDataSource error,视频文件源有问题");
            // 解析出错回调
            if (onParseVideoInfoListener != null) {
                onParseVideoInfoListener.onParseVideoInfoError(videoUri);
            }
            return playingVideoInfo;
        }

        playingVideoInfo = new VideoInfo();
        // Uri
        playingVideoInfo.setUri(videoUri);
        // displayName
        playingVideoInfo.setDisplayName(videoUri.substring(videoUri.lastIndexOf(File.separator) +
                1));
        // title
        playingVideoInfo.setTitle(playingVideoInfo.getDisplayName().substring(0, playingVideoInfo
                .getDisplayName().lastIndexOf(".")));
        // duration
        // 可能MediaMetadataRetriever无法解析出视频时长，而VideoView可以
        int duration = 0;
        try {
            duration = Integer.valueOf(CharsetTranscoder.getDefaultEncodeString(mmr
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
        } catch (Exception e) {
            Logger.logE("VideoInfoParser-----------------------解析视频时长出错");
            // playingVideoInfo = null;
            // // 解析出错回调
            // if (onParseVideoInfoListener != null) {
            // onParseVideoInfoListener.onParseVideoInfoError();
            // }
            // return playingVideoInfo;

            duration = 0;
        }
        playingVideoInfo.setDuration(duration);

        try {
            // width
            playingVideoInfo.setWidth(Integer.valueOf(CharsetTranscoder.getDefaultEncodeString
                    (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))));
            // height
            playingVideoInfo.setHeight(Integer.valueOf(CharsetTranscoder.getDefaultEncodeString
                    (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))));
        } catch (Exception e) {
            Logger.logE("VideoInfoParser-----------------------解析视频宽高出错");
            playingVideoInfo = null;
            // 解析出错回调
            if (onParseVideoInfoListener != null) {
                onParseVideoInfoListener.onParseVideoInfoError(videoUri);
            }
            return playingVideoInfo;
        }

        Logger.logD("VideoInfoParser-----------------------视频解析完成");
        // 解析成功回调
        if (onParseVideoInfoListener != null) {
            onParseVideoInfoListener.onParseVideoInfoComplete(playingVideoInfo);
        }
        try {
            mmr.release();
        } catch (Exception e) {
            Logger.logE("VideoInfoParser-----------------------释放MediaMetadataRetriever出错");
        }
        return playingVideoInfo;
    }

    public interface OnParseVideoInfoListener {
        // 开始解析
        void onParseVideoInfoStart(String videoUri);

        // 文件不存在
        void onParseVideoFileNotExist(String videoUri);

        // 解析错误
        void onParseVideoInfoError(String videoUri);

        // 解析完成
        void onParseVideoInfoComplete(VideoInfo info);
    }
}