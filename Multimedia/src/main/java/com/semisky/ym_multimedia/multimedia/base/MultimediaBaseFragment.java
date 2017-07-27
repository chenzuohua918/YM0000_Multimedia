package com.semisky.ym_multimedia.multimedia.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.semisky.ym_multimedia.BaseFragment;

/**
 * 多媒体的Fragment基类
 * 
 * @author Anter
 * 
 */
public abstract class MultimediaBaseFragment extends BaseFragment {

	@Override
	public void setListener() {
		setLeftViewsListener();
		setRightViewsListener();
		setMiddleViewsListener();
	}

	/** 设置左侧控件监听器 */
	public abstract void setLeftViewsListener();

	/** 设置左侧控件监听器 */
	public abstract void setRightViewsListener();

	/** 设置左侧控件监听器 */
	public abstract void setMiddleViewsListener();

	/** 重置适配器 */
	public abstract void resetAdapters();

	/** 初始化所有状态 */
	public abstract void initStatus();

	/** 更新视频开关UI */
	public abstract void setSwitcherState(boolean isPlaying);

	/** 刷新播放模式显示 */
	public abstract void updatePlayMode(int mode);

	/** 更新USB状态 */
	public abstract void updateUsbStatus();

	/** 选择U盘 */
	public abstract void chooseUsb(int usbFlag);

	/** 打开目录 */
	public abstract void openDirectory(int usbFlag, String directory);

	/** 刷新列表 */
	public abstract void notifyDataSetChanged(int usbFlag, String directory,
			boolean scrollToPlay);

	/** 刷新当前路径下视频文件及包含视频文件的文件夹列表 */
	public abstract void updateCurrentDirectory(int usbFlag);

	@Override
	public void createView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		resetAdapters();
		super.createView(inflater, container, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		// 初始化获取恢复所有状态
		initStatus();
	}
}
