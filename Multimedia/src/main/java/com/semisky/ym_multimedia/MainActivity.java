package com.semisky.ym_multimedia;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.WindowManager;

import com.semisky.ym_multimedia.common.controller.FragmentSwitchController;
import com.semisky.ym_multimedia.common.controller.FragmentSwitchController.SwitchFragmentCallback;
import com.semisky.ym_multimedia.common.controller.StatusbarController;
import com.semisky.ym_multimedia.common.controller.StatusbarController.StatusbarControllerCallback;
import com.semisky.ym_multimedia.common.utils.CommonConstants;
import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;
import com.semisky.ym_multimedia.multimedia.utils.PreferencesUtil;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager.OnUsbStateChangeListener;
import com.semisky.ym_multimedia.music.fragment.MusicFragment;
import com.semisky.ym_multimedia.music.service.MusicPlayService;
import com.semisky.ym_multimedia.music.utils.MusicFragmentCallback;
import com.semisky.ym_multimedia.music.utils.MusicPlayStateSaver;
import com.semisky.ym_multimedia.photo.fragment.PhotoFragment;
import com.semisky.ym_multimedia.radio.fragment.RadioFragment;
import com.semisky.ym_multimedia.video.fragment.VideoFragment;
import com.semisky.ym_multimedia.video.utils.VideoPlayStateSaver;
import com.semisky.ym_multimedia.ymbluetooth.bluetooth.BluetoothFragment;

/**
 * 主Activity
 *
 * @author Anter
 */
public class MainActivity extends FragmentActivity implements SwitchFragmentCallback,
        StatusbarControllerCallback, MusicFragmentCallback, OnUsbStateChangeListener {
    private FragmentManager mFragmentManager;
    private FragmentTransaction mTransaction;
    private PhotoFragment mPhotoFragment;
    private MusicFragment mMusicFragment;
    private VideoFragment mVideoFragment;
    private RadioFragment mRadioFragment;
    private BluetoothFragment mBluetoothFragment;
    private WallpaperManager mWallpaperManager;
    private int flag_fragment_in_container = -1;// 标记当前在Activity中的是哪个Fragment
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mMusicFragment != null) {
                mMusicFragment.onServiceDisconnected(name);
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (mMusicFragment != null) {
                mMusicFragment.onServiceConnected(name, service);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.logD("MainActivity-----------------------onCreate");
        // 设置不让布局因SystemUI的可见或隐藏而重新layout
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        register();
        skipFromOutside(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.logD("MainActivity-----------------------onNewIntent");
        setIntent(intent);
        skipFromOutside(getIntent());
    }

    /**
     * 从外部跳转（Launcher或文件管理器）
     */
    private void skipFromOutside(Intent intent) {
        String skipTag = intent.getStringExtra("multimediatag");
        if (TextUtils.isEmpty(skipTag)) {
            switch (PreferencesUtil.getInstance().getFinallyEnjoy()) {
                case MultimediaConstants.ENJOY_MUSIC:
                    skipTag = MultimediaConstants.SKIPTAG_MUSIC;
                    break;
                case MultimediaConstants.ENJOY_VIDEO:
                    skipTag = MultimediaConstants.SKIPTAG_VIDEO;
                    break;
                default:
                    break;
            }
        }
        if (MultimediaConstants.SKIPTAG_PHOTO.equals(skipTag)) {
            // 如果没有传该参数，说明是从Launcher点击跳转的；如果传了该参数，说明是从文件管理器中跳转过来的。
            String filePath = intent.getStringExtra("filePath");
            if (!TextUtils.isEmpty(filePath)) {
                Logger.logI("文件管理器跳转播放图片：" + filePath);
                PreferencesUtil.getInstance().setCurrentPlayingPhotoUri(filePath);
            }
            // 切换到图片Fragment
            FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_PHOTO, null);
        } else if (MultimediaConstants.SKIPTAG_MUSIC.equals(skipTag)) {
            // 如果没有传该参数，说明是从Launcher点击跳转的；如果传了该参数，说明是从文件管理器中跳转过来的。
            String filePath = intent.getStringExtra("filePath");
            if (!TextUtils.isEmpty(filePath)) {
                Logger.logI("文件管理器跳转播放音乐：" + filePath);
                // 从文件管理器跳转播放音乐
                MusicPlayStateSaver.getInstance().setFromFileManager(true);
                // 设置从文件管理器传来的音乐路径
                MusicPlayStateSaver.getInstance().setMusicUriFromFileManager(filePath);
                // 从文件管理器跳转，该音乐没有准备过
                MusicPlayStateSaver.getInstance().setPrepared(false);
            }
            // 切换到音乐Fragment
            FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_MUSIC, null);
        } else if (MultimediaConstants.SKIPTAG_VIDEO.equals(skipTag)) {
            // 如果没有传该参数，说明是从Launcher点击跳转的；如果传了该参数，说明是从文件管理器中跳转过来的。
            String filePath = intent.getStringExtra("filePath");
            if (!TextUtils.isEmpty(filePath)) {
                Logger.logI("文件管理器跳转播放视频：" + filePath);
                // 从文件管理器跳转播放视频
                VideoPlayStateSaver.getInstance().setFromFileManager(true);
                // 设置从文件管理器传来的视频路径
                VideoPlayStateSaver.getInstance().setVideoUriFromFileManager(filePath);
            }
            // 切换到视频Fragment
            FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_VIDEO, null);
        } else if (MultimediaConstants.SKIPTAG_RADIO.equals(skipTag)) {
            FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_RADIO, null);
        } else if (MultimediaConstants.SKIPTAG_BLUETOOTH.equals(skipTag)) {
            Bundle bundle = new Bundle();
            bundle.putInt(CommonConstants.KEY_GOTO_BT, CommonConstants.GOTO_BT_DEFAULT);
            // 切换到蓝牙Fragment
            FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_BT, bundle);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.logD("MainActivity-----------------------onResume");
        if (mWallpaperManager == null) {
            mWallpaperManager = WallpaperManager.getInstance(this);
        }
        // 获取当前壁纸
        Drawable wallpaperDrawable = mWallpaperManager.getDrawable();
        if (wallpaperDrawable != null) {
            // 设置当前壁纸为背景
            getWindow().setBackgroundDrawable(wallpaperDrawable);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new ContextWrapper(newBase) {

            @Override
            public Object getSystemService(String name) {
                /*
                 * VideoView内部的AudioManager会对Activity持有一个强引用，而AudioManager的生命周期比较长
				 * 导致这个Activity始终无法被回收
				 * 所以创建的时候把全局的Context传给VideoView，而不是Activity本身
				 */
                if (Context.AUDIO_SERVICE.equals(name)) {
                    return getApplicationContext().getSystemService(name);
                }
                return super.getSystemService(name);
            }
        });
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof PhotoFragment) {
            mPhotoFragment = (PhotoFragment) fragment;
        } else if (fragment instanceof MusicFragment) {
            mMusicFragment = (MusicFragment) fragment;
        } else if (fragment instanceof VideoFragment) {
            mVideoFragment = (VideoFragment) fragment;
        } else if (fragment instanceof RadioFragment) {
            mRadioFragment = (RadioFragment) fragment;
        } else if (fragment instanceof BluetoothFragment) {
            mBluetoothFragment = (BluetoothFragment) fragment;
        }
    }

    /**
     * 注册相关
     */
    private void register() {
        // 注册切换Fragment Controller回调
        FragmentSwitchController.getInstance().setSwitchFragmentCallback(this);
        // 注册显示隐藏状态栏Controller回调
        StatusbarController.getInstance().setStatusbarControllerCallback(this);
        // 注册U盘状态监听器
        UsbStateManager.getInstance().registerUsbStateChangeListener(this);
    }

    /**
     * 切换Fragment
     */
    private void switchFragment(int fragmentFlag, Bundle bundle) {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }
        mTransaction = mFragmentManager.beginTransaction();
        switch (fragmentFlag) {
            case CommonConstants.FLAG_PHOTO:// 图片页
                if (mPhotoFragment == null) {
                    mPhotoFragment = new PhotoFragment();
                }
                mTransaction.replace(R.id.fragment_container, mPhotoFragment);
                break;
            case CommonConstants.FLAG_MUSIC:// 音乐页
                if (mMusicFragment == null) {
                    mMusicFragment = new MusicFragment();
                    mMusicFragment.setMusicFragmentCallback(this);
                }
                mTransaction.replace(R.id.fragment_container, mMusicFragment);
                break;
            case CommonConstants.FLAG_VIDEO:// 视频页
                if (mVideoFragment == null) {
                    mVideoFragment = new VideoFragment();
                }
                mTransaction.replace(R.id.fragment_container, mVideoFragment);
                break;
            case CommonConstants.FLAG_RADIO:// 收音机页
                if (mRadioFragment == null) {
                    mRadioFragment = new RadioFragment();
                }
                mTransaction.replace(R.id.fragment_container, mRadioFragment);
                break;
            case CommonConstants.FLAG_BT:// 蓝牙页
                if (mBluetoothFragment == null) {
                    mBluetoothFragment = new BluetoothFragment();
                }
                if (bundle != null) {
                    // 传递参数
                    mBluetoothFragment.setmEntyMode(bundle.getInt(CommonConstants.KEY_GOTO_BT));
                }
                mTransaction.replace(R.id.fragment_container, mBluetoothFragment);
                break;
            default:
                break;
        }
        // 事务提交（有可能是在onSaveInstanceState之后接收到，导致异常，所以使用commitAllowingStateLoss）
        mTransaction.commitAllowingStateLoss();
        // 设置正在显示的Fragment的标识
        flag_fragment_in_container = fragmentFlag;
    }

    /**
     * 绑定MusicPlayService
     */
    private void bindMusicPlayService() {
        Intent intent = new Intent(this, MusicPlayService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 解绑MusicPlayService
     */
    private void unbindMusicPlayService() {
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        Logger.logD("MainActivity-----------------------onDestroy");
        unregister();

        super.onDestroy();
    }

    private void unregister() {
        // 注销切换Fragment Controller回调（页面不可见后是不能切换Fragment的）
        FragmentSwitchController.getInstance().setSwitchFragmentCallback(null);
        // 注销显示隐藏状态栏Controller回调
        StatusbarController.getInstance().setStatusbarControllerCallback(null);
        // 注销U盘状态监听器
        UsbStateManager.getInstance().unregisterUsbStateChangeListener(this);
        // 注销MusicFragment回调接口
        if (mMusicFragment != null) {
            mMusicFragment.setMusicFragmentCallback(null);
        }
    }

    @Override
    public void callToSwitchFragment(int fragment_flag, Bundle bundle) {// 有切换Fragment请求
        switchFragment(fragment_flag, bundle);
    }

    @Override
    public void requestShowStatusbar() {// 显示状态栏请求
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void requestDismissStatusbar() {// 隐藏状态栏请求
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void requestBindMusicPlayService() {// 绑定MusicPlayService请求
        bindMusicPlayService();
    }

    @Override
    public void requestUnbindMusicPlayService() {// 解绑MusicPlayService请求
        unbindMusicPlayService();
    }

    @Override
    public void onUsbMounted(int usbFlag) {
    }

    @Override
    public void onUsbUnMounted(int usbFlag) {
        if (UsbStateManager.getInstance().hasNoUsbMounted()) {//
            // 没有挂载U盘，说明当前拔出的是最后一个U盘，此时不管有没有播放都应该退出应用
            // 退出Activity
            finish();
            return;
        }

        switch (flag_fragment_in_container) {
            case CommonConstants.FLAG_PHOTO:// 当前是PhotoFragment在Activity中
                // 当前播放的图片Uri
                String currentPlayingPhotoUri = PreferencesUtil.getInstance()
                        .getCurrentPlayingPhotoUri();
                if (!TextUtils.isEmpty(currentPlayingPhotoUri)) {
                    switch (usbFlag) {
                        case MultimediaConstants.FLAG_USB1:// 如果拔出的是U盘1
                            if (currentPlayingPhotoUri.startsWith(MultimediaConstants.PATH_USB1)) {
                                // 退出Activity
                                finish();
                            }
                            break;
                        case MultimediaConstants.FLAG_USB2:// 如果拔出的是U盘2
                            if (currentPlayingPhotoUri.startsWith(MultimediaConstants.PATH_USB2)) {
                                // 退出Activity
                                finish();
                            }
                            break;
                        default:
                            break;
                    }
                }
                break;
            case CommonConstants.FLAG_MUSIC:// 当前是MusicFragment在Activity中
                // 正在播放的音乐Uri
                String currentPlayingMusicUri = PreferencesUtil.getInstance()
                        .getCurrentPlayingMusicUri();
                if (!TextUtils.isEmpty(currentPlayingMusicUri)) {
                    switch (usbFlag) {
                        case MultimediaConstants.FLAG_USB1:// 如果拔出的是U盘1
                            if (currentPlayingMusicUri.startsWith(MultimediaConstants.PATH_USB1))
                            {// 正在播放U盘1的音乐
                                // 退出Activity
                                finish();
                            }
                            break;
                        case MultimediaConstants.FLAG_USB2:// 如果拔出的是U盘2
                            if (currentPlayingMusicUri.startsWith(MultimediaConstants.PATH_USB2))
                            {// 正在播放U盘2的音乐
                                // 退出Activity
                                finish();
                            }
                            break;
                        default:
                            break;
                    }
                }
                break;
            case CommonConstants.FLAG_VIDEO:// 当前是VideoFragment在Activity中
                // 正在播放的视频Uri
                String currentPlayingVideoUri = PreferencesUtil.getInstance()
                        .getCurrentPlayingVideoUri();
                if (!TextUtils.isEmpty(currentPlayingVideoUri)) {
                    switch (usbFlag) {
                        case MultimediaConstants.FLAG_USB1:// 如果拔出的是U盘1
                            if (currentPlayingVideoUri.startsWith(MultimediaConstants.PATH_USB1))
                            {// 正在播放U盘1的视频
                                // 退出Activity
                                finish();
                            }
                            break;
                        case MultimediaConstants.FLAG_USB2:// 如果拔出的是U盘2
                            if (currentPlayingVideoUri.startsWith(MultimediaConstants.PATH_USB2))
                            {// 正在播放U盘2的视频
                                // 退出Activity
                                finish();
                            }
                            break;
                        default:
                            break;
                    }
                }
                break;
            default:
                break;
        }
    }
}
