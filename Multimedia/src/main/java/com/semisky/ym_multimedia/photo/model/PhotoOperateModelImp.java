package com.semisky.ym_multimedia.photo.model;

import android.content.Context;
import android.text.TextUtils;

import com.semisky.ym_multimedia.multimedia.dao.MediaScanner;
import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;
import com.semisky.ym_multimedia.multimedia.utils.PreferencesUtil;

/**
 * 图片操作Model实现类
 *
 * @author Anter
 */
public class PhotoOperateModelImp implements PhotoOperateModel {
    private static PhotoOperateModelImp instance;
    private Context mContext;
    private PhotoOperateModelCallback callback;

    private PhotoOperateModelImp(Context context) {
        this.mContext = context;
    }

    private static synchronized void syncInit(Context context) {
        if (instance == null) {
            instance = new PhotoOperateModelImp(context);
        }
    }

    public static PhotoOperateModelImp getInstance(Context context) {
        if (instance == null) {
            syncInit(context);
        }
        return instance;
    }

    @Override
    public void setPhotoOperateModelCallback(PhotoOperateModelCallback callback) {// 设置图片控制回调接口
        this.callback = callback;
    }

    @Override
    public void resumePlayState() {// 恢复播放状态
        if (callback != null) {
            // 获取当前展示的图片路径
            String currentPlayingPhotoUri = PreferencesUtil.getInstance()
                    .getCurrentPlayingPhotoUri();
            if (TextUtils.isEmpty(currentPlayingPhotoUri)) {// 如果没有图片文件，不能点开始
                return;
            } else {
                if (currentPlayingPhotoUri.startsWith(MultimediaConstants.PATH_USB1)) {
                    if (TextUtils.isEmpty(MediaScanner.getInstance(mContext)
                            .getFirstScannedUsb1PhotoUri())) {// 没有扫描到U盘1的第一张图片
                        return;
                    }
                } else if (currentPlayingPhotoUri.startsWith(MultimediaConstants.PATH_USB2)) {
                    if (TextUtils.isEmpty(MediaScanner.getInstance(mContext)
                            .getFirstScannedUsb2PhotoUri())) {// 没有扫描到U盘2第一张图片
                        return;
                    }
                }
            }

            if (PreferencesUtil.getInstance().isPhotoFinallyPlaying()) {// 如果最后在播放
                callback.onSwitchOn();
            } else {// 如果没在播放
                callback.onSwitchOff();
            }
        }
    }

    @Override
    public void switchOn() {// 开关打开（播放）
        if (callback != null) {// 为空，说明PhotoFragment不在前台，不响应操作
            // 获取当前展示的图片路径
            String currentPlayingPhotoUri = PreferencesUtil.getInstance()
                    .getCurrentPlayingPhotoUri();
            if (TextUtils.isEmpty(currentPlayingPhotoUri)) {// 如果没有图片文件，不能点开始
                return;
            } else {
                if (currentPlayingPhotoUri.startsWith(MultimediaConstants.PATH_USB1)) {
                    if (TextUtils.isEmpty(MediaScanner.getInstance(mContext)
                            .getFirstScannedUsb1PhotoUri())) {// 没有扫描到U盘1的第一张图片
                        return;
                    }
                } else if (currentPlayingPhotoUri.startsWith(MultimediaConstants.PATH_USB2)) {
                    if (TextUtils.isEmpty(MediaScanner.getInstance(mContext)
                            .getFirstScannedUsb2PhotoUri())) {// 没有扫描到U盘2第一张图片
                        return;
                    }
                }
            }

            if (!PreferencesUtil.getInstance().isPhotoFinallyPlaying()) {// 如果没在播放
                PreferencesUtil.getInstance().setPhotoFinallyPlaying(true);
                callback.onSwitchOn();
            }
        }
    }

    @Override
    public void switchOff() {// 开关关闭（暂停）
        if (callback != null// 为空，说明PhotoFragment不在前台，不响应操作
                && PreferencesUtil.getInstance().isPhotoFinallyPlaying()) {// 如果正在播放
            PreferencesUtil.getInstance().setPhotoFinallyPlaying(false);
            callback.onSwitchOff();
        }
    }

    @Override
    public void switchOnOff() {// 开关打开或关闭（播放或暂停）
        if (callback != null) {// 为空，说明PhotoFragment不在前台，不响应操作
            // 获取当前展示的图片路径
            String currentPlayingPhotoUri = PreferencesUtil.getInstance()
                    .getCurrentPlayingPhotoUri();
            if (TextUtils.isEmpty(currentPlayingPhotoUri)) {// 如果没有图片文件，不能点开始
                return;
            } else {
                if (currentPlayingPhotoUri.startsWith(MultimediaConstants.PATH_USB1)) {
                    if (TextUtils.isEmpty(MediaScanner.getInstance(mContext)
                            .getFirstScannedUsb1PhotoUri())) {// 没有扫描到U盘1的第一张图片
                        return;
                    }
                } else if (currentPlayingPhotoUri.startsWith(MultimediaConstants.PATH_USB2)) {
                    if (TextUtils.isEmpty(MediaScanner.getInstance(mContext)
                            .getFirstScannedUsb2PhotoUri())) {// 没有扫描到U盘2第一张图片
                        return;
                    }
                }
            }

            if (PreferencesUtil.getInstance().isPhotoFinallyPlaying()) {// 如果正在播放
                PreferencesUtil.getInstance().setPhotoFinallyPlaying(false);
                callback.onSwitchOff();
            } else {
                PreferencesUtil.getInstance().setPhotoFinallyPlaying(true);
                callback.onSwitchOn();
            }
        }
    }

    @Override
    public void previousPhoto() {// 上一张图片
        if (callback != null) {
            callback.onPreviousPhoto();
        }
    }

    @Override
    public void nextPhoto() {// 下一张图片
        if (callback != null) {
            callback.onNextPhoto();
        }
    }

    @Override
    public void rotateLeft() {// 左翻转
        if (callback != null) {
            callback.onRotateLeft();
        }
    }

    @Override
    public void rotateRight() {// 右翻转
        if (callback != null) {
            callback.onRotateRight();
        }
    }

    @Override
    public void scaleBig() {// 放大
        if (callback != null) {
            callback.onScaleBig();
        }
    }

    @Override
    public void scaleSmall() {// 缩小
        if (callback != null) {
            callback.onScaleSmall();
        }
    }
}