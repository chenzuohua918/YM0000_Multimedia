package com.semisky.ym_multimedia.multimedia.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.semisky.ym_multimedia.MyApplication;
import com.semisky.ym_multimedia.music.utils.MusicConstants;

import java.lang.ref.WeakReference;

public class PreferencesUtil {
    private static WeakReference<PreferencesManager> mReference;

    private static synchronized void syncInit() {
        if (mReference == null || mReference.get() == null) {
            mReference = new WeakReference<PreferencesManager>(new PreferencesManager());
        }
    }

    public static PreferencesManager getInstance() {
        if (mReference == null || mReference.get() == null) {
            syncInit();
        }
        return mReference.get();
    }

    public static class PreferencesManager {
        private static final String FILE_NAME = "YM_Multimedia";

        private PreferencesManager() {
        }

        private SharedPreferences getSP() {
            return MyApplication.getInstance().getSharedPreferences(FILE_NAME, Context
                    .MODE_PRIVATE);
        }

        /**
         * 读取最后使用的多媒体（默认音乐）
         */
        public int getFinallyEnjoy() {
            return getSP().getInt("finallyEnjoy", MultimediaConstants.ENJOY_MUSIC);
        }

        /**
         * 保存最后使用的多媒体
         */
        public boolean setFinallyEnjoy(int finallyEnjoy) {
            return getSP().edit().putInt("finallyEnjoy", finallyEnjoy).commit();
        }

        // 图片

        /**
         * 读取图片最后选择的USB接口标识
         */
        public int getPhotoFinallyChooseUsbFlag() {
            return getSP().getInt("photoFinallyChooseUsbFlag", MultimediaConstants.FLAG_USB1);
        }

        /**
         * 保存图片最后选择的USB接口标识
         */
        public boolean setPhotoFinallyChooseUsbFlag(int usbFlag) {
            return getSP().edit().putInt("photoFinallyChooseUsbFlag", usbFlag).commit();
        }

        /**
         * 读取USB1的顶部目录路径
         */
        public String getPhotoUsb1RootDirectory() {
            return getSP().getString("photoUsb1RootDirectory", MultimediaConstants.PATH_USB1);
        }

        /**
         * 保存USB1的顶部目录路径
         */
        public boolean setPhotoUsb1RootDirectory(String rootDirectory) {
            return getSP().edit().putString("photoUsb1RootDirectory", rootDirectory).commit();
        }

        /**
         * 读取USB2的顶部目录路径
         */
        public String getPhotoUsb2RootDirectory() {
            return getSP().getString("photoUsb2RootDirectory", MultimediaConstants.PATH_USB2);
        }

        /**
         * 保存USB2的顶部目录路径
         */
        public boolean setPhotoUsb2RootDirectory(String rootDirectory) {
            return getSP().edit().putString("photoUsb2RootDirectory", rootDirectory).commit();
        }

        /**
         * 读取当前播放的图片文件路径
         */
        public String getCurrentPlayingPhotoUri() {
            return getSP().getString("currentPlayingPhotoUri", null);
        }

        /**
         * 保存当前播放的图片文件路径
         */
        public boolean setCurrentPlayingPhotoUri(String photoUri) {
            return getSP().edit().putString("currentPlayingPhotoUri", photoUri).commit();
        }

        /**
         * 最后图片是否在播放
         */
        public boolean isPhotoFinallyPlaying() {
            return getSP().getBoolean("photoFinallyPlaying", false);
        }

        /**
         * 保存最后图片是否在播放（只保存用户手动操作状态）
         */
        public boolean setPhotoFinallyPlaying(boolean isPlaying) {
            return getSP().edit().putBoolean("photoFinallyPlaying", isPlaying).commit();
        }

        // 音频

        /**
         * 读取图片最后选择的USB接口标识
         */
        public int getMusicFinallyChooseUsbFlag() {
            return getSP().getInt("musicFinallyChooseUsbFlag", MultimediaConstants.FLAG_USB1);
        }

        /**
         * 保存图片最后选择的USB接口标识
         */
        public boolean setMusicFinallyChooseUsbFlag(int usbFlag) {
            return getSP().edit().putInt("musicFinallyChooseUsbFlag", usbFlag).commit();
        }

        /**
         * 读取USB1的顶部目录路径
         */
        public String getMusicUsb1RootDirectory() {
            return getSP().getString("musicUsb1RootDirectory", MultimediaConstants.PATH_USB1);
        }

        /**
         * 保存USB1的顶部目录路径
         */
        public boolean setMusicUsb1RootDirectory(String rootDirectory) {
            return getSP().edit().putString("musicUsb1RootDirectory", rootDirectory).commit();
        }

        /**
         * 读取USB2的顶部目录路径
         */
        public String getMusicUsb2RootDirectory() {
            return getSP().getString("musicUsb2RootDirectory", MultimediaConstants.PATH_USB2);
        }

        /**
         * 保存USB2的顶部目录路径
         */
        public boolean setMusicUsb2RootDirectory(String rootDirectory) {
            return getSP().edit().putString("musicUsb2RootDirectory", rootDirectory).commit();
        }

        /**
         * 读取播放模式（默认文件夹循环）
         */
        public int getMusicPlayMode() {
            return getSP().getInt("musicPlayMode", MusicConstants.MODE_CIRCLE_FOLDER);
        }

