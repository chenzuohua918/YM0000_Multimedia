package com.semisky.ym_multimedia.video.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.common.controller.FragmentSwitchController;
import com.semisky.ym_multimedia.common.controller.StatusbarController;
import com.semisky.ym_multimedia.common.utils.AppUtil;
import com.semisky.ym_multimedia.common.utils.CommonConstants;
import com.semisky.ym_multimedia.common.utils.FastClickUtil;
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
import com.semisky.ym_multimedia.multimedia.view.UsbRadioButton;
import com.semisky.ym_multimedia.multimedia.view.UsbRootDirectoryButton;
import com.semisky.ym_multimedia.video.adapter.VideoFileAdapter;
import com.semisky.ym_multimedia.video.bean.VideoInfo;
import com.semisky.ym_multimedia.video.factorytest.VideoFactoryTestConstants;
import com.semisky.ym_multimedia.video.model.VideoConsoleViewModel;
import com.semisky.ym_multimedia.video.model.VideoConsoleViewModel.VideoConsoleViewStateCallback;
import com.semisky.ym_multimedia.video.model.VideoKeyLongClickModel;
import com.semisky.ym_multimedia.video.model.VideoKeyLongClickModel.OnVideoKeyLongClickListener;
import com.semisky.ym_multimedia.video.model.VideoPlayModeChangeModel;
import com.semisky.ym_multimedia.video.model.VideoPlayModeChangeModel.OnVideoPlayModeChangeListener;
import com.semisky.ym_multimedia.video.model.VideoUriSubModel;
import com.semisky.ym_multimedia.video.model.VideoUriSubModel.VideoUriSubResultCallback;
import com.semisky.ym_multimedia.video.utils.OnVideoInfosChangeListener;
import com.semisky.ym_multimedia.video.utils.VideoConstants;
import com.semisky.ym_multimedia.video.utils.VideoInfoParser;
import com.semisky.ym_multimedia.video.utils.VideoInfoParser.OnParseVideoInfoListener;
import com.semisky.ym_multimedia.video.utils.VideoPlayStateSaver;
import com.semisky.ym_multimedia.video.utils.VideoUtil;
import com.semisky.ym_multimedia.video.view.CurtainView;
import com.semisky.ym_multimedia.video.view.VideoView;
import com.semisky.ym_multimedia.video.view.VideoView.OnSlidingListener;
import com.semisky.ym_multimedia.video.view.VideoView.SurfaceHolderCallback;

import java.io.File;
import java.lang.ref.WeakReference;

