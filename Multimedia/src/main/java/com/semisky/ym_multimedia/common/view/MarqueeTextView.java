package com.semisky.ym_multimedia.common.view;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 永久获得焦点的跑马灯TextView
 *
 * @author Anter
 */
public class MarqueeTextView extends TextView {

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarqueeTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setMarqueeRepeatLimit(-1);// 1代表1次，-1代表无限循环
        setSingleLine(true);
        setHorizontallyScrolling(true);// 让文字可以水平滑动
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    /**
     * 开始走跑马灯
     */
    public void startMarquee() {
        setEllipsize(TruncateAt.MARQUEE);
    }

    /**
     * 停止走跑马灯
     */
    public void stopMarquee() {
        setEllipsize(TruncateAt.END);
    }
}
