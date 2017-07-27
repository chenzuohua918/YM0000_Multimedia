package com.semisky.ym_multimedia.multimedia.view;

import java.lang.ref.WeakReference;

import com.semisky.ym_multimedia.R;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class RightFileManagerView extends LinearLayout {
	private MyHandler myHandler;
	private OnRightFileManagerViewStateListener listener;
	private int hide_time = 10000;

	private static final int MSG_HIDE = 0x01;

	public RightFileManagerView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.layout_right_filemanager,
				this);
		myHandler = new MyHandler(this);
	}

	public RightFileManagerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RightFileManagerView(Context context) {
		this(context, null);
	}

	public interface OnRightFileManagerViewStateListener {
		void onRightFileManagerViewShow();

		void onRightFileManagerViewHide();
	}

	public void setOnRightFileManagerViewStateListener(
			OnRightFileManagerViewStateListener listener) {
		this.listener = listener;
	}

	private static class MyHandler extends Handler {
		private static WeakReference<RightFileManagerView> mReference;

		public MyHandler(RightFileManagerView view) {
			mReference = new WeakReference<RightFileManagerView>(view);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mReference.get() == null) {
				return;
			}

			switch (msg.what) {
			case MSG_HIDE:
				mReference.get().hide();
				break;
			default:
				break;
			}
		}
	}

	public boolean isVisible() {
		return getVisibility() == VISIBLE;
	}

	/** 显示 */
	public void show() {
		setVisibility(VISIBLE);
		// 开始隐藏倒计时
		startHideCountDown();

		if (listener != null) {
			listener.onRightFileManagerViewShow();
		}
	}

	/** 隐藏 */
	public void hide() {
		setVisibility(INVISIBLE);

		if (listener != null) {
			listener.onRightFileManagerViewHide();
		}
	}

	/** 开始隐藏倒计时 */
	public void startHideCountDown() {
		myHandler.removeMessages(MSG_HIDE);
		myHandler.sendEmptyMessageDelayed(MSG_HIDE, hide_time);
	}

	/** 取消隐藏倒计时 */
	public void cancelHideCountDown() {
		myHandler.removeMessages(MSG_HIDE);
	}
}
