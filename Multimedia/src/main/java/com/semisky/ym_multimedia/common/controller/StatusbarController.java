package com.semisky.ym_multimedia.common.controller;

/**
 * 状态栏显示隐藏Controller类（方便全局控制状态栏的显示隐藏）
 *
 * @author Anter
 */
public class StatusbarController {
    private static StatusbarController instance;
    private StatusbarControllerCallback callback;

    private StatusbarController() {
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new StatusbarController();
        }
    }

    public static StatusbarController getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    public void setStatusbarControllerCallback(StatusbarControllerCallback callback) {
        this.callback = callback;
    }

    /**
     * 显示状态栏（去全屏）
     */
    public void showStatusbar() {
        if (callback != null) {
            callback.requestShowStatusbar();
        }
    }

    /**
     * 隐藏状态栏（全屏）
     */
    public void dismissStatusbar() {
        if (callback != null) {
            callback.requestDismissStatusbar();
        }
    }

    public interface StatusbarControllerCallback {
        // 显示状态栏（去全屏）请求
        void requestShowStatusbar();

        // 隐藏状态栏（全屏）请求
        void requestDismissStatusbar();
    }
}
