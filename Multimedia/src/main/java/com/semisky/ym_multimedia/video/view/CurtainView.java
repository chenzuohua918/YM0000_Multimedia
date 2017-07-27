package com.semisky.ym_multimedia.video.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * 幕布View
 * 
 * @author Anter
 * 
 */
public class CurtainView extends View {
	private ObjectAnimator dismissAnimator;// 幕布隐藏动画

	public CurtainView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// 默认是不显示幕布的
		setAlpha(0f);
	}

	public CurtainView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CurtainView(Context context) {
		this(context, null);
	}

	/** 显示幕布 */
	public void show() {
		// 如果隐藏动画正在执行，应先停止该动画
		if (dismissAnimator != null && dismissAnimator.isRunning()) {
			dismissAnimator.cancel();
		}
		// 显示时不做渐变
		setAlpha(1f);
	}

	/** 隐藏幕布 */
	public void dismiss() {
		// 隐藏时做渐变
		if (dismissAnimator == null) {
			dismissAnimator = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.0f);
			dismissAnimator.setDuration(1000);
			dismissAnimator.setInterpolator(new LinearInterpolator());
		}
		dismissAnimator.start();
	}
}
