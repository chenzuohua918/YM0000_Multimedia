package com.semisky.ym_multimedia.music.model;

import android.text.TextUtils;

import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicUriSubModel {
    private static MusicUriSubModel instance;
    private List<MusicUriSubResultCallback> callbacks;

    private MusicUriSubModel() {
        callbacks = new ArrayList<MusicUriSubResultCallback>();
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new MusicUriSubModel();
        }
    }

    public static MusicUriSubModel getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    /**
     * 注册回调监听器
     */
    public void registerMusicUriSubResultCallback(MusicUriSubResultCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    /**
     * 注销回调监听器
     */
    public void unregisterMusicUriSubResultCallback(MusicUriSubResultCallback callback) {
        callbacks.remove(callback);
    }

    /**
     * 回到上一级目录
     */
    public void backToParentDir(int usbFlag, String currentDir) {
        if (TextUtils.isEmpty(currentDir)) {
            return;
        }

        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                if (MultimediaConstants.PATH_USB1.equals(currentDir)) {//  已经是根目录，不做操作
                    return;
                } else {// 否则，返回上一层目录
                    for (MusicUriSubResultCallback callback : callbacks) {
                        if (callback != null) {
                            callback.onBackToParentDirectory(MultimediaConstants.FLAG_USB1, currentDir.substring(0, currentDir.lastIndexOf(File
											.separator)));
                        }
                    }
                }
                break;
            case MultimediaConstants.FLAG_USB2:
                if (MultimediaConstants.PATH_USB2.equals(currentDir)) {//  已经是根目录，不做操作
                    return;
                } else {// 否则，返回上一层目录
                    for (MusicUriSubResultCallback callback : callbacks) {
                        if (callback != null) {
                            callback.onBackToParentDirectory(MultimediaConstants.FLAG_USB2,
									currentDir.substring(0, currentDir.lastIndexOf(File
											.separator)));
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 分解歌曲路径
     */
    public void subMusicUri(int usbFlag, String rootDirectory, String musicUri) {
        if (TextUtils.isEmpty(rootDirectory) || TextUtils.isEmpty(musicUri)) {
            return;
        }

        if (musicUri.startsWith(rootDirectory)) {
            if (new File(musicUri).isDirectory()) {// 文件夹
                for (MusicUriSubResultCallback callback : callbacks) {
                    if (callback != null) {
                        callback.onMusicUriSubResultFolder(usbFlag, rootDirectory, musicUri);
                    }
                }
            } else {// 音乐文件
                for (MusicUriSubResultCallback callback : callbacks) {
                    if (callback != null) {
                        callback.onMusicUriSubResultFile(usbFlag, rootDirectory, musicUri);
                    }
                }
            }
        }
    }

    /**
     * 回调接口
     */
    public interface MusicUriSubResultCallback {
        // 分析之后确定为文件夹
        void onMusicUriSubResultFolder(int usbFlag, String rootDirectory, String folderUri);

        // 分析之后确定为音乐文件
        void onMusicUriSubResultFile(int usbFlag, String rootDirectory, String musicUri);

        void onBackToParentDirectory(int usbFlag, String parentDir);
    }
}
