package com.semisky.ym_multimedia;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.semisky.ym_multimedia.common.utils.AppUtil;
import com.semisky.ym_multimedia.common.utils.FragmentStateManager;

/**
 * Fragment基类
 *
 * @author Anter
 */
public abstract class BaseFragment extends Fragment {

    /**
     * 初始化Model
     */
    public abstract void initModel();

    /**
     * 初始化所有控件
     */
    public void initView() {
        initLeftViews();
        initRightViews();
        initMiddleViews();
    }

    /**
     * 初始化左侧控件（包含左侧的多颜色按钮）
     */
    public abstract void initLeftViews();

    /**
     * 初始化右侧控件（包含右上角三个按钮及文件列表控件）
     */
    public abstract void initRightViews();

    /**
     * 初始化中间剩余的所有控件
     */
    public abstract void initMiddleViews();

    /**
     * 设置SystemUI标题
     */
    public abstract int getSystemUITitleResId();

    /**
     * 设置监听器
     */
    public abstract void setListener();

    /**
     * 注册相关
     */
    public abstract void register();

    /**
     * 注销相关
     */
    public abstract void unregister();

    /**
     * 得到不为空的Context
     */
    public Context getContext() {
        return MyApplication.getInstance();
    }

    public void createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initModel();
        initView();
        setListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 设置状态栏显示
        setSystemUITitle(getSystemUITitleResId());
        // 设置前台Fragment名字
        FragmentStateManager.getInstance().setCurrentFragmentName(getClass().getSimpleName());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            // 设置状态栏显示
            setSystemUITitle(getSystemUITitleResId());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 设置前台Fragment名字为空
        FragmentStateManager.getInstance().setCurrentFragmentName(null);
    }

    /**
     * 设置状态栏标题
     */
    private void setSystemUITitle(int resId) {
        String fragmentName = getClass().getSimpleName();
        if (fragmentName.equals("RadioFragment") || fragmentName.equals("FMFragment") ||
				fragmentName.equals("AMFragment")) {
            Intent intent = new Intent("com.semisky.nl.currentPage");
            intent.putExtra("app_title", getContext().getString(resId));
            intent.putExtra("app_memory", 1);
            getContext().sendBroadcast(intent);
        } else {
            Intent intent = new Intent("com.semisky.nl.currentPage");
            intent.putExtra("app_title", getContext().getString(resId));
            getContext().sendBroadcast(intent);
        }
    }

    /**
     * 设置TextView类控件的左边Icon
     */
    public void setDrawableLeft(TextView textView, int resId) {
        Drawable leftDrawable = getResources().getDrawable(resId);
        leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable
				.getMinimumHeight());
        textView.setCompoundDrawables(leftDrawable, null, null, null);
    }

    /**
     * 设置TextView类控件的顶部Icon
     */
    public void setDrawableTop(TextView textView, int resId) {
        Drawable topDrawable = getResources().getDrawable(resId);
        topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
        textView.setCompoundDrawables(null, topDrawable, null, null);
    }

    /**
     * 跳转音效界面
     */
    public void gotoSound() {
        AppUtil.startActivity(getContext(), "com.semisky.ym0000_k60.settings", "com.semisky.ym0000_k60.settings.activities.SettingsActivity", "sound", "sound");
    }

    /**
     * 退出Activity并回到主界面
     */
    public void finishActivityAndBackHome() {
        if (getActivity() != null) {
            // 回到主界面
            AppUtil.backHome(getContext());
            // finish MainActivity
            getActivity().finish();
        }
    }
}
