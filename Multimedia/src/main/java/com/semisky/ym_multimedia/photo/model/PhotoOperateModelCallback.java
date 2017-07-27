package com.semisky.ym_multimedia.photo.model;

/**
 * 图片控制回调接口
 * 
 * @author Anter
 * 
 */
public interface PhotoOperateModelCallback {
	void onSwitchOn();

	void onSwitchOff();

	void onPreviousPhoto();
	
	void onNextPhoto();

	void onRotateLeft();

	void onRotateRight();

	void onScaleBig();

	void onScaleSmall();
}
