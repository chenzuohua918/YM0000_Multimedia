package com.semisky.ym_multimedia.radio.utils;

import android.content.Context;

import java.io.IOException;

import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.radio.utils.RadioStatus.SearchNearStrongChannel;

/**
 * 收音机协议
 * 
 * @author Anter
 */
public class ProtocolUtil {
	private Context mContext;
	private static ProtocolUtil instance;
	private android.os.ProtocolManager proxy = android.os.ProtocolManager
			.getInstance();
	private boolean isMute = true;

	public static final int MODE_OPEN = 1;
	public static final int MODE_CLOSE = 6;

	public ProtocolUtil(Context mContext) {
		super();
		this.mContext = mContext;
	}

	public static synchronized ProtocolUtil getInstance(Context mContext) {
		if (instance == null) {
			instance = new ProtocolUtil(mContext);
		}
		return instance;
	}

	/**
	 * 注册频点信息接收回调接口
	 */
	public void registerRadioInfoReceiver(
			android.os.IRadioInfoInterface radioInfoReceiver) {
		Logger.logI("ProtocolManager-----------------------registerRadioInfoReceiver");
		proxy.registerRadioInfoListener(radioInfoReceiver);
	}

	/**
	 * 反注册频点信息接收回调接口
	 */
	public void unregisterRadioInfoReceiver(
			android.os.IRadioInfoInterface radioInfoReceiver) {
		Logger.logI("ProtocolManager-----------------------unregisterRadioInfoReceiver");
		proxy.unregisterRadioInfoListener(radioInfoReceiver);
	}

	/**
	 * 注册实体按键监听器
	 */
	public void registerKeyPressListener(
			android.os.IKeyPressInterface mIKeyPressInterface) {
		Logger.logI("ProtocolManager-----------------------registerKeyPressListener");
		proxy.registerKeyPressListener(mIKeyPressInterface);
	}

	/**
	 * 释放实体按键监听器
	 */
	public void unregisterKeyPressListener(
			android.os.IKeyPressInterface mIKeyPressInterface) {
		Logger.logI("ProtocolManager-----------------------unregisterKeyPressListener");
		proxy.unregisterKeyPressListener(mIKeyPressInterface);
	}

	// public void muteSet(int flag) {
	// proxy.muteSet(flag);
	// }

	// public void lound_set(int level) {
	// proxy.lound_set(level);
	// }

	// public void areaSet(int i) {
	// proxy.areaSet(i);
	// }

	/**
	 * 播放频点
	 * 
	 * @param i
	 *            频点（当为0时代表搜索所有AM强信号台）
	 */
	public void amSearch(int i) {
		if (i == 0) {
			Logger.logI("ProtocolManager-----------------------searchAllAM");
			// 静音
			closeRadioVolDoubleInsure();
		} else {
			Logger.logI("ProtocolManager-----------------------amSearch---" + i);
			if (SettingsUtil.getInstance().isRadioLatestOpened(mContext)// 收音机打开了
					&& RadioStatus.hasFocus// 有音频焦点
					&& RadioStatus.searchNearState == SearchNearStrongChannel.NEITHER// 没有在搜索上下一个强信号台
					&& !RadioStatus.isSearchingFM && !RadioStatus.isSearchingAM) {// 没有在搜索所有FM／AM
				// 取消静音
				openRadioVolDoubleInsure();
			}
		}
		proxy.amSearch(i);
	}

