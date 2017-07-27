package com.semisky.ym_multimedia.music.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.common.controller.FragmentSwitchController;
import com.semisky.ym_multimedia.common.utils.CommonConstants;
import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.common.view.MarqueeTextView;
import com.semisky.ym_multimedia.multimedia.base.MultimediaBaseFragment;
import com.semisky.ym_multimedia.multimedia.dao.MediaScanner;
import com.semisky.ym_multimedia.multimedia.model.MediaDataModel;
import com.semisky.ym_multimedia.multimedia.model.MediaDataModelDBImp;
import com.semisky.ym_multimedia.multimedia.utils.FileUriUtil;
import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;
import com.semisky.ym_multimedia.multimedia.utils.OnScanMediaFileListener;
import com.semisky.ym_multimedia.multimedia.utils.PreferencesUtil;
import com.semisky.ym_multimedia.multimedia.utils.TimeTransformer;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager.OnUsbStateChangeListener;
import com.semisky.ym_multimedia.multimedia.view.RightFileManagerView;
import com.semisky.ym_multimedia.multimedia.view.RightFileManagerView
        .OnRightFileManagerViewStateListener;
import com.semisky.ym_multimedia.multimedia.view.UsbRadioButton;
import com.semisky.ym_multimedia.multimedia.view.UsbRootDirectoryButton;
import com.semisky.ym_multimedia.music.adapter.MusicFileAdapter;
import com.semisky.ym_multimedia.music.bean.MusicInfo;
import com.semisky.ym_multimedia.music.factorytest.MusicFactoryTestConstants;
import com.semisky.ym_multimedia.music.model.MusicPlayModeChangeModel;
import com.semisky.ym_multimedia.music.model.MusicPlayModeChangeModel.OnMusicPlayModeChangeListener;
import com.semisky.ym_multimedia.music.model.MusicUriSubModel;
import com.semisky.ym_multimedia.music.model.MusicUriSubModel.MusicUriSubResultCallback;
import com.semisky.ym_multimedia.music.service.MusicPlayCallback;
import com.semisky.ym_multimedia.music.service.MusicPlayService;
import com.semisky.ym_multimedia.music.service.MusicPlayService.MusicPlayBinder;
import com.semisky.ym_multimedia.music.utils.MusicConstants;
import com.semisky.ym_multimedia.music.utils.MusicFragmentCallback;
import com.semisky.ym_multimedia.music.utils.MusicInfoParser;
import com.semisky.ym_multimedia.music.utils.MusicInfoParser.OnParseMusicInfoListener;
import com.semisky.ym_multimedia.music.utils.MusicLyricParser;
import com.semisky.ym_multimedia.music.utils.MusicLyricParser.OnLyricParseListener;
import com.semisky.ym_multimedia.music.utils.OnMusicInfosChangeListener;
import com.semisky.ym_multimedia.music.view.CircleImageView;
import com.semisky.ym_multimedia.music.view.LrcView;

import java.io.File;

