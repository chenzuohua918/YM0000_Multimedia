package com.semisky.ym_multimedia.photo.model;

import android.text.TextUtils;

import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoUriSubModel {
    private static PhotoUriSubModel instance;
    private List<PhotoUriSubResultCallback> callbacks;

    private PhotoUriSubModel() {
        callbacks = new ArrayList<PhotoUriSubResultCallback>();
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new PhotoUriSubModel();
        }
    }

    public static PhotoUriSubModel getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    /**
     * 注册回调监听器
     */
    public void registerPhotoUriSubResultCallback(PhotoUriSubResultCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    /**
     * 注销回调监听器
     */
    public void unregisterPhotoUriSubResultCallback(PhotoUriSubResultCallback callback) {
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
                    for (PhotoUriSubResultCallback callback : callbacks) {
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
                    for (PhotoUriSubResultCallback callback : callbacks) {
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
     * 分解图片路径
     */
    public void subPhotoUri(int usbFlag, String rootDirectory, String photoUri) {
        if (TextUtils.isEmpty(rootDirectory) || TextUtils.isEmpty(photoUri)) {
            return;
        }

        if (photoUri.startsWith(rootDirectory)) {
            if (new File(photoUri).isDirectory()) {// 文件夹
                for (PhotoUriSubResultCallback callback : callbacks) {
                    if (callback != null) {
                        callback.onPhotoUriSubResultFolder(usbFlag, rootDirectory, photoUri);
                    }
                }
            } else {// 图片文件
                for (PhotoUriSubResultCallback callback : callbacks) {
                    if (callback != null) {
                        callback.onPhotoUriSubResultFile(usbFlag, rootDirectory, photoUri);
                    }
                }
            }
        }
    }

    /**
     * 回调接口
     */
    public interface PhotoUriSubResultCallback {
        // 分析之后确定为文件夹
        void onPhotoUriSubResultFolder(int usbFlag, String rootDirectory, String folderUri);

        // 分析之后确定为图片文件
        void onPhotoUriSubResultFile(int usbFlag, String rootDirectory, String photoUri);

        void onBackToParentDirectory(int usbFlag, String parentDir);
    }
}