	/**
	 * 搜索所有AM强信号台
	 */
	public void searchAllAM() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				amSearch(0);
			}
		}).start();
	}

	/**
	 * 播放频点
	 * 
	 * @param i
	 *            频点（当为0时代表搜索所有FM强信号台）
	 */
	public void fmSearch(int i) {
		if (i == 0) {
			Logger.logI("ProtocolManager-----------------------searchAllFM");
			// 静音
			closeRadioVolDoubleInsure();
		} else {
			Logger.logI("ProtocolManager-----------------------fmSearch---" + i);
			if (SettingsUtil.getInstance().isRadioLatestOpened(mContext)// 收音机打开了
					&& RadioStatus.hasFocus// 有音频焦点
					&& RadioStatus.searchNearState == SearchNearStrongChannel.NEITHER// 没有在搜索上下一个强信号台
					&& !RadioStatus.isSearchingFM && !RadioStatus.isSearchingAM) {// 没有在搜索所有FM／AM
				// 取消静音
				openRadioVolDoubleInsure();
			}
		}
		proxy.fmSearch(i);
	}

	/**
	 * 搜索所有FM强信号台
	 */
	public void searchAllFM() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				fmSearch(0);
			}
		}).start();
	}

	/**
	 * 收音机开关
	 * 
	 * @param i
	 *            1是开，6是关
	 */
	public void mode_convert(int i) {
		Logger.logI("ProtocolManager-----------------------mode_convert---" + i);
		proxy.mode_convert(i);
	}

	// public void dxLocalSet(int radio_mode, int seqNum) {
	// proxy.dxLocalSet(radio_mode, seqNum);
	// }

	/**
	 * 搜索上一个AM强信号台（搜索AM和FM是同一个命令）
	 * 
	 * @param value
	 * @return
	 */
	public int am_searchPrev(int value) {
		Logger.logI("ProtocolManager-----------------------am_searchPrev---"
				+ value);
		// 静音
		closeRadioVolDoubleInsure();
		return proxy.fm_searchPrev(value);
	}

	/**
	 * 搜索下一个AM强信号台（搜索AM和FM是同一个命令）
	 * 
	 * @param value
	 * @return
	 */
	public int am_searchNext(int value) {
		Logger.logI("ProtocolManager-----------------------am_searchNext---"
				+ value);
		// 静音
		closeRadioVolDoubleInsure();
		return proxy.fm_searchNext(value);
	}

	/**
	 * 搜索上一个FM强信号台（搜索AM和FM是同一个命令）
	 * 
	 * @param value
	 * @return
	 */
	public int fm_searchPrev(int value) {
		Logger.logI("ProtocolManager-----------------------fm_searchPrev---"
				+ value);
		// 静音
		closeRadioVolDoubleInsure();
		return proxy.fm_searchPrev(value);
	}

	/**
	 * 搜索下一个FM强信号台（搜索AM和FM是同一个命令）
	 * 
	 * @param value
	 * @return
	 */
	public int fm_searchNext(int value) {
		Logger.logI("ProtocolManager-----------------------fm_searchNext---"
				+ value);
		// 静音
		closeRadioVolDoubleInsure();
		return proxy.fm_searchNext(value);
	}

	/**
	 * 设置音量
	 * 
	 * @param type
	 *            音量类型
	 * @param vol
	 *            音量值
	 * @param flag
	 */
	public void setVolumn(int type, int vol, int flag) {
		Logger.logI("ProtocolManager-----------------------setVolumn---" + vol);
		proxy.setVolumn(type, vol, flag);
	}

	/**
	 * 获取当前音量
	 * 
	 * @return
	 */
	public int getRadioVolume() {
		int volume = proxy.getStreamVolume(12);
		Logger.logI("ProtocolManager-----------------------getStreamVolume---"
				+ volume);
		return volume;
	}

	/**
	 * 获取最大音量
	 * 
	 * @return
	 */
	public int getMaxRadioVolume() {
		int volume = proxy.getMaxRadioVol();
		Logger.logI("ProtocolManager-----------------------getMaxRadioVolume---"
				+ volume);
		return volume;
	}

	/**
	 * 打开声音开关
	 */
	public void openRadioVol() {
		Logger.logI("ProtocolManager-----------------------openRadioVol");
		proxy.openRadioVol();
		setLineSwitch(true);
	}

	/**
	 * 关闭声音开关
	 */
	public void closeRadioVol() {
		Logger.logI("ProtocolManager-----------------------closeRadioVol");
		proxy.closeRadioVol();
		setLineSwitch(false);
	}

	private void setLineSwitch(boolean on) {
		try {
			Runtime.getRuntime().exec("tinymix 59 " + (on ? 1 : 0));
			Runtime.getRuntime().exec("tinymix 62 " + (on ? 1 : 0));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 1为在打电话，0为不在打电话
	 * 
	 * @return
	 */
	public int getBTPhoneStatus() {
		return proxy.getBTPhoneStatus();
	}

	/**
	 * 蓝牙是否在打电话
	 * 
	 * @return
	 */
	public boolean isBTTalking() {
		boolean isTalking = (getBTPhoneStatus() == 1);
		if (isTalking) {
			Logger.logI("ProtocolManager-----------------------BT is talking");
		} else {
			Logger.logI("ProtocolManager-----------------------BT is not talking");
		}
		return isTalking;
	}

	/**
	 * 打开声音，双重保证
	 */
	public void openRadioVolDoubleInsure() {
		if (isMute) {
			isMute = false;
			// muteForDepop(300);
			// 先openRadioVol，再mode_convert，防止爆破音
			openRadioVol();
			mode_convert(MODE_OPEN);
		}
	}

	/**
	 * 关闭声音，双重保证
	 */
	public void closeRadioVolDoubleInsure() {
		if (!isMute) {
			isMute = true;
			// muteForDepop(500);
			// 先mode_convert，再openRadioVol
			mode_convert(MODE_CLOSE);
			closeRadioVol();
		}
	}

	/**
	 * 静音的接口，直接Mute功放
	 * 
	 * @param delaytoUnmute_Ms
	 *            表示延迟多少毫秒取消静音，防止应用异常导致整机无声音（10～10000），一般静音不要超过３秒
	 */
	public void muteForDepop(int delaytoUnmute_Ms) {
		proxy.muteForDepop(delaytoUnmute_Ms);
	}

}
