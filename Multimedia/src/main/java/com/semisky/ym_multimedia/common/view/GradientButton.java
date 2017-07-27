package com.semisky.ym_multimedia.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;

import com.semisky.ym_multimedia.R;

/**
 * 渐变色背景Button
 * 
 * @author Anter
 * 
 */
public class GradientButton extends Button {
	private int mNormalStartColor;// 不按压起始颜色
	private int mNormalEndColor;// 不按压结束颜色
	private int mPressedStartColor;// 按压起始颜色
	private int mPressedEndColor;// 按压结束颜色
	private int mDirection;// 渐变方向

	public static final int SHAPE_RECTAGLE = 0;// 矩形
	public static final int SHAPE_OVAL = 1;// 椭圆
	public static final int SHAPE_LINE = 2;// 水平直线
	public static final int SHAPE_RING = 3;// 环形

	public static final int LEFT_RIGHT = 0;// 从左到右
	public static final int TOP_BOTTOM = 1;// 从上到下
	public static final int RIGHT_LEFT = 2;// 从右到左
	public static final int BOTTOM_TOP = 3;// 从下到上

	public GradientButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// 文字水平居中显示
		setGravity(Gravity.CENTER_HORIZONTAL);

		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.GradientView);
		mNormalStartColor = typedArray.getColor(
				R.styleable.GradientView_startColor_normal, 0xFFFFFFFF);
		mNormalEndColor = typedArray.getColor(
				R.styleable.GradientView_endColor_normal, 0xFFFFFFFF);
		mPressedStartColor = typedArray.getColor(
				R.styleable.GradientView_startColor_pressed, 0xFFFFFFFF);
		mPressedEndColor = typedArray.getColor(
				R.styleable.GradientView_endColor_pressed, 0xFFFFFFFF);
		mDirection = typedArray.getInt(R.styleable.GradientView_direction,
				LEFT_RIGHT);
		Orientation orientation = null;
		switch (mDirection) {
		case LEFT_RIGHT:
			orientation = GradientDrawable.Orientation.LEFT_RIGHT;
			break;
		case TOP_BOTTOM:
			orientation = GradientDrawable.Orientation.TOP_BOTTOM;
			break;
		case RIGHT_LEFT:
			orientation = GradientDrawable.Orientation.RIGHT_LEFT;
			break;
		case BOTTOM_TOP:
			orientation = GradientDrawable.Orientation.BOTTOM_TOP;
			break;
		default:
			break;
		}
		int[] normalColors = { mNormalStartColor, mNormalEndColor };
		int[] pressedColors = { mPressedStartColor, mPressedEndColor };
		GradientDrawable normalDrawable = new GradientDrawable(orientation,
				normalColors);
		GradientDrawable pressedDrawable = new GradientDrawable(orientation,
				pressedColors);
		StateListDrawable background = new StateListDrawable();
		background.addState(new int[] { android.R.attr.state_pressed },
				pressedDrawable);
		background.addState(new int[] { -android.R.attr.state_pressed },
				normalDrawable);// 注意里面的“-”号，当XML的设定是false时，就需要使用资源符号的负值来设定。
		setBackground(background);
		typedArray.recycle();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	public GradientButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GradientButton(Context context) {
		this(context, null);
	}

}
