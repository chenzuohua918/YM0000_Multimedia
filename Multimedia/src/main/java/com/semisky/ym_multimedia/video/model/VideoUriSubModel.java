package com.semisky.ym_multimedia.video.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;

public class VideoUriSubModel {
    private static VideoUriSubModel instance;
    private List<VideoUriSubResultCallback> callbacks;

    private VideoUriSubModel() {
        callbacks = new ArrayList<VideoUriSubResultCallback>();
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new VideoUriSubModel();
        }
    }

    public static VideoUriSubModel getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    /**
     * 注册回调监听器
     */
    public void registerVideoUriSubResultCallback(VideoUriSubResultCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    /**
     * 注销回调监听器
     */
    public void unregisterVideoUriSubResultCallback(VideoUriSubResultCallback callback) {
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
                    for (VideoUriSubResultCallback callback : callbacks) {
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
                    for (VideoUriSubResultCallback callback : callbacks) {
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
     * 分解视频路径
     */
    public void subVideoUri(int usbFlag, String rootDirectory, String videoUri) {
        if (TextUtils.isEmpty(rootDirectory) || TextUtils.isEmpty(videoUri)) {
            return;
        }

        if (videoUri.startsWith(rootDirectory)) {
            if (new File(videoUri).isDirectory()) {// 文件夹
                for (VideoUriSubResultCallback callback : callbacks) {
                    if (callback != null) {
                        callback.onVideoUriSubResultFolder(usbFlag, rootDirectory, videoUri);
                    }
                }
            } else {// 视频文件
                for (VideoUriSubResultCallback callback : callbacks) {
                    if (callback != null) {
                        callback.onVideoUriSubResultFile(usbFlag, rootDirectory, videoUri);
                    }
                }
            }
        }
    }

    /**
     * 回调接口
     */
    public interface VideoUriSubResultCallback {
        // 分析之后确定为文件夹
        void onVideoUriSubResultFolder(int usbFlag, String rootDirectory, String folderUri);

        // 分析之后确定为视频文件
        void onVideoUriSubResultFile(int usbFlag, String rootDirectory, String videoUri);

        void onBackToParentDirectory(int usbFlag, String parentDir);
    }
}
