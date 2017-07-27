package com.semisky.ym_multimedia.multimedia.view;

import android.content.Context;
import android.util.AttributeSet;

import com.semisky.ym_multimedia.common.view.MarqueeTextView;
import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;

/**
 * 跑马灯Button，并将根目录的"/storage/usb0"替换成"/USB1"，"/storage/usb1"替换成"/USB2"
 * 
 * @author Anter
 * 
 */
public class UsbRootDirectoryButton extends MarqueeTextView {

	public UsbRootDirectoryButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public UsbRootDirectoryButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public UsbRootDirectoryButton(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		// 开始跑马灯
		startMarquee();
	}
	
	public void setText(int usbFlag, String text) {
		switch (usbFlag) {
		case MultimediaConstants.FLAG_USB1:
			setText(text.replaceFirst(MultimediaConstants.PATH_USB1, "/USB1"));
			break;
		case MultimediaConstants.FLAG_USB2:
			setText(text.replaceFirst(MultimediaConstants.PATH_USB2, "/USB2"));
			break;
		default:
			break;
		}
	}

	public String getText(int usbFlag) {
		String text = getText().toString().trim();
		switch (usbFlag) {
		case MultimediaConstants.FLAG_USB1:
			return text.replaceFirst("/USB1", MultimediaConstants.PATH_USB1);
		case MultimediaConstants.FLAG_USB2:
			return text.replaceFirst("/USB2", MultimediaConstants.PATH_USB2);
		default:
			break;
		}
		return text;
	}
}