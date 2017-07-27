package com.semisky.ym_multimedia.multimedia.view;

import com.semisky.ym_multimedia.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * U盘选项卡按钮
 *
 * @author Anter
 */
public class UsbRadioButton extends RadioButton {
    private int mountedIconRes, unMountedIconRes;// 挂载与未挂载时的图标
    private int mountedTextColor, unMountedTextColor;// 挂载与未挂载时按钮文字颜色

    public UsbRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.UsbRadioButton);
        mountedIconRes = typedArray.getResourceId(
                R.styleable.UsbRadioButton_mountedIconRes,
                R.drawable.icon_usb1_selected);
        unMountedIconRes = typedArray.getResourceId(
                R.styleable.UsbRadioButton_unMountedIconRes,
                R.drawable.icon_usb1_normal);
        mountedTextColor = typedArray.getColor(
                R.styleable.UsbRadioButton_mountedTextColor,
                getResources().getColor(R.color.usb_text_selected_color));
        unMountedTextColor = typedArray.getColor(
                R.styleable.UsbRadioButton_unMountedTextColor,
                getResources().getColor(R.color.usb_text_normal_color));
        typedArray.recycle();
    }

    public UsbRadioButton(Context context) {
        this(context, null);
    }

    /**
     * 设置挂载状态UI
     */
    public void setUsbMounted(boolean mounted) {
        // 设置是否可点击
        setEnabled(mounted);

        if (mounted) {
            setDrawableTop(this, mountedIconRes);
            setTextColor(mountedTextColor);
        } else {
            setDrawableTop(this, unMountedIconRes);
            setTextColor(unMountedTextColor);
        }
    }

    /**
     * 设置TextView类控件的顶部Icon
     */
    private void setDrawableTop(TextView textView, int resId) {
        Drawable topDrawable = getResources().getDrawable(resId);
        topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(),
                topDrawable.getMinimumHeight());
        textView.setCompoundDrawables(null, topDrawable, null, null);
    }

    /**
     * 设置选中状态
     */
    public void setChoosed(boolean choosed) {
        setChecked(choosed);
    }
}