public class MusicFragment extends MultimediaBaseFragment implements OnTouchListener,
        OnClickListener, OnLongClickListener, OnItemClickListener, CompoundButton
                .OnCheckedChangeListener, OnSeekBarChangeListener, OnScanMediaFileListener,
        MusicUriSubResultCallback, OnMusicInfosChangeListener, OnUsbStateChangeListener,
        OnParseMusicInfoListener, OnMusicPlayModeChangeListener, OnLyricParseListener,
        OnRightFileManagerViewStateListener {
    private View contentView;
    private View linear_left, // 左侧控制栏
            linear_top_right;// 右上角三个按钮
    private RightFileManagerView linear_filemanager;// 右侧目录栏
    private Button btn_switcher,// 播放开关
            btn_previous,// 上一曲按钮
            btn_next,// 下一曲按钮
            btn_playmode,// 播放模式按钮
            btn_sound,// 音效按钮
            btn_list;// 媒体列表
    private MarqueeTextView tv_music_title, // 歌曲标题
            tv_album,// 专辑名
            tv_artist;// 演唱者
    private TextView title_album, title_artist;// 专辑标题，演唱标题
    private TextView tv_ratio;// 歌曲比例
    private TextView tv_notice;// 左下角提示文字
    private LrcView lrcView;// 歌词
    private FrameLayout linear_album_picture;
    private CircleImageView album_picture;// 专辑图片
    private SeekBar sb_music;// 播放进度条
    private TextView tv_playtime,// 播放时间
            tv_duration;// 歌曲总时长
    private View ib_radio,// 跳转收音机按钮
            ib_video,// 跳转视频按钮
            ib_bt_music;// 跳转蓝牙音乐按钮
    private UsbRadioButton rb_usb1, rb_usb2;// USB选项卡
    private LinearLayout layout_list_usb1, layout_list_usb2;
    private UsbRootDirectoryButton root_directory1, root_directory2;// 根目录
    private ListView lv_usb1, lv_usb2;// 文件列表
    private MusicFileAdapter musicFileAdapter1, musicFileAdapter2;
    private MusicPlayService mService;
    private MediaDataModel mMediaDataModel;// 多媒体数据获取Model
    private MusicFragmentCallback mMusicFragmentCallback;// 回调Activity接口
    /**
     * MusicPlayService播放状态回调接口
     */
    private MusicPlayCallback mMusicPlayCallback = new MusicPlayCallback() {

        @Override
        public void onResetFirst() {// 播放前重置
            if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
                return;
            }
            // 设置开关为暂停状态
            setSwitcherState(false);
            // 重置歌词显示
            if (lrcView != null) {
                lrcView.reset();
            }
            // 重置播放进度显示
            setProgress(0);
        }

        @Override
        public void onPrepareStart(String musicUri) {//  开始准备
            if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
                return;
            }

            // 设置开关为暂停状态
            setSwitcherState(false);
            // 刷新列表选中状态
            if (musicUri.startsWith(MultimediaConstants.PATH_USB1)) {
                notifyDataSetChanged(MultimediaConstants.FLAG_USB1, PreferencesUtil.getInstance()
                        .getMusicUsb1RootDirectory(), true);
            } else if (musicUri.startsWith(MultimediaConstants.PATH_USB2)) {
                notifyDataSetChanged(MultimediaConstants.FLAG_USB2, PreferencesUtil.getInstance()
                        .getMusicUsb2RootDirectory(), true);
            }
        }

        @Override
        public void onMusicNotExist(String musicUri) {// 音乐文件不存在
            if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
                return;
            }

            // 设置开关为暂停状态
            setSwitcherState(false);
        }

        @Override
        public void onPrepareError(String musicUri) {// Prepare出错回调
            if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
                return;
            }

            // 设置开关为暂停状态
            setSwitcherState(false);
        }

        @Override
        public void onMusicPlay(String musicUri) {// 开始播放回调
            if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
                return;
            }

            // 设置开关为播放状态
            setSwitcherState(true);
        }

        @Override
        public void onUpdatePlayProgress(int progress) {// 更新播放进度回调
            if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
                return;
            }

            // 更新歌词显示
            if (lrcView != null) {
                lrcView.updateTime(progress, true);
            }
            // 设置播放进度显示
            setProgress(progress);
        }

        @Override
        public void onSeekToProgress(final int progress) {
            if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
                return;
            }

            // 同步歌词
            if (lrcView != null) {
                lrcView.onDrag(progress, false);
            }
            // 同步更新进度时间显示
            setProgress(progress);
        }

        @Override
        public void onMusicPause() {// 暂停播放回调
            if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
                return;
            }

            // 设置开关为暂停状态
            setSwitcherState(false);
        }

        @Override
        public void onMusicStop() {// 停止播放回调
            if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
                return;
            }

            // 设置开关为暂停状态
            setSwitcherState(false);
        }

        @Override
        public void onRestorePlayState() {// 重新获取播放状态
            if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
                return;
            }

            // 设置开关状态
            setSwitcherState(mService.isPlaying());
            // 设置播放进度显示
            int progress = mService.getProgress();
            if (progress >= 0) {// 合法进度
                setProgress(progress);
            }
        }
    };
    /**
     * 工厂测试广播接收器
     */
    private BroadcastReceiver factoryTestReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MusicFactoryTestConstants.SEMISKY_MUSIC_NEXT.equals(action)) {// 下一首
                if (mService != null) {
                    mService.sendMessageToPlayNext(0, false);
                }
            } else if (MusicFactoryTestConstants.SEMISKY_MUSIC_PREVIOUS.equals(action)) {// 上一首
                if (mService != null) {
                    mService.sendMessageToPlayPrevious(0);
                }
            } else if (MusicFactoryTestConstants.SEMISKY_MUSIC_PAUSE.equals(action)) {// 暂停
                if (mService != null) {
                    mService.sendMessageToPause(0, true);
                }
            } else if (MusicFactoryTestConstants.SEMISKY_MUSIC_START.equals(action)) {// 播放
                if (mService != null) {
                    mService.sendMessageToStart(0, true);
                }
            } else if (MusicFactoryTestConstants.SEMISKY_MUSIC_SEEKTO.equals(action)) {// 指定进度
                if (mService != null) {
                    mService.seekToByService(intent.getIntExtra("PROGRESS", -1));
                }
            } else if (MusicFactoryTestConstants.SEMISKY_MUSIC_PLAYFILE.equals(action)) {// 指定播放
                if (mService != null) {
                    mService.sendMessageToPlay(intent.getStringExtra("PATH"), -1, 0, true, true);
                }
            }
        }
    };

    public void setMusicFragmentCallback(MusicFragmentCallback callback) {
        this.mMusicFragmentCallback = callback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        Logger.logD("MusicFragment-----------------------onCreateView");
        contentView = inflater.inflate(R.layout.fragment_music, container, false);
        // 初始化
        createView(inflater, container, savedInstanceState);
        return contentView;
    }

    @Override
    public void onResume() {
        Logger.logD("MusicFragment-----------------------onResume");
        // 启动并绑定MusicPlayService
        startService();
        // 注册相关
        register();

        super.onResume();

        // 保存最后使用的多媒体类型为音乐
        PreferencesUtil.getInstance().setFinallyEnjoy(MultimediaConstants.ENJOY_MUSIC);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.logD("MusicFragment-----------------------onConfigurationChanged");
        // 本地语言发生改变，重置语言显示
        btn_previous.setText(R.string.previous_music);
        btn_next.setText(R.string.next_music);
        btn_sound.setText(R.string.sound);
        btn_list.setText(R.string.list);
        title_album.setText(R.string.album);
        title_artist.setText(R.string.artist);
        lrcView.setLabel(R.string.no_lyric);
    }

    @Override
    public void resetAdapters() {
        // 需要手动置空，否则再次切换时成员变量不会为null
        musicFileAdapter1 = null;
        musicFileAdapter2 = null;
    }

    @Override
    public void initModel() {
        mMediaDataModel = MediaDataModelDBImp.getInstance(getContext());
    }

    @Override
    public void initLeftViews() {
        linear_left = contentView.findViewById(R.id.linear_left);
        btn_switcher = (Button) linear_left.findViewById(R.id.btn_switcher);
        btn_previous = (Button) linear_left.findViewById(R.id.btn_previous);
        btn_next = (Button) linear_left.findViewById(R.id.btn_next);
        btn_playmode = (Button) linear_left.findViewById(R.id.btn_playmode);
        btn_sound = (Button) linear_left.findViewById(R.id.btn_sound);
        btn_list = (Button) linear_left.findViewById(R.id.btn_list);
    }

    @Override
    public void initRightViews() {
        linear_top_right = contentView.findViewById(R.id.linear_top_right);
        linear_filemanager = (RightFileManagerView) contentView.findViewById(R.id
                .linear_filemanager);
        ib_radio = linear_top_right.findViewById(R.id.ib_radio);
        ib_video = linear_top_right.findViewById(R.id.ib_video);
        ib_bt_music = linear_top_right.findViewById(R.id.ib_bt_music);
        rb_usb1 = (UsbRadioButton) linear_filemanager.findViewById(R.id.rb_usb1);
        rb_usb2 = (UsbRadioButton) linear_filemanager.findViewById(R.id.rb_usb2);
        layout_list_usb1 = (LinearLayout) linear_filemanager.findViewById(R.id.layout_list_usb1);
        layout_list_usb2 = (LinearLayout) linear_filemanager.findViewById(R.id.layout_list_usb2);
        root_directory1 = (UsbRootDirectoryButton) layout_list_usb1.findViewById(R.id
                .root_directory1);
        root_directory2 = (UsbRootDirectoryButton) layout_list_usb2.findViewById(R.id
                .root_directory2);
        lv_usb1 = (ListView) layout_list_usb1.findViewById(R.id.lv_usb1);
        lv_usb2 = (ListView) layout_list_usb2.findViewById(R.id.lv_usb2);
    }

    @Override
    public void initMiddleViews() {
        tv_music_title = (MarqueeTextView) contentView.findViewById(R.id.tv_music_title);
        title_album = (TextView) contentView.findViewById(R.id.title_album);
        title_artist = (TextView) contentView.findViewById(R.id.title_artist);
        tv_album = (MarqueeTextView) contentView.findViewById(R.id.tv_album);
        tv_artist = (MarqueeTextView) contentView.findViewById(R.id.tv_artist);
        tv_ratio = (TextView) contentView.findViewById(R.id.tv_ratio);
        tv_notice = (TextView) contentView.findViewById(R.id.tv_notice);
        lrcView = (LrcView) contentView.findViewById(R.id.lrcView);
        linear_album_picture = (FrameLayout) contentView.findViewById(R.id.linear_album_picture);
        album_picture = (CircleImageView) linear_album_picture.findViewById(R.id.album_picture);
        tv_playtime = (TextView) contentView.findViewById(R.id.tv_playtime);
        sb_music = (SeekBar) contentView.findViewById(R.id.sb_music);
        tv_duration = (TextView) contentView.findViewById(R.id.tv_duration);
    }

    @Override
    public int getSystemUITitleResId() {
        return R.string.music;
    }

    @Override
    public void setLeftViewsListener() {
        btn_switcher.setOnClickListener(this);
        btn_previous.setOnTouchListener(this);
        btn_previous.setOnClickListener(this);
        btn_previous.setOnLongClickListener(this);
        btn_next.setOnTouchListener(this);
        btn_next.setOnClickListener(this);
        btn_next.setOnLongClickListener(this);
        btn_playmode.setOnClickListener(this);
        btn_sound.setOnClickListener(this);
        btn_list.setOnTouchListener(this);
        btn_list.setOnClickListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setRightViewsListener() {
        ib_radio.setOnClickListener(this);
        ib_video.setOnClickListener(this);
        ib_bt_music.setOnClickListener(this);
        linear_filemanager.setOnRightFileManagerViewStateListener(this);
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

    public void setMiddleViewsListener() {
        linear_album_picture.setOnClickListener(this);
        // 设置长按监听是为了不长按后抬起还触发onClick事件
        linear_album_picture.setOnLongClickListener(this);
        lrcView.setOnClickListener(this);
        // 设置长按监听是为了不长按后抬起还触发onClick事件
        lrcView.setOnLongClickListener(this);
        sb_music.setOnSeekBarChangeListener(this);
    }

    @Override
    public void register() {
        // 注册扫描媒体监听器
        MediaScanner.getInstance(getContext()).registerScanMediaFileListener(this);
        // 设置音频数据变化监听器
        mMediaDataModel.setOnMusicInfosChangeListener(this);
        // 注册U盘状态变化监听器
        UsbStateManager.getInstance().registerUsbStateChangeListener(this);
        // 设置音频播放模式切换监听器
        MusicPlayModeChangeModel.getInstance(getContext()).setOnMusicPlayModeChangeListener(this);
        // 注册音频文件解析监听器
        MusicInfoParser.getInstance().registerParseMusicInfoListener(this);
        // 注册歌曲路径分解回调监听器
        MusicUriSubModel.getInstance().registerMusicUriSubResultCallback(this);
        // 注册歌词解析监听器
        MusicLyricParser.getInstance().setOnLyricParseListener(this);
        // 注册工厂测试广播接收器
        registerFactoryTestReceiver();
    }

    /**
     * 关联MusicPlayService
     */
    private void startService() {
        // 启动MusicPlayService
        getContext().startService(new Intent(getContext(), MusicPlayService.class));
        // 回调MainActivity绑定MusicPlayService
        if (mMusicFragmentCallback != null) {
            mMusicFragmentCallback.requestBindMusicPlayService();
        }
    }

    public void onServiceConnected(ComponentName name, IBinder iBinder) {//
        // 绑定MusicPlayService成功（MainActivity调用）
        Logger.logD("MusicFragment-----------------------onServiceConnected");
        // 得到MusicPlayService句柄
        mService = ((MusicPlayBinder) iBinder).getService();
        // 绑定成功
        mService.onServiceConnected();
        // 设置MusicPlayService回调接口
        mService.setMusicPlayCallback(mMusicPlayCallback);
        // 恢复播放情况
        mService.restorePlayState();
    }

    public void onServiceDisconnected(ComponentName name) {// 解除MusicPlayService绑定（MainActivity调用）
        Logger.logD("MusicFragment-----------------------onServiceDisconnected");
        // 句柄置空
        mService = null;
    }

    @Override
    public void initStatus() {
        // 设置循环模式UI
        updatePlayMode(PreferencesUtil.getInstance().getMusicPlayMode());
        // 初始化歌词和专辑图片是否显示
        setLyricViewStatus(PreferencesUtil.getInstance().isLyricViewShowing(), false);
        // 刷新USB状态
        updateUsbStatus();
    }

    @Override
    public void setSwitcherState(boolean isPlaying) {
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
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        switch (mode) {
            case MusicConstants.MODE_CIRCLE_FOLDER:// 文件夹循环
                setDrawableTop(btn_playmode, R.drawable.icon_mode_circle_folder);
                btn_playmode.setText(R.string.playmode_circle_folder);
                break;
            case MusicConstants.MODE_CIRCLE_SINGLE:// 单曲循化
                setDrawableTop(btn_playmode, R.drawable.icon_mode_circle_single);
                btn_playmode.setText(R.string.playmode_circle_single);
                break;
            case MusicConstants.MODE_RANDOM:// 随机播放
                setDrawableTop(btn_playmode, R.drawable.icon_mode_random);
                btn_playmode.setText(R.string.playmode_random);
                break;
            case MusicConstants.MODE_CIRCLE_ALL:// 全部循环
                setDrawableTop(btn_playmode, R.drawable.icon_mode_circle_all);
                btn_playmode.setText(R.string.playmode_circle_all);
                break;
            default:
                break;
        }
    }

    @Override
    public void updateUsbStatus() {
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        int finallyChooseUsbFlag = PreferencesUtil.getInstance().getMusicFinallyChooseUsbFlag();
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
                            .getMusicUsb1RootDirectory());
                    PreferencesUtil.getInstance().setMusicFinallyChooseUsbFlag
                            (MultimediaConstants.FLAG_USB1);
                }
                break;
            case MultimediaConstants.FLAG_USB2:
                if (UsbStateManager.getInstance().isUsb2Mounted()) {// USB2已插上
                    layout_list_usb1.setVisibility(View.INVISIBLE);// 这里用INVISIBLE，用GONE跑马灯第一次无效
                    layout_list_usb2.setVisibility(View.VISIBLE);

                    rb_usb1.setChoosed(false);

                    openDirectory(usbFlag, PreferencesUtil.getInstance()
                            .getMusicUsb2RootDirectory());
                    PreferencesUtil.getInstance().setMusicFinallyChooseUsbFlag
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
                PreferencesUtil.getInstance().setMusicUsb1RootDirectory(directory);
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
                PreferencesUtil.getInstance().setMusicUsb2RootDirectory(directory);
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
    public void updateCurrentDirectory(int usbFlag) {// 刷新当前路径下音乐文件及包含音乐文件的文件夹列表
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                String directory1 = PreferencesUtil.getInstance().getMusicUsb1RootDirectory();
                if (!new File(directory1).exists()) {// 如果保存的文件夹不存在
                    directory1 = MultimediaConstants.PATH_USB1;
                }
                // 刷新列表
                notifyDataSetChanged(usbFlag, directory1, false);
                break;
            case MultimediaConstants.FLAG_USB2:
                String directory2 = PreferencesUtil.getInstance().getMusicUsb2RootDirectory();
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
    public void notifyDataSetChanged(int usbFlag, String directory, boolean scrollToPlay) {
        // 获取某个目录下的音乐文件，刷新列表，并滚动到当前播放位置
        if (TextUtils.isEmpty(directory)) {
            return;
        }

        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                // 获取该目录下的直属的音乐文件及包含音乐文件的文件夹（相同的文件夹只添加一次）
                mMediaDataModel.queryMusicDirectUnder(usbFlag, directory);
                // 刷新列表
                if (musicFileAdapter1 == null) {
                    musicFileAdapter1 = new MusicFileAdapter(getActivity(), MediaDataModelDBImp
                            .getInstance(getContext()).getUsb1ShowingMusicList());
                    lv_usb1.setAdapter(musicFileAdapter1);
                } else {
                    musicFileAdapter1.notifyDataSetChanged();
                }
                if (scrollToPlay) {
                    // 获取当前正在播放的音乐的Uri
                    String currentPlayingMusicUri1 = PreferencesUtil.getInstance()
                            .getCurrentPlayingMusicUri();
                    // 如果正在播放当前目录下的音乐文件，则滚动到当前播放音乐文件位置
                    if (!TextUtils.isEmpty(currentPlayingMusicUri1) && directory.equals
                            (currentPlayingMusicUri1.substring(0,
                                    currentPlayingMusicUri1.lastIndexOf(File.separator)))) {
                        // 获取正在播放的音乐文件在当前列表的位置
                        int pos = mMediaDataModel.getMusicPosition(usbFlag,
                                currentPlayingMusicUri1);
                        // 滚动列表
                        if (pos != -1) {
                            lv_usb1.smoothScrollToPosition(pos);
                        }
                    }
                }
                break;
            case MultimediaConstants.FLAG_USB2:
                // 获取该目录下的直属的音乐文件及包含音乐文件的文件夹（相同的文件夹只添加一次）
                mMediaDataModel.queryMusicDirectUnder(usbFlag, directory);
                // 刷新列表
                if (musicFileAdapter2 == null) {
                    musicFileAdapter2 = new MusicFileAdapter(getActivity(), MediaDataModelDBImp
                            .getInstance(getContext()).getUsb2ShowingMusicList());
                    lv_usb2.setAdapter(musicFileAdapter2);
                } else {
                    musicFileAdapter2.notifyDataSetChanged();
                }
                if (scrollToPlay) {
                    // 获取当前正在播放的音乐的Uri
                    String currentPlayingMusicUri2 = PreferencesUtil.getInstance()
                            .getCurrentPlayingMusicUri();
                    // 如果正在播放当前目录下的音乐文件，则滚动到当前播放音乐文件位置
                    if (!TextUtils.isEmpty(currentPlayingMusicUri2) && directory.equals
                            (currentPlayingMusicUri2.substring(0,
                                    currentPlayingMusicUri2.lastIndexOf(File.separator)))) {
                        // 获取正在播放的音乐文件在当前列表的位置
                        int pos = mMediaDataModel.getMusicPosition(usbFlag,
                                currentPlayingMusicUri2);
                        // 滚动列表
                        if (pos != -1) {
                            lv_usb2.smoothScrollToPosition(pos);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置歌词和专辑图片的显示状态
     *
     * @param showLyric 是否显示歌词，隐藏专辑图片
     * @param saveState 是否保存状态
     */
    private void setLyricViewStatus(boolean showLyric, boolean saveState) {
        if (saveState) {
            PreferencesUtil.getInstance().setLyricViewShowing(showLyric);
        }

        if (getActivity() != null) {
            if (showLyric) {
                linear_album_picture.setVisibility(View.INVISIBLE);// 不能用GONE，因为LrcView需要做一些初始化操作
                lrcView.setVisibility(View.VISIBLE);
            } else {
                linear_album_picture.setVisibility(View.VISIBLE);
                lrcView.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 设置左下角文字提示
     */
    private void showNotice(int resid) {
        // 显示错误提示
        tv_notice.setVisibility(View.VISIBLE);
        tv_notice.setText(resid);
    }

    /**
     * 隐藏左下角文字提示
     */
    private void dismissNotice() {
        tv_notice.setVisibility(View.GONE);
    }

    /**
     * 设置最大进度
     */
    private void setMax(int duration) {
        Logger.logD("MusicFragment-----------------------setMax = " + TimeTransformer
                .getMusicFormatTime(duration));
        // 设置进度条最大值
        if (sb_music != null) {
            sb_music.setMax(duration);
        }
        // 设置时长显示
        if (tv_duration != null) {
            tv_duration.setText(TimeTransformer.getMusicFormatTime(duration));
        }
    }

    /**
     * 设置播放进度UI
     */
    private void setProgress(int progress) {
        // 设置进度条进度
        if (sb_music != null) {
            sb_music.setProgress(progress);
        }
        // 设置进度时间显示
        if (tv_playtime != null) {
            tv_playtime.setText(TimeTransformer.getMusicFormatTime(progress));
        }
    }

    @Override
    public void onStop() {
        Logger.logD("MusicFragment-----------------------onStop");
        // 停止快进或快退
        if (mService != null) {
            mService.stopAdjustProgressBackward();
            mService.stopAdjustProgressForward();
        }
        // 注销相关
        unregister();
        // 解绑
        unBind();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.logD("MusicFragment-----------------------onDestroy");
    }

    /**
     * 注销
     */
    @Override
    public void unregister() {
        // 注销扫描媒体监听器
        MediaScanner.getInstance(getContext()).unregisterScanMediaFileListener(this);
        // 注销音频数据变化监听器
        mMediaDataModel.setOnMusicInfosChangeListener(null);
        // 注销U盘状态变化监听器
        UsbStateManager.getInstance().unregisterUsbStateChangeListener(this);
        // 注销音频播放模式切换监听器
        MusicPlayModeChangeModel.getInstance(getContext()).setOnMusicPlayModeChangeListener(null);
        // 注销音频文件监听器
        MusicInfoParser.getInstance().unregisterParseMusicInfoListener(this);
        // 注销歌曲路径分解回调监听器
        MusicUriSubModel.getInstance().unregisterMusicUriSubResultCallback(this);
        // 注销歌词解析监听器
        MusicLyricParser.getInstance().setOnLyricParseListener(null);
        // 注销工厂测试广播接收器
        unregisterFactoryTestReceiver();
    }

    /**
     * 解除后台绑定
     */
    private void unBind() {
        Logger.logD("MusicFragment-----------------------unBind");
        if (mService != null) {
            // 释放MusicPlayService回调接口
            mService.setMusicPlayCallback(null);
            // 置空
            mService = null;
        }
        if (mMusicFragmentCallback != null) {
            // 回调MainActivity解绑MusicPlayService
            mMusicFragmentCallback.requestUnbindMusicPlayService();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (v.getId()) {
                    case R.id.btn_list:
                    case R.id.rb_usb1:
                    case R.id.rb_usb2:
                    case R.id.root_directory1:
                    case R.id.root_directory2:
                    case R.id.lv_usb1:
                    case R.id.lv_usb2:
                        // 手指在音乐列表控件上按下时，先取消计时，手指抬起时重新开始计时
                        linear_filemanager.cancelHideCountDown();
                        break;
                    default:
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                switch (v.getId()) {
                    case R.id.btn_previous:
                        if (mService != null) {
                            // 停止快退
                            mService.stopAdjustProgressBackward();
                        }
                        break;
                    case R.id.btn_next:
                        if (mService != null) {
                            // 停止快进
                            mService.stopAdjustProgressForward();
                        }
                        break;
                    case R.id.btn_list:
                    case R.id.rb_usb1:
                    case R.id.rb_usb2:
                    case R.id.root_directory1:
                    case R.id.root_directory2:
                    case R.id.lv_usb1:
                    case R.id.lv_usb2:
                        // 如果手指抬起，且音乐列表可见，开始计时
                        linear_filemanager.startHideCountDown();
                        break;
                    default:
                        break;
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
            case R.id.btn_switcher:// 播放开关
                if (mService != null) {
                    if (mService.isPlaying()) {
                        mService.sendMessageToPause(0, true);
                    } else {
                        mService.sendMessageToStart(0, true);
                    }
                }
                break;
            case R.id.btn_previous:// 上一曲按钮
                if (mService != null) {
                    mService.sendMessageToPlayPrevious(0);
                }
                break;
            case R.id.btn_next:// 下一曲按钮
                if (mService != null) {
                    mService.sendMessageToPlayNext(0, true);
                }
                break;
            case R.id.btn_playmode:// 循环模式按钮
                MusicPlayModeChangeModel.getInstance(getContext()).changePlayMode();
                break;
            case R.id.btn_sound:// 跳转音效设置界面
                gotoSound();
                break;
            case R.id.btn_list:// 媒体列表按钮
                if (linear_filemanager.isVisible()) {
                    linear_filemanager.hide();
                } else {
                    linear_filemanager.show();
                }
                break;
            case R.id.ib_radio:// 切换到收音机
                FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_RADIO,
                        null);
                break;
            case R.id.ib_video:// 切换到视频
                FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_VIDEO,
                        null);
                break;
            case R.id.ib_bt_music:// 切换到蓝牙音乐
                Bundle bundle = new Bundle();
                bundle.putInt(CommonConstants.KEY_GOTO_BT, CommonConstants.GOTO_BT_MUSIC);
                FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_BT,
                        bundle);
                break;
            case R.id.root_directory1:// 回到上一级目录
                MusicUriSubModel.getInstance().backToParentDir(MultimediaConstants.FLAG_USB1,
                        root_directory1.getText(MultimediaConstants.FLAG_USB1));
                break;
            case R.id.root_directory2:// 回到上一级目录
                MusicUriSubModel.getInstance().backToParentDir(MultimediaConstants.FLAG_USB2,
                        root_directory2.getText(MultimediaConstants.FLAG_USB2));
                break;
            case R.id.linear_album_picture:
                // 显示歌词
                setLyricViewStatus(true, true);
                break;
            case R.id.lrcView:
                // 显示专辑图片
                setLyricViewStatus(false, true);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.btn_previous:// 触发长按事件，开始快退
                if (mService != null) {
                    mService.startAdjustProgressBackward();
                }
                return true;// 独占，防止触发onClick事件
            case R.id.btn_next:// 触发长按事件，开始快进
                if (mService != null) {
                    mService.startAdjustProgressForward();
                }
                return true;// 独占，防止触发onClick事件
            case R.id.linear_album_picture:
            case R.id.lrcView:
                return true;// 独占，防止触发onClick事件
            default:
                break;
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.rb_usb1:// 选择U盘1
                if (isChecked) {
                    chooseUsb(MultimediaConstants.FLAG_USB1);
                }
                break;
            case R.id.rb_usb2:// 选择U盘2
                if (isChecked) {
                    chooseUsb(MultimediaConstants.FLAG_USB2);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mService != null) {
            mService.onStartTrackingTouch();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mService != null) {
            mService.onStopTrackingTouch(seekBar.getProgress());
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {// 用户拖动
            // 更新播放进度时间显示
            if (tv_playtime != null) {
                tv_playtime.setText(TimeTransformer.getMusicFormatTime(progress));
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.lv_usb1:
                // 分解musicUri，根据结果来决定是打开目录还是播放音乐
                MusicUriSubModel.getInstance().subMusicUri(MultimediaConstants.FLAG_USB1,
                        PreferencesUtil.getInstance().getMusicUsb1RootDirectory(), (String)
                                musicFileAdapter1.getItem(position));
                break;
            case R.id.lv_usb2:
                // 分解musicUri，根据结果来决定是打开目录还是播放音乐
                MusicUriSubModel.getInstance().subMusicUri(MultimediaConstants.FLAG_USB2,
                        PreferencesUtil.getInstance().getMusicUsb2RootDirectory(), (String)
                                musicFileAdapter2.getItem(position));
                break;
            default:
                break;
        }
    }

    @Override
    public void onScanStart(int usbFlag) {// 扫描开始

    }

    @Override
    public void onNotifyRefreshList(int usbFlag) {// 每隔一段时间通知刷新媒体列表
        // 更新当前音乐列表
        updateCurrentDirectory(usbFlag);
    }

    @Override
    public void onScannedFirstPhoto(int usbFlag, String photoUri) {
    }

    @Override
    public void onScannedFirstMusic(int usbFlag, String musicUri) {
        // 扫描到第一个音乐文件回调（用于初次插入U盘默认播放第一首歌曲）

        // 获取当前正在播放的音乐的Uri
        String currentPlayingMusicUri = PreferencesUtil.getInstance().getCurrentPlayingMusicUri();
        if (TextUtils.isEmpty(currentPlayingMusicUri) || !new File(currentPlayingMusicUri).exists
                ()) {// 说明从未播放过音乐或者记忆的歌曲不存在了，此时播放扫描到的第一首歌曲（可能MediaScanner
            // 扫描到第一个音乐文件的时候，MusicFragment未在前台，这样就收不到onScannedFirstMusic()
            // 回调，所以需要在MusicFragment打开的时候做一次判断）
            // 默认播放扫描到的第一首音乐
            if (mService != null) {
                mService.sendMessageToPlay(musicUri, -1, 0, true, true);
            }
        }
    }

    @Override
    public void onScannedFirstVideo(int usbFlag, String videoUri) {
    }

    @Override
    public void onScanFinish(int usbFlag) {// 扫描结束
        // 扫描结束，更新下当前音乐列表
        updateCurrentDirectory(usbFlag);
        // 获取当前播放的音乐Uri
        String currentPlayingMusicUri = PreferencesUtil.getInstance().getCurrentPlayingMusicUri();
        // 扫描结束，更新下当前选取的音乐的比例
        tv_ratio.setText(mMediaDataModel.getMusicPosition(currentPlayingMusicUri));
        // 扫描结束，更新下当前选取的音乐的歌词显示
        if (!lrcView.hasLrc()) {// 如果当前未加载出歌词（可能之前没有扫描到，扫描结束时再尝试一次）
            MusicLyricParser.getInstance().loadLyric(getContext(), lrcView,
                    currentPlayingMusicUri, mMediaDataModel);
        }
    }

    @Override
    public void onUsbMounted(int usbFlag) {// U盘插入（能响应此回调方法，说明正在音乐界面）
        // 刷新USB状态显示
        updateUsbStatus();

        // 恢复播放状态
        String currentPlayingMusicUri = PreferencesUtil.getInstance().getCurrentPlayingMusicUri();
        if (!TextUtils.isEmpty(currentPlayingMusicUri) && new File(currentPlayingMusicUri).exists
                ()) {// 播放过并且文件存在（如果不存在，播放扫描到的第一首歌曲，此时刚挂载，还未扫描到第一首歌曲，需在扫描到第一首歌曲回调中再次判断）
            if ((currentPlayingMusicUri.startsWith(MultimediaConstants.PATH_USB1) && usbFlag ==
                    MultimediaConstants.FLAG_USB1) || (currentPlayingMusicUri.startsWith
                    (MultimediaConstants.PATH_USB2) && usbFlag == MultimediaConstants.FLAG_USB2))
            {// 如果插入的U盘是最后播放的那个U盘，恢复播放
                if (mService != null) {
                    mService.sendMessageToPlay(currentPlayingMusicUri, PreferencesUtil
                            .getInstance().getCurrentPlayingMusicProgress(), 0, PreferencesUtil
                            .getInstance().isMusicFinallyPlaying(), false);
                }
            }
        }
    }

    @Override
    public void onUsbUnMounted(int usbFlag) {// U盘拔出（能响应此回调方法，说明正在音乐界面）
        updateUsbStatus();
    }

    @Override
    public void onMusicInfosClear(int usbFlag) {// 歌曲数据清除
        updateCurrentDirectory(usbFlag);
    }

    @Override
    public void onMusicPlayModeChanged(int mode) {// 播放模式变化
        updatePlayMode(mode);
    }

    @Override
    public void onParseMusicInfoStart(String musicUri) {// 开始解析
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        // 开始解析了，显示专辑标题和演唱标题
        title_album.setVisibility(View.VISIBLE);
        title_artist.setVisibility(View.VISIBLE);
        // 设置歌曲名
        tv_music_title.setText("");
        // 设置专辑名
        tv_album.setText("");
        // 设置演唱者
        tv_artist.setText("");
        // 设置歌曲比例
        tv_ratio.setText("");
        // 隐藏错误提示
        dismissNotice();
        // 设置专辑图片
        album_picture.setImageDrawable(null);
        // 设置进度条最大值
        setMax(0);
        // 重置进度条进度
        setProgress(0);
    }

    @Override
    public void onParseMusicFileNotExists(String musicUri) {// 文件不存在
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        // 设置歌曲名
        tv_music_title.setText(FileUriUtil.getFileTitle(musicUri));
        // 显示音乐文件不存在提示
        showNotice(R.string.music_not_exists);
    }

    @Override
    public void onParseMusicInfoError(String musicUri) {// 解析出错
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        // 设置歌曲名
        tv_music_title.setText(FileUriUtil.getFileTitle(musicUri));
        // 设置歌曲比例
        tv_ratio.setText(mMediaDataModel.getMusicPosition(musicUri));
        // 显示错误提示
        showNotice(R.string.unsupported_file);
    }

    @Override
    public void onParseMusicInfoComplete(MusicInfo info) {// 解析成功
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        // 设置歌曲名
        tv_music_title.setText(info.getTitle());
        // 设置专辑名
        tv_album.setText(info.getAlbum());
        // 设置演唱者
        tv_artist.setText(info.getArtist());
        // 设置歌曲比例
        tv_ratio.setText(mMediaDataModel.getMusicPosition(info.getUri()));
        // 加载歌词
        MusicLyricParser.getInstance().loadLyric(getContext(), lrcView, info.getUri(),
                mMediaDataModel);
        // 加载专辑图片
        MusicInfoParser.getInstance().setAlbumImage(album_picture, info.getUri());
        // 设置进度条最大值
        setMax(info.getDuration());
    }

    @Override
    public void onLyricNotExists(String musicUri) {// 对应歌曲的歌词文件不存在
    }

    @Override
    public void onParseLyricFail(String musicUri) {// 解析歌词文件失败
    }

    @Override
    public void onParseLyricSuccess(String musicUri) {// 解析歌词文件成功
        // 歌词解析成功后才能进行歌词定位操作，主要是为了onRestorePlayState()方法中重新获取播放状态刷新
        if (mService != null && getActivity() != null) {
            // 获取播放进度
            int progress = mService.getProgress();
            if (progress >= 0) {// 进度合法
                lrcView.onDrag(progress, false);
            }
        }
    }

    @Override
    public void onMusicUriSubResultFolder(int usbFlag, String rootDirectory, String folderUri)
    {// itemClick判断的结果是文件夹
        // 打开目录
        openDirectory(usbFlag, folderUri);
    }

    @Override
    public void onMusicUriSubResultFile(int usbFlag, String rootDirectory, String musicUri) {//
        // itemClick判断的结果是音乐文件
        // 播放音乐文件
        if (mService != null) {
            mService.sendMessageToPlay(musicUri, -1, 0, true, true);
        }
    }

    @Override
    public void onBackToParentDirectory(int usbFlag, String parentDir) {
        // 打开父目录
        openDirectory(usbFlag, parentDir);
    }

    @Override
    public void onRightFileManagerViewShow() {// 显示右侧音乐列表回调
        linear_top_right.setVisibility(View.GONE);
    }

    @Override
    public void onRightFileManagerViewHide() {// 隐藏右侧音乐列表回调
        linear_top_right.setVisibility(View.VISIBLE);
    }

    /**
     * 注册工厂测试广播接收器
     */
    private void registerFactoryTestReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(MusicFactoryTestConstants.SEMISKY_MUSIC_NEXT);
        mFilter.addAction(MusicFactoryTestConstants.SEMISKY_MUSIC_PREVIOUS);
        mFilter.addAction(MusicFactoryTestConstants.SEMISKY_MUSIC_PAUSE);
        mFilter.addAction(MusicFactoryTestConstants.SEMISKY_MUSIC_START);
        mFilter.addAction(MusicFactoryTestConstants.SEMISKY_MUSIC_SEEKTO);
        mFilter.addAction(MusicFactoryTestConstants.SEMISKY_MUSIC_PLAYFILE);
        getContext().registerReceiver(factoryTestReceiver, mFilter);
    }

    /**
     * 注销工厂测试广播接收器
     */
    private void unregisterFactoryTestReceiver() {
        getContext().unregisterReceiver(factoryTestReceiver);
    }
}