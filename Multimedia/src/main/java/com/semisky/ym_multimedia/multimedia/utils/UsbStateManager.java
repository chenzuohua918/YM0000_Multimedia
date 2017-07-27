package com.semisky.ym_multimedia.multimedia.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.semisky.ym_multimedia.MainActivity;
import com.semisky.ym_multimedia.common.utils.AppUtil;
import com.semisky.ym_multimedia.common.utils.FragmentStateManager;

public class UsbStateManager {
    private static UsbStateManager instance;
    private List<OnUsbStateChangeListener> listeners;
    private boolean usb1Mounted, usb2Mounted;// U盘1/2是否挂载

    private UsbStateManager() {
        listeners = new ArrayList<OnUsbStateChangeListener>();
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new UsbStateManager();
        }
    }

    public static UsbStateManager getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    /**
     * 注册U盘状态变化监听器
     *
     * @param listener
     */
    public void registerUsbStateChangeListener(OnUsbStateChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * 注销U盘状态变化监听器
     */
    public void unregisterUsbStateChangeListener(OnUsbStateChangeListener listener) {
        listeners.remove(listener);
    }

    public boolean isUsb1Mounted() {
        return usb1Mounted;
    }

    public boolean isUsb2Mounted() {
        return usb2Mounted;
    }

    /**
     * 是否没挂载U盘
     */
    public boolean hasNoUsbMounted() {
        return !usb1Mounted && !usb2Mounted;
    }

    /**
     * 文件所属的U盘是否挂载
     */
    public boolean isFileBelongUsbMounted(String filePath) {
        if (filePath.startsWith(MultimediaConstants.PATH_USB1)) {
            return usb1Mounted;
        } else if (filePath.startsWith(MultimediaConstants.PATH_USB2)) {
            return usb2Mounted;
        }
        return false;
    }

    /**
     * 通知所有观察者有U盘插入
     */
    public void notifyAllObserversUsbMounted(Context context, int usbFlag) {
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                usb1Mounted = true;
                break;
            case MultimediaConstants.FLAG_USB2:
                usb2Mounted = true;
                break;
            default:
                break;
        }

        // 插上U盘时如果不在多媒体界面并且不在倒车界面并且不在蓝牙通话界面，则自动跳转多媒体
        if (!FragmentStateManager.getInstance().isMultimediaFragmentsFront()
            //				&&
            //				&&
                ) {
            switch (PreferencesUtil.getInstance().getFinallyEnjoy()) {
                case MultimediaConstants.ENJOY_MUSIC:
                    AppUtil.startActivity(context, MainActivity.class, "multimediatag", "music");
                    break;
                case MultimediaConstants.ENJOY_VIDEO:
                    AppUtil.startActivity(context, MainActivity.class, "multimediatag", "video");
                    break;
                default:
                    break;
            }
        }

        for (OnUsbStateChangeListener listener : listeners) {
            if (listener != null) {
                listener.onUsbMounted(usbFlag);
            }
        }
    }

    /**
     * 通知所有观察者有U盘拔出
     */
    public void notifyAllObserversUsbUnMounted(int usbFlag) {
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                usb1Mounted = false;
                break;
            case MultimediaConstants.FLAG_USB2:
                usb2Mounted = false;
                break;
            default:
                break;
        }

        for (OnUsbStateChangeListener listener : listeners) {
            if (listener != null) {
                listener.onUsbUnMounted(usbFlag);
            }
        }
    }

    /**
     * U盘状态变化监听器
     */
    public interface OnUsbStateChangeListener {
        void onUsbMounted(int usbFlag);

        void onUsbUnMounted(int usbFlag);
    }
}