public class VideoFragment extends MultimediaBaseFragment implements OnTouchListener,
        OnClickListener, OnLongClickListener, CompoundButton.OnCheckedChangeListener,
        OnItemClickListener, SurfaceHolderCallback, OnScanMediaFileListener,
        OnVideoInfosChangeListener, OnUsbStateChangeListener, OnParseVideoInfoListener,
        OnSeekBarChangeListener, VideoUriSubResultCallback, OnVideoPlayModeChangeListener,
        VideoConsoleViewStateCallback, OnSlidingListener, OnVideoKeyLongClickListener {
    private final VideoHandler mVideoHandler = new VideoHandler(this);
    private View contentView;
    private View linear_left, // 左侧控制栏
            linear_top_right,// 右上角三个按钮
            linear_filemanager;// 右侧目录栏
    private Button btn_switcher,// 播放开关
            btn_previous,// 上一个按钮
            btn_next,// 下一个按钮
            btn_playmode,// 播放模式按钮
            btn_sound,// 音效按钮
            btn_list;// 媒体列表按钮
    private VideoView mVideoView;// 视频控件
    private CurtainView curtainView;// 黑色幕布（用以遮住切换视频时的闪屏、花屏现象）
    private MarqueeTextView tv_notice;// 文字提示
    private RelativeLayout linear_seekbar;// 进度条栏
    private SeekBar sb_video;// 播放进度条
    private TextView tv_playtime,// 播放时间
            tv_duration;// 视频总时长
    private View ib_radio,// 跳转收音机按钮
            ib_music,// 跳转媒体音乐按钮
            ib_bt_music;// 跳转蓝牙音乐按钮
    private UsbRadioButton rb_usb1, rb_usb2;// USB选项卡
    private LinearLayout layout_list_usb1, layout_list_usb2;
    private UsbRootDirectoryButton root_directory1, root_directory2;// 根目录
    private ListView lv_usb1, lv_usb2;// 文件列表
    private VideoFileAdapter videoFileAdapter1, videoFileAdapter2;// 文件列表1／2的适配器
    private AudioManager mAudioManager;
    private MediaDataModel mMediaDataModel;// 多媒体数据获取Model
    private Dialog mSpeedWarningDialog;// 车速警告弹框
    private int period_fast_click = 300;// 快速点击响应周期
    private int touchResponseTime = 300;// 界定短按时间
    private int warning_speed = 30;// 警告车速
    private int safe_speed = 20;// 安全车速
    private long videoViewTouchDownTime;// 手指在VideoView按下的时间
    private boolean isEnableToGetCurrentPosition = false;// 当前是否能获取合法的播放进度
    private boolean sendMessageToAdjustBackward = false;// 是否发消息快退
    private boolean sendMessageToAdjustForward = false;// 是否发消息快进
    /**
     * 准备播放相关接口
     */
    private OnPreparedListener mPreparedListener = new OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            Logger.logD("VideoFragment-----------------------视频播放准备完成");
            // 准备完成，此时可以获取播放进度
            isEnableToGetCurrentPosition = true;
        }
    };
    /**
     * 播放完毕相关接口
     */
    private OnCompletionListener mCompletionListener = new OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            Logger.logD("VideoFragment-----------------------视频播放完毕，1秒后播放下一个");
            // 播放结束，此时不可获取播放进度
            isEnableToGetCurrentPosition = false;
            // 如果有在发送更新播放进度消息，先停止发送
            stopSendMessageToUpdatePlayProgress();
            // 1秒后播放下一个视频
            sendMessageToPlayNext(1000, false);
        }
    };
    /**
     * 错误相关接口
     */
    private OnErrorListener mErrorListener = new OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            // 出错，此时不可获取播放进度
            isEnableToGetCurrentPosition = false;

            switch (what) {// 出现的错误类型
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:// 未知错误（有时会出现解析视频正常，但是一播放就异常，说明没有相应License）
                    Logger.logE("VideoFragment-----------------------未知错误");
                    // 设置开关状态为暂停
                    setSwitcherState(false);
                    // 文字提示未知错误
                    if (getActivity() != null) {
                        String videoUri = PreferencesUtil.getInstance().getCurrentPlayingVideoUri();
                        if (TextUtils.isEmpty(videoUri) || !new File(videoUri).exists()) {//
                            // 因视频文件不存在导致的未知错误
                            // 提示视频不存在
                            showNotice(videoUri, R.string.video_not_exists);
                        } else {
                            // 提示播放视频发生未知错误
                            showNotice(videoUri, R.string.unknown_error);
                        }
                        // 出现错误时（文件不存在或者视频无法播放），重置时长
                        setMax(0);
                        // 出现错误时（文件不存在或者视频无法播放），不允许隐藏控制视图
                        VideoConsoleViewModel.getInstance().setEnableToHide(false);
                        // 出现错误时（文件不存在或者视频无法播放），显示控制视图，并且稍后不隐藏
                        VideoConsoleViewModel.getInstance().showVideoConsoleView(false);
                    }
                    // 有时会出现解析视频正常，但是一播放就异常的情况（比如缺少License），需要重置视频解析结果为null，否则按播放键还会变换UI状态
                    setPlayingVideoInfo(null);
                    // 未知错误，3秒后播放下一个视频，为了在单曲循环时不造成死循环，传true
                    sendMessageToPlayNext(3000, true);
                    break;
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:// 服务器错误
                    Logger.logE("VideoFragment-----------------------服务器错误");
                    break;
                default:
                    break;
            }

            switch (extra) {// 针对与具体错误的附加码, 用于定位错误更详细信息
                case MediaPlayer.MEDIA_ERROR_IO:// 本地文件或网络相关错误
                    Logger.logE("VideoFragment-----------------------本地文件或网络相关错误");
                    break;
                case MediaPlayer.MEDIA_ERROR_MALFORMED:// 比特流不符合相关的编码标准和文件规范
                    Logger.logE("VideoFragment-----------------------比特流不符合相关的编码标准和文件规范");
                    break;
                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:// 框架不支持该功能
                    Logger.logE("VideoFragment-----------------------框架不支持该功能");
                    break;
                case MediaPlayer.MEDIA_ERROR_TIMED_OUT:// 一些操作超时
                    Logger.logE("VideoFragment-----------------------一些操作超时");
                    break;
                default:
                    break;
            }
            return true;
        }
    };
    /**
     * 信息相关接口
     */
    private OnInfoListener mInfoListener = new OnInfoListener() {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {// 信息或者警告的类型
                case MediaPlayer.MEDIA_INFO_UNKNOWN:// 未知的信息
                    Logger.logE("VideoFragment-----------------------未知的信息");
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:// 视频过于复杂解码太慢
                    Logger.logE("VideoFragment-----------------------视频过于复杂解码太慢");
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:// 开始渲染第一帧
                    Logger.logD("VideoFragment-----------------------开始渲染第一帧");
                    // 渲染第一帧时立马隐藏幕布还是能看到卡帧，所以幕布做了动画渐变隐藏处理
                    dismissCurtainView();
                    // 有时候MediaMetadataRetriever无法解析出视频时长，而VideoView却可以
                    if (getPlayingVideoInfo() != null && getPlayingVideoInfo().getDuration() ==
                            0) {// 如果解析结果时长为0，尝试用VideoView再一次获取时长
                        int duration = mVideoView.getDuration();
                        if (duration > 0) {
                            getPlayingVideoInfo().setDuration(duration);
                            // 设置进度条最大值
                            setMax(duration);
                        }
                    }
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:// 暂停播放开始缓冲更多数据
                    Logger.logD("VideoFragment-----------------------暂停播放开始缓冲更多数据");
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:// 缓冲了足够的数据重新开始播放
                    Logger.logD("VideoFragment-----------------------缓冲了足够的数据重新开始播放");
                    break;
                case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:// 错误交叉
                    Logger.logE("VideoFragment-----------------------错误交叉");
                    break;
                case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:// 媒体不能够搜索
                    Logger.logE("VideoFragment-----------------------媒体不能够搜索");
                    break;
                case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:// 一组新的元数据用
                    Logger.logD("VideoFragment-----------------------一组新的元数据用");
                    break;
                case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:// 不支持字幕
                    Logger.logE("VideoFragment-----------------------不支持字幕");
                    break;
                case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:// 读取字幕时间过长
                    Logger.logE("VideoFragment-----------------------读取字幕时间过长");
                    break;
                default:
                    break;
            }
            return true;
        }
    };
    /**
     * 中控实体按钮监听
     */
    private android.os.IKeyPressInterface mIKeyPressInterface = new android.os.IKeyPressInterface
            .Stub() {

        public void onKeyPressed(int keyCode, int mode) {
            switch (keyCode) {
                case CommonConstants.KEY_PREV:// 按键上一曲
                    switch (mode) {
                        case CommonConstants.KEY_MODE_CLICK:// 短按
                            Logger.logD("VideoFragment-----------------------KEY_PREV短按");
                            // 切换上一个视频
                            sendMessageToPlayPrevious(0);
                            break;
                        case CommonConstants.KEY_MODE_LONG_CLICK:// 长按
                            Logger.logD("VideoFragment-----------------------KEY_PREV长按");
                            // 响应上一曲按钮长按事件
                            VideoKeyLongClickModel.getInstance().receiveKeyLongClickPrevEvent();
                            break;
                        default:
                            break;
                    }
                    break;
                case CommonConstants.KEY_NEXT:// 按键下一曲
                    switch (mode) {
                        case CommonConstants.KEY_MODE_CLICK:// 短按
                            Logger.logD("VideoFragment-----------------------KEY_NEXT短按");
                            // 切换下一个视频
                            sendMessageToPlayNext(0, true);
                            break;
                        case CommonConstants.KEY_MODE_LONG_CLICK:// 长按
                            Logger.logD("VideoFragment-----------------------KEY_NEXT长按");
                            // 响应下一曲按钮长按事件
                            VideoKeyLongClickModel.getInstance().receiveKeyLongClickNextEvent();
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

        public String onGetAppInfo() {// 返回标记
            return "video";
        }
    };
    /**
     * 音频焦点监听器
     */
    private OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener
            () {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK://
                    // 暂时失去AudioFocus，但是可以继续播放（如导航时），不过要降低音量。
                    Logger.logD
                            ("VideoFragment-----------------------AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    if (mVideoView != null && getActivity() != null) {
                        // 设置混音最低比例
                        mVideoView.setLowestVolumeRatio(AppUtil.calNavMixLowestRatio(getContext()));
                        // 音量渐变降低
                        mVideoView.fadeDownVolume();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:// 暂时失去了AudioFocus,
                    // 但很快会重新得到焦点（如来电时），必须停止Audio的播放，但是因为可能会很快再次获得AudioFocus，这里可以不释放Media资源。
                    Logger.logD("VideoFragment-----------------------AUDIOFOCUS_LOSS_TRANSIENT");
                    // 注销中控按钮监听器（来电弹框状态下不能操作视频）
                    unregisterKeyPressListener();

                    if (mVideoView != null && getActivity() != null) {
                        mVideoView.setLowestVolumeRatio(0f);
                        if (isPlaying()) {
                            // 暂停播放（因为不是用户自主暂停播放的，所以不保存状态）
                            sendMessageToPause(0, false);
                        }
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:// 失去AudioFocus，并将会持续很长的时间（如播放音乐或视频时）
                    Logger.logD("VideoFragment-----------------------AUDIOFOCUS_LOSS");
                    // 注销中控按钮监听器（失去音频永久焦点状态下不能操作视频）
                    unregisterKeyPressListener();

                    if (mVideoView != null && getActivity() != null) {
                        mVideoView.setLowestVolumeRatio(0f);
                        if (isPlaying()) {
                            // 暂停播放（因为不是用户自主暂停播放的，所以不保存状态）
                            sendMessageToPause(0, false);
                        }
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:// 获得AudioFocus
                    Logger.logD("VideoFragment-----------------------AUDIOFOCUS_GAIN");
                    // 注册中控按钮监听器
                    registerKeyPressListener();

                    // 如果失去焦点前是在播放的，获取焦点后继续播放
                    if (mVideoView != null && getActivity() != null && PreferencesUtil
                            .getInstance().isVideoFinallyPlaying()) {
                        if (mVideoView.getLowestVolumeRatio() > 0f) {
                            // 说明是AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK之后重新获取音频焦点
                            // 把声音拉高
                            mVideoView.fadeUpVolume();
                        } else {// 说明是AUDIOFOCUS_LOSS_TRANSIENT之后重新获取音频焦点
                            // 恢复播放
                            sendMessageToStart(0, false);
                        }
                    }
                    break;
                default:
                    break;
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
            if (VideoFactoryTestConstants.SEMISKY_VIDEO_NEXT.equals(action)) {// 下一个视频
                sendMessageToPlayNext(0, false);
            } else if (VideoFactoryTestConstants.SEMISKY_VIDEO_PREVIOUS.equals(action)) {// 上一个视频
                sendMessageToPlayPrevious(0);
            } else if (VideoFactoryTestConstants.SEMISKY_VIDEO_PAUSE.equals(action)) {// 暂停
                sendMessageToPause(0, true);
            } else if (VideoFactoryTestConstants.SEMISKY_VIDEO_START.equals(action)) {// 播放
                sendMessageToStart(0, true);
            } else if (VideoFactoryTestConstants.SEMISKY_VIDEO_SEEKTO.equals(action)) {// 指定进度
                seekToProgress(intent.getIntExtra("PROGRESS", -1), true);
            } else if (VideoFactoryTestConstants.SEMISKY_VIDEO_PLAYFILE.equals(action)) {// 指定播放
                sendMessageToPlay(intent.getStringExtra("PATH"), -1, 0, true, true);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        Logger.logD("VideoFragment-----------------------onCreateView");
        contentView = inflater.inflate(R.layout.fragment_video, container, false);
        // 关闭倒车的RearCamera，释放视频资源
        VideoUtil.closedRearCamera();
        // 初始化
        createView(inflater, container, savedInstanceState);
        return contentView;
    }

    @Override
    public void onResume() {
        Logger.logD("VideoFragment-----------------------onResume");
        // 开启车速监控
        startSpeedMonitor();
        // 注册相关
        register();

        super.onResume();

        // 保存最后使用的多媒体类型为视频
        PreferencesUtil.getInstance().setFinallyEnjoy(MultimediaConstants.ENJOY_VIDEO);
        // 注册中控按钮监听器
        registerKeyPressListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.logD("VideoFragment-----------------------onConfigurationChanged");
        // 本地语言发生改变，重置语言显示
        btn_previous.setText(R.string.previous_video);
        btn_next.setText(R.string.next_video);
        btn_sound.setText(R.string.sound);
        btn_list.setText(R.string.list);
    }

    /**
     * 手动重置
     */
    @Override
    public void resetAdapters() {
        // 需要手动置空，否则再次切换时成员变量不会为null
        videoFileAdapter1 = null;
        videoFileAdapter2 = null;
    }

    @Override
    public void initModel() {
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
        linear_filemanager = contentView.findViewById(R.id.linear_filemanager);
        ib_radio = linear_top_right.findViewById(R.id.ib_radio);
        ib_music = linear_top_right.findViewById(R.id.ib_music);
        ib_bt_music = linear_top_right.findViewById(R.id.ib_bt_music);
        rb_usb1 = (UsbRadioButton) linear_filemanager.findViewById(R.id.rb_usb1);
        rb_usb2 = (UsbRadioButton) linear_filemanager.findViewById(R.id.rb_usb2);
        layout_list_usb1 = (LinearLayout) linear_filemanager.findViewById(R.id.layout_list_usb1);
        layout_list_usb2 = (LinearLayout) linear_filemanager.findViewById(R.id.layout_list_usb2);
        root_directory1 = (UsbRootDirectoryButton) linear_filemanager.findViewById(R.id
                .root_directory1);
        root_directory2 = (UsbRootDirectoryButton) linear_filemanager.findViewById(R.id
                .root_directory2);
        lv_usb1 = (ListView) linear_filemanager.findViewById(R.id.lv_usb1);
        lv_usb2 = (ListView) linear_filemanager.findViewById(R.id.lv_usb2);
    }

    @Override
    public void initMiddleViews() {
        mVideoView = (VideoView) contentView.findViewById(R.id.videoView);
        curtainView = (CurtainView) contentView.findViewById(R.id.curtainView);
        tv_notice = (MarqueeTextView) contentView.findViewById(R.id.tv_notice);
        tv_playtime = (TextView) contentView.findViewById(R.id.tv_playtime);
        linear_seekbar = (RelativeLayout) contentView.findViewById(R.id.linear_seekbar);
        sb_video = (SeekBar) contentView.findViewById(R.id.sb_video);
        tv_duration = (TextView) contentView.findViewById(R.id.tv_duration);
    }

    @Override
    public int getSystemUITitleResId() {
        return R.string.video;
    }

    @Override
    public void setLeftViewsListener() {
        btn_switcher.setOnTouchListener(this);
        btn_switcher.setOnClickListener(this);
        btn_previous.setOnTouchListener(this);
        btn_previous.setOnClickListener(this);
        btn_previous.setOnLongClickListener(this);
        btn_next.setOnTouchListener(this);
        btn_next.setOnClickListener(this);
        btn_next.setOnLongClickListener(this);
        btn_playmode.setOnTouchListener(this);
        btn_playmode.setOnClickListener(this);
        btn_sound.setOnTouchListener(this);
        btn_sound.setOnClickListener(this);
        btn_list.setOnTouchListener(this);
        btn_list.setOnClickListener(this);
    }

    @Override
    public void setRightViewsListener() {
        ib_radio.setOnTouchListener(this);
        ib_radio.setOnClickListener(this);
        ib_music.setOnTouchListener(this);
        ib_music.setOnClickListener(this);
        ib_bt_music.setOnTouchListener(this);
        ib_bt_music.setOnClickListener(this);
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
        mVideoView.setOnTouchListener(this);
        mVideoView.setSurfaceHolderCallback(this);
        mVideoView.setOnPreparedListener(mPreparedListener);
        mVideoView.setOnCompletionListener(mCompletionListener);
        mVideoView.setOnErrorListener(mErrorListener);
        mVideoView.setOnInfoListener(mInfoListener);
        mVideoView.setOnSlidingListener(this);

        tv_playtime.setOnTouchListener(this);
        tv_playtime.setOnClickListener(this);
        sb_video.setOnSeekBarChangeListener(this);
        tv_duration.setOnTouchListener(this);
        tv_duration.setOnClickListener(this);
    }

    @Override
    public void register() {
        // 注册扫描媒体监听器
        MediaScanner.getInstance(getContext()).registerScanMediaFileListener(this);
        // 设置音频数据变化监听器
        mMediaDataModel = MediaDataModelDBImp.getInstance(getContext());
        mMediaDataModel.setOnVideoInfosChangeListener(this);
        // 注册U盘状态变化监听器
        UsbStateManager.getInstance().registerUsbStateChangeListener(this);
        // 设置视频播放模式切换监听器
        VideoPlayModeChangeModel.getInstance(getContext()).setOnVideoPlayModeChangeListener(this);
        // 设置视频解析监听器
        VideoInfoParser.getInstance().setOnParseVideoInfoListener(this);
        // 注册视频路径分解回调监听器
        VideoUriSubModel.getInstance().registerVideoUriSubResultCallback(this);
        // 注册视频控制视图显示隐藏的回调监听器
        VideoConsoleViewModel.getInstance().setCallback(this);
        // 注册实体按钮上下一曲长按事件监听器
        VideoKeyLongClickModel.getInstance().setOnVideoKeyLongClickListener(this);
        // 注册工厂测试广播接收器
        registerFactoryTestReceiver();
    }

    @Override
    public void initStatus() {
        // 显示控制视图
        VideoConsoleViewModel.getInstance().showVideoConsoleView(true);
        // 设置开关UI状态
        setSwitcherState(isPlaying());
        // 设置循环模式UI
        updatePlayMode(PreferencesUtil.getInstance().getVideoPlayMode());
        // 设置USB状态
        updateUsbStatus();
        // 默认显示黑色幕布
        showCurtainView();
        // 申请音频焦点
        requestAudioFocus();
        // 初次播放或者恢复上次播放
        String videoUri = PreferencesUtil.getInstance().getCurrentPlayingVideoUri();
        if (TextUtils.isEmpty(videoUri) || !new File(videoUri).exists()) {//
            // 说明从未播放过视频或者记忆的视频不存在了（可能MediaScanner扫描到第一个视频文件的时候，VideoFragment
            // 未在前台，这样就收不到onScannedFirstVideo()回调，所以需要在VideoFragment打开的时候做一次判断）
            // 从扫描类中获取第一个扫描到的视频文件
            videoUri = MediaScanner.getInstance(getContext()).getFirstScannedVideoUri();
            if (TextUtils.isEmpty(videoUri)) {// 暂时没有扫描到视频文件或者压根没有插U盘
                // 此时不可隐藏控制视图
                VideoConsoleViewModel.getInstance().setEnableToHide(false);
                // 左上角提示“无相关媒体文件”
                showNotice(null, R.string.no_such_file);
            } else {
                // 初次自动播放
                Logger.logD("VideoFragment-----------------------从未播放过视频，默认播放第一个扫描到的视频：" +
                        videoUri);
                // 发送播放视频消息
                sendMessageToPlay(videoUri, -1, 0, true, true);
            }
        } else {// 说明播放过视频，恢复播放
            int progress = 0;// 播放进度
            boolean autoStart = true;// 是否启动播放
            boolean saveState = true;// 是否保存播放状态（播放或暂停）
            if (VideoPlayStateSaver.getInstance().isFromFileManager()) {// 从文件管理器跳转播放的
                // 获取从文件管理器传来的视频路径
                videoUri = VideoPlayStateSaver.getInstance().getVideoUriFromFileManager();
                // 重置变量
                VideoPlayStateSaver.getInstance().setFromFileManager(false);
                VideoPlayStateSaver.getInstance().setVideoUriFromFileManager(null);
            } else {
                progress = PreferencesUtil.getInstance().getCurrentPlayingVideoProgress();
                autoStart = PreferencesUtil.getInstance().isVideoFinallyPlaying();
                saveState = false;// 因为是恢复播放，所以不需要再保存播放状态
            }
            // 发送播放视频消息
            sendMessageToPlay(videoUri, progress, 0, autoStart, saveState);
        }
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
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        switch (mode) {
            case VideoConstants.MODE_CIRCLE_FOLDER:// 文件夹循环
                setDrawableTop(btn_playmode, R.drawable.icon_mode_circle_folder);
                btn_playmode.setText(R.string.playmode_circle_folder);
                break;
            case VideoConstants.MODE_CIRCLE_SINGLE:// 单曲循化
                setDrawableTop(btn_playmode, R.drawable.icon_mode_circle_single);
                btn_playmode.setText(R.string.playmode_circle_single);
                break;
            case VideoConstants.MODE_RANDOM:// 随机播放
                setDrawableTop(btn_playmode, R.drawable.icon_mode_random);
                btn_playmode.setText(R.string.playmode_random);
                break;
            case VideoConstants.MODE_CIRCLE_ALL:// 全部循环
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

        int finallyChooseUsbFlag = PreferencesUtil.getInstance().getVideoFinallyChooseUsbFlag();
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
                            .getVideoUsb1RootDirectory());
                    PreferencesUtil.getInstance().setVideoFinallyChooseUsbFlag
                            (MultimediaConstants.FLAG_USB1);
                }
                break;
            case MultimediaConstants.FLAG_USB2:
                if (UsbStateManager.getInstance().isUsb2Mounted()) {// USB2已插上
                    layout_list_usb1.setVisibility(View.INVISIBLE);// 这里用INVISIBLE，用GONE跑马灯第一次无效
                    layout_list_usb2.setVisibility(View.VISIBLE);

                    rb_usb1.setChoosed(false);

                    openDirectory(usbFlag, PreferencesUtil.getInstance()
                            .getVideoUsb2RootDirectory());
                    PreferencesUtil.getInstance().setVideoFinallyChooseUsbFlag
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
                PreferencesUtil.getInstance().setVideoUsb1RootDirectory(directory);
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
                PreferencesUtil.getInstance().setVideoUsb2RootDirectory(directory);
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
    public void updateCurrentDirectory(int usbFlag) {// 刷新当前路径下视频文件及包含视频文件的文件夹列表
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                String directory1 = PreferencesUtil.getInstance().getVideoUsb1RootDirectory();
                if (!new File(directory1).exists()) {// 如果保存的文件夹不存在
                    directory1 = MultimediaConstants.PATH_USB1;
                }
                // 刷新列表
                notifyDataSetChanged(usbFlag, directory1, false);
                break;
            case MultimediaConstants.FLAG_USB2:
                String directory2 = PreferencesUtil.getInstance().getVideoUsb2RootDirectory();
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
        // 获取某个目录下的视频文件，刷新列表，并滚动到当前播放位置
        if (TextUtils.isEmpty(directory)) {
            return;
        }

        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                // 获取该目录下的直属的视频文件及包含视频文件的文件夹（相同的文件夹只添加一次）
                mMediaDataModel.queryVideoDirectUnder(usbFlag, directory);
                // 刷新列表
                if (videoFileAdapter1 == null) {
                    videoFileAdapter1 = new VideoFileAdapter(getActivity(), MediaDataModelDBImp
                            .getInstance(getContext()).getUsb1ShowingVideoList());
                    lv_usb1.setAdapter(videoFileAdapter1);
                } else {
                    videoFileAdapter1.notifyDataSetChanged();
                }
                if (scrollToPlay) {
                    // 获取当前正在播放的视频的Uri
                    String currentPlayingVideoUri1 = PreferencesUtil.getInstance()
                            .getCurrentPlayingVideoUri();
                    // 如果正在播放当前目录下的视频文件，则滚动到当前播放视频文件位置
                    if (!TextUtils.isEmpty(currentPlayingVideoUri1) && directory.equals
                            (currentPlayingVideoUri1.substring(0,
                                    currentPlayingVideoUri1.lastIndexOf(File.separator)))) {
                        // 获取正在播放的视频文件在当前列表的位置
                        int pos = mMediaDataModel.getVideoPosition(MultimediaConstants.FLAG_USB1,
                                currentPlayingVideoUri1);
                        // 滚动列表
                        if (pos != -1) {
                            lv_usb1.smoothScrollToPosition(pos);
                        }
                    }
                }
                break;
            case MultimediaConstants.FLAG_USB2:
                // 获取该目录下的直属的视频文件及包含视频文件的文件夹（相同的文件夹只添加一次）
                mMediaDataModel.queryVideoDirectUnder(usbFlag, directory);
                // 刷新列表
                if (videoFileAdapter2 == null) {
                    videoFileAdapter2 = new VideoFileAdapter(getActivity(), MediaDataModelDBImp
                            .getInstance(getContext()).getUsb2ShowingVideoList());
                    lv_usb2.setAdapter(videoFileAdapter2);
                } else {
                    videoFileAdapter2.notifyDataSetChanged();
                }
                if (scrollToPlay) {
                    // 获取当前正在播放的视频的Uri
                    String currentPlayingVideoUri2 = PreferencesUtil.getInstance()
                            .getCurrentPlayingVideoUri();
                    // 如果正在播放当前目录下的视频文件，则滚动到当前播放视频文件位置
                    if (!TextUtils.isEmpty(currentPlayingVideoUri2) && directory.equals
                            (currentPlayingVideoUri2.substring(0,
                                    currentPlayingVideoUri2.lastIndexOf(File.separator)))) {
                        // 获取正在播放的视频文件在当前列表的位置
                        int pos = mMediaDataModel.getVideoPosition(MultimediaConstants.FLAG_USB2,
                                currentPlayingVideoUri2);
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
     * 显示幕布
     */
    private void showCurtainView() {
        curtainView.show();
    }

    /**
     * 隐藏幕布
     */
    private void dismissCurtainView() {
        curtainView.dismiss();
    }

    /**
     * 设置左上角文字提示
     */
    private void showNotice(String videoUri, int resId) {
        // 显示错误提示
        tv_notice.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(videoUri)) {
            tv_notice.setText(resId);
        } else {
            tv_notice.setText(getString(resId) + "（" + FileUriUtil.getFileTitle(videoUri) + "）");
        }
    }

    /**
     * 隐藏左上角文字提示
     */
    private void dismissNotice() {
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
     * 获取播放进度
     *
     * @return -1时表示获取值非法
     */
    private synchronized int getProgress() {
        if (mVideoView != null && isEnableToGetCurrentPosition) {
            return mVideoView.getCurrentPosition();
        }
        return -1;
    }

    /**
     * 保存播放进度
     */
    private void saveProgress(int progress) {// 合法播放进度
        if (progress != -1) {
            PreferencesUtil.getInstance().setCurrentPlayingVideoProgress(progress);
        }
    }

    /**
     * 设置最大进度
     */
    private void setMax(int duration) {
        // 设置进度条最大值
        sb_video.setMax(duration);
        // 设置时长显示
        tv_duration.setText(TimeTransformer.getVideoFormatTime(duration));
    }

    /**
     * 设置播放进度UI
     */
    private void updateProgress(int progress) {
        // 设置进度条进度
        sb_video.setProgress(progress);
        // 设置进度时间显示
        tv_playtime.setText(TimeTransformer.getVideoFormatTime(progress));
    }

    /**
     * 准备视频文件
     */
    private boolean prepare(String videoUri) {
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得做播放操作
            return false;
        }
        if (mVideoView == null) {
            return false;
        }
        if (TextUtils.isEmpty(videoUri)) {
            return false;
        }
        // 先停止刷新播放进度
        stopSendMessageToUpdatePlayProgress();
        // 保存将要播放的视频路径（包括无法播放的视频路径）
        PreferencesUtil.getInstance().setCurrentPlayingVideoUri(videoUri);
        // 重置开关状态
        setSwitcherState(false);
        // 显示黑色幕布
        showCurtainView();
        // 准备新视频，此时不可获取播放进度
        isEnableToGetCurrentPosition = false;
        // 设置播放源
        mVideoView.setVideoPath(videoUri);
        // 刷新列表选中状态
        if (videoUri.startsWith(MultimediaConstants.PATH_USB1)) {
            notifyDataSetChanged(MultimediaConstants.FLAG_USB1, PreferencesUtil.getInstance()
                    .getVideoUsb1RootDirectory(), true);
        } else if (videoUri.startsWith(MultimediaConstants.PATH_USB2)) {
            notifyDataSetChanged(MultimediaConstants.FLAG_USB2, PreferencesUtil.getInstance()
                    .getVideoUsb2RootDirectory(), true);
        }
        // 解析视频文件
        if (VideoInfoParser.getInstance().parseVideoInfo(videoUri) != null) {// 解析成功
            Logger.logD("VideoFragment-----------------------prepare() " + videoUri.substring
                    (videoUri.lastIndexOf(File.separator) + 1) + " success!!!");
            return true;
        } else {// 解析失败
            Logger.logE("VideoFragment-----------------------prepare() " + videoUri.substring
                    (videoUri.lastIndexOf(File.separator) + 1) + " fail!!!");
            // 解析失败，3秒后播放下一个视频，为了在单曲循环时不造成死循环，给true
            sendMessageToPlayNext(3000, true);
            return false;
        }
    }

    /**
     * 播放
     */
    private boolean startVideoView(boolean saveState) {
        if (android.os.ProtocolManager.getInstance().getVehicleSpeed() >= warning_speed &&
                VideoUtil.isSpeedWarningOpened(getContext())) {// 达到了警告速度，并且行车中禁止播放视频开关已经打开，则不能播放
            return false;
        }
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得做播放操作
            return false;
        }
        if (mVideoView == null) {
            return false;
        }
        if (!isCurrentVideoEnableToPlay()) {// 如果当前视频无法播放，则使其无法启动即可
            return false;
        }
        if (sb_video.getProgress() != getProgress()) {//
            // 如果实际播放进度与进度条进度不符，说明在快进快退时点击了播放按钮，此时需要seekTo到进度条进度
            // seekTo到进度条的进度
            seekToProgress(sb_video.getProgress(), true);
        }
        // 音量从0开始渐变上升（先将声音设为最低，再start，防止pop音）
        mVideoView.fadeUpVolumeFromZero();
        // 播放
        mVideoView.start();
        if (saveState) {// 如果需要保存播放状态
            // 记忆播放状态，状态为播放
            PreferencesUtil.getInstance().setVideoFinallyPlaying(true);
        }
        // 刷新前台视频开始播放
        setSwitcherState(true);
        // 开始刷新播放进度
        sendMessageToUpdatePlayProgress(0);
        Logger.logD("VideoFragment-----------------------startVideoView() saveState=" + saveState);
        return true;
    }

    /**
     * 发送启动消息
     */
    public void sendMessageToStart(int delayMillis, boolean saveState) {
        // 移除队列中的播放控制消息
        removePlayControlMessages();
        // 移除队列中的快退快进消息
        removeAdjustProgressMessages();
        // 发送启动消息
        mVideoHandler.sendMessageDelayed(mVideoHandler.obtainMessage(VideoConstants
                .MSG_VIDEO_START, saveState), delayMillis);
    }

    /**
     * 播放指定视频文件的指定位置
     *
     * @param videoUri  视频文件路径
     * @param progress  播放进度（-1代表从头开始）
     * @param autoStart 是否准备好之后自动开始播放
     * @param saveState 是否需要记忆播放状态（播放或暂停）
     * @return
     */
    private boolean play(String videoUri, int progress, boolean autoStart, boolean saveState) {
        if (mVideoView == null) {
            return false;
        }
        if (!prepare(videoUri)) {
            return false;
        }
        if (progress >= 0) {
            if (seekToProgress(progress, false)) {// 定点播放
                Logger.logD("VideoFragment-----------------------播放：" + videoUri.substring
                        (videoUri.lastIndexOf(File.separator) + 1) + " progress = " +
                        TimeTransformer.getVideoFormatTime(progress));
                if (autoStart) {
                    return startVideoView(saveState);
                }
            } else {
                Logger.logE("VideoFragment-----------------------seekToProgress:" +
                        TimeTransformer.getVideoFormatTime(progress) + " fail!!!");
            }
        } else {// 从头播放
            Logger.logD("VideoFragment-----------------------开始播放：" + videoUri.substring(videoUri
                    .lastIndexOf(File.separator) + 1));
            if (autoStart) {
                return startVideoView(saveState);
            }
        }
        return false;
    }

    /**
     * 发送播放视频消息
     */
    void sendMessageToPlay(String videoUri, int progress, int delayMillis, boolean autoStart,
                           boolean saveState) {
        // 移除队列中的播放控制消息
        removePlayControlMessages();
        // 发送播放消息
        Message msg = mVideoHandler.obtainMessage(VideoConstants.MSG_VIDEO_PLAY);
        Bundle bundle = new Bundle();
        bundle.putString("videoUri", videoUri);
        bundle.putInt("progress", progress);
        bundle.putBoolean("autoStart", autoStart);
        bundle.putBoolean("saveState", saveState);
        msg.setData(bundle);
        mVideoHandler.sendMessageDelayed(msg, delayMillis);
    }

    /**
     * 播放指定位置（有些视频无法做seekTo操作）；
     * （seekTo位置不准甚至从头开始）原因：其实seekTo跳转的位置其实并不是参数所带的position
     * ，而是离position最近的关键帧。当视频在相应的position位置缺少关键帧的情况下
     * ，调用seekTo方法是无法在当前位置开始播放。这时会寻找离指定position最近的关键帧位置开始播放。
     *
     * @param progress 进度
     * @param fromUser 是否用户拖动
     * @return
     */
    private boolean seekToProgress(int progress, boolean fromUser) {
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得做播放操作
            return false;
        }
        if (mVideoView == null) {
            return false;
        }
        if (progress >= 0
        /*
         * 因为有可能MediaMetadataRetriever解析视频时长为0，而在VideoView
		 * start后渲染第一帧时可以获取时长，所以这里不加这个判断
		 */
            // && progress <= VideoInfoParser.getInstance()
            // .getPlayingVideoInfo().getDuration()
                ) {
            VideoInfo videoInfo = VideoInfoParser.getInstance().getPlayingVideoInfo();
            if (videoInfo != null) {
                int duration = videoInfo.getDuration();
                if (duration > 0) {// 视频时长是合法的
                    if (progress >= duration) {// 拖动进度到最后
                        // 为防止反弹，切换下一个视频
                        sendMessageToPlayNext(0, true);
                    }
                }
            }
            mVideoView.seekTo(progress);
            if (!fromUser) {// 如果不是用户拖动的，则需要刷新下播放进度
                // 刷新播放进度
                updateProgress(progress);
            }
            // 保存播放进度
            saveProgress(progress);
            Logger.logD("VideoFragment-----------------------seekTo = " + TimeTransformer
                    .getVideoFormatTime(progress));
            return true;
        }
        return false;
    }

    /**
     * 微调快退（不直接seekTo，而是先调整进度条进度，然后再根据进度条最终进度来seekTo）
     */
    private void slightAdjustProgressBackward() {
        // 获取进度条的进度
        int progress = sb_video.getProgress();
        if (progress >= 0) {// 合法进度
            // 步退
            progress -= 1000;
            if (progress < 0) {// 退到头了
                // 由于此处removeMessages无效，所以使用变量来停止快退
                sendMessageToAdjustBackward = false;
                // 切换上一个视频
                sendMessageToPlayPrevious(0);
            } else {
                // 只更新进度显示，停止快退时再seekTo
                updateProgress(progress);
            }
        }
    }

    /**
     * 微调快进（不直接seekTo，而是先调整进度条进度，然后再根据进度条最终进度来seekTo）
     */
    private void slightAdjustProgressForward() {
        if (!isCurrentVideoEnableToPlay()) {// 当前视频无法播放
            return;
        }

        // 获取进度条的进度
        int progress = sb_video.getProgress();
        if (progress >= 0) {// 合法进度
            // 步进
            progress += 1000;
            if (progress > getPlayingVideoInfo().getDuration()) {// 超出总时长了
                // 由于此处removeMessages无效，所以使用变量来停止快进
                sendMessageToAdjustForward = false;
                // 切换下一个视频
                sendMessageToPlayNext(0, true);
            } else {
                // 只更新进度显示，停止快进时再seekTo
                updateProgress(progress);
            }
        }
    }

    /**
     * 开始快退
     */
    public void startAdjustProgressBackward() {
        // 移除快进快退消息
        removeAdjustProgressMessages();
        // 如果快退之前是播放的，先暂停播放
        if (PreferencesUtil.getInstance().isVideoFinallyPlaying()) {// 之前是播放的
            // 暂停播放（不保存暂停状态，快退结束后恢复原先状态）
            sendMessageToPause(0, false);
        }
        sendMessageToAdjustBackward = true;
        // 发送快退消息
        mVideoHandler.sendEmptyMessage(VideoConstants.MSG_VIDEO_SUB);
    }

    /**
     * 停止快退
     */
    public void stopAdjustProgressBackward() {
        if (mVideoHandler.hasMessages(VideoConstants.MSG_VIDEO_SUB)) {// 有快退消息，说明正在快退
            // 移除快退消息
            mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_SUB);
            sendMessageToAdjustBackward = false;
            // 停止快退时seekTo
            seekToProgress(sb_video.getProgress(), false);
            // 如果快退之前是播放的，快退之后恢复播放
            if (PreferencesUtil.getInstance().isVideoFinallyPlaying()) {// 之前是播放的
                // 恢复播放
                sendMessageToStart(0, false);
            }
        }
    }

    /**
     * 开始快进
     */
    public void startAdjustProgressForward() {
        // 移除快进快退消息
        removeAdjustProgressMessages();
        // 如果快进之前是播放的，先暂停播放
        if (PreferencesUtil.getInstance().isVideoFinallyPlaying()) {// 快进之前是播放的
            // 暂停播放（不保存暂停状态，快进结束后恢复原先状态）
            sendMessageToPause(0, false);
        }
        sendMessageToAdjustForward = true;
        // 发送快进消息
        mVideoHandler.sendEmptyMessage(VideoConstants.MSG_VIDEO_PLUS);
    }

    /**
     * 停止快进
     */
    public void stopAdjustProgressForward() {
        if (mVideoHandler.hasMessages(VideoConstants.MSG_VIDEO_PLUS)) {// 有快进消息，说明正在快进
            // 移除快进消息
            mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_PLUS);
            sendMessageToAdjustForward = false;
            // 停止快进时seekTo
            seekToProgress(sb_video.getProgress(), false);
            // 如果快进之前是播放的，快退之后恢复播放
            if (PreferencesUtil.getInstance().isVideoFinallyPlaying()) {// 快进之前是播放的
                // 恢复播放
                sendMessageToStart(0, false);
            }
        }
    }

    /**
     * 移除快进快退消息
     */
    private void removeAdjustProgressMessages() {
        mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_SUB);
        mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_PLUS);
    }

    /**
     * 获取当前播放的视频信息
     */
    private VideoInfo getPlayingVideoInfo() {
        return VideoInfoParser.getInstance().getPlayingVideoInfo();
    }

    /**
     * 设置当前播放的视频信息
     */
    private void setPlayingVideoInfo(VideoInfo info) {
        VideoInfoParser.getInstance().setPlayingVideoInfo(info);
    }

    /**
     * 当前视频是否是可播放的
     */
    private boolean isCurrentVideoEnableToPlay() {
        return getPlayingVideoInfo() != null;
    }

    /**
     * 是否在播放视频
     */
    private boolean isPlaying() {
        return mVideoView != null && mVideoView.isPlaying();
    }

    /**
     * 暂停
     */
    private boolean pause(boolean saveState) {
        if (isPlaying()) {
            // 停止音量渐变，将声音设为0
            mVideoView.muteVolume();
            // 先停止刷新播放进度（防止在暂停后还发送一两个更新消息）
            stopSendMessageToUpdatePlayProgress();
            // 保存最后播放进度
            saveProgress(getProgress());
            // 暂停
            mVideoView.pause();
            if (saveState) {// 如果需要记忆播放状态
                // 记忆播放状态，状态为暂停
                PreferencesUtil.getInstance().setVideoFinallyPlaying(false);
            }
            // 刷新前台视频暂停播放
            setSwitcherState(false);
            Logger.logD("VideoFragment-----------------------pause()");
            return true;
        }
        return false;
    }

    /**
     * 发送暂停消息
     */
    public void sendMessageToPause(int delayMillis, boolean saveState) {
        // 移除队列中的播放控制消息
        removePlayControlMessages();
        // 移除队列中的快退快进消息
        removeAdjustProgressMessages();
        // 发送暂停消息
        mVideoHandler.sendMessageDelayed(mVideoHandler.obtainMessage(VideoConstants
                .MSG_VIDEO_PAUSE, saveState), delayMillis);
    }

    /**
     * 停止
     */
    private void stop() {
        if (mVideoView == null) {
            return;
        }
        // 先停止刷新播放进度（防止在暂停后还发送一两个更新消息）
        stopSendMessageToUpdatePlayProgress();
        // 停止并释放MediaPlayer
        mVideoView.stopPlayback();
        // 刷新前台视频停止播放
        setSwitcherState(false);
        Logger.logD("VideoFragment-----------------------stop()");
    }

    /**
     * 发送停止播放消息
     */
    public void sendMessageToStop() {
        // 移除队列中的播放控制消息
        removePlayControlMessages();
        // 移除队列中的快退快进消息
        removeAdjustProgressMessages();
        // 发送停止播放消息
        mVideoHandler.sendEmptyMessage(VideoConstants.MSG_VIDEO_STOP);
    }

    /**
     * 删除播放控制相关的消息
     */
    void removePlayControlMessages() {
        mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_PLAY);
        mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_START);
        mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_PAUSE);
        mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_STOP);
    }

    /**
     * 发送更新播放进度消息
     */
    void sendMessageToUpdatePlayProgress(int delayMillis) {
        mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_UPDATE_PROGRESS);
        mVideoHandler.sendEmptyMessageDelayed(VideoConstants.MSG_VIDEO_UPDATE_PROGRESS,
                delayMillis);
    }

    /**
     * 停止发送更新播放进度消息
     */
    void stopSendMessageToUpdatePlayProgress() {
        mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_UPDATE_PROGRESS);
    }

    /**
     * 播放上一个视频（播放上一个视频时，需要做一些判断）
     * 1、单曲循环：将要播放的视频文件无法解析播放，此情况会造成死循环；2、文件夹循环：当前文件夹下只有一个视频文件
     * ，并且该视频文件无法解析播放，此情况会造成死循环
     * ；3、随机播放模式：该U盘下只有一个视频文件，并且该视频文件无法解析播放，此情况会造成死循环；4、全部循环
     * ：该U盘下只有一个视频文件，并且该视频文件无法解析播放，此情况会造成死循环。
     */
    private void previous() {
        if (FastClickUtil.enableToResponseClick(period_fast_click)) {
            play(mMediaDataModel.getPreviousPlayVideoUriByPlayMode(), -1, true, true);
        }
    }

    /**
     * 发送播放上一个视频消息
     *
     * @param delayMillis 延迟多久切换上一曲
     */
    public void sendMessageToPlayPrevious(int delayMillis) {
        // 移除队列中的播放控制消息
        removePlayControlMessages();
        // 移除队列中的快退快进消息
        removeAdjustProgressMessages();
        // 移除队列中的上下一曲消息
        removePreviousNextMessages();
        // 发送上一曲消息
        mVideoHandler.sendEmptyMessageDelayed(VideoConstants.MSG_VIDEO_PREVIOUS, delayMillis);
    }

    /**
     * 播放下一视频（播放下一个视频时，需要做一些判断）
     * 1、单曲循环：将要播放的视频文件无法解析播放，此情况会造成死循环；2、文件夹循环：当前文件夹下只有一个视频文件
     * ，并且该视频文件无法解析播放，此情况会造成死循环
     * ；3、随机播放模式：该U盘下只有一个视频文件，并且该视频文件无法解析播放，此情况会造成死循环；4、全部循环
     * ：该U盘下只有一个视频文件，并且该视频文件无法解析播放，此情况会造成死循环。
     *
     * @param fromUser 是否用户手动切换，或者其他原因需要切换别的视频
     */
    private void next(boolean fromUser) {
        if (FastClickUtil.enableToResponseClick(period_fast_click)) {
            play(mMediaDataModel.getNextPlayVideoUriByPlayMode(fromUser), -1, true, true);
        }
    }

    /**
     * 发送播放下一个视频消息
     *
     * @param delayMillis 延迟多久切换下一曲
     * @param fromUser    是否用户手动切换，或者其他原因需要切换别的视频
     */
    public void sendMessageToPlayNext(int delayMillis, boolean fromUser) {
        // 移除队列中的播放控制消息
        removePlayControlMessages();
        // 移除队列中的快退快进消息
        removeAdjustProgressMessages();
        // 移除队列中的上下一曲消息
        removePreviousNextMessages();
        // 发送下一曲消息
        mVideoHandler.sendMessageDelayed(mVideoHandler.obtainMessage(VideoConstants
                .MSG_VIDEO_NEXT, fromUser), delayMillis);
    }

    /**
     * 删除上下一个视频消息
     */
    void removePreviousNextMessages() {
        mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_PREVIOUS);
        mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_NEXT);
    }

    /**
     * 弹出行车警告弹框
     */
    @SuppressLint("InflateParams")
    private void showSpeedWarning() {
        if (getActivity() == null) {
            return;
        }

        if (mSpeedWarningDialog == null) {
            mSpeedWarningDialog = new Dialog(getActivity(), R.style.SpeedWarningDialog);
            mSpeedWarningDialog.setContentView(R.layout.dialog_speed_warning);
            // “继续”按钮
            Button btn_continue = (Button) mSpeedWarningDialog.findViewById(R.id.btn_continue);
            btn_continue.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 关闭行车警告弹框
                    mSpeedWarningDialog.dismiss();
                    // 继续播放
                    sendMessageToStart(0, true);
                    // 重启控制视图隐藏计时
                    VideoConsoleViewModel.getInstance().setEnableToHide(true);
                    VideoConsoleViewModel.getInstance().restartCountdown();
                }
            });
            // “退出”按钮
            Button btn_quit = (Button) mSpeedWarningDialog.findViewById(R.id.btn_quit);
            btn_quit.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 关闭行车警告弹框
                    mSpeedWarningDialog.dismiss();
                    // 退出Activity
                    finishActivityAndBackHome();
                }
            });
        }
        mSpeedWarningDialog.show();
        // 设置弹框宽高
        WindowManager.LayoutParams lParams = mSpeedWarningDialog.getWindow().getAttributes();
        lParams.width = 400;
        lParams.height = 300;
        mSpeedWarningDialog.getWindow().setAttributes(lParams);

        // 显示控制视图并且不自动隐藏
        VideoConsoleViewModel.getInstance().setEnableToHide(false);
        VideoConsoleViewModel.getInstance().showVideoConsoleView(false);
    }

    /**
     * 关闭行车警告弹框
     */
    private void dismissSpeedWarning() {
        if (getActivity() == null) {
            return;
        }
        if (mSpeedWarningDialog != null && mSpeedWarningDialog.isShowing()) {
            mSpeedWarningDialog.dismiss();
        }
    }

    /**
     * 开始车速监控
     */
    private void startSpeedMonitor() {
        mVideoHandler.removeMessages(VideoConstants.MSG_VIDEO_GET_SPEED);
        mVideoHandler.sendEmptyMessage(VideoConstants.MSG_VIDEO_GET_SPEED);
    }

    /**
     * 申请音频焦点
     */
    private int requestAudioFocus() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        }
        int audioFocusState = mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);// 获取音频永久焦点
        if (audioFocusState == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {// 申请音频永久焦点成功
            Logger.logD("VideoFragment-----------------------requestAudioFocus success");
        } else {// 申请音频永久焦点失败
            Logger.logE("VideoFragment-----------------------requestAudioFocus fail");
        }
        return audioFocusState;
    }

    /**
     * 释放音频焦点
     */
    private int abandonAudioFocus() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        }
        int audioFocusState = mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        if (audioFocusState == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {// 释放音频永久焦点成功
            Logger.logD("VideoFragment-----------------------abandonAudioFocus success");
        } else {// 释放音频永久焦点失败
            Logger.logE("VideoFragment-----------------------abandonAudioFocus fail");
        }
        return audioFocusState;
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.logD("VideoFragment-----------------------onPause");
    }

    @Override
    public void onStop() {
        Logger.logD("VideoFragment-----------------------onStop");
        // 取消隐藏控制视图的任务
        VideoConsoleViewModel.getInstance().cancelCountdown();
        // 移除所有消息
        mVideoHandler.removeCallbacksAndMessages(null);
        // 去全屏
        cancelFullSceen();
        // 注销相关
        unregister();
        // 停止播放
        sendMessageToStop();
        // 释放音频焦点
        abandonAudioFocus();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.logD("VideoFragment-----------------------onDestroy");
    }

    @Override
    public void unregister() {
        // 注销扫描媒体监听器
        MediaScanner.getInstance(getContext()).unregisterScanMediaFileListener(this);
        // 注销音频数据变化监听器
        mMediaDataModel.setOnVideoInfosChangeListener(null);
        // 注销U盘状态变化监听器
        UsbStateManager.getInstance().unregisterUsbStateChangeListener(this);
        // 注销视频播放模式切换监听器
        VideoPlayModeChangeModel.getInstance(getContext()).setOnVideoPlayModeChangeListener(null);
        // 注销视频解析监听器
        VideoInfoParser.getInstance().setOnParseVideoInfoListener(null);
        // 注销视频路径分解回调监听器
        VideoUriSubModel.getInstance().unregisterVideoUriSubResultCallback(this);
        // 注销视频控制视图显示隐藏的回调监听器
        VideoConsoleViewModel.getInstance().setCallback(null);
        // 注销实体按钮上下一曲长按事件监听器
        VideoKeyLongClickModel.getInstance().setOnVideoKeyLongClickListener(null);
        // 注销中控按钮监听器
        unregisterKeyPressListener();
        // 注销工厂测试广播接收器
        unregisterFactoryTestReceiver();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // 移除队列中的快退快进消息
        removeAdjustProgressMessages();
        // 取消隐藏控制视图
        VideoConsoleViewModel.getInstance().cancelCountdown();
        // 如果正在播放，则先暂停
        if (PreferencesUtil.getInstance().isVideoFinallyPlaying()) {// 之前是播放的
            // 暂停播放（不保存状态）
            sendMessageToPause(0, false);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // 开始隐藏控制视图计时
        VideoConsoleViewModel.getInstance().restartCountdown();
        // seekTo
        seekToProgress(seekBar.getProgress(), true);
        // 如果拖动前是播放状态，则松开手后恢复播放
        if (PreferencesUtil.getInstance().isVideoFinallyPlaying()) {// 之前是播放的
            // 恢复播放
            sendMessageToStart(0, false);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {// 用户手动拖动
            // 更新播放进度时间显示
            tv_playtime.setText(TimeTransformer.getVideoFormatTime(progress));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (v.getId()) {
                    case R.id.videoView:
                        // 记录按下VideoView的时间
                        videoViewTouchDownTime = System.currentTimeMillis();
                        break;
                    case R.id.tv_playtime:
                    case R.id.tv_duration:
                    case R.id.btn_switcher:
                    case R.id.btn_previous:
                    case R.id.btn_next:
                    case R.id.btn_playmode:
                    case R.id.btn_sound:
                    case R.id.btn_list:
                    case R.id.ib_radio:
                    case R.id.ib_music:
                    case R.id.ib_bt_music:
                    case R.id.rb_usb1:
                    case R.id.rb_usb2:
                    case R.id.root_directory1:
                    case R.id.root_directory2:
                    case R.id.lv_usb1:
                    case R.id.lv_usb2:
                        // 取消隐藏控制视图
                        VideoConsoleViewModel.getInstance().cancelCountdown();
                        break;
                    default:
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                switch (v.getId()) {
                    case R.id.videoView:
                        if (System.currentTimeMillis() - videoViewTouchDownTime <
                                touchResponseTime) {// 如果按下的时间非常短，相当于点击
                            if (VideoConsoleViewModel.getInstance().isEnableToHide()) {
                                if (linear_left.getVisibility() == View.VISIBLE) {// 正在显示
                                    // 隐藏控制视图
                                    VideoConsoleViewModel.getInstance().hideVideoConsoleView();
                                } else {// 已经隐藏了
                                    // 显示控制视图
                                    VideoConsoleViewModel.getInstance().showVideoConsoleView(true);
                                }
                            }
                        }
                        break;
                    case R.id.btn_previous:
                        stopAdjustProgressBackward();
                        // 开始隐藏控制视图计时
                        VideoConsoleViewModel.getInstance().restartCountdown();
                        break;
                    case R.id.btn_next:
                        stopAdjustProgressForward();
                        // 开始隐藏控制视图计时
                        VideoConsoleViewModel.getInstance().restartCountdown();
                        break;
                    case R.id.tv_playtime:
                    case R.id.tv_duration:
                    case R.id.btn_switcher:
                    case R.id.btn_playmode:
                    case R.id.btn_sound:
                    case R.id.btn_list:
                    case R.id.ib_radio:
                    case R.id.ib_music:
                    case R.id.ib_bt_music:
                    case R.id.rb_usb1:
                    case R.id.rb_usb2:
                    case R.id.root_directory1:
                    case R.id.root_directory2:
                    case R.id.lv_usb1:
                    case R.id.lv_usb2:
                        // 开始隐藏控制视图计时
                        VideoConsoleViewModel.getInstance().restartCountdown();
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
            case R.id.btn_switcher:// 开关
                if (isPlaying()) {
                    sendMessageToPause(0, true);
                } else {
                    sendMessageToStart(0, true);
                }
                break;
            case R.id.btn_previous:// 上一个
                sendMessageToPlayPrevious(0);
                break;
            case R.id.btn_next:// 下一个
                sendMessageToPlayNext(0, true);
                break;
            case R.id.btn_playmode:// 切换视频播放模式
                VideoPlayModeChangeModel.getInstance(getContext()).changePlayMode();
                break;
            case R.id.btn_sound:// 跳转音效设置界面
                gotoSound();
                break;
            case R.id.btn_list:// 显示或隐藏媒体列表
                // 显示文件列表还是显示右上角按钮栏
                if (linear_top_right.getVisibility() == View.VISIBLE) {
                    linear_filemanager.setVisibility(View.VISIBLE);
                    linear_top_right.setVisibility(View.GONE);
                } else {
                    linear_filemanager.setVisibility(View.INVISIBLE);// 这里用INVISIBLE，用GONE跑马灯第一次无效
                    linear_top_right.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.ib_radio:// 切换到收音机
                FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_RADIO,
                        null);
                break;
            case R.id.ib_music:// 切换到多媒体音乐
                FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_MUSIC,
                        null);
                break;
            case R.id.ib_bt_music:// 切换到蓝牙音乐
                Bundle bundle = new Bundle();
                bundle.putInt(CommonConstants.KEY_GOTO_BT, CommonConstants.GOTO_BT_MUSIC);
                FragmentSwitchController.getInstance().switchFragment(CommonConstants.FLAG_BT,
                        bundle);
                break;
            case R.id.root_directory1:
                VideoUriSubModel.getInstance().backToParentDir(MultimediaConstants.FLAG_USB1,
                        root_directory1.getText(MultimediaConstants.FLAG_USB1));
                break;
            case R.id.root_directory2:
                VideoUriSubModel.getInstance().backToParentDir(MultimediaConstants.FLAG_USB2,
                        root_directory2.getText(MultimediaConstants.FLAG_USB2));
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.btn_previous:
                startAdjustProgressBackward();
                return true;// 独占，防止触发onClick事件
            case R.id.btn_next:
                startAdjustProgressForward();
                return true;// 独占，防止触发onClick事件
            default:
                break;
        }
        return false;
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
                // 分解videoUri，根据结果来决定是打开目录还是播放视频
                VideoUriSubModel.getInstance().subVideoUri(MultimediaConstants.FLAG_USB1,
                        PreferencesUtil.getInstance().getVideoUsb1RootDirectory(), (String)
                                videoFileAdapter1.getItem(position));
                break;
            case R.id.lv_usb2:
                // 分解videoUri，根据结果来决定是打开目录还是播放视频
                VideoUriSubModel.getInstance().subVideoUri(MultimediaConstants.FLAG_USB2,
                        PreferencesUtil.getInstance().getVideoUsb2RootDirectory(), (String)
                                videoFileAdapter2.getItem(position));
            default:
                break;
        }
    }

    /**
     * 注册中控实体按钮监听器
     */
    private void registerKeyPressListener() {
        Logger.logD("VideoFragment-----------------------registerKeyPressListener");
        android.os.ProtocolManager.getInstance().registerKeyPressListener(mIKeyPressInterface);
    }

    /**
     * 注销中控实体按钮监听器
     */
    private void unregisterKeyPressListener() {
        Logger.logD("VideoFragment-----------------------unregisterKeyPressListener");
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
    public void onScannedFirstPhoto(int usbFlag, String photoUri) {
    }

    @Override
    public void onScannedFirstMusic(int usbFlag, String musicUri) {
    }

    @Override
    public void onScannedFirstVideo(int usbFlag, String videoUri) {//
        // 扫描到第一个视频文件回调（用于初次插入U盘默认播放第一个视频）
        // 获取当前正在播放的视频的Uri
        String currentPlayingVideoUri = PreferencesUtil.getInstance().getCurrentPlayingVideoUri();
        if (TextUtils.isEmpty(currentPlayingVideoUri) || !new File(currentPlayingVideoUri).exists
                ()) {// 说明从未播放过视频或者记忆的视频不存在了，此时播放扫描到的第一个视频（可能MediaScanner
            // 扫描到第一个视频文件的时候，VideoFragment未在前台，这样就收不到onScannedFirstVideo()
            // 回调，所以需要在VideoFragment打开的时候做一次判断）
            // 默认播放扫描到的第一个视频
            sendMessageToPlay(videoUri, -1, 0, true, true);
        }
    }

    @Override
    public void onScanFinish(int usbFlag) {// 扫描结束
        // 扫描结束，更新下当前视频列表
        updateCurrentDirectory(usbFlag);
    }

    @Override
    public void onVideoInfosClear(int usbFlag) {
        updateCurrentDirectory(usbFlag);
    }

    @Override
    public void onVideoPlayModeChanged(int mode) {// 播放模式变化
        updatePlayMode(mode);
    }

    @Override
    public void onUsbMounted(int usbFlag) {// U盘插入（能响应此回调方法，说明正在音乐界面）
        // 刷新USB状态显示
        updateUsbStatus();

        // 恢复播放状态
        String currentPlayingVideoUri = PreferencesUtil.getInstance().getCurrentPlayingVideoUri();
        if (!TextUtils.isEmpty(currentPlayingVideoUri) && new File(currentPlayingVideoUri).exists
                ()) {// 播放过并且文件存在（如果不存在，播放扫描到的第一个视频，此时刚挂载，还未扫描到第一个视频，需在扫描到第一个视频回调中再次判断）
            if ((currentPlayingVideoUri.startsWith(MultimediaConstants.PATH_USB1) && usbFlag ==
                    MultimediaConstants.FLAG_USB1) || (currentPlayingVideoUri.startsWith
                    (MultimediaConstants.PATH_USB2) && usbFlag == MultimediaConstants.FLAG_USB2))
            {// 如果插入的U盘是最后播放的那个U盘，恢复播放
                sendMessageToPlay(currentPlayingVideoUri, PreferencesUtil.getInstance()
                        .getCurrentPlayingVideoProgress(), 0, PreferencesUtil.getInstance()
                        .isVideoFinallyPlaying(), false);
            }
        }
    }

    @Override
    public void onUsbUnMounted(int usbFlag) {// U盘拔出（能响应此回调方法，说明正在音乐界面）
        updateUsbStatus();
    }

    @Override
    public void onParseVideoInfoStart(String videoUri) {// 解析开始
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }
        // 隐藏错误提示
        dismissNotice();
        // 设置进度条最大值
        setMax(0);
        // 重置进度条进度
        updateProgress(0);
    }

    @Override
    public void onParseVideoFileNotExist(String videoUri) {// 解析文件不存在
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }
        // 显示错误提示
        showNotice(videoUri, R.string.video_not_exists);
    }

    @Override
    public void onParseVideoInfoError(String videoUri) {// 解析出错
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }
        // 显示错误提示
        showNotice(videoUri, R.string.unsupported_file);
    }

    @Override
    public void onParseVideoInfoComplete(VideoInfo info) {// 解析完成
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }
        // 设置进度条最大值
        setMax(info.getDuration());
        // 解析成功时，允许隐藏控制视图
        VideoConsoleViewModel.getInstance().setEnableToHide(true);
        // 解析成功时，如果控制视图是显示状态，则稍后隐藏
        VideoConsoleViewModel.getInstance().restartCountdown();
    }

    @Override
    public void onVideoUriSubResultFolder(int usbFlag, String rootDirectory, String folderUri) {
        // 打开目录
        openDirectory(usbFlag, folderUri);
    }

    @Override
    public void onVideoUriSubResultFile(int usbFlag, String rootDirectory, String videoUri) {
        sendMessageToPlay(videoUri, -1, 0, true, true);
    }

    @Override
    public void onBackToParentDirectory(int usbFlag, String parentDir) {
        // 打开目录
        openDirectory(usbFlag, parentDir);
    }

    @Override
    public void onShowVideoConsoleView() {// 设置控制视图显示
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        linear_left.setVisibility(View.VISIBLE);
        linear_seekbar.setVisibility(View.VISIBLE);
        linear_top_right.setVisibility(View.VISIBLE);
        linear_filemanager.setVisibility(View.INVISIBLE);// 这里用INVISIBLE，用GONE跑马灯第一次无效
        // 显示状态栏
        cancelFullSceen();
    }

    @Override
    public void onHideVideoConsoleView() {// 设置控制视图隐藏
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        linear_left.setVisibility(View.GONE);
        linear_seekbar.setVisibility(View.GONE);
        linear_top_right.setVisibility(View.GONE);
        linear_filemanager.setVisibility(View.INVISIBLE);
        // 隐藏状态栏
        setFullScreen();
    }

    @Override
    public void onSlidingLeft(float distanceX) {// 手指左滑
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        // if (mVideoView != null) {
        // // 滑动快退时显示控制视图
        // VideoConsoleViewController.getInstance().showVideoConsoleView(true);
        // // 计算快退进度
        // int current = mVideoView.getCurrentPosition();
        // int backwardTime = (int) (distanceX / mVideoView.getWidth() *
        // mVideoView
        // .getDuration()) / 3;
        // int currentTime = current - backwardTime;
        // if (currentTime < 0) {
        // currentTime = 0;
        // }
        // // 快退
        // seekToProgress(currentTime, false);
        // }
    }

    @Override
    public void onSlidingRight(float distanceX) {// 手指右滑
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        // if (mVideoView != null) {
        // // 滑动快进时显示控制视图
        // VideoConsoleViewController.getInstance().showVideoConsoleView(true);
        // // 计算快进进度
        // int current = mVideoView.getCurrentPosition();
        // int duration = mVideoView.getDuration();
        // int forwardTime = (int) (distanceX / mVideoView.getWidth() *
        // duration) / 3;
        // int currentTime = current + forwardTime;
        // if (currentTime > duration) {
        // currentTime = duration;
        // }
        // // 快进
        // seekToProgress(currentTime, false);
        // }
    }

    @Override
    public void onVideoViewDoubleTap() {// 手指双击VideoView
        if (getActivity() == null) {// 如果该Fragment已经与FragmentActivity解除关联，此时不得操作UI
            return;
        }

        // 模拟开关点击（播放或者暂停）
        // btn_switcher.performClick();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logger.logD("VideoFragment-----------------------surfaceCreated()");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Logger.logD("VideoFragment-----------------------surfaceChanged():width=" + w + " " +
                "height=" + h);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.logD("VideoFragment-----------------------surfaceDestroyed()");
        // 停止记录播放进度，防止在关闭瞬间时间置0
        stopSendMessageToUpdatePlayProgress();
    }

    @Override
    public void onStartKeyLongClick(int keyCode) {// 开始长按按键
        // 显示控制视图并且不隐藏
        VideoConsoleViewModel.getInstance().setEnableToHide(false);
        VideoConsoleViewModel.getInstance().showVideoConsoleView(false);

        switch (keyCode) {
            case CommonConstants.KEY_PREV:
                startAdjustProgressBackward();
                break;
            case CommonConstants.KEY_NEXT:
                startAdjustProgressForward();
                break;
            default:
                break;
        }
    }

    @Override
    public void onStopKeyLongClick(int keyCode) {// 结束长按按键
        switch (keyCode) {
            case CommonConstants.KEY_PREV:
                stopAdjustProgressBackward();
                break;
            case CommonConstants.KEY_NEXT:
                stopAdjustProgressForward();
                break;
            default:
                break;
        }

        // 重新开始计时隐藏控制视图
        VideoConsoleViewModel.getInstance().setEnableToHide(true);
        VideoConsoleViewModel.getInstance().restartCountdown();
    }

    @Override
    public void onReceiveKeyLongClickEvent(int keyCode) {
    }

    /**
     * 注册工厂测试广播接收器
     */
    private void registerFactoryTestReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(VideoFactoryTestConstants.SEMISKY_VIDEO_NEXT);
        mFilter.addAction(VideoFactoryTestConstants.SEMISKY_VIDEO_PREVIOUS);
        mFilter.addAction(VideoFactoryTestConstants.SEMISKY_VIDEO_PAUSE);
        mFilter.addAction(VideoFactoryTestConstants.SEMISKY_VIDEO_START);
        mFilter.addAction(VideoFactoryTestConstants.SEMISKY_VIDEO_SEEKTO);
        mFilter.addAction(VideoFactoryTestConstants.SEMISKY_VIDEO_PLAYFILE);
        getContext().registerReceiver(factoryTestReceiver, mFilter);
    }

    /**
     * 注销工厂测试广播接收器
     */
    private void unregisterFactoryTestReceiver() {
        getContext().unregisterReceiver(factoryTestReceiver);
    }

    private static class VideoHandler extends Handler {
        private static WeakReference<VideoFragment> mReference;

        public VideoHandler(VideoFragment fragment) {
            mReference = new WeakReference<VideoFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mReference.get() == null) {
                return;
            }

            switch (msg.what) {
                case VideoConstants.MSG_VIDEO_PLAY:// 播放消息
                    Bundle bundle = msg.getData();
                    mReference.get().play(bundle.getString("videoUri"), bundle.getInt("progress")
                            , bundle.getBoolean("autoStart"), bundle.getBoolean("saveState"));
                    break;
                case VideoConstants.MSG_VIDEO_START:// 开始播放
                    mReference.get().startVideoView((Boolean) msg.obj);
                    break;
                case VideoConstants.MSG_VIDEO_PAUSE:// 暂停播放
                    mReference.get().pause((Boolean) msg.obj);
                    break;
                case VideoConstants.MSG_VIDEO_STOP:// 停止播放
                    mReference.get().stop();
                    break;
                case VideoConstants.MSG_VIDEO_PREVIOUS:// 播放上一个视频
                    mReference.get().previous();
                    break;
                case VideoConstants.MSG_VIDEO_NEXT:// 播放下一个视频
                    mReference.get().next((Boolean) msg.obj);
                    break;
                case VideoConstants.MSG_VIDEO_UPDATE_PROGRESS:// 更新播放进度
                    int progress = mReference.get().getProgress();
                /*
                 * 这里刷新UI和保存SharedPreferences将耗时约60ms
				 */
                    // 刷新播放进度
                    mReference.get().updateProgress(progress);
                    // 保存播放进度
                    mReference.get().saveProgress(progress);
                    // 接着发送消息
                    mReference.get().mVideoHandler.sendEmptyMessageDelayed(VideoConstants
                            .MSG_VIDEO_UPDATE_PROGRESS, 200);// 时间设短些，防止跳秒
                    break;
                case VideoConstants.MSG_VIDEO_GET_SPEED:// 获取车速
                    // 获取当前车速
                    int speed = android.os.ProtocolManager.getInstance().getVehicleSpeed();
                    if (VideoUtil.isSpeedWarningOpened(mReference.get().getContext())) {//
                        // 行车中禁止播放视频开关打开了
                        if (speed >= mReference.get().warning_speed) {// 超过警告车速
                            // 暂停视频播放
                            mReference.get().sendMessageToPause(0, true);
                            // 警告弹框提示
                            mReference.get().showSpeedWarning();
                        } else if (speed <= mReference.get().safe_speed) {// 回到安全车速
                            mReference.get().dismissSpeedWarning();
                        }
                    }
                    // 每秒判断一下车速
                    mReference.get().mVideoHandler.sendEmptyMessageDelayed(VideoConstants
                            .MSG_VIDEO_GET_SPEED, 1000);
                    break;
                case VideoConstants.MSG_VIDEO_SUB:// 快退
                    if (mReference.get().sendMessageToAdjustBackward) {
                        mReference.get().slightAdjustProgressBackward();
                        mReference.get().mVideoHandler.sendEmptyMessageDelayed(VideoConstants
                                .MSG_VIDEO_SUB, 125);
                    }
                    break;
                case VideoConstants.MSG_VIDEO_PLUS:// 快进
                    if (mReference.get().sendMessageToAdjustForward) {
                        mReference.get().slightAdjustProgressForward();
                        mReference.get().mVideoHandler.sendEmptyMessageDelayed(VideoConstants
                                .MSG_VIDEO_PLUS, 125);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
