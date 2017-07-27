package com.semisky.ym_multimedia.video.model;

import android.os.Handler;
import android.os.Message;

import com.semisky.ym_multimedia.common.utils.CommonConstants;

import java.lang.ref.WeakReference;

public class VideoKeyLongClickModel {
    private static final int MSG_START_LONG_CLICK_PREV = 0X01;
    private static final int MSG_START_LONG_CLICK_NEXT = 0X02;
    private static final int MSG_RECEIVE_LONG_CLICK_PREV = 0X03;
    private static final int MSG_RECEIVE_LONG_CLICK_NEXT = 0X04;
    private static final int MSG_END_LONG_CLICK_PREV = 0X05;
    private static final int MSG_END_LONG_CLICK_NEXT = 0X06;
    private static VideoKeyLongClickModel instance;
    private LongClickHandler mHandler;
    private OnVideoKeyLongClickListener onVideoKeyLongClickListener;
    private boolean isKeyPrevPressing = false;// 是否正在按压上一曲按钮
    private boolean isKeyNextPressing = false;// 是否正在按压下一曲按钮
    private int longClickInterval = 150;// MCU发送长按事件的时间间隔为100毫秒多一点，加上需要留给响应代码运行的时间，暂时设置150毫秒

    private VideoKeyLongClickModel() {
        mHandler = new LongClickHandler(this);
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new VideoKeyLongClickModel();
        }
    }

    public static VideoKeyLongClickModel getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    public void setOnVideoKeyLongClickListener(OnVideoKeyLongClickListener onVideoKeyLongClickListener) {
        this.onVideoKeyLongClickListener = onVideoKeyLongClickListener;
    }

    /**
     * 接收到上一曲按钮长按事件
     */
    public void receiveKeyLongClickPrevEvent() {
        // 如果队列中有上一曲按钮长按结束消息，先移除该消息
        mHandler.removeMessages(MSG_END_LONG_CLICK_PREV);

        if (!isKeyPrevPressing) {// 当前没有正在按压上一曲按钮，说明是接收到第一个上一曲按钮长按事件
            isKeyPrevPressing = true;
            // 开始回调
            mHandler.sendEmptyMessage(MSG_START_LONG_CLICK_PREV);
        }

        mHandler.sendEmptyMessage(MSG_RECEIVE_LONG_CLICK_PREV);

        // 发送延迟消息，longClickInterval毫秒后如果还没有接收到下一个上一曲按钮长按消息，说明长按结束
        mHandler.sendEmptyMessageDelayed(MSG_END_LONG_CLICK_PREV, longClickInterval);
    }

    /**
     * 接收到下一曲按钮长按事件
     */
    public void receiveKeyLongClickNextEvent() {
        // 如果队列中有下一曲按钮长按结束消息，先移除该消息
        mHandler.removeMessages(MSG_END_LONG_CLICK_NEXT);

        if (!isKeyNextPressing) {// 当前没有正在按压下一曲按钮，说明是接收到第一个下一曲按钮长按事件
            isKeyNextPressing = true;
            // 开始回调
            mHandler.sendEmptyMessage(MSG_START_LONG_CLICK_NEXT);
        }

        mHandler.sendEmptyMessage(MSG_RECEIVE_LONG_CLICK_NEXT);

        // 发送延迟消息，longClickInterval毫秒后如果还没有接收到下一个下一曲按钮长按消息，说明长按结束
        mHandler.sendEmptyMessageDelayed(MSG_END_LONG_CLICK_NEXT, longClickInterval);
    }

    public interface OnVideoKeyLongClickListener {
        void onStartKeyLongClick(int keyCode);

        void onStopKeyLongClick(int keyCode);

        void onReceiveKeyLongClickEvent(int keyCode);
    }

    private static class LongClickHandler extends Handler {
        private static WeakReference<VideoKeyLongClickModel> mReference;

        public LongClickHandler(VideoKeyLongClickModel model) {
            mReference = new WeakReference<VideoKeyLongClickModel>(model);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mReference.get() == null) {
                return;
            }

            switch (msg.what) {
                case MSG_START_LONG_CLICK_PREV:// 开始长按上一曲按钮
                    if (mReference.get().onVideoKeyLongClickListener != null) {
                        mReference.get().onVideoKeyLongClickListener.onStartKeyLongClick
								(CommonConstants.KEY_PREV);
                    }
                    break;
                case MSG_START_LONG_CLICK_NEXT:// 开始长按下一曲按钮
                    if (mReference.get().onVideoKeyLongClickListener != null) {
                        mReference.get().onVideoKeyLongClickListener.onStartKeyLongClick
								(CommonConstants.KEY_NEXT);
                    }
                    break;
                case MSG_RECEIVE_LONG_CLICK_PREV:
                    if (mReference.get().onVideoKeyLongClickListener != null) {
                        mReference.get().onVideoKeyLongClickListener.onReceiveKeyLongClickEvent
								(CommonConstants.KEY_PREV);
                    }
                    break;
                case MSG_RECEIVE_LONG_CLICK_NEXT:
                    if (mReference.get().onVideoKeyLongClickListener != null) {
                        mReference.get().onVideoKeyLongClickListener.onReceiveKeyLongClickEvent
								(CommonConstants.KEY_NEXT);
                    }
                    break;
                case MSG_END_LONG_CLICK_PREV:// 接收到该消息说明上一曲按钮长按事件已经结束
                    // 重置变量
                    mReference.get().isKeyPrevPressing = false;
                    // 结束回调
                    if (mReference.get().onVideoKeyLongClickListener != null) {
                        mReference.get().onVideoKeyLongClickListener.onStopKeyLongClick
								(CommonConstants.KEY_PREV);
                    }
                    break;
                case MSG_END_LONG_CLICK_NEXT:// 接收到该消息说明下一曲按钮长按事件已经结束
                    // 重置变量
                    mReference.get().isKeyNextPressing = false;
                    // 结束回调
                    if (mReference.get().onVideoKeyLongClickListener != null) {
                        mReference.get().onVideoKeyLongClickListener.onStopKeyLongClick
								(CommonConstants.KEY_NEXT);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
