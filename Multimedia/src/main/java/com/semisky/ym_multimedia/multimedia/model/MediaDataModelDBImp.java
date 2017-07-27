package com.semisky.ym_multimedia.multimedia.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.semisky.ym_multimedia.multimedia.dao.DBConfiguration;
import com.semisky.ym_multimedia.multimedia.dao.MediaDBManager;
import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;
import com.semisky.ym_multimedia.multimedia.utils.PreferencesUtil;
import com.semisky.ym_multimedia.music.utils.MusicConstants;
import com.semisky.ym_multimedia.music.utils.OnMusicInfosChangeListener;
import com.semisky.ym_multimedia.photo.utils.OnPhotoInfosChangeListener;
import com.semisky.ym_multimedia.video.utils.OnVideoInfosChangeListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 媒体文件数据库操作Model
 *
 * @author Anter
 */
public class MediaDataModelDBImp implements MediaDataModel {
    private static MediaDataModelDBImp instance;
    private Context mContext;
    private List<String> usb1ShowingPhotoList = new ArrayList<String>();
    private List<String> usb2ShowingPhotoList = new ArrayList<String>();
    private List<String> usb1ShowingMusicList = new ArrayList<String>();
    private List<String> usb2ShowingMusicList = new ArrayList<String>();
    private List<String> usb1ShowingVideoList = new ArrayList<String>();
    private List<String> usb2ShowingVideoList = new ArrayList<String>();
    private OnPhotoInfosChangeListener onPhotoInfosChangeListener;
    private OnMusicInfosChangeListener onMusicInfosChangeListener;
    private OnVideoInfosChangeListener onVideoInfosChangeListener;

    private MediaDataModelDBImp(Context context) {
        this.mContext = context;
    }

    private static synchronized void syncInit(Context context) {
        if (instance == null) {
            instance = new MediaDataModelDBImp(context);
        }
    }

    public static MediaDataModelDBImp getInstance(Context context) {
        if (instance == null) {
            syncInit(context);
        }
        return instance;
    }

    public List<String> getUsb1ShowingPhotoList() {
        return usb1ShowingPhotoList;
    }

    public List<String> getUsb2ShowingPhotoList() {
        return usb2ShowingPhotoList;
    }

    public List<String> getUsb1ShowingMusicList() {
        return usb1ShowingMusicList;
    }

    public List<String> getUsb2ShowingMusicList() {
        return usb2ShowingMusicList;
    }

    public List<String> getUsb1ShowingVideoList() {
        return usb1ShowingVideoList;
    }

    public List<String> getUsb2ShowingVideoList() {
        return usb2ShowingVideoList;
    }

    @Override
    public void setOnPhotoInfosChangeListener(OnPhotoInfosChangeListener listener) {
        this.onPhotoInfosChangeListener = listener;
    }

    @Override
    public void setOnMusicInfosChangeListener(OnMusicInfosChangeListener listener) {
        this.onMusicInfosChangeListener = listener;
    }

    @Override
    public void setOnVideoInfosChangeListener(OnVideoInfosChangeListener listener) {
        this.onVideoInfosChangeListener = listener;
    }

    @Override
    public void addPhotoUri(int usbFlag, String photoUri) {
        // 插入图片信息到数据库
        MediaDBManager.getInstance(mContext).insertPhoto(usbFlag, photoUri, photoUri.substring(0,
                photoUri.lastIndexOf(File.separator)));
    }

    @Override
    public Cursor queryAllPhoto() {// 查询所有图片
        return MediaDBManager.getInstance(mContext).queryAllPhoto();
    }

    @Override
    public Cursor queryPhotoIncludeFolder(String folderUri) {// 获取该文件夹下的所有图片（包含含有图片文件的文件夹）
        return MediaDBManager.getInstance(mContext).queryPhotoIncludeFolder(folderUri);
    }

    @Override
    public Cursor queryPhotoExcludeFolder(String folderUri) {// 获取该文件夹下的所有图片（不包含含有图片文件的文件夹）
        return MediaDBManager.getInstance(mContext).queryPhotoExcludeFolder(folderUri);
    }

