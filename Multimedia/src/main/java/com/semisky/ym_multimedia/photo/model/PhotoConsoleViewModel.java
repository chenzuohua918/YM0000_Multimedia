package com.semisky.ym_multimedia.photo.model;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 图片播放页控制视图的显示隐藏控制器
 *
 * @author Anter
 */
public class PhotoConsoleViewModel {
    private static final int MSG_HIDE = 0x01;// 隐藏控制视图
    private static PhotoConsoleViewModel instance;
    private ModelHandler mHandler;
    private PhotoConsoleViewStateCallback callback;
    private int hide_time = 3000;// 多久没操作后隐藏

    private PhotoConsoleViewModel() {
        mHandler = new ModelHandler(this);
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new PhotoConsoleViewModel();
        }
    }

    public static PhotoConsoleViewModel getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    public void setCallback(PhotoConsoleViewStateCallback callback) {
        this.callback = callback;
    }

    /**
     * 显示控制视图
     */
    public void showPhotoConsoleView() {
        // 显示回调
        if (callback != null) {
            callback.onShowPhotoConsoleView();
        }
        // 开始倒计时
        startCountdown();
    }

    /**
     * 隐藏控制视图
     *
     * @param time_out 是否是超时隐藏
     */
    public void hidePhotoConsoleView(boolean time_out) {
        // 取消计时
        cancelCountdown();
        // 隐藏回调
        if (callback != null) {
            callback.onHidePhotoConsoleView(time_out);
        }
    }

    /**
     * 开始隐藏倒计时
     */
    private void startCountdown() {
        mHandler.sendEmptyMessageDelayed(MSG_HIDE, hide_time);
    }

    /**
     * 重新开始隐藏倒计时
     */
    public void restartCountdown() {
        mHandler.removeMessages(MSG_HIDE);
        startCountdown();
    }

    /**
     * 停止隐藏倒计时
     */
    public void cancelCountdown() {
        mHandler.removeMessages(MSG_HIDE);
    }

    public interface PhotoConsoleViewStateCallback {
        // 设置控制视图显示
        void onShowPhotoConsoleView();

        // 设置控制视图隐藏
        void onHidePhotoConsoleView(boolean time_out);
    }

    private static class ModelHandler extends Handler {
        private static WeakReference<PhotoConsoleViewModel> mReference;

        public ModelHandler(PhotoConsoleViewModel controller) {
            mReference = new WeakReference<PhotoConsoleViewModel>(controller);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mReference.get() == null) {
                return;
            }

            switch (msg.what) {
                case MSG_HIDE:
                    mReference.get().hidePhotoConsoleView(true);
                    break;
                default:
                    break;
            }
        }
    }
}
