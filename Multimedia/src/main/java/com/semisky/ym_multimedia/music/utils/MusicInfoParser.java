package com.semisky.ym_multimedia.music.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;

import com.semisky.ym_multimedia.common.utils.CharsetTranscoder;
import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager;
import com.semisky.ym_multimedia.music.bean.MusicInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 音乐文件解析类
 *
 * @author Anter
 */
public class MusicInfoParser {
    private static MusicInfoParser instance;
    private List<OnParseMusicInfoListener> onParseMusicInfoListeners;
    private MusicInfo playingMusicInfo;// 正在播放的音乐的信息
    private String mAlbumFlag;// 标识符，防错乱

    private MusicInfoParser() {
        onParseMusicInfoListeners = new ArrayList<OnParseMusicInfoListener>();
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new MusicInfoParser();
        }
    }

    public static MusicInfoParser getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    /**
     * 注册解析监听器
     */
    public void registerParseMusicInfoListener(OnParseMusicInfoListener listener) {
        if (!onParseMusicInfoListeners.contains(listener)) {
            onParseMusicInfoListeners.add(listener);
        }
    }

    /**
     * 注销解析监听器
     */
    public void unregisterParseMusicInfoListener(OnParseMusicInfoListener listener) {
        onParseMusicInfoListeners.remove(listener);
    }

    public MusicInfo getPlayingMusicInfo() {
        return playingMusicInfo;
    }

    /**
     * 解析音频文件
     *
     * @param musicUri 音频文件路径
     * @return
     */
    public MusicInfo parseMusicFile(String musicUri) {
        playingMusicInfo = null;
        Logger.logD("MusicInfoParser-----------------------歌曲解析开始");
        // 解析开始回调
        for (OnParseMusicInfoListener listener : onParseMusicInfoListeners) {
            if (listener != null) {
                listener.onParseMusicInfoStart(musicUri);
            }
        }

        if (TextUtils.isEmpty(musicUri)) {
            Logger.logE("MusicInfoParser-----------------------歌曲路径为空");
            return playingMusicInfo;
        }

        if (!UsbStateManager.getInstance().isFileBelongUsbMounted(musicUri)//
                // 判断歌曲所在的U盘是否挂载，因为有可能拔出U盘后new
                // File(musicUri).exists()依然为true
                || !new File(musicUri).exists()) {
            Logger.logE("MusicInfoParser-----------------------歌曲文件不存在");
            // 文件不存在回调
            for (OnParseMusicInfoListener listener : onParseMusicInfoListeners) {
                if (listener != null) {
                    listener.onParseMusicFileNotExists(musicUri);
                }
            }
            return playingMusicInfo;
        }

        // 音视频解析工具对象
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            // 设置解析源
            mmr.setDataSource(musicUri);
        } catch (Exception e) {
            Logger.logE("MusicInfoParser-----------------------setDataSource error,音频文件源有问题");
            // 解析出错回调
            for (OnParseMusicInfoListener listener : onParseMusicInfoListeners) {
                if (listener != null) {
                    listener.onParseMusicInfoError(musicUri);
                }
            }
            return playingMusicInfo;
        }

        playingMusicInfo = new MusicInfo();
        // Uri
        playingMusicInfo.setUri(musicUri);
        // displayName
        playingMusicInfo.setDisplayName(musicUri.substring(musicUri.lastIndexOf(File.separator) +
                1));
        // title
        playingMusicInfo.setTitle(playingMusicInfo.getDisplayName().substring(0, playingMusicInfo
                .getDisplayName().lastIndexOf(".")));
        // artist
        String artist;
        try {
            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        } catch (Exception e) {
            Logger.logE("MusicInfoParser-----------------------无法解析出演唱者");
            artist = null;
        }
        if (TextUtils.isEmpty(artist)) {
            artist = "Unknown";
        } else {
            artist = CharsetTranscoder.getDefaultEncodeString(artist);
        }
        playingMusicInfo.setArtist(artist);
        // album
        String album;
        try {
            album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        } catch (Exception e) {
            Logger.logE("MusicInfoParser-----------------------无法解析出专辑名");
            album = null;
        }
        if (TextUtils.isEmpty(album)) {
            album = "Unknown";
        } else {
            album = CharsetTranscoder.getDefaultEncodeString(album);
        }
        playingMusicInfo.setAlbum(album);
        // duration
        int duration;
        try {
            String durationString = mmr.extractMetadata(MediaMetadataRetriever
                    .METADATA_KEY_DURATION);
            duration = Integer.valueOf(CharsetTranscoder.getDefaultEncodeString(durationString));
        } catch (Exception e) {
            Logger.logE("MusicInfoParser-----------------------无法解析出歌曲时长");
            duration = 0;
        }
        playingMusicInfo.setDuration(duration);
        Logger.logD("MusicInfoParser-----------------------音频解析完成");
        // 解析成功回调
        for (OnParseMusicInfoListener listener : onParseMusicInfoListeners) {
            if (listener != null) {
                listener.onParseMusicInfoComplete(playingMusicInfo);
            }
        }
        try {
            mmr.release();
        } catch (Exception e) {
            Logger.logE("MusicInfoParser-----------------------释放MediaMetadataRetriever出错");
        }
        return playingMusicInfo;
    }

    private String getFlag() {
        return mAlbumFlag;
    }

    private void setFlag(String musicUri) {
        this.mAlbumFlag = musicUri;
    }

    /**
     * 加载专辑图片
     */
    public void setAlbumImage(final ImageView imageView, final String musicUri) {
        setFlag(musicUri);

        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Logger.logD("MusicInfoParser-----------------------开始获取专辑缩略图");
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                return getAlbumBitmap(params[0]);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
                if (!TextUtils.isEmpty(getFlag()) && getFlag().equals(musicUri)) {// 是正在播放的歌曲的路径
                    if (result == null) {
                        Logger.logE("MusicInfoParser-----------------------无法获取专辑图片");
                    } else {
                        Logger.logD("MusicInfoParser-----------------------获取专辑图片成功");
                    }
                    imageView.setImageBitmap(result);
                }
            }
        }.execute(musicUri);
    }

    /**
     * 获取专辑Bitmap
     *
     * @param musicUri 歌曲路径
     * @return
     */
    private Bitmap getAlbumBitmap(String musicUri) {
        // 新建音视频解析工具对象
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            // 设置解析源
            mmr.setDataSource(musicUri);
            byte[] art = mmr.getEmbeddedPicture();
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.length);
            }
        } catch (Exception e) {
            return null;
        } finally {
            try {
                mmr.release();
                mmr = null;
            } catch (Exception e) {
                Logger.logE("MusicInfoParser-----------------------释放MediaMetadataRetriever出错");
            }
        }
        return null;
    }

    /**
     * 歌曲解析监听器
     *
     * @author Anter
     */
    public interface OnParseMusicInfoListener {
        // 开始解析
        void onParseMusicInfoStart(String musicUri);

        // 文件不存在
        void onParseMusicFileNotExists(String musicUri);

        // 解析错误
        void onParseMusicInfoError(String musicUri);

        // 解析完成
        void onParseMusicInfoComplete(MusicInfo info);
    }
}