package com.semisky.ym_multimedia.ymbluetooth.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

import com.semisky.ym_multimedia.R;

public class VerticalTextView extends TextView {
	public final static int ORIENTATION_DEFAULT = 0;// TextView默认样式
	// 以下只能一个方向到底，不可换行
	public final static int ORIENTATION_UP_TO_DOWN = 1;// 从上往下走向（顺时针旋转90度）
	public final static int ORIENTATION_DOWN_TO_UP = 2;// 从下往上走向（逆时针旋转90度）
	public final static int ORIENTATION_LEFT_TO_RIGHT = 3;// 从左往右走向（不旋转）
	public final static int ORIENTATION_RIGHT_TO_LEFT = 4;// 从右往左走向（顺时针旋转180度）

	Rect text_bounds = new Rect();
	private int direction;

	public VerticalTextView(Context context) {
		super(context);
	}

	public VerticalTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.verticaltextview);
		direction = a.getInt(R.styleable.verticaltextview_vtdirection,
				ORIENTATION_DEFAULT);// 默认标准显示
		a.recycle();

		requestLayout();
		invalidate();
	}

	public void setDirection(int direction) {
		this.direction = direction;

		requestLayout();
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (direction == ORIENTATION_DEFAULT) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		} else {
			getPaint().getTextBounds(getText().toString(), 0,
					getText().length(), text_bounds);
			if (direction == ORIENTATION_LEFT_TO_RIGHT
					|| direction == ORIENTATION_RIGHT_TO_LEFT) {
				setMeasuredDimension(measureHeight(widthMeasureSpec),
						measureWidth(heightMeasureSpec));
			} else if (direction == ORIENTATION_UP_TO_DOWN
					|| direction == ORIENTATION_DOWN_TO_UP) {
				setMeasuredDimension(measureWidth(widthMeasureSpec),
						measureHeight(heightMeasureSpec));
			}
		}
	}

	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		} else {
			result = text_bounds.height() + getPaddingTop()
					+ getPaddingBottom();
			// result = text_bounds.height();
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		} else {
			result = text_bounds.width() + getPaddingLeft() + getPaddingRight();
			// result = text_bounds.width();
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (direction == ORIENTATION_DEFAULT) {
			super.onDraw(canvas);
			return;
		}

		canvas.save();

		int startX = 0;
		int startY = 0;
		int stopX = 0;
		int stopY = 0;
		Path path = new Path();
		if (direction == ORIENTATION_UP_TO_DOWN) {
			startX = (getWidth() - text_bounds.height() >> 1);
			startY = (getHeight() - text_bounds.width() >> 1);
			stopX = (getWidth() - text_bounds.height() >> 1);
			stopY = (getHeight() + text_bounds.width() >> 1);
			path.moveTo(startX, startY);
			path.lineTo(stopX, stopY);
		} else if (direction == ORIENTATION_DOWN_TO_UP) {
			startX = (getWidth() + text_bounds.height() >> 1);
			startY = (getHeight() + text_bounds.width() >> 1);
			stopX = (getWidth() + text_bounds.height() >> 1);
			stopY = (getHeight() - text_bounds.width() >> 1);
			path.moveTo(startX, startY);
			path.lineTo(stopX, stopY);
		} else if (direction == ORIENTATION_LEFT_TO_RIGHT) {
			startX = (getWidth() - text_bounds.width() >> 1);
			startY = (getHeight() + text_bounds.height() >> 1);
			stopX = (getWidth() + text_bounds.width() >> 1);
			stopY = (getHeight() + text_bounds.height() >> 1);
			path.moveTo(startX, startY);
			path.lineTo(stopX, stopY);

		} else if (direction == ORIENTATION_RIGHT_TO_LEFT) {
			startX = (getWidth() + text_bounds.width() >> 1);
			startY = (getHeight() - text_bounds.height() >> 1);
			stopX = (getWidth() - text_bounds.width() >> 1);
			stopY = (getHeight() - text_bounds.height() >> 1);
			path.moveTo(startX, startY);
			path.lineTo(stopX, stopY);
		}

		this.getPaint().setColor(this.getCurrentTextColor());
		// canvas.drawLine(startX, startY, stopX, stopY, this.getPaint());
		canvas.drawTextOnPath(getText().toString(), path, 0, 0, this.getPaint());

		canvas.restore();
	}

	public void setVText(CharSequence text){
		setText(text);
		String language = getResources().getConfiguration().locale
				.getLanguage();
		if (language.equals("en")) {// 当前系统语言为英文
			setDirection(VerticalTextView.ORIENTATION_UP_TO_DOWN);// 文字竖排
		} else {// 当前系统语言为中文
			setDirection(VerticalTextView.ORIENTATION_DEFAULT);// 文字标准排版
		}
	}
}
