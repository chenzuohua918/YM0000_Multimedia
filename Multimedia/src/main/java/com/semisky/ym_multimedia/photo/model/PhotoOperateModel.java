package com.semisky.ym_multimedia.photo.model;


/**
 * 图片操作Model接口
 * 
 * @author Anter
 * 
 */
public interface PhotoOperateModel {
	// 设置图片控制回调接口
	void setPhotoOperateModelCallback(PhotoOperateModelCallback callback);

	// 恢复播放状态
	void resumePlayState();

	// 开关打开（播放）
	void switchOn();

	// 开关关闭（暂停）
	void switchOff();

	// 开关打开或关闭（播放或暂停）
	void switchOnOff();

	// 上一张图片
	void previousPhoto();

	// 下一张图片
	void nextPhoto();

	// 左翻转
	void rotateLeft();

	// 右翻转
	void rotateRight();

	// 放大
	void scaleBig();

	// 缩小
	void scaleSmall();
}
