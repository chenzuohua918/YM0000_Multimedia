package com.semisky.ym_multimedia.common.utils;

/**
 * Fragment状态管理
 *
 * @author Anter
 */
public class FragmentStateManager {
    private static FragmentStateManager instance;
    private String currentFragmentName;// 当前显示的Fragment名字

    private FragmentStateManager() {
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new FragmentStateManager();
        }
    }

    public static FragmentStateManager getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    public String getCurrentFragmentName() {
        return currentFragmentName;
    }

    public void setCurrentFragmentName(String name) {
        this.currentFragmentName = name;
    }

    /**
     * 是否是音乐／视频／图片Fragment在前台
     */
    public boolean isMultimediaFragmentsFront() {
        return currentFragmentName != null && ("MusicFragment".equals(currentFragmentName) || "VideoFragment".equals(currentFragmentName) || "PhotoFragment".equals(currentFragmentName));
    }
}