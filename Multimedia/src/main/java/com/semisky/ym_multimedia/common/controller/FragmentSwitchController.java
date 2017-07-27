package com.semisky.ym_multimedia.common.controller;

import android.os.Bundle;

/**
 * Fragment切换Controller类（方便全局控制Fragment的切换）
 *
 * @author Anter
 */
public class FragmentSwitchController {
    private static FragmentSwitchController instance;
    private SwitchFragmentCallback callback;

    private FragmentSwitchController() {
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new FragmentSwitchController();
        }
    }

    public static FragmentSwitchController getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    public void setSwitchFragmentCallback(SwitchFragmentCallback callback) {
        this.callback = callback;
    }

    /**
     * 切换Fragment
     */
    public void switchFragment(int fragment_flag, Bundle bundle) {
        if (callback != null) {
            callback.callToSwitchFragment(fragment_flag, bundle);
        }
    }

    public interface SwitchFragmentCallback {
        void callToSwitchFragment(int fragment_flag, Bundle bundle);
    }
}