        /**
         * 保存播放模式
         */
        public boolean setMusicPlayMode(int mode) {
            return getSP().edit().putInt("musicPlayMode", mode).commit();
        }

        /**
         * 读取当前播放的音乐路径
         */
        public String getCurrentPlayingMusicUri() {
            return getSP().getString("currentPlayingMusicUri", null);
        }

        /**
         * 保存当前播放的音乐路径
         */
        public boolean setCurrentPlayingMusicUri(String musicUri) {
            return getSP().edit().putString("currentPlayingMusicUri", musicUri).commit();
        }

        /**
         * 读取当前播放的音乐歌词路径
         */
        public String getCurrentPlayingMusicLyricUri() {
            return getSP().getString("currentPlayingMusicLyricUri", null);
        }

        /**
         * 保存当前播放的音乐歌词路径
         */
        public boolean setCurrentPlayingMusicLyricUri(String lyricUri) {
            return getSP().edit().putString("currentPlayingMusicLyricUri", lyricUri).commit();
        }

        /**
         * 读取当前播放的音乐的播放进度
         */
        public int getCurrentPlayingMusicProgress() {
            return getSP().getInt("currentPlayingMusicProgress", 0);
        }

        /**
         * 保存当前播放的音乐的播放进度
         */
        public boolean setCurrentPlayingMusicProgress(int progress) {
            return getSP().edit().putInt("currentPlayingMusicProgress", progress).commit();
        }

        /**
         * 最后音乐是否在播放
         */
        public boolean isMusicFinallyPlaying() {
            return getSP().getBoolean("musicFinallyPlaying", false);
        }

        /**
         * 保存最后音乐是否在播放（只保存用户手动操作状态）
         */
        public boolean setMusicFinallyPlaying(boolean isPlaying) {
            return getSP().edit().putBoolean("musicFinallyPlaying", isPlaying).commit();
        }

        /**
         * 是否显示歌词（显示歌词时，专辑图片隐藏），默认隐藏
         */
        public boolean isLyricViewShowing() {
            return getSP().getBoolean("lyricViewShowing", false);
        }

        /**
         * 保存是否显示歌词
         */
        public boolean setLyricViewShowing(boolean isShowing) {
            return getSP().edit().putBoolean("lyricViewShowing", isShowing).commit();
        }

        // 视频

        /**
         * 读取图片最后选择的USB接口标识
         */
        public int getVideoFinallyChooseUsbFlag() {
            return getSP().getInt("videoFinallyChooseUsbFlag", MultimediaConstants.FLAG_USB1);
        }

        /**
         * 保存图片最后选择的USB接口标识
         */
        public boolean setVideoFinallyChooseUsbFlag(int usbFlag) {
            return getSP().edit().putInt("videoFinallyChooseUsbFlag", usbFlag).commit();
        }

        /**
         * 读取USB1的顶部目录路径
         */
        public String getVideoUsb1RootDirectory() {
            return getSP().getString("videoUsb1RootDirectory", MultimediaConstants.PATH_USB1);
        }

        /**
         * 保存USB1的顶部目录路径
         */
        public boolean setVideoUsb1RootDirectory(String rootDirectory) {
            return getSP().edit().putString("videoUsb1RootDirectory", rootDirectory).commit();
        }

        /**
         * 读取USB2的顶部目录路径
         */
        public String getVideoUsb2RootDirectory() {
            return getSP().getString("videoUsb2RootDirectory", MultimediaConstants.PATH_USB2);
        }

        /**
         * 保存USB2的顶部目录路径
         */
        public boolean setVideoUsb2RootDirectory(String rootDirectory) {
            return getSP().edit().putString("videoUsb2RootDirectory", rootDirectory).commit();
        }

        /**
         * 读取播放模式（默认文件夹循环）
         */
        public int getVideoPlayMode() {
            return getSP().getInt("videoPlayMode", MusicConstants.MODE_CIRCLE_FOLDER);
        }

        /**
         * 保存播放模式
         */
        public boolean setVideoPlayMode(int mode) {
            return getSP().edit().putInt("videoPlayMode", mode).commit();
        }

        /**
         * 读取当前播放的视频文件路径
         */
        public String getCurrentPlayingVideoUri() {
            return getSP().getString("currentPlayingVideoUri", null);
        }

        /**
         * 保存当前播放的视频文件路径
         */
        public boolean setCurrentPlayingVideoUri(String videoUri) {
            return getSP().edit().putString("currentPlayingVideoUri", videoUri).commit();
        }

        /**
         * 读取当前播放的视频文件的播放进度
         */
        public int getCurrentPlayingVideoProgress() {
            return getSP().getInt("currentPlayingVideoProgress", 0);
        }

        /**
         * 保存当前播放的视频文件的播放进度
         */
        public boolean setCurrentPlayingVideoProgress(int progress) {
            return getSP().edit().putInt("currentPlayingVideoProgress", progress).commit();
        }

        /**
         * 最后视频是否在播放
         */
        public boolean isVideoFinallyPlaying() {
            return getSP().getBoolean("videoFinallyPlaying", false);
        }

        /**
         * 保存最后视频是否在播放（只保存用户手动操作状态）
         */
        public boolean setVideoFinallyPlaying(boolean isPlaying) {
            return getSP().edit().putBoolean("videoFinallyPlaying", isPlaying).commit();
        }
    }
}
