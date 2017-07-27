package com.semisky.ym_multimedia.video.model;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 视频播放页控制视图的显示隐藏控制器
 *
 * @author Anter
 */
public class VideoConsoleViewModel {
    private static final int MSG_HIDE = 0x01;// 隐藏控制视图
    private static VideoConsoleViewModel instance;
    private ModelHandler mHandler;
    private VideoConsoleViewStateCallback callback;
    private boolean enableToHide = true;// 是否允许隐藏控制视图
    private int hide_time = 3000;// 多久没操作后隐藏

    private VideoConsoleViewModel() {
        mHandler = new ModelHandler(this);
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new VideoConsoleViewModel();
        }
    }

    public static VideoConsoleViewModel getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    public void setCallback(VideoConsoleViewStateCallback callback) {
        this.callback = callback;
    }

    public boolean isEnableToHide() {
        return enableToHide;
    }

    public void setEnableToHide(boolean enableToHide) {
        this.enableToHide = enableToHide;
    }

    /**
     * 显示控制视图
     *
     * @param hide_later 是否稍后自动隐藏
     */
    public void showVideoConsoleView(boolean hide_later) {
        // 显示回调
        if (callback != null) {
            callback.onShowVideoConsoleView();
        }
        if (hide_later) {
            // 开始倒计时
            startCountdown();
        } else {
            // 停止计时
            cancelCountdown();
        }
    }

    /**
     * 隐藏控制视图
     */
    public void hideVideoConsoleView() {
        if (!enableToHide) {
            return;
        }

        // 取消计时
        cancelCountdown();
        // 隐藏回调
        if (callback != null) {
            callback.onHideVideoConsoleView();
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
     * 停止倒计时
     */
    public void cancelCountdown() {
        mHandler.removeMessages(MSG_HIDE);
    }

    public interface VideoConsoleViewStateCallback {
        // 设置控制视图显示
        void onShowVideoConsoleView();

        // 设置控制视图隐藏
        void onHideVideoConsoleView();
    }

    private static class ModelHandler extends Handler {
        private static WeakReference<VideoConsoleViewModel> mReference;

        public ModelHandler(VideoConsoleViewModel controller) {
            mReference = new WeakReference<VideoConsoleViewModel>(controller);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mReference.get() == null) {
                return;
            }

            switch (msg.what) {
                case MSG_HIDE:// 隐藏控制视图
                    mReference.get().hideVideoConsoleView();
                    break;
                default:
                    break;
            }
        }
    }
}