    @Override
    public void queryPhotoDirectUnder(int usbFlag, String folderUri) {//
        // 获取该目录下的直属的图片文件及包含图片文件的文件夹（相同的文件夹只添加一次）
        Cursor cursor = MediaDBManager.getInstance(mContext).queryPhotoDirectUnder(folderUri);
        if (cursor != null) {
            switch (usbFlag) {
                case MultimediaConstants.FLAG_USB1:
                    usb1ShowingPhotoList.clear();
                    while (cursor.moveToNext()) {
                        String photoUri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                                .PhotoConfiguration.PHOTO_URI));
                        String endUri = photoUri.substring(folderUri.length() + 1);
                        if (endUri.contains(File.separator)) {// 说明是包含图片文件的文件夹
                            String folder = folderUri + File.separator + endUri.substring(0,
                                    endUri.indexOf(File.separator));
                            if (!usb1ShowingPhotoList.contains(folder)) {
                                usb1ShowingPhotoList.add(folder);
                            }
                        } else {// 说明是图片文件
                            usb1ShowingPhotoList.add(photoUri);
                        }
                    }
                    break;
                case MultimediaConstants.FLAG_USB2:
                    usb2ShowingPhotoList.clear();
                    while (cursor.moveToNext()) {
                        String photoUri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                                .PhotoConfiguration.PHOTO_URI));
                        String endUri = photoUri.substring(folderUri.length() + 1);
                        if (endUri.contains(File.separator)) {// 说明是包含图片文件的文件夹
                            String folder = folderUri + File.separator + endUri.substring(0,
                                    endUri.indexOf(File.separator));
                            if (!usb2ShowingPhotoList.contains(folder)) {
                                usb2ShowingPhotoList.add(folder);
                            }
                        } else {// 说明是图片文件
                            usb2ShowingPhotoList.add(photoUri);
                        }
                    }
                    break;
                default:
                    break;
            }
            cursor.close();
        }
    }

    @Override
    public String getPreviousPlayPhotoUri() {// 获取上一张要预览的图片Uri
        String playingPhotoUri = PreferencesUtil.getInstance().getCurrentPlayingPhotoUri();
        if (TextUtils.isEmpty(playingPhotoUri) || !new File(playingPhotoUri).exists()) {
            return null;
        }

        int usbFlag = -1;
        if (playingPhotoUri.startsWith(MultimediaConstants.PATH_USB1)) {
            usbFlag = MultimediaConstants.FLAG_USB1;
        } else if (playingPhotoUri.startsWith(MultimediaConstants.PATH_USB2)) {
            usbFlag = MultimediaConstants.FLAG_USB2;
        }

        if (usbFlag == -1) {
            return null;
        }
        Cursor cursor = MediaDBManager.getInstance(mContext).queryAllPhoto(usbFlag);
        if (cursor != null) {
            String photoUri = null;
            while (cursor.moveToNext()) {
                photoUri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                        .PhotoConfiguration.PHOTO_URI));
                if (photoUri.equals(playingPhotoUri)) {
                    if (!cursor.moveToPrevious()) {// 如果没有上一个，说明到顶了
                        // 游标移至底部
                        cursor.moveToLast();
                    }
                    photoUri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                            .PhotoConfiguration.PHOTO_URI));
                    break;
                }
            }
            cursor.close();
            return photoUri;
        }
        return null;
    }

    @Override
    public String getNextPlayPhotoUri() {// 获取下一张要预览的图片Uri
        String playingPhotoUri = PreferencesUtil.getInstance().getCurrentPlayingPhotoUri();
        if (TextUtils.isEmpty(playingPhotoUri) || !new File(playingPhotoUri).exists()) {
            return null;
        }

        int usbFlag = -1;
        if (playingPhotoUri.startsWith(MultimediaConstants.PATH_USB1)) {
            usbFlag = MultimediaConstants.FLAG_USB1;
        } else if (playingPhotoUri.startsWith(MultimediaConstants.PATH_USB2)) {
            usbFlag = MultimediaConstants.FLAG_USB2;
        }

        if (usbFlag == -1) {
            return null;
        }
        Cursor cursor = MediaDBManager.getInstance(mContext).queryAllPhoto(usbFlag);
        if (cursor != null) {
            String photoUri = null;
            while (cursor.moveToNext()) {
                photoUri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                        .PhotoConfiguration.PHOTO_URI));
                if (photoUri.equals(playingPhotoUri)) {
                    if (!cursor.moveToNext()) {// 如果没有下一个，说明到底了
                        // 游标移至顶部
                        cursor.moveToFirst();
                    }
                    photoUri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                            .PhotoConfiguration.PHOTO_URI));
                    break;
                }
            }
            cursor.close();
            return photoUri;
        }
        return null;
    }

    @Override
    public int getPhotoPosition(int usbFlag, String photoUri) {// 获取图片在当前列表的位置
        int position = -1;
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                for (String uri : usb1ShowingPhotoList) {
                    position++;
                    if (uri.equals(photoUri)) {
                        return position;
                    }
                }
                break;
            case MultimediaConstants.FLAG_USB2:
                for (String uri : usb2ShowingPhotoList) {
                    position++;
                    if (uri.equals(photoUri)) {
                        return position;
                    }
                }
                break;
            default:
                break;
        }
        return -1;
    }

    @Override
    public void deletePhotoUri(int usbFlag) {
        // 删除图片信息
        MediaDBManager.getInstance(mContext).deletePhoto(usbFlag);
        // 通知PhotoFragment
        if (onPhotoInfosChangeListener != null) {
            onPhotoInfosChangeListener.onPhotoInfosClear(usbFlag);
        }
    }

    @Override
    public void addMusicUri(int usbFlag, String musicUri) {
        // 插入歌曲信息到数据库
        MediaDBManager.getInstance(mContext).insertMusic(usbFlag, musicUri, musicUri.substring(0,
                musicUri.lastIndexOf(File.separator)));
    }

    @Override
    public Cursor queryAllMusic() {// 查询所有音乐
        return MediaDBManager.getInstance(mContext).queryAllMusic();
    }

    @Override
    public Cursor queryMusicIncludeFolder(String folderUri) {// 获取该文件夹下的所有音乐（包含含有音乐文件的文件夹）
        return MediaDBManager.getInstance(mContext).queryMusicIncludeFolder(folderUri);
    }

    @Override
    public Cursor queryMusicExcludeFolder(String folderUri) {// 获取该文件夹下的所有音乐（不包含含有音乐文件的文件夹）
        return MediaDBManager.getInstance(mContext).queryMusicExcludeFolder(folderUri);
    }

    @Override
    public void queryMusicDirectUnder(int usbFlag, String folderUri) {//
        // 获取该目录下的直属的音乐文件及包含音乐文件的文件夹（相同的文件夹只添加一次）
        Cursor cursor = MediaDBManager.getInstance(mContext).queryMusicDirectUnder(folderUri);
        if (cursor != null) {
            switch (usbFlag) {
                case MultimediaConstants.FLAG_USB1:
                    usb1ShowingMusicList.clear();
                    while (cursor.moveToNext()) {
                        String musicUri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                                .MusicConfiguration.MUSIC_URI));
                        String endUri = musicUri.substring(folderUri.length() + 1);
                        if (endUri.contains(File.separator)) {// 说明是包含歌曲文件的文件夹
                            String folder = folderUri + File.separator + endUri.substring(0,
                                    endUri.indexOf(File.separator));
                            if (!usb1ShowingMusicList.contains(folder)) {
                                usb1ShowingMusicList.add(folder);
                            }
                        } else {// 说明是歌曲文件
                            usb1ShowingMusicList.add(musicUri);
                        }
                    }
                    break;
                case MultimediaConstants.FLAG_USB2:
                    usb2ShowingMusicList.clear();
                    while (cursor.moveToNext()) {
                        String musicUri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                                .MusicConfiguration.MUSIC_URI));
                        String endUri = musicUri.substring(folderUri.length() + 1);
                        if (endUri.contains(File.separator)) {// 说明是包含歌曲文件的文件夹
                            String folder = folderUri + File.separator + endUri.substring(0,
                                    endUri.indexOf(File.separator));
                            if (!usb2ShowingMusicList.contains(folder)) {
                                usb2ShowingMusicList.add(folder);
                            }
                        } else {// 说明是歌曲文件
                            usb2ShowingMusicList.add(musicUri);
                        }
                    }
                    break;
                default:
                    break;
            }
            cursor.close();
        }
    }

    @Override
    public String getMusicPosition(String musicUri) {// 获取该音乐文件在它直属的文件夹中的位置（不包含含有音乐文件的文件夹）
        if (TextUtils.isEmpty(musicUri) || !musicUri.contains(File.separator)) {
            return "";
        }

        Cursor cursor = queryMusicExcludeFolder(musicUri.substring(0, musicUri.lastIndexOf(File
                .separator)));
        String result = "";
        while (cursor.moveToNext()) {
            String Uri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                    .MusicConfiguration.MUSIC_URI));
            if (Uri.equals(musicUri)) {
                result = (cursor.getPosition() + 1) + "/" + cursor.getCount();
            }
        }
        cursor.close();
        return result;
    }

    @Override
    public String getPreviousPlayMusicUriByPlayMode() {// 获取上一首要播放的歌曲Uri
        String playingMusicUri = PreferencesUtil.getInstance().getCurrentPlayingMusicUri();
        if (TextUtils.isEmpty(playingMusicUri) || !new File(playingMusicUri).exists()) {
            return null;
        }

        switch (PreferencesUtil.getInstance().getMusicPlayMode()) {
            case MusicConstants.MODE_CIRCLE_FOLDER:// 文件夹循环
                Cursor cursor1 = MediaDBManager.getInstance(mContext).queryMusicExcludeFolder
                        (playingMusicUri.substring(0, playingMusicUri.lastIndexOf(File.separator)));
                if (cursor1 != null) {
                    String musicUri = null;
                    while (cursor1.moveToNext()) {
                        musicUri = cursor1.getString(cursor1.getColumnIndex(DBConfiguration
                                .MusicConfiguration.MUSIC_URI));
                        if (musicUri.equals(playingMusicUri)) {// 找到正在播放的音乐文件的位置
                            if (!cursor1.moveToPrevious()) {// 如果没有上一个，说明到顶了
                                // 游标移至底部
                                cursor1.moveToLast();
                            }
                            musicUri = cursor1.getString(cursor1.getColumnIndex(DBConfiguration
                                    .MusicConfiguration.MUSIC_URI));
                            break;
                        }
                    }
                    cursor1.close();
                    return musicUri;
                }
                break;
            case MusicConstants.MODE_CIRCLE_SINGLE:// 单曲循环（手动切换时同全部循环）
                // return playingMusicUri;
            case MusicConstants.MODE_CIRCLE_ALL:// 全部循环
                int usbFlag = -1;
                if (playingMusicUri.startsWith(MultimediaConstants.PATH_USB1)) {
                    usbFlag = MultimediaConstants.FLAG_USB1;
                } else if (playingMusicUri.startsWith(MultimediaConstants.PATH_USB2)) {
                    usbFlag = MultimediaConstants.FLAG_USB2;
                }

                if (usbFlag == -1) {
                    return null;
                }
                Cursor cursor3 = MediaDBManager.getInstance(mContext).queryAllMusic(usbFlag);
                if (cursor3 != null) {
                    String musicUri = null;
                    while (cursor3.moveToNext()) {
                        musicUri = cursor3.getString(cursor3.getColumnIndex(DBConfiguration
                                .MusicConfiguration.MUSIC_URI));
                        if (musicUri.equals(playingMusicUri)) {// 找到正在播放的音乐文件的位置
                            if (!cursor3.moveToPrevious()) {// 如果没有上一个，说明到顶了
                                // 游标移至底部
                                cursor3.moveToLast();
                            }
                            musicUri = cursor3.getString(cursor3.getColumnIndex(DBConfiguration
                                    .MusicConfiguration.MUSIC_URI));
                            break;
                        }
                    }
                    cursor3.close();
                    return musicUri;
                }
                break;
            case MusicConstants.MODE_RANDOM:// 随机播放
                Cursor cursor2 = MediaDBManager.getInstance(mContext).queryMusicExcludeFolder
                        (playingMusicUri.substring(0, playingMusicUri.lastIndexOf(File.separator)));
                if (cursor2 != null) {
                    int position = (int) (Math.random() * cursor2.getCount());
                    if (cursor2.moveToPosition(position)) {
                        String musicUri = cursor2.getString(cursor2.getColumnIndex
                                (DBConfiguration.MusicConfiguration.MUSIC_URI));
                        cursor2.close();
                        return musicUri;
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public String getNextPlayMusicUriByPlayMode(boolean fromUser) {// 获取下一首要播放的歌曲Uri
        String playingMusicUri = PreferencesUtil.getInstance().getCurrentPlayingMusicUri();
        if (TextUtils.isEmpty(playingMusicUri) || !new File(playingMusicUri).exists()) {
            return null;
        }

        switch (PreferencesUtil.getInstance().getMusicPlayMode()) {
            case MusicConstants.MODE_CIRCLE_FOLDER:// 文件夹循环
                Cursor cursor = MediaDBManager.getInstance(mContext).queryMusicExcludeFolder
                        (playingMusicUri.substring(0, playingMusicUri.lastIndexOf(File.separator)));
                if (cursor != null) {
                    String musicUri = null;
                    while (cursor.moveToNext()) {
                        musicUri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                                .MusicConfiguration.MUSIC_URI));
                        if (musicUri.equals(playingMusicUri)) {// 找到正在播放的音乐文件的位置
                            if (!cursor.moveToNext()) {// 如果没有下一个，说明到底了
                                // 游标移至顶部
                                cursor.moveToFirst();
                            }
                            musicUri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                                    .MusicConfiguration.MUSIC_URI));
                            break;
                        }
                    }
                    cursor.close();
                    return musicUri;
                }
                break;
            case MusicConstants.MODE_CIRCLE_SINGLE:// 单曲循环
                if (!fromUser) {// 如果fromUser，说明是用户手动切换下一首，此时切换下一首，否则单曲播放
                    return playingMusicUri;
                }
            case MusicConstants.MODE_CIRCLE_ALL:// 全部循环
                int usbFlag = -1;
                if (playingMusicUri.startsWith(MultimediaConstants.PATH_USB1)) {
                    usbFlag = MultimediaConstants.FLAG_USB1;
                } else if (playingMusicUri.startsWith(MultimediaConstants.PATH_USB2)) {
                    usbFlag = MultimediaConstants.FLAG_USB2;
                }

                if (usbFlag == -1) {
                    return null;
                }
                Cursor cursor3 = MediaDBManager.getInstance(mContext).queryAllMusic(usbFlag);
                if (cursor3 != null) {
                    String musicUri = null;
                    while (cursor3.moveToNext()) {
                        musicUri = cursor3.getString(cursor3.getColumnIndex(DBConfiguration
                                .MusicConfiguration.MUSIC_URI));
                        if (musicUri.equals(playingMusicUri)) {// 找到正在播放的音乐文件的位置
                            if (!cursor3.moveToNext()) {// 如果没有下一个，说明到底了
                                // 游标移至顶部
                                cursor3.moveToFirst();
                            }
                            musicUri = cursor3.getString(cursor3.getColumnIndex(DBConfiguration
                                    .MusicConfiguration.MUSIC_URI));
                            break;
                        }
                    }
                    cursor3.close();
                    return musicUri;
                }
                break;
            case MusicConstants.MODE_RANDOM:// 随机播放
                Cursor cursor2 = MediaDBManager.getInstance(mContext).queryMusicExcludeFolder
                        (playingMusicUri.substring(0, playingMusicUri.lastIndexOf(File.separator)));
                if (cursor2 != null) {
                    int position = (int) (Math.random() * cursor2.getCount());
                    if (cursor2.moveToPosition(position)) {
                        String musicUri = cursor2.getString(cursor2.getColumnIndex
                                (DBConfiguration.MusicConfiguration.MUSIC_URI));
                        cursor2.close();
                        return musicUri;
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public int getMusicPosition(int usbFlag, String musicUri) {// 获取音乐在当前列表下的位置
        int position = -1;
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                for (String uri : usb1ShowingMusicList) {
                    position++;
                    if (uri.equals(musicUri)) {
                        return position;
                    }
                }
                break;
            case MultimediaConstants.FLAG_USB2:
                for (String uri : usb2ShowingMusicList) {
                    position++;
                    if (uri.equals(musicUri)) {
                        return position;
                    }
                }
                break;
            default:
                break;
        }
        return -1;
    }

    @Override
    public void deleteMusicUri(int usbFlag) {
        // 删除歌曲信息
        MediaDBManager.getInstance(mContext).deleteMusic(usbFlag);
        // 通知MusicFragment
        if (onMusicInfosChangeListener != null) {
            onMusicInfosChangeListener.onMusicInfosClear(usbFlag);
        }
    }

    @Override
    public void addLyricUri(int usbFlag, String lyricUri) {
        // 插入歌词信息数据库
        String lyricName = lyricUri.substring(lyricUri.lastIndexOf(File.separator) + 1);
        MediaDBManager.getInstance(mContext).insertLyric(usbFlag, lyricUri, lyricName.substring
                (0, lyricName.lastIndexOf(".")));
    }

    @Override
    public String getLyricUri(String musicUri) {// 获取该音乐文件匹配的歌词文件路径
        String lyricUri = "";
        if (TextUtils.isEmpty(musicUri)) {
            return lyricUri;
        }

        Cursor cursor = MediaDBManager.getInstance(mContext).queryLyric(musicUri);
        if (cursor != null && cursor.moveToNext()) {
            lyricUri = cursor.getString(cursor.getColumnIndex(DBConfiguration.LyricConfiguration
                    .LYRIC_URI));
            cursor.close();
        }
        return lyricUri;
    }

    @Override
    public void deleteLyricUri(int usbFlag) {
        // 删除歌词信息
        MediaDBManager.getInstance(mContext).deleteLyric(usbFlag);
    }

    @Override
    public void addVideoUri(int usbFlag, String videoUri) {
        // 插入视频信息到数据库
        MediaDBManager.getInstance(mContext).insertVideo(usbFlag, videoUri, videoUri.substring(0,
                videoUri.lastIndexOf(File.separator)));
    }

    @Override
    public Cursor queryAllVideo() {// 查询所有视频
        return MediaDBManager.getInstance(mContext).queryAllVideo();
    }

    @Override
    public Cursor queryVideoIncludeFolder(String folderUri) {// 获取该文件夹下的所有视频（包含含有视频文件的文件夹）
        return MediaDBManager.getInstance(mContext).queryVideoIncludeFolder(folderUri);
    }

    @Override
    public Cursor queryVideoExcludeFolder(String folderUri) {// 获取该文件夹下的所有视频（不包含含有视频文件的文件夹）
        return MediaDBManager.getInstance(mContext).queryVideoExcludeFolder(folderUri);
    }

    @Override
    public void queryVideoDirectUnder(int usbFlag, String folderUri) {//
        // 获取该目录下的直属的视频文件及包含视频文件的文件夹（相同的文件夹只添加一次）
        Cursor cursor = MediaDBManager.getInstance(mContext).queryVideoDirectUnder(folderUri);
        if (cursor != null) {
            switch (usbFlag) {
                case MultimediaConstants.FLAG_USB1:
                    usb1ShowingVideoList.clear();
                    while (cursor.moveToNext()) {
                        String videoUri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                                .VideoConfiguration.VIDEO_URI));
                        String endUri = videoUri.substring(folderUri.length() + 1);
                        if (endUri.contains(File.separator)) {// 说明是包含视频文件的文件夹
                            String folder = folderUri + File.separator + endUri.substring(0,
                                    endUri.indexOf(File.separator));
                            if (!usb1ShowingVideoList.contains(folder)) {
                                usb1ShowingVideoList.add(folder);
                            }
                        } else {// 说明是视频文件
                            usb1ShowingVideoList.add(videoUri);
                        }
                    }
                    break;
                case MultimediaConstants.FLAG_USB2:
                    usb2ShowingVideoList.clear();
                    while (cursor.moveToNext()) {
                        String videoUri = cursor.getString(cursor.getColumnIndex(DBConfiguration
                                .VideoConfiguration.VIDEO_URI));
                        String endUri = videoUri.substring(folderUri.length() + 1);
                        if (endUri.contains(File.separator)) {// 说明是包含视频文件的文件夹
                            String folder = folderUri + File.separator + endUri.substring(0,
                                    endUri.indexOf(File.separator));
                            if (!usb2ShowingVideoList.contains(folder)) {
                                usb2ShowingVideoList.add(folder);
                            }
                        } else {// 说明是视频文件
                            usb2ShowingVideoList.add(videoUri);
                        }
                    }
                    break;
                default:
                    break;
            }
            cursor.close();
        }
    }

    @Override
    public String getPreviousPlayVideoUriByPlayMode() {// 获取上一个要播放的视频Uri
        String playingVideoUri = PreferencesUtil.getInstance().getCurrentPlayingVideoUri();
        if (TextUtils.isEmpty(playingVideoUri) || !new File(playingVideoUri).exists()) {
            return null;
        }

        switch (PreferencesUtil.getInstance().getVideoPlayMode()) {
            case MusicConstants.MODE_CIRCLE_FOLDER:// 文件夹循环
                Cursor cursor1 = MediaDBManager.getInstance(mContext).queryVideoExcludeFolder
                        (playingVideoUri.substring(0, playingVideoUri.lastIndexOf(File.separator)));
                if (cursor1 != null) {
                    String videoUri = null;
                    while (cursor1.moveToNext()) {
                        videoUri = cursor1.getString(cursor1.getColumnIndex(DBConfiguration
                                .VideoConfiguration.VIDEO_URI));
                        if (videoUri.equals(playingVideoUri)) {// 找到正在播放的视频文件的位置
                            if (!cursor1.moveToPrevious()) {// 如果没有上一个，说明到顶了
                                // 游标移至底部
                                cursor1.moveToLast();
                            }
                            videoUri = cursor1.getString(cursor1.getColumnIndex(DBConfiguration
                                    .VideoConfiguration.VIDEO_URI));
                            break;
                        }
                    }
                    cursor1.close();
                    return videoUri;
                }
                break;
            case MusicConstants.MODE_CIRCLE_SINGLE:// 单曲循环（手动切换时同全部循环）
                // return playingVideoUri;
            case MusicConstants.MODE_CIRCLE_ALL:// 全部循环
                int usbFlag = -1;
                if (playingVideoUri.startsWith(MultimediaConstants.PATH_USB1)) {
                    usbFlag = MultimediaConstants.FLAG_USB1;
                } else if (playingVideoUri.startsWith(MultimediaConstants.PATH_USB2)) {
                    usbFlag = MultimediaConstants.FLAG_USB2;
                }

                if (usbFlag == -1) {
                    return null;
                }
                Cursor cursor3 = MediaDBManager.getInstance(mContext).queryAllVideo(usbFlag);
                if (cursor3 != null) {
                    String videoUri = null;
                    while (cursor3.moveToNext()) {
                        videoUri = cursor3.getString(cursor3.getColumnIndex(DBConfiguration
                                .VideoConfiguration.VIDEO_URI));
                        if (videoUri.equals(playingVideoUri)) {// 找到正在播放的视频文件的位置
                            if (!cursor3.moveToPrevious()) {// 如果没有上一个，说明到顶了
                                // 游标移至底部
                                cursor3.moveToLast();
                            }
                            videoUri = cursor3.getString(cursor3.getColumnIndex(DBConfiguration
                                    .VideoConfiguration.VIDEO_URI));
                            break;
                        }
                    }
                    cursor3.close();
                    return videoUri;
                }
                break;
            case MusicConstants.MODE_RANDOM:// 随机播放
                Cursor cursor2 = MediaDBManager.getInstance(mContext).queryVideoExcludeFolder
                        (playingVideoUri.substring(0, playingVideoUri.lastIndexOf(File.separator)));
                if (cursor2 != null) {
                    int position = (int) (Math.random() * cursor2.getCount());
                    if (cursor2.moveToPosition(position)) {
                        String musicUri = cursor2.getString(cursor2.getColumnIndex
                                (DBConfiguration.VideoConfiguration.VIDEO_URI));
                        cursor2.close();
                        return musicUri;
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public String getNextPlayVideoUriByPlayMode(boolean fromUser) {// 获取下一个要播放的视频Uri
        String playingVideoUri = PreferencesUtil.getInstance().getCurrentPlayingVideoUri();
        if (TextUtils.isEmpty(playingVideoUri) || !new File(playingVideoUri).exists()) {
            return null;
        }

        switch (PreferencesUtil.getInstance().getVideoPlayMode()) {
            case MusicConstants.MODE_CIRCLE_FOLDER:// 文件夹循环
                Cursor cursor1 = MediaDBManager.getInstance(mContext).queryVideoExcludeFolder
                        (playingVideoUri.substring(0, playingVideoUri.lastIndexOf(File.separator)));
                if (cursor1 != null) {
                    String videoUri = null;
                    while (cursor1.moveToNext()) {
                        videoUri = cursor1.getString(cursor1.getColumnIndex(DBConfiguration
                                .VideoConfiguration.VIDEO_URI));
                        if (videoUri.equals(playingVideoUri)) {// 找到正在播放的视频文件的位置
                            if (!cursor1.moveToNext()) {// 如果没有下一个，说明到底了
                                // 游标移至顶部
                                cursor1.moveToFirst();
                            }
                            videoUri = cursor1.getString(cursor1.getColumnIndex(DBConfiguration
                                    .VideoConfiguration.VIDEO_URI));
                            break;
                        }
                    }
                    cursor1.close();
                    return videoUri;
                }
                break;
            case MusicConstants.MODE_CIRCLE_SINGLE:// 单曲循环
                if (!fromUser) {// 如果fromUser，说明是用户手动切换下一首，此时切换下一首，否则单曲播放
                    return playingVideoUri;
                }
            case MusicConstants.MODE_CIRCLE_ALL:// 全部循环
                int usbFlag = -1;
                if (playingVideoUri.startsWith(MultimediaConstants.PATH_USB1)) {
                    usbFlag = MultimediaConstants.FLAG_USB1;
                } else if (playingVideoUri.startsWith(MultimediaConstants.PATH_USB2)) {
                    usbFlag = MultimediaConstants.FLAG_USB2;
                }

                if (usbFlag == -1) {
                    return null;
                }
                Cursor cursor3 = MediaDBManager.getInstance(mContext).queryAllVideo(usbFlag);
                if (cursor3 != null) {
                    String videoUri = null;
                    while (cursor3.moveToNext()) {
                        videoUri = cursor3.getString(cursor3.getColumnIndex(DBConfiguration
                                .VideoConfiguration.VIDEO_URI));
                        if (videoUri.equals(playingVideoUri)) {// 找到正在播放的视频文件的位置
                            if (!cursor3.moveToNext()) {// 如果没有下一个，说明到底了
                                // 游标移至顶部
                                cursor3.moveToFirst();
                            }
                            videoUri = cursor3.getString(cursor3.getColumnIndex(DBConfiguration
                                    .VideoConfiguration.VIDEO_URI));
                            break;
                        }
                    }
                    cursor3.close();
                    return videoUri;
                }
                break;
            case MusicConstants.MODE_RANDOM:// 随机播放
                Cursor cursor2 = MediaDBManager.getInstance(mContext).queryVideoExcludeFolder
                        (playingVideoUri.substring(0, playingVideoUri.lastIndexOf(File.separator)));
                if (cursor2 != null) {
                    int position = (int) (Math.random() * cursor2.getCount());
                    if (cursor2.moveToPosition(position)) {
                        String musicUri = cursor2.getString(cursor2.getColumnIndex
                                (DBConfiguration.VideoConfiguration.VIDEO_URI));
                        cursor2.close();
                        return musicUri;
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public int getVideoPosition(int usbFlag, String videoUri) {// 获取视频在当前列表下的位置
        int position = -1;
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                for (String uri : usb1ShowingVideoList) {
                    position++;
                    if (uri.equals(videoUri)) {
                        return position;
                    }
                }
                break;
            case MultimediaConstants.FLAG_USB2:
                for (String uri : usb2ShowingVideoList) {
                    position++;
                    if (uri.equals(videoUri)) {
                        return position;
                    }
                }
                break;
            default:
                break;
        }
        return -1;
    }

    @Override
    public void deleteVideoUri(int usbFlag) {
        // 删除视频信息
        MediaDBManager.getInstance(mContext).deleteVideo(usbFlag);
        // 通知VideoFragment
        if (onVideoInfosChangeListener != null) {
            onVideoInfosChangeListener.onVideoInfosClear(usbFlag);
        }
    }

    @Override
    public void deleteAllMediaUri(int usbFlag) {
        this.deletePhotoUri(usbFlag);
        this.deleteMusicUri(usbFlag);
        this.deleteLyricUri(usbFlag);
        this.deleteVideoUri(usbFlag);
    }
}