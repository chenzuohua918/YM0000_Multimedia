package com.semisky.ym_multimedia.photo.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.common.controller.StatusbarController;
import com.semisky.ym_multimedia.common.utils.CommonConstants;
import com.semisky.ym_multimedia.common.utils.FastClickUtil;
import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.multimedia.base.MultimediaBaseFragment;
import com.semisky.ym_multimedia.multimedia.dao.MediaScanner;
import com.semisky.ym_multimedia.multimedia.model.MediaDataModel;
import com.semisky.ym_multimedia.multimedia.model.MediaDataModelDBImp;
import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;
import com.semisky.ym_multimedia.multimedia.utils.OnScanMediaFileListener;
import com.semisky.ym_multimedia.multimedia.utils.PreferencesUtil;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager.OnUsbStateChangeListener;
import com.semisky.ym_multimedia.multimedia.view.UsbRadioButton;
import com.semisky.ym_multimedia.multimedia.view.UsbRootDirectoryButton;
import com.semisky.ym_multimedia.photo.adapter.PhotoFileAdapter;
import com.semisky.ym_multimedia.photo.factorytest.PhotoFactoryTestConstants;
import com.semisky.ym_multimedia.photo.model.PhotoConsoleViewModel;
import com.semisky.ym_multimedia.photo.model.PhotoConsoleViewModel.PhotoConsoleViewStateCallback;
import com.semisky.ym_multimedia.photo.model.PhotoOperateModel;
import com.semisky.ym_multimedia.photo.model.PhotoOperateModelCallback;
import com.semisky.ym_multimedia.photo.model.PhotoOperateModelImp;
import com.semisky.ym_multimedia.photo.model.PhotoUriSubModel;
import com.semisky.ym_multimedia.photo.model.PhotoUriSubModel.PhotoUriSubResultCallback;
import com.semisky.ym_multimedia.photo.utils.OnPhotoInfosChangeListener;
import com.semisky.ym_multimedia.photo.utils.PhotoConstants;
import com.semisky.ym_multimedia.photo.utils.PhotoUtil;
import com.semisky.ym_multimedia.photo.view.photoview.PhotoView;

import java.io.File;
import java.lang.ref.WeakReference;

public class PhotoFragment extends MultimediaBaseFragment implements OnTouchListener,
        OnClickListener, CompoundButton.OnCheckedChangeListener, OnItemClickListener,
        OnScanMediaFileListener, OnUsbStateChangeListener, OnPhotoInfosChangeListener,
        PhotoUriSubResultCallback, PhotoConsoleViewStateCallback, PhotoOperateModelCallback {
    private View contentView;
    private View linear_left, // 左侧控制栏
            linear_filemanager;// 右侧目录栏
    private PhotoView mPhotoView;// 图片控件
    private Button btn_switcher,// 自动切换开关
            btn_previous,// 上一张按钮
            btn_next,// 下一张按钮
            btn_rotate_left,// 左翻转按钮
            btn_rotate_right,// 右翻转按钮
            btn_scale_big,// 放大按钮
            btn_scale_small,// 缩小按钮
            btn_list;// 媒体列表按钮
    private TextView tv_notice;
    private UsbRadioButton rb_usb1, rb_usb2;// USB选项卡
    private LinearLayout layout_list_usb1, layout_list_usb2;
    private UsbRootDirectoryButton root_directory1, root_directory2;// 根目录
    private ListView lv_usb1, lv_usb2;// 文件列表
    private PhotoFileAdapter photoFileAdapter1, photoFileAdapter2;
    private PhotoHandler mPhotoHandler = new PhotoHandler(this);
    /* 下次点击空白处是否显示右侧目录栏（默认右侧目录栏是显示的，所以在未超时情况下点击空白处是会和左侧控制栏一起显示的） */
    private boolean showPhotoListNextClickBlank = true;
    private MediaDataModel mMediaDataModel;// 多媒体数据获取Model
    private PhotoOperateModel mPhotoOperateModel;// 图片操作Model
    private int photo_change_interval = 3000;// 自动切换图片间隔
    private int period_fast_click = 200;// 快速点击响应周期
    private int touchResponseTime = 300;// 界定短按时间
    private long photoViewTouchDownTime;// 手指在图片上按下的时间
    /**
     * 中控实体按钮监听
     */
    private android.os.IKeyPressInterface mIKeyPressInterface = new android.os.IKeyPressInterface
            .Stub() {

        public void onKeyPressed(int keyCode, int mode) {
            switch (keyCode) {
                case CommonConstants.KEY_PREV:// 按键上一曲
                    Logger.logD("PhotoFragment-----------------------KEY_PREV");
                    previous();
                    break;
                case CommonConstants.KEY_NEXT:// 按键下一曲
                    Logger.logD("PhotoFragment-----------------------KEY_NEXT");
                    next();
                    break;
                default:
                    break;
            }
        }

        public String onGetAppInfo() {// 返回标记
            return "photo";
        }
    };
    /**
     * 工厂测试广播接收器
     */
    private BroadcastReceiver factoryTestReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PhotoFactoryTestConstants.SEMISKY_PICTURE_START.equals(action)) {// 开始轮播图片
                mPhotoOperateModel.switchOn();
            } else if (PhotoFactoryTestConstants.SEMISKY_PICTURE_STOP.equals(action)) {// 停止轮播图片
                mPhotoOperateModel.switchOff();
            } else if (PhotoFactoryTestConstants.SEMISKY_PICTURE_NEXT.equals(action)) {// 下一张
                mPhotoOperateModel.nextPhoto();
            } else if (PhotoFactoryTestConstants.SEMISKY_PICTURE_PREVIOUS.equals(action)) {// 上一张
                mPhotoOperateModel.previousPhoto();
            } else if (PhotoFactoryTestConstants.SEMISKY_PICTURE_SHOWFILE.equals(action)) {// 指定播放
                displayPhoto(intent.getStringExtra("PATH"));
            } else if (PhotoFactoryTestConstants.SEMISKY_PICTURE_ROTATION_LEFT.equals(action))
            {// 左翻转
                mPhotoOperateModel.rotateLeft();
            } else if (PhotoFactoryTestConstants.SEMISKY_PICTURE_ROTATION_RIGHT.equals(action))
            {// 右翻转
                mPhotoOperateModel.rotateRight();
            } else if (PhotoFactoryTestConstants.SEMISKY_PICTURE_SCALE_BIG.equals(action)) {// 放大
                mPhotoOperateModel.scaleBig();
            } else if (PhotoFactoryTestConstants.SEMISKY_PICTURE_SCALE_SMALL.equals(action)) {// 缩小
                mPhotoOperateModel.scaleSmall();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        Logger.logD("PhotoFragment-----------------------onCreateView");
        contentView = inflater.inflate(R.layout.fragment_photo, container, false);
        // 初始化
        createView(inflater, container, savedInstanceState);
        return contentView;
    }

    @Override
    public void onResume() {
        Logger.logD("PhotoFragment-----------------------onResume");
        // 注册相关
        register();

        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.logD("PhotoFragment-----------------------onConfigurationChanged");
        btn_previous.setText(R.string.previous_photo);
        btn_next.setText(R.string.next_photo);
        btn_rotate_left.setText(R.string.rotate_left);
        btn_rotate_right.setText(R.string.rotate_right);
        btn_scale_big.setText(R.string.scale_big);
        btn_scale_small.setText(R.string.scale_small);
        btn_list.setText(R.string.list);
        tv_notice.setText(R.string.no_such_file);
    }

    @Override
    public void resetAdapters() {
        // 需要手动置空，否则再次切换时成员变量不会为null
        photoFileAdapter1 = null;
        photoFileAdapter2 = null;
    }

    @Override
    public void initModel() {
    }

    @Override
    public void initLeftViews() {
        linear_left = contentView.findViewById(R.id.linear_left);
        btn_switcher = (Button) contentView.findViewById(R.id.btn_switcher);
        btn_previous = (Button) contentView.findViewById(R.id.btn_previous);
        btn_next = (Button) contentView.findViewById(R.id.btn_next);
        btn_rotate_left = (Button) contentView.findViewById(R.id.btn_rotate_left);
        btn_rotate_right = (Button) contentView.findViewById(R.id.btn_rotate_right);
        btn_scale_big = (Button) contentView.findViewById(R.id.btn_scale_big);
        btn_scale_small = (Button) contentView.findViewById(R.id.btn_scale_small);
        btn_list = (Button) contentView.findViewById(R.id.btn_list);
    }

    @Override
    public void initRightViews() {
        linear_filemanager = contentView.findViewById(R.id.linear_filemanager);
        rb_usb1 = (UsbRadioButton) contentView.findViewById(R.id.rb_usb1);
        rb_usb2 = (UsbRadioButton) contentView.findViewById(R.id.rb_usb2);
        layout_list_usb1 = (LinearLayout) contentView.findViewById(R.id.layout_list_usb1);
        layout_list_usb2 = (LinearLayout) contentView.findViewById(R.id.layout_list_usb2);
        root_directory1 = (UsbRootDirectoryButton) layout_list_usb1.findViewById(R.id
                .root_directory1);
        root_directory2 = (UsbRootDirectoryButton) layout_list_usb2.findViewById(R.id
                .root_directory2);
        lv_usb1 = (ListView) layout_list_usb1.findViewById(R.id.lv_usb1);
        lv_usb2 = (ListView) layout_list_usb2.findViewById(R.id.lv_usb2);
    }

    @Override
    public void initMiddleViews() {
        tv_notice = (TextView) contentView.findViewById(R.id.tv_notice);
        mPhotoView = (PhotoView) contentView.findViewById(R.id.photoView);
    }

    @Override
    public int getSystemUITitleResId() {
        return R.string.photo;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setLeftViewsListener() {
        btn_switcher.setOnTouchListener(this);
        btn_switcher.setOnClickListener(this);
        btn_previous.setOnTouchListener(this);
        btn_previous.setOnClickListener(this);
        btn_next.setOnTouchListener(this);
        btn_next.setOnClickListener(this);
        btn_rotate_left.setOnTouchListener(this);
        btn_rotate_left.setOnClickListener(this);
        btn_rotate_right.setOnTouchListener(this);
        btn_rotate_right.setOnClickListener(this);
        btn_scale_big.setOnTouchListener(this);
        btn_scale_big.setOnClickListener(this);
        btn_scale_small.setOnTouchListener(this);
        btn_scale_small.setOnClickListener(this);
        btn_list.setOnTouchListener(this);
        btn_list.setOnClickListener(this);
    }

    @Override
    public void setRightViewsListener() {
        rb_usb1.setOnTouchListener(this);
        rb_usb1.setOnCheckedChangeListener(this);
        rb_usb2.setOnTouchListener(this);
        rb_usb2.setOnCheckedChangeListener(this);
        root_directory1.setOnTouchListener(this);
        root_directory1.setOnClickListener(this);
        root_directory2.setOnTouchListener(this);
        root_directory2.setOnClickListener(this);
        lv_usb1.setOnTouchListener(this);
        lv_usb1.setOnItemClickListener(this);
        lv_usb2.setOnTouchListener(this);
        lv_usb2.setOnItemClickListener(this);
    }

    @Override
    public void setMiddleViewsListener() {
        mPhotoView.setOnTouchListener(this);
    }

    @Override
    public void register() {
        // 注册扫描媒体监听器
        MediaScanner.getInstance(getContext()).registerScanMediaFileListener(this);
        // 设置图片数据变化监听器
        mMediaDataModel = MediaDataModelDBImp.getInstance(getContext());
        mMediaDataModel.setOnPhotoInfosChangeListener(this);
        // 注册U盘状态变化监听器
        UsbStateManager.getInstance().registerUsbStateChangeListener(this);
        // 注册图片路径分解回调监听器
        PhotoUriSubModel.getInstance().registerPhotoUriSubResultCallback(this);
        // 注册图片控制视图显示隐藏回调监听器
        PhotoConsoleViewModel.getInstance().setCallback(this);
        // 设置图片操作监听器
        mPhotoOperateModel = PhotoOperateModelImp.getInstance(getContext());
        mPhotoOperateModel.setPhotoOperateModelCallback(this);
        // 注册中控按键监听
        registerKeyPressListener();
        // 注册工厂测试广播接收器
        registerFactoryTestReceiver();
    }

    @Override
    public void initStatus() {
        // 设置USB状态
        updateUsbStatus();
        // 默认显示控制视图
        PhotoConsoleViewModel.getInstance().showPhotoConsoleView();
        // 初次播放或者显示记忆的图片
        String photoUri = PreferencesUtil.getInstance().getCurrentPlayingPhotoUri();
        if (TextUtils.isEmpty(photoUri) || !new File(photoUri).exists()) {//
            // 说明从未播放过图片或者记忆的图片不存在了（可能MediaScanner扫描到第一个图片文件的时候，PhotoFragment
            // 未在前台，这样就收不到onScannedFirstPhoto()回调，所以需要在PhotoFragment打开的时候做一次判断）
            // 从扫描类中获取第一个扫描到的图片文件
            photoUri = MediaScanner.getInstance(getContext()).getFirstScannedPhotoUri();
            if (TextUtils.isEmpty(photoUri)) {// 暂时没有扫描到图片文件或者压根没有插U盘

            } else {// 获取到第一个扫描到的图片文件路径
                Logger.logD("PhotoFragment-----------------------从未播放过图片，默认显示第一张扫描到的图片：" +
                        photoUri);
            }
        }
        // 显示记忆图片
        displayPhoto(photoUri);
        // 恢复图片播放状态
        mPhotoOperateModel.resumePlayState();
    }

    @Override
    public void setSwitcherState(boolean isPlaying) {
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        if (btn_switcher != null) {
            if (isPlaying) {
                setDrawableTop(btn_switcher, R.drawable.icon_play_selector);
                btn_switcher.setText(R.string.pause);
            } else {
                setDrawableTop(btn_switcher, R.drawable.icon_pause_selector);
                btn_switcher.setText(R.string.play);
            }
        }
    }

    @Override
    public void updatePlayMode(int mode) {
    }

    @Override
    public void updateUsbStatus() {
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        int finallyChooseUsbFlag = PreferencesUtil.getInstance().getPhotoFinallyChooseUsbFlag();
        // 最后选择的USB接口标识
        switch (finallyChooseUsbFlag) {
            case MultimediaConstants.FLAG_USB1:
                if (UsbStateManager.getInstance().isUsb1Mounted()) {//
                    // 如果最后选择的是USB1，且USB1口插了U盘，那么选中USB1
                    rb_usb1.setUsbMounted(true);
                    rb_usb1.setChoosed(true);
                    rb_usb2.setChoosed(false);
                    if (UsbStateManager.getInstance().isUsb2Mounted()) {//
                        // 如果最后选择的是USB1，且USB1口插了U盘，USB2口也插了U盘，那么USB2可选择
                        rb_usb2.setUsbMounted(true);
                    } else {// 如果最后选择的是USB1，USB2口没插U盘，那么USB2不可选择
                        rb_usb2.setUsbMounted(false);
                    }
                } else {// 如果最后选择的是USB1，而USB1口没插U盘，那么USB1不可选择
                    rb_usb1.setChoosed(false);
                    rb_usb1.setUsbMounted(false);
                    if (UsbStateManager.getInstance().isUsb2Mounted()) {//
                        // 如果最后选择的是USB1，而USB1口没插U盘，但USB2口插了U盘，那么选中USB2
                        rb_usb2.setUsbMounted(true);
                        rb_usb2.setChoosed(true);
                    } else {// 如果最后选择的是USB1，而USB1口没插U盘，USB2口也没插U盘，那么两个都不可选择
                        rb_usb2.setChoosed(false);
                        rb_usb2.setUsbMounted(false);
                    }
                }
                break;
            case MultimediaConstants.FLAG_USB2:
                if (UsbStateManager.getInstance().isUsb2Mounted()) {//
                    // 如果最后选择的是USB2，USB2口插了U盘，那么选中USB2
                    rb_usb2.setUsbMounted(true);
                    rb_usb2.setChoosed(true);
                    rb_usb1.setChoosed(false);
                    if (UsbStateManager.getInstance().isUsb1Mounted()) {//
                        // 如果最后选择的是USB2，USB2口插了U盘，USB1口也插了U盘，那么USB1可选择
                        rb_usb1.setUsbMounted(true);
                    } else {// 如果最后选择的是USB2，USB2口插了U盘，USB1口没插U盘，那么USB1不可选择
                        rb_usb1.setUsbMounted(false);
                    }
                } else {// 如果最后选择的是USB2，而USB2口没插U盘，那么USB2不可选择
                    rb_usb2.setChoosed(false);
                    rb_usb2.setUsbMounted(false);
                    if (UsbStateManager.getInstance().isUsb1Mounted()) {//
                        // 如果最后选择的是USB2，而USB2口没插U盘，但USB1口插了U盘，那么选中USB1
                        rb_usb1.setUsbMounted(true);
                        rb_usb1.setChoosed(true);
                    } else {// 如果最后选择的是USB2，而USB2口没插U盘，USB1口也没插U盘，那么两个都不可选择
                        rb_usb1.setChoosed(false);
                        rb_usb1.setUsbMounted(false);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void chooseUsb(int usbFlag) {
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                if (UsbStateManager.getInstance().isUsb1Mounted()) {// USB1已插上
                    layout_list_usb1.setVisibility(View.VISIBLE);
                    layout_list_usb2.setVisibility(View.INVISIBLE);// 这里用INVISIBLE，用GONE跑马灯第一次无效

                    rb_usb2.setChoosed(false);

                    openDirectory(usbFlag, PreferencesUtil.getInstance()
                            .getPhotoUsb1RootDirectory());
                    PreferencesUtil.getInstance().setPhotoFinallyChooseUsbFlag
                            (MultimediaConstants.FLAG_USB1);
                }
                break;
            case MultimediaConstants.FLAG_USB2:
                if (UsbStateManager.getInstance().isUsb2Mounted()) {// USB2已插上
                    layout_list_usb1.setVisibility(View.INVISIBLE);// 这里用INVISIBLE，用GONE跑马灯第一次无效
                    layout_list_usb2.setVisibility(View.VISIBLE);

                    rb_usb1.setChoosed(false);

                    openDirectory(usbFlag, PreferencesUtil.getInstance()
                            .getPhotoUsb2RootDirectory());
                    PreferencesUtil.getInstance().setPhotoFinallyChooseUsbFlag
                            (MultimediaConstants.FLAG_USB2);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void openDirectory(int usbFlag, String directory) {// 打开某个目录
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                if (!new File(directory).exists()) {// 目录不存在（换了U盘之后记忆的目录要重置）
                    directory = MultimediaConstants.PATH_USB1;
                }
                // 保存当前目录
                PreferencesUtil.getInstance().setPhotoUsb1RootDirectory(directory);
                // 刷新当前目录显示
                root_directory1.setText(usbFlag, directory);
                // 刷新列表
                notifyDataSetChanged(usbFlag, directory, true);
                break;
            case MultimediaConstants.FLAG_USB2:
                if (!new File(directory).exists()) {// 目录不存在（换了U盘之后记忆的目录要重置）
                    directory = MultimediaConstants.PATH_USB2;
                }
                // 保存当前目录
                PreferencesUtil.getInstance().setPhotoUsb2RootDirectory(directory);
                // 刷新当前目录显示
                root_directory2.setText(usbFlag, directory);
                // 刷新列表
                notifyDataSetChanged(usbFlag, directory, true);
                break;
            default:
                break;
        }
    }

    @Override
    public void updateCurrentDirectory(int usbFlag) {// 刷新当前路径下图片文件及包含图片文件的文件夹列表
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                String directory1 = PreferencesUtil.getInstance().getPhotoUsb1RootDirectory();
                if (!new File(directory1).exists()) {// 如果保存的文件夹不存在
                    directory1 = MultimediaConstants.PATH_USB1;
                }
                // 刷新列表
                notifyDataSetChanged(usbFlag, directory1, false);
                break;
            case MultimediaConstants.FLAG_USB2:
                String directory2 = PreferencesUtil.getInstance().getPhotoUsb2RootDirectory();
                if (!new File(directory2).exists()) {// 如果保存的文件夹不存在
                    directory2 = MultimediaConstants.PATH_USB2;
                }
                // 刷新列表
                notifyDataSetChanged(usbFlag, directory2, false);
                break;
            default:
                break;
        }
    }

    @Override
    public void notifyDataSetChanged(int usbFlag, String directory, boolean scrollToPlay) {//
        // 获取某个目录下的图片文件，刷新列表，并滚动到当前播放位置
        if (TextUtils.isEmpty(directory)) {
            return;
        }

        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                // 获取该目录下的直属的图片文件及包含图片文件的文件夹（相同的文件夹只添加一次）
                mMediaDataModel.queryPhotoDirectUnder(usbFlag, directory);
                // 刷新列表
                if (photoFileAdapter1 == null) {
                    photoFileAdapter1 = new PhotoFileAdapter(getActivity(), MediaDataModelDBImp
                            .getInstance(getContext()).getUsb1ShowingPhotoList());
                    lv_usb1.setAdapter(photoFileAdapter1);
                } else {
                    photoFileAdapter1.notifyDataSetChanged();
                }
                // 获取当前正在播放的图片的Uri
                String currentPlayingPhotoUri1 = PreferencesUtil.getInstance()
                        .getCurrentPlayingPhotoUri();
                // 如果正在播放当前目录下的图片文件，则滚动到当前播放图片文件位置
                if (!TextUtils.isEmpty(currentPlayingPhotoUri1) && directory.equals
                        (currentPlayingPhotoUri1.substring(0, currentPlayingPhotoUri1.lastIndexOf
                                (File.separator)))) {
                    // 获取正在播放的图片文件在当前列表的位置
                    int pos = mMediaDataModel.getPhotoPosition(usbFlag, currentPlayingPhotoUri1);
                    // 滚动列表
                    if (pos != -1) {
                        lv_usb1.smoothScrollToPosition(pos);
                    }
                }
                break;
            case MultimediaConstants.FLAG_USB2:
                // 获取该目录下的直属的图片文件及包含图片文件的文件夹（相同的文件夹只添加一次）
                mMediaDataModel.queryPhotoDirectUnder(usbFlag, directory);
                // 刷新列表
                if (photoFileAdapter2 == null) {
                    photoFileAdapter2 = new PhotoFileAdapter(getActivity(), MediaDataModelDBImp
                            .getInstance(getContext()).getUsb2ShowingPhotoList());
                    lv_usb2.setAdapter(photoFileAdapter2);
                } else {
                    photoFileAdapter2.notifyDataSetChanged();
                }
                // 获取当前正在播放的图片的Uri
                String currentPlayingPhotoUri2 = PreferencesUtil.getInstance()
                        .getCurrentPlayingPhotoUri();
                // 如果正在播放当前目录下的图片文件，则滚动到当前播放图片文件位置
                if (!TextUtils.isEmpty(currentPlayingPhotoUri2) && directory.equals
                        (currentPlayingPhotoUri2.substring(0, currentPlayingPhotoUri2.lastIndexOf
                                (File.separator)))) {
                    // 获取正在播放的图片文件在当前列表的位置
                    int pos = mMediaDataModel.getPhotoPosition(usbFlag, currentPlayingPhotoUri2);
                    // 滚动列表
                    if (pos != -1) {
                        lv_usb2.smoothScrollToPosition(pos);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 显示左上角“无图片文件”文字提示
     */
    private void showNoPhotoFileNotice() {
        tv_notice.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏左上角“无图片文件”文字提示
     */
    private void dismissNoPhotoFileNotice() {
        tv_notice.setVisibility(View.GONE);
    }

    /**
     * 全屏
     */
    private void setFullScreen() {
        StatusbarController.getInstance().dismissStatusbar();
    }

    /**
     * 取消全屏
     */
    private void cancelFullSceen() {
        StatusbarController.getInstance().showStatusbar();
    }

    /**
     * 展示图片
     */
    private void displayPhoto(String photoUri) {
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        if (TextUtils.isEmpty(photoUri) || !new File(photoUri).exists()) {// 文件不存在了
            mPhotoView.setImageBitmap(null);
            return;
        }

        PreferencesUtil.getInstance().setCurrentPlayingPhotoUri(photoUri);
        // 隐藏“无图片文件”文字提示
        dismissNoPhotoFileNotice();
        // 加载图片
        PhotoUtil.displayImageByGlide(getContext(), "file://" + photoUri, mPhotoView);
        // 刷新列表选中状态
        if (photoUri.startsWith(MultimediaConstants.PATH_USB1)) {
            notifyDataSetChanged(MultimediaConstants.FLAG_USB1, PreferencesUtil.getInstance()
                    .getPhotoUsb1RootDirectory(), true);
        } else if (photoUri.startsWith(MultimediaConstants.PATH_USB2)) {
            notifyDataSetChanged(MultimediaConstants.FLAG_USB2, PreferencesUtil.getInstance()
                    .getPhotoUsb2RootDirectory(), true);
        }
    }

    /**
     * 开始自动播放
     */
    private void startChangePhoto() {
        mPhotoHandler.removeMessages(PhotoConstants.MSG_CHANGE_PHOTO_INTERVAL);
        mPhotoHandler.sendEmptyMessageDelayed(PhotoConstants.MSG_CHANGE_PHOTO_INTERVAL,
                photo_change_interval);
    }

    /**
     * 停止自动播放
     */
    private void stopChangePhoto() {
        mPhotoHandler.removeMessages(PhotoConstants.MSG_CHANGE_PHOTO_INTERVAL);
    }

    /**
     * 上一张
     */
    private void previous() {
        if (FastClickUtil.enableToResponseClick(period_fast_click)) {// 应对快速点击
            displayPhoto(mMediaDataModel.getPreviousPlayPhotoUri());
        }
    }

    /**
     * 下一张
     */
    private void next() {
        if (FastClickUtil.enableToResponseClick(period_fast_click)) {// 应对快速点击
            displayPhoto(mMediaDataModel.getNextPlayPhotoUri());
        }
    }

    /**
     * 左旋转
     */
    private void rotateLeft() {
        if (FastClickUtil.enableToResponseClick(period_fast_click)) {// 应对快速点击
            mPhotoView.handRotate(-90);
        }
    }

    /**
     * 右旋转
     */
    private void rotateRight() {
        if (FastClickUtil.enableToResponseClick(period_fast_click)) {// 应对快速点击
            mPhotoView.handRotate(90);
        }
    }

    /**
     * 放大
     */
    private void scaleBig() {
        if (FastClickUtil.enableToResponseClick(period_fast_click)) {// 应对快速点击
            mPhotoView.handScale(1 / 0.7f);
        }
    }

    /**
     * 缩小
     */
    private void scaleSmall() {
        if (FastClickUtil.enableToResponseClick(period_fast_click)) {// 应对快速点击
            mPhotoView.handScale(0.7f);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.logD("PhotoFragment-----------------------onPause");
    }

    @Override
    public void onStop() {
        Logger.logD("PhotoFragment-----------------------onStop");
        // 移除所有消息
        mPhotoHandler.removeCallbacksAndMessages(null);
        // 去全屏
        cancelFullSceen();
        // 注销相关
        unregister();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.logD("PhotoFragment-----------------------onDestroy");
    }

    /**
     * 注销
     */
    @Override
    public void unregister() {
        // 注销扫描媒体监听器
        MediaScanner.getInstance(getContext()).unregisterScanMediaFileListener(this);
        // 注销图片数据变化监听器
        mMediaDataModel.setOnPhotoInfosChangeListener(null);
        // 注销U盘状态变化监听器
        UsbStateManager.getInstance().unregisterUsbStateChangeListener(this);
        // 注销图片路径分解回调监听器
        PhotoUriSubModel.getInstance().unregisterPhotoUriSubResultCallback(this);
        // 注销图片控制视图显示隐藏回调监听器
        PhotoConsoleViewModel.getInstance().setCallback(null);
        // 注销图片操作监听器
        mPhotoOperateModel.setPhotoOperateModelCallback(null);
        // 注销中控按键监听
        unregisterKeyPressListener();
        // 注销工厂测试广播接收器
        unregisterFactoryTestReceiver();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指在任何控件上按下时，先取消计时，手指抬起时重新开始计时
                PhotoConsoleViewModel.getInstance().cancelCountdown();

                if (v.getId() == R.id.photoView) {
                    // 记录按下PhotoView的时间
                    photoViewTouchDownTime = System.currentTimeMillis();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (v.getId() == R.id.photoView) {
                    if (System.currentTimeMillis() - photoViewTouchDownTime < touchResponseTime)
                    {// 如果按下的时间非常短，相当于点击
                        if (linear_left.getVisibility() == View.VISIBLE) {
                            // 隐藏控制视图
                            PhotoConsoleViewModel.getInstance().hidePhotoConsoleView(false);
                        } else {
                            // 显示控制视图
                            PhotoConsoleViewModel.getInstance().showPhotoConsoleView();
                        }
                        break;
                    }
                }
                // 如果手指抬起，且左侧控制栏可见，开始计时
                if (linear_left.getVisibility() == View.VISIBLE) {
                    PhotoConsoleViewModel.getInstance().restartCountdown();
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switcher:// 点击播放暂停按钮
                mPhotoOperateModel.switchOnOff();
                break;
            case R.id.btn_previous:// 点击上一张按钮
                mPhotoOperateModel.previousPhoto();
                break;
            case R.id.btn_next:// 点击下一张按钮
                mPhotoOperateModel.nextPhoto();
                break;
            case R.id.btn_rotate_left:// 点击左翻转按钮
                mPhotoOperateModel.rotateLeft();
                break;
            case R.id.btn_rotate_right:// 点击右翻转按钮
                mPhotoOperateModel.rotateRight();
                break;
            case R.id.btn_scale_big:// 点击放大按钮
                mPhotoOperateModel.scaleBig();
                break;
            case R.id.btn_scale_small:// 点击缩小按钮
                mPhotoOperateModel.scaleSmall();
                break;
            case R.id.btn_list:// 媒体列表按钮
                if (linear_filemanager.getVisibility() == View.VISIBLE) {
                    linear_filemanager.setVisibility(View.GONE);
                    // 点击媒体列表按钮对右侧目录栏进行隐藏，则下次点击空白处不显示右侧目录栏
                    showPhotoListNextClickBlank = false;
                } else {
                    linear_filemanager.setVisibility(View.VISIBLE);
                    // 点击媒体列表按钮对右侧目录栏进行显示，则下次在不超时情况下点击空白处显示右侧目录栏
                    showPhotoListNextClickBlank = true;
                }
                break;
            case R.id.root_directory1:// 返回上层目录
                PhotoUriSubModel.getInstance().backToParentDir(MultimediaConstants.FLAG_USB1,
                        root_directory1.getText(MultimediaConstants.FLAG_USB1));
                break;
            case R.id.root_directory2:// 返回上层目录
                PhotoUriSubModel.getInstance().backToParentDir(MultimediaConstants.FLAG_USB2,
                        root_directory2.getText(MultimediaConstants.FLAG_USB2));
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.rb_usb1:
                if (isChecked) {
                    chooseUsb(MultimediaConstants.FLAG_USB1);
                }
                break;
            case R.id.rb_usb2:
                if (isChecked) {
                    chooseUsb(MultimediaConstants.FLAG_USB2);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.lv_usb1:
                // 分解photoUri，根据分解结果来决定是打开目录还是展示图片
                PhotoUriSubModel.getInstance().subPhotoUri(MultimediaConstants.FLAG_USB1,
                        PreferencesUtil.getInstance().getPhotoUsb1RootDirectory(), (String)
                                photoFileAdapter1.getItem(position));
                break;
            case R.id.lv_usb2:
                // 分解photoUri，根据分解结果来决定是打开目录还是展示图片
                PhotoUriSubModel.getInstance().subPhotoUri(MultimediaConstants.FLAG_USB2,
                        PreferencesUtil.getInstance().getPhotoUsb2RootDirectory(), (String)
                                photoFileAdapter2.getItem(position));
                break;
            default:
                break;
        }
    }

    /**
     * 注册中控实体按钮监听器
     */
    private void registerKeyPressListener() {
        Logger.logD("PhotoFragment-----------------------registerKeyPressListener");
        android.os.ProtocolManager.getInstance().registerKeyPressListener(mIKeyPressInterface);
    }

    /**
     * 注销中控实体按钮监听器
     */
    private void unregisterKeyPressListener() {
        Logger.logD("PhotoFragment-----------------------unregisterKeyPressListener");
        android.os.ProtocolManager.getInstance().unregisterKeyPressListener(mIKeyPressInterface);
    }

    @Override
    public void onScanStart(int usbFlag) {// 扫描开始

    }

    @Override
    public void onNotifyRefreshList(int usbFlag) {// 每隔一段时间通知刷新媒体列表
        updateCurrentDirectory(usbFlag);
    }

    @Override
    public void onScannedFirstPhoto(int usbFlag, String photoUri) {//
        // 扫描到第一个图片文件回调（用于初次插入U盘默认播放第一张图片）
        // 获取当前正在播放的图片的Uri
        String currentPlayingPhotoUri = PreferencesUtil.getInstance().getCurrentPlayingPhotoUri();
        if (TextUtils.isEmpty(currentPlayingPhotoUri) || !new File(currentPlayingPhotoUri).exists
                ()) {// 说明从未播放过图片或者上次播放的图片发生变化了（可能MediaScanner扫描到第一个图片文件的时候，PhotoFragment
            // 未在前台，这样就收不到onScannedFirstPhoto()回调，所以需要在PhotoFragment打开的时候做一次判断）
            // 播放扫描到的第一张图片
            displayPhoto(photoUri);
        }
        // 隐藏左上角“无图片文件”文字提示
        dismissNoPhotoFileNotice();
    }

    @Override
    public void onScannedFirstMusic(int usbFlag, String musicUri) {
    }

    @Override
    public void onScannedFirstVideo(int usbFlag, String videoUri) {
    }

    @Override
    public void onScanFinish(int usbFlag) {// 扫描结束
        // 扫描结束，更新下当前图片列表
        updateCurrentDirectory(usbFlag);
    }

    @Override
    public void onUsbMounted(int usbFlag) {// U盘插入（能响应此回调方法，说明正在音乐界面）
        updateUsbStatus();

        // 恢复播放状态
        String currentPlayingPhotoUri = PreferencesUtil.getInstance().getCurrentPlayingPhotoUri();
        if (!TextUtils.isEmpty(currentPlayingPhotoUri) && new File(currentPlayingPhotoUri).exists
                ()) {// 播放过并且文件存在（如果不存在，播放扫描到的第一张图片，此时刚挂载，还未扫描到第一张图片，需在扫描到第一张图片回调中再次判断）
            if ((currentPlayingPhotoUri.startsWith(MultimediaConstants.PATH_USB1) && usbFlag ==
                    MultimediaConstants.FLAG_USB1) || (currentPlayingPhotoUri.startsWith
                    (MultimediaConstants.PATH_USB2) && usbFlag == MultimediaConstants.FLAG_USB2))
            {// 如果插入的U盘是最后播放的那个U盘，恢复播放
                // 展示图片
                displayPhoto(currentPlayingPhotoUri);
                // 恢复播放状态
                mPhotoOperateModel.resumePlayState();
            }
        }
    }

    @Override
    public void onUsbUnMounted(int usbFlag) {// U盘拔出（能响应此回调方法，说明正在音乐界面）
        updateUsbStatus();
    }

    @Override
    public void onPhotoInfosClear(int usbFlag) {// 图片数据被清空
        updateCurrentDirectory(usbFlag);
    }

    @Override
    public void onPhotoUriSubResultFolder(int usbFlag, String rootDirectory, String folderUri) {
        // 打开目录
        openDirectory(usbFlag, folderUri);
    }

    @Override
    public void onPhotoUriSubResultFile(int usbFlag, String rootDirectory, String photoUri) {
        // 停止自动切换
        mPhotoOperateModel.switchOff();
        // 播放图片
        displayPhoto(photoUri);
    }

    @Override
    public void onBackToParentDirectory(int usbFlag, String parentDir) {
        // 打开目录
        openDirectory(usbFlag, parentDir);
    }

    @Override
    public void onShowPhotoConsoleView() {
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        // 去全屏
        cancelFullSceen();
        // 显示左侧控制视图
        linear_left.setVisibility(View.VISIBLE);

        if (showPhotoListNextClickBlank) {// 如果点击空白处需要显示图片文件列表
            linear_filemanager.setVisibility(View.VISIBLE);
        } else {
            linear_filemanager.setVisibility(View.INVISIBLE);// 这里用INVISIBLE，用GONE跑马灯第一次无效
        }
    }

    @Override
    public void onHidePhotoConsoleView(boolean time_out) {
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        // 全屏
        setFullScreen();
        if (time_out) {// 如果是超时隐藏的，则下次点击图片不显示右侧目录栏
            showPhotoListNextClickBlank = false;
        }
        // 隐藏左侧控制栏
        linear_left.setVisibility(View.GONE);
        // 左侧控制栏隐藏时，右侧目录栏随即隐藏
        linear_filemanager.setVisibility(View.INVISIBLE);// 这里用INVISIBLE，用GONE跑马灯第一次无效
    }

    @Override
    public void onSwitchOn() {// 开关打开（播放）
        // 开始切换图片
        startChangePhoto();
        // 设置开关状态为打开
        setSwitcherState(true);
    }

    @Override
    public void onSwitchOff() {// 开关关闭（暂停）
        // 停止切换图片
        stopChangePhoto();
        // 设置开关状态为暂停
        setSwitcherState(false);
    }

    @Override
    public void onPreviousPhoto() {// 上一张图片
        // 停止自动切换
        mPhotoOperateModel.switchOff();
        // 切换上一张图片
        previous();
    }

    @Override
    public void onNextPhoto() {// 下一张图片
        // 停止自动切换
        mPhotoOperateModel.switchOff();
        // 切换下一张图片
        next();
    }

    @Override
    public void onRotateLeft() {// 左翻转
        // 停止自动切换
        mPhotoOperateModel.switchOff();
        // 左翻转图片
        rotateLeft();
    }

    @Override
    public void onRotateRight() {// 右翻转
        // 停止自动切换
        mPhotoOperateModel.switchOff();
        // 右翻转图片
        rotateRight();
    }

    @Override
    public void onScaleBig() {// 放大
        // 停止自动切换
        mPhotoOperateModel.switchOff();
        // 放大图片
        scaleBig();
    }

    @Override
    public void onScaleSmall() {// 缩小
        // 停止自动切换
        mPhotoOperateModel.switchOff();
        // 缩小图片
        scaleSmall();
    }

    /**
     * 注册工厂测试广播接收器
     */
    private void registerFactoryTestReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(PhotoFactoryTestConstants.SEMISKY_PICTURE_START);
        mFilter.addAction(PhotoFactoryTestConstants.SEMISKY_PICTURE_STOP);
        mFilter.addAction(PhotoFactoryTestConstants.SEMISKY_PICTURE_NEXT);
        mFilter.addAction(PhotoFactoryTestConstants.SEMISKY_PICTURE_PREVIOUS);
        mFilter.addAction(PhotoFactoryTestConstants.SEMISKY_PICTURE_SHOWFILE);
        mFilter.addAction(PhotoFactoryTestConstants.SEMISKY_PICTURE_ROTATION_LEFT);
        mFilter.addAction(PhotoFactoryTestConstants.SEMISKY_PICTURE_ROTATION_RIGHT);
        mFilter.addAction(PhotoFactoryTestConstants.SEMISKY_PICTURE_SCALE_BIG);
        mFilter.addAction(PhotoFactoryTestConstants.SEMISKY_PICTURE_SCALE_SMALL);
        getContext().registerReceiver(factoryTestReceiver, mFilter);
    }

    /**
     * 注销工厂测试广播接收器
     */
    private void unregisterFactoryTestReceiver() {
        getContext().unregisterReceiver(factoryTestReceiver);
    }

    private static class PhotoHandler extends Handler {
        private static WeakReference<PhotoFragment> mReference;

        public PhotoHandler(PhotoFragment fragment) {
            mReference = new WeakReference<PhotoFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mReference.get() == null) {
                return;
            }

            switch (msg.what) {
                case PhotoConstants.MSG_CHANGE_PHOTO_INTERVAL:// 图片播放
                    mReference.get().next();
                    mReference.get().mPhotoHandler.sendEmptyMessageDelayed(PhotoConstants
                            .MSG_CHANGE_PHOTO_INTERVAL, mReference.get().photo_change_interval);
                    break;
                default:
                    break;
            }
        }
    }
}