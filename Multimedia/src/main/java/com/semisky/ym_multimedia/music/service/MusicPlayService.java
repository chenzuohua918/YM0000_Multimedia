package com.semisky.ym_multimedia.music.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.semisky.ym_multimedia.common.utils.AppUtil;
import com.semisky.ym_multimedia.common.utils.CommonConstants;
import com.semisky.ym_multimedia.common.utils.FastClickUtil;
import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.multimedia.dao.MediaScanner;
import com.semisky.ym_multimedia.multimedia.model.MediaDataModel;
import com.semisky.ym_multimedia.multimedia.model.MediaDataModelDBImp;
import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;
import com.semisky.ym_multimedia.multimedia.utils.PreferencesUtil;
import com.semisky.ym_multimedia.multimedia.utils.TimeTransformer;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager.OnUsbStateChangeListener;
import com.semisky.ym_multimedia.music.bean.MusicInfo;
import com.semisky.ym_multimedia.music.model.MusicKeyLongClickModel;
import com.semisky.ym_multimedia.music.model.MusicKeyLongClickModel.OnMusicKeyLongClickListener;
import com.semisky.ym_multimedia.music.utils.MusicConstants;
import com.semisky.ym_multimedia.music.utils.MusicInfoParser;
import com.semisky.ym_multimedia.music.utils.MusicInfoParser.OnParseMusicInfoListener;
import com.semisky.ym_multimedia.music.utils.MusicPlayStateSaver;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * 音乐播放后台
 *
 * @author Anter
 */
public class MusicPlayService extends Service implements OnUsbStateChangeListener,
        OnMusicKeyLongClickListener {
    private final MusicPlayHandler mHandler = new MusicPlayHandler(this);
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private MusicPlayCallback musicPlayCallback;// 回调MusicFragment接口
    private MediaDataModel mMediaDataModel;
    private boolean isEnableToGetCurrentPosition = false;// 当前是否能获取合法的播放进度
    private int period_fast_click = 300;// 快速点击响应周期
    private float mCurrentVolumeRatio = 1.0f;// 音量大小比例（MediaPlayer的音量控制范围0.0——1.0）
    private float lowest_ratio = 0.0f;// 音量最低比例
    private float highest_ratio = 1.0f;// 音量最高比例
    private float volume_step_sub = 0.1f;// 每次音量减小比例
    private float volume_step_plus = 0.1f;// 每次音量增大比例
    private int fade_down_delayMillis = 150;// 每次音量减小时间间隔
    private int fade_up_delayMillis = 150;// 每次音量增大时间间隔
    private boolean sendMessageToAdjustBackward = false;// 是否发消息快退
    private boolean sendMessageToAdjustForward = false;// 是否发消息快进
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
                            Logger.logD("MusicPlayService-----------------------KEY_PREV短按");
                            // 切换上一首
                            sendMessageToPlayPrevious(0);
                            break;
                        case CommonConstants.KEY_MODE_LONG_CLICK:// 长按
                            Logger.logD("MusicPlayService-----------------------KEY_PREV长按");
                            MusicKeyLongClickModel.getInstance().receiveKeyLongClickPrevEvent();
                            break;
                        default:
                            break;
                    }
                    break;
                case CommonConstants.KEY_NEXT:// 按键下一曲
                    switch (mode) {
                        case CommonConstants.KEY_MODE_CLICK:// 短按
                            Logger.logD("MusicPlayService-----------------------KEY_NEXT短按");
                            // 切换下一首
                            sendMessageToPlayNext(0, true);
                            break;
                        case CommonConstants.KEY_MODE_LONG_CLICK:// 长按
                            Logger.logD("MusicPlayService-----------------------KEY_NEXT长按");
                            MusicKeyLongClickModel.getInstance().receiveKeyLongClickNextEvent();
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
            return "music";
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
                            ("MusicPlayService-----------------------AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    // 设置混音最低比例
                    lowest_ratio = AppUtil.calNavMixLowestRatio(MusicPlayService.this);
                    // 音量渐变降低
                    fadeDownVolume();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:// 暂时失去了AudioFocus,
                    // 但很快会重新得到焦点（如来电时），必须停止Audio的播放，但是因为可能会很快再次获得AudioFocus，这里可以不释放Media资源。
                    Logger.logD("MusicPlayService-----------------------AUDIOFOCUS_LOSS_TRANSIENT");
                    // 注销中控按钮监听器（来电弹框状态下不能操作音乐）
                    unregisterKeyPressListener();

                    // 设置混音最低比例
                    lowest_ratio = 0f;
                    // 暂停播放（因为不是用户自主暂停播放的，所以不保存状态）
                    sendMessageToPause(0, false);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:// 失去AudioFocus，并将会持续很长的时间（如播放音乐或视频时）
                    Logger.logD("MusicPlayService-----------------------AUDIOFOCUS_LOSS");
                    // 注销中控按钮监听器（来电弹框状态下不能操作音乐）
                    unregisterKeyPressListener();

                    // 设置混音最低比例
                    lowest_ratio = 0f;
                    // 暂停播放（因为不是用户自主暂停播放的，所以不保存状态）
                    sendMessageToPause(0, false);
                    // 释放音频焦点
                    abandonAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:// 获得AudioFocus
                    Logger.logD("MusicPlayService-----------------------AUDIOFOCUS_GAIN");
                    // 注册中控按钮监听器
                    registerKeyPressListener();

                    if (PreferencesUtil.getInstance().isMusicFinallyPlaying()) {
                        // 失去焦点前是在播放的，获取焦点后继续播放
                        if (lowest_ratio > 0f) {// 说明是AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK之后重新获取音频焦点
                            // 把音量拉高
                            fadeUpVolume();
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
     * 播放准备完成监听器
     */
    private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            Logger.logD("MusicPlayService-----------------------onPrepared()");
            // 歌曲准备完毕，此时可以获取播放进度
            isEnableToGetCurrentPosition = true;
            // 歌曲准备完毕
            MusicPlayStateSaver.getInstance().setPrepared(true);
        }
    };
    /**
     * 播放结束监听器
     */
    private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            Logger.logD("MusicPlayService-----------------------歌曲播放结束，1秒钟后播放下一首");
            // 歌曲播放结束，此时不能再获取播放进度
            isEnableToGetCurrentPosition = false;
            // 歌曲播放结束，设为未准备好状态
            MusicPlayStateSaver.getInstance().setPrepared(false);
            // 如果有在发送更新播放进度消息，先停止发送
            stopSendMessageToUpdatePlayProgress();
            // 1秒后播放下一首歌曲
            sendMessageToPlayNext(1000, false);
        }
    };
    /**
     * seekTo操作完成监听器（seekTo是异步操作）
     */
    private OnSeekCompleteListener mOnSeekCompleteListener = new OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            Logger.logD("MusicPlayService-----------------------seekTo:" + TimeTransformer
                    .getMusicFormatTime(mMediaPlayer.getCurrentPosition()));
        }
    };
    /**
     * 媒体播放器出错监听器
     */
    private OnErrorListener mOnErrorListener = new OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            // 媒体播放器出错时，不能获取播放进度
            isEnableToGetCurrentPosition = false;

            switch (what) {// 出现的错误类型
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:// 未知错误
                    Logger.logE("MusicPlayService-----------------------未知错误");
                    break;
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:// 服务器错误
                    Logger.logE("MusicPlayService-----------------------服务器错误");
                    break;
                default:
                    break;
            }

            switch (extra) {// 针对与具体错误的附加码, 用于定位错误更详细信息
                case MediaPlayer.MEDIA_ERROR_IO:// 本地文件或网络相关错误
                    Logger.logE("MusicPlayService-----------------------本地文件或网络相关错误");
                    break;
                case MediaPlayer.MEDIA_ERROR_MALFORMED:// 比特流不符合相关的编码标准和文件规范
                    Logger.logE("MusicPlayService-----------------------比特流不符合相关的编码标准和文件规范");
                    break;
                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:// 框架不支持该功能
                    Logger.logE("MusicPlayService-----------------------框架不支持该功能");
                    break;
                case MediaPlayer.MEDIA_ERROR_TIMED_OUT:// 一些操作超时
                    Logger.logE("MusicPlayService-----------------------一些操作超时");
                    break;
                default:
                    break;
            }

            return true;
        }
    };
    /**
     * 歌曲文件解析监听器
     */
    private OnParseMusicInfoListener mParseMusicInfoListener = new OnParseMusicInfoListener() {

        @Override
        public void onParseMusicFileNotExists(String musicUri) {// 文件不存在

        }

        @Override
        public void onParseMusicInfoStart(String musicUri) {// 开始解析

        }

        @Override
        public void onParseMusicInfoError(String musicUri) {// 解析出错
            Logger.logD("MusicPlayService-----------------------解析歌曲文件出错，3秒后播放下一首");
            // 为了在单曲循环时不造成死循环，给true
            sendMessageToPlayNext(3000, true);
        }

        @Override
        public void onParseMusicInfoComplete(MusicInfo info) {// 解析成功
            Logger.logD("MusicPlayService-----------------------解析歌曲文件成功");
        }
    };

    public void setMusicPlayCallback(MusicPlayCallback musicPlayCallback) {
        this.musicPlayCallback = musicPlayCallback;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.logD("MusicPlayService-----------------------onCreate");
        initMediaPlayer();
        initModel();
        register();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.logD("MusicPlayService-----------------------onStartCommand");
        // 申请音频焦点（只在绑定成功时申请是不够的，因为绑定需要一些时间，这段时间可能启动了别的音频页）
        requestAudioFocus();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.logD("MusicPlayService-----------------------onBind");
        return new MusicPlayBinder();
    }

    /**
     * 绑定成功
     */
    public void onServiceConnected() {
    }

    /**
     * 初始化MediaPlayer
     */
    private void initMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
    }

    private void initModel() {
        mMediaDataModel = MediaDataModelDBImp.getInstance(getApplication());
        // Service执行onCreate说明歌曲是未准备好状态
        MusicPlayStateSaver.getInstance().setPrepared(false);
    }

    /**
     * 注册相关
     */
    private void register() {
        // 注册U盘状态变化监听器
        UsbStateManager.getInstance().registerUsbStateChangeListener(this);
        // 注册歌曲文件解析监听器
        MusicInfoParser.getInstance().registerParseMusicInfoListener(mParseMusicInfoListener);
        // 注册实体按钮上下一曲长按事件监听器
        MusicKeyLongClickModel.getInstance().setOnMusicKeyLongClickListener(this);
    }

    /**
     * 获取当前播放的歌曲信息
     */
    public MusicInfo getPlayingMusicInfo() {
        return MusicInfoParser.getInstance().getPlayingMusicInfo();
    }

    /**
     * 当前歌曲是否可播放
     */
    private boolean isCurrentMusicEnableToPlay() {
        return getPlayingMusicInfo() != null;
    }

    /**
     * 是否在播放歌曲文件
     */
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    /**
     * 获取播放进度
     *
     * @return -1时表示获取值非法
     */
    public synchronized int getProgress() {
        if (mMediaPlayer != null && isEnableToGetCurrentPosition) {
            return mMediaPlayer.getCurrentPosition();
        }
        return -1;
    }

    /**
     * 保存播放进度
     */
    private void saveProgress(int progress) {
        if (progress >= 0) {// 合法播放进度
            PreferencesUtil.getInstance().setCurrentPlayingMusicProgress(progress);
        }
    }

    /**
     * 准备歌曲文件
     */
    private boolean prepare(String musicUri) {
        if (TextUtils.isEmpty(musicUri)) {
            Logger.logE("MusicPlayService-----------------------prepare() " + musicUri + " is empty");
            return false;
        }
        try {
            if (mMediaPlayer != null) {
                // 先停止刷新播放进度
                stopSendMessageToUpdatePlayProgress();
                // 读取之前播放的歌曲Uri
                String currentPlayingMusicUri = PreferencesUtil.getInstance()
                        .getCurrentPlayingMusicUri();
                if (!TextUtils.isEmpty(currentPlayingMusicUri) && !currentPlayingMusicUri.equals
                        (musicUri)) {// 说明切换了不同的歌曲
                    // 重置记忆的歌词Uri
                    PreferencesUtil.getInstance().setCurrentPlayingMusicLyricUri(null);
                }
                // 保存将要播放的歌曲路径（包括无法播放的歌曲路径）
                PreferencesUtil.getInstance().setCurrentPlayingMusicUri(musicUri);
                // 重置MediaPlayer
                mMediaPlayer.reset();
                // MediaPlayer重置后不可获取播放进度
                isEnableToGetCurrentPosition = false;
                // MediaPlayer重置后，歌曲设为未准备状态
                MusicPlayStateSaver.getInstance().setPrepared(false);
                // 重置回调
                if (musicPlayCallback != null) {
                    musicPlayCallback.onResetFirst();
                }
                // prepare()开始回调
                if (musicPlayCallback != null) {
                    musicPlayCallback.onPrepareStart(musicUri);
                }
                if (MusicInfoParser.getInstance().parseMusicFile(musicUri) != null) {// 解析成功
                    // 设置音频流的类型
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    // setDataSource时如果Uri非法，则会抛异常
                    mMediaPlayer.setDataSource(musicUri);
                    mMediaPlayer.prepare();
                    return true;
                } else {// 音乐文件不存在或者解析失败
                    if (!new File(musicUri).exists()) {// 音乐文件不存在
                        Logger.logE("MusicPlayService-----------------------prepare() " +
                                musicUri.substring(musicUri.lastIndexOf(File.separator) + 1) +
                                " not exists!");
                        if (musicPlayCallback != null) {
                            musicPlayCallback.onMusicNotExist(musicUri);
                        }
                    } else {// 解析音乐文件失败
                        Logger.logE("MusicPlayService-----------------------prepare() " +
                                musicUri.substring(musicUri.lastIndexOf(File.separator) + 1) +
                                " fail!");
                        if (musicPlayCallback != null) {
                            musicPlayCallback.onPrepareError(musicUri);
                        }
                        // 解析失败，3秒后播放下一首，为了在单曲循环时不造成死循环，给true
                        sendMessageToPlayNext(3000, true);
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            Logger.logE("MusicPlayService-----------------------prepare() exception");
            if (musicPlayCallback != null) {
                musicPlayCallback.onPrepareError(musicUri);
            }
            // 解析失败，3秒后播放下一首，为了在单曲循环时不造成死循环，给true
            sendMessageToPlayNext(3000, true);
            return false;
        }
        return true;
    }

    /**
     * 播放
     */
    private boolean startByService(boolean saveState) {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            if (!isCurrentMusicEnableToPlay()) {// 如果当前歌曲无法播放，则使其无法启动即可，防止MediaPlayer.start()错误
                return false;
            }
            // 音量从0开始渐变上升（先将声音设为最低，再start，防止pop音）
            fadeUpVolumeFromZero();
            // 播放
            mMediaPlayer.start();
            if (saveState) {// 如果需要记忆播放状态
                // 保存播放状态，状态为播放
                PreferencesUtil.getInstance().setMusicFinallyPlaying(true);
            }
            // 通知前台开始播放音乐
            if (musicPlayCallback != null) {
                musicPlayCallback.onMusicPlay(PreferencesUtil.getInstance()
                        .getCurrentPlayingMusicUri());
            }
            // 开始刷新播放进度
            sendMessageToUpdatePlayProgress(0);
            Logger.logD("MusicPlayService-----------------------startByService()");
            return true;
        }
        return false;
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
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MusicConstants.MSG_SERVICE_START,
                saveState), delayMillis);
    }

    /**
     * 播放指定音乐文件的指定progress
     *
     * @param musicUri  音乐文件路径
     * @param progress  播放进度（-1代表从头开始）
     * @param autoStart 是否准备好之后自动开始播放
     * @param saveState 是否需要记忆播放状态
     * @return
     */
    private boolean play(String musicUri, int progress, boolean autoStart, boolean saveState) {
        if (mMediaPlayer != null) {
            if (!prepare(musicUri)) {
                return false;
            }
            if (progress >= 0) {
                if (seekToByService(progress)) {// 定点播放
                    Logger.logD("MusicPlayService-----------------------播放：" + musicUri.substring
                            (musicUri.lastIndexOf(File.separator) + 1) + " progress = " +
                            TimeTransformer.getMusicFormatTime(progress));
                    if (autoStart) {
                        return startByService(saveState);
                    }
                }
            } else {// 从头播放
                Logger.logD("MusicPlayService-----------------------开始播放：" + musicUri.substring
                        (musicUri.lastIndexOf(File.separator) + 1));
                if (autoStart) {
                    return startByService(saveState);
                }
            }
        }
        return false;
    }

    /**
     * 发送播放消息
     */
    public void sendMessageToPlay(String musicUri, int progress, int delayMillis, boolean
            autoStart, boolean saveState) {
        // 移除队列中的播放控制的消息
        removePlayControlMessages();
        // 发送播放消息
        Message msg = mHandler.obtainMessage(MusicConstants.MSG_SERVICE_PLAY);
        Bundle bundle = new Bundle();
        bundle.putString("musicUri", musicUri);
        bundle.putInt("progress", progress);
        bundle.putBoolean("autoStart", autoStart);
        bundle.putBoolean("saveState", saveState);
        msg.setData(bundle);
        mHandler.sendMessageDelayed(msg, delayMillis);
    }

    /**
     * 播放指定位置
     */
    public boolean seekToByService(int progress) {
        if (mMediaPlayer != null) {
            try {
                if (progress >= 0 && progress <= mMediaPlayer.getDuration()) {
                    mMediaPlayer.seekTo(progress);
                    // 保存播放进度
                    saveProgress(progress);
                    // 回调刷新进度显示
                    if (musicPlayCallback != null) {
                        musicPlayCallback.onSeekToProgress(progress);
                    }
                    return true;
                }
            } catch (Exception e) {
                Logger.logE("MusicPlayService-----------------------seekToByService error!");
                return false;
            }
        }
        return false;
    }

    /**
     * 微调快退
     */
    private void slightAdjustProgressBackward() {
        // 获取当前播放进度
        int progress = getProgress();
        if (progress >= 0) {// 合法进度
            progress -= 1000;
            if (progress < 0) {// 退到头了
                // 由于此处removeMessages无效，所以使用变量来停止快退
                sendMessageToAdjustBackward = false;
                // 切换上一首
                sendMessageToPlayPrevious(0);
            } else {
                // 调整进度
                seekToByService(progress);
            }
        }
    }

    /**
     * 微调快进
     */
    private void slightAdjustProgressForward() {
        if (!isCurrentMusicEnableToPlay()) {// 当前歌曲无法播放
            return;
        }

        // 获取当前播放进度
        int progress = getProgress();
        if (progress >= 0) {// 合法进度
            progress += 1000;
            if (progress > getPlayingMusicInfo().getDuration()) {// 超出总时长了
                // 由于此处removeMessages无效，所以使用变量来停止快进
                sendMessageToAdjustForward = false;
                // 切换下一首歌曲
                sendMessageToPlayNext(0, true);
            } else {
                // 调整进度
                seekToByService(progress);
            }
        }
    }

    /**
     * 快进快退开始准备
     */
    public void onStartTrackingTouch() {
        // 移除快进快退消息
        removeAdjustProgressMessages();
    }

    /**
     * 快进快退停止
     */
    public void onStopTrackingTouch(int progress) {
        // 松开手指后seekTo执行（之所以不放在onProgressChanged中执行是因为如果那样，当用户手指滑动到末尾时播放会结束，再往回滑时就跳转下一首了。）
        seekToByService(progress);
    }

    /**
     * 开始快退
     */
    public void startAdjustProgressBackward() {
        // 移除快进快退消息
        removeAdjustProgressMessages();
        // 如果快退之前是播放的，先暂停播放
        if (PreferencesUtil.getInstance().isMusicFinallyPlaying()) {// 之前是播放的
            // 暂停播放（不保存暂停状态，快退结束后恢复原先状态）
            sendMessageToPause(0, false);
        }
        sendMessageToAdjustBackward = true;
        // 发送快退消息
        mHandler.sendEmptyMessage(MusicConstants.MSG_SERVICE_SUB);
    }

    /**
     * 停止快退
     */
    public void stopAdjustProgressBackward() {
        if (mHandler.hasMessages(MusicConstants.MSG_SERVICE_SUB)) {// 有快退消息，说明正在快退
            // 移除快退消息
            mHandler.removeMessages(MusicConstants.MSG_SERVICE_SUB);
            sendMessageToAdjustBackward = false;
            // 如果快退之前是播放的，快退之后恢复播放
            if (PreferencesUtil.getInstance().isMusicFinallyPlaying()) {// 之前是播放的
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
        if (PreferencesUtil.getInstance().isMusicFinallyPlaying()) {// 快进之前是播放的
            // 暂停播放（不保存暂停状态，快进结束后恢复原先状态）
            sendMessageToPause(0, false);
        }
        sendMessageToAdjustForward = true;
        // 发送快进消息
        mHandler.sendEmptyMessage(MusicConstants.MSG_SERVICE_PLUS);
    }

    /**
     * 停止快进
     */
    public void stopAdjustProgressForward() {
        if (mHandler.hasMessages(MusicConstants.MSG_SERVICE_PLUS)) {// 有快进消息，说明正在快进
            // 移除快进消息
            mHandler.removeMessages(MusicConstants.MSG_SERVICE_PLUS);
            sendMessageToAdjustForward = false;
            // 如果快进之前是播放的，快退之后恢复播放
            if (PreferencesUtil.getInstance().isMusicFinallyPlaying()) {// 快进之前是播放的
                // 恢复播放
                sendMessageToStart(0, false);
            }
        }
    }

    /**
     * 移除快进快退消息
     */
    private void removeAdjustProgressMessages() {
        mHandler.removeMessages(MusicConstants.MSG_SERVICE_SUB);
        mHandler.removeMessages(MusicConstants.MSG_SERVICE_PLUS);
    }

    /**
     * 暂停
     */
    private boolean pauseByService(boolean saveState) {
        if (isPlaying()) {
            // 先停止刷新播放进度（防止在暂停后还发送一两个更新消息）
            stopSendMessageToUpdatePlayProgress();
            // 保存最后播放进度
            saveProgress(getProgress());
            // 暂停
            mMediaPlayer.pause();
            if (saveState) {// 如果需要记忆播放状态
                // 保存播放状态，状态为暂停
                PreferencesUtil.getInstance().setMusicFinallyPlaying(false);
            }
            // 通知前台暂停播放音乐
            if (musicPlayCallback != null) {
                musicPlayCallback.onMusicPause();
            }
            Logger.logD("MusicPlayService-----------------------pauseByService()");
            return true;
        }
        return false;
    }

    /**
     * 发送暂停消息
     */
    public void sendMessageToPause(int delayMillis, boolean saveState) {
        // 移除队列中的播放控制的消息
        removePlayControlMessages();
        // 移除队列中的快退快进消息
        removeAdjustProgressMessages();
        // 移除音量控制的消息
        removeVolumeControlMessages();
        // 发送暂停消息
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MusicConstants.MSG_SERVICE_PAUSE,
                saveState), delayMillis);
    }

    /**
     * 停止
     */
    private void stopByService() {
        if (mMediaPlayer != null) {
            // 先停止刷新播放进度（防止在暂停后还发送一两个更新消息）
            stopSendMessageToUpdatePlayProgress();
            // 停止
            mMediaPlayer.stop();
            // 释放
            mMediaPlayer.release();
            mMediaPlayer = null;
            // 通知前台停止播放音乐
            if (musicPlayCallback != null) {
                musicPlayCallback.onMusicStop();
            }
            Logger.logD("MusicPlayService-----------------------stopByService()");
        }
    }

    /**
     * 发送停止播放消息
     */
    private void sendMessageToStop() {
        // 移除队列中播放控制的消息
        removePlayControlMessages();
        // 移除队列中的快退快进消息
        removeAdjustProgressMessages();
        // 移除音量控制的消息
        removeVolumeControlMessages();
        // 发送停止消息
        mHandler.sendEmptyMessage(MusicConstants.MSG_SERVICE_STOP);
    }

    /**
     * 删除播放控制相关的消息
     */
    void removePlayControlMessages() {
        mHandler.removeMessages(MusicConstants.MSG_SERVICE_PLAY);
        mHandler.removeMessages(MusicConstants.MSG_SERVICE_START);
        mHandler.removeMessages(MusicConstants.MSG_SERVICE_PAUSE);
        mHandler.removeMessages(MusicConstants.MSG_SERVICE_STOP);
    }

    /**
     * 发送更新播放进度消息
     */
    void sendMessageToUpdatePlayProgress(int delayMillis) {
        mHandler.removeMessages(MusicConstants.MSG_SERVICE_UPDATE_PROGRESS);
        mHandler.sendEmptyMessageDelayed(MusicConstants.MSG_SERVICE_UPDATE_PROGRESS, delayMillis);
    }

    /**
     * 停止发送更新播放进度消息
     */
    void stopSendMessageToUpdatePlayProgress() {
        mHandler.removeMessages(MusicConstants.MSG_SERVICE_UPDATE_PROGRESS);
    }

    /**
     * 上一曲（播放上一曲时，需要做一些判断）
     * 1、单曲循环：将要播放的歌曲文件无法解析播放，此情况会造成死循环；2、文件夹循环：当前文件夹下只有一个歌曲文件
     * ，并且该歌曲文件无法解析播放，此情况会造成死循环
     * ；3、随机播放模式：该U盘下只有一个歌曲文件，并且该歌曲文件无法解析播放，此情况会造成死循环；4、全部循环
     * ：该U盘下只有一个歌曲文件，并且该歌曲文件无法解析播放，此情况会造成死循环。
     */
    private void previousByService() {
        if (FastClickUtil.enableToResponseClick(period_fast_click)) {
            play(mMediaDataModel.getPreviousPlayMusicUriByPlayMode(), -1, true, true);
        }
    }

    /**
     * 发送播放上一曲消息
     *
     * @param delayMillis 延迟多久切换上一曲
     */
    public void sendMessageToPlayPrevious(int delayMillis) {
        // 移除队列中的播放控制消息
        removePlayControlMessages();
        // 移除队列中的快退快进消息
        removeAdjustProgressMessages();
        // 移除上下一曲消息
        removePreviousNextMessages();
        // 发送上一曲消息
        mHandler.sendEmptyMessageDelayed(MusicConstants.MSG_SERVICE_PREVIOUS, delayMillis);
    }

    /**
     * 下一曲（播放下一曲时，需要做一些判断）
     * 1、单曲循环：将要播放的歌曲文件无法解析播放，此情况会造成死循环；2、文件夹循环：当前文件夹下只有一个歌曲文件
     * ，并且该歌曲文件无法解析播放，此情况会造成死循环
     * ；3、随机播放模式：该U盘下只有一个歌曲文件，并且该歌曲文件无法解析播放，此情况会造成死循环；4、全部循环
     * ：该U盘下只有一个歌曲文件，并且该歌曲文件无法解析播放，此情况会造成死循环。
     *
     * @param fromUser 是否用户手动切换，或者其他原因需要切换别的音乐
     */
    private void nextByService(boolean fromUser) {
        if (FastClickUtil.enableToResponseClick(period_fast_click)) {
            play(mMediaDataModel.getNextPlayMusicUriByPlayMode(fromUser), -1, true, true);
        }
    }

    /**
     * 发送播放下一曲消息
     *
     * @param delayMillis 延迟多久切换下一曲
     * @param fromUser    是否用户手动切换，或者其他原因需要切换别的视频
     */
    public void sendMessageToPlayNext(int delayMillis, boolean fromUser) {
        // 移除队列中的播放控制消息
        removePlayControlMessages();
        // 移除队列中的快退快进消息
        removeAdjustProgressMessages();
        // 移除上下一曲消息
        removePreviousNextMessages();
        // 发送下一曲消息
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MusicConstants.MSG_SERVICE_NEXT,
                fromUser), delayMillis);
    }

    /**
     * 删除上下一首消息
     */
    void removePreviousNextMessages() {
        mHandler.removeMessages(MusicConstants.MSG_SERVICE_PREVIOUS);
        mHandler.removeMessages(MusicConstants.MSG_SERVICE_NEXT);
    }

    /**
     * 从当前比例开始渐变调高音量
     */
    private void fadeUpVolume() {
        removeVolumeControlMessages();
        mHandler.sendEmptyMessage(MusicConstants.MSG_SERVICE_FADE_UP);
    }

    /**
     * 从0开始音量渐变上升
     */
    public void fadeUpVolumeFromZero() {
        removeVolumeControlMessages();
        setMusicVolume(mCurrentVolumeRatio = 0f);
        mHandler.sendEmptyMessage(MusicConstants.MSG_SERVICE_FADE_UP);
    }

    /**
     * 开始渐变调低音量
     */
    public void fadeDownVolume() {
        removeVolumeControlMessages();
        mHandler.sendEmptyMessage(MusicConstants.MSG_SERVICE_FADE_DOWN);
    }

    /**
     * 静音
     */
    private void muteVolume() {
        removeVolumeControlMessages();
        setMusicVolume(mCurrentVolumeRatio = 0f);
    }

    /**
     * 删除音量相关的消息
     */
    private void removeVolumeControlMessages() {
        mHandler.removeMessages(MusicConstants.MSG_SERVICE_FADE_UP);
        mHandler.removeMessages(MusicConstants.MSG_SERVICE_FADE_DOWN);
    }

    /**
     * 设置音乐音量，范围（0.0——1.0）
     */
    private void setMusicVolume(float volume) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(volume, volume);
            Logger.logI("MusicPlayService-----------------------设置音量比例 = " + volume);
        }
    }

    /**
     * 恢复播放情况
     */
    public void restorePlayState() {
        if (MusicPlayStateSaver.getInstance().isPrepared()) {// 歌曲是准备好的状态
            // 恢复歌曲的ID3信息显示
            MusicInfoParser.getInstance().parseMusicFile(PreferencesUtil.getInstance()
                    .getCurrentPlayingMusicUri());
            if (isCurrentMusicEnableToPlay()) {// 解析成功了才能播放
                if (PreferencesUtil.getInstance().isMusicFinallyPlaying()) {
                    // 如果最后用户选择的是播放而非暂停，则恢复界面后继续播放
                    startByService(false);
                }
            }
            // 恢复歌曲播放状态UI显示
            if (musicPlayCallback != null) {
                musicPlayCallback.onRestorePlayState();
            }
        } else {// 歌曲是未准备好的状态
            String musicUri = PreferencesUtil.getInstance().getCurrentPlayingMusicUri();
            if (TextUtils.isEmpty(musicUri)//
                    // 说明从未播放过音乐（可能MediaScanner扫描到第一个音乐文件的时候，MusicFragment
                    // 未在前台，这样就收不到onScannedFirstMusic()回调，所以需要在MusicFragment打开的时候做一次判断）
                    || !new File(musicUri).exists()) {// 或者记忆的播放歌曲不存在了，说明歌曲删除了或者换了U盘
                // 从扫描类中获取第一个扫描到的歌曲文件
                musicUri = MediaScanner.getInstance(getApplication()).getFirstScannedMusicUri();
                if (TextUtils.isEmpty(musicUri)) {// 暂时没有扫描到第一首歌曲或者压根没有插U盘

                } else {// 已经扫描到了第一首歌曲
                    // 初次自动播放
                    Logger.logD("MusicPlayService-----------------------从未播放过音乐，默认播放第一首扫描到的音乐：" +
                            musicUri);
                    // 发送播放音乐消息
                    sendMessageToPlay(musicUri, -1, 0, true, true);
                }
            } else {
                int progress = 0;// 播放进度
                boolean playOrNot = true;// 是否启动播放
                boolean saveState = true;// 是否保存播放状态（播放或暂停）
                if (MusicPlayStateSaver.getInstance().isFromFileManager()) {// 从文件管理器跳转播放的
                    musicUri = MusicPlayStateSaver.getInstance().getMusicUriFromFileManager();
                    // 重置变量
                    MusicPlayStateSaver.getInstance().setFromFileManager(false);
                    MusicPlayStateSaver.getInstance().setMusicUriFromFileManager(null);
                } else {
                    progress = PreferencesUtil.getInstance().getCurrentPlayingMusicProgress();
                    playOrNot = PreferencesUtil.getInstance().isMusicFinallyPlaying();
                    saveState = false;// 因为是恢复播放，所以不需要再保存播放状态
                }
                // 发送播放音乐消息
                sendMessageToPlay(musicUri, progress, 0, playOrNot, saveState);
                // 恢复歌曲播放状态UI显示
                if (musicPlayCallback != null) {
                    musicPlayCallback.onRestorePlayState();
                }
            }
        }
    }

    /**
     * 申请音频焦点
     */
    private int requestAudioFocus() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        }
        int audioFocusState = mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);// 获取音频永久焦点
        if (audioFocusState == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {// 申请音频永久焦点成功
            Logger.logD("MusicPlayService-----------------------requestAudioFocus success");
        } else {// 申请音频永久焦点失败
            Logger.logE("MusicPlayService-----------------------requestAudioFocus fail");
        }

        // 因为能在后台播放时响应中控按键，所以申请音频焦点的时候注册中控按键监听。
        registerKeyPressListener();

        return audioFocusState;
    }

    /**
     * 释放音频焦点
     */
    private int abandonAudioFocus() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        }
        int audioFocusState = mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        if (audioFocusState == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {// 释放音频永久焦点成功
            Logger.logD("MusicPlayService-----------------------abandonAudioFocus success");
        } else {// 释放音频永久焦点失败
            Logger.logE("MusicPlayService-----------------------abandonAudioFocus fail");
        }
        return audioFocusState;
    }

    /**
     * 注册中控实体按钮监听器
     */
    private void registerKeyPressListener() {
        Logger.logD("MusicPlayService-----------------------registerKeyPressListener");
        android.os.ProtocolManager.getInstance().registerKeyPressListener(mIKeyPressInterface);
    }

    /**
     * 注销中控实体按钮监听器
     */
    private void unregisterKeyPressListener() {
        Logger.logD("MusicPlayService-----------------------unregisterKeyPressListener");
        android.os.ProtocolManager.getInstance().unregisterKeyPressListener(mIKeyPressInterface);
    }

    @Override
    public void onDestroy() {
        Logger.logD("MusicPlayService-----------------------onDestroy");
        // 后台停止，设为未准备好状态
        MusicPlayStateSaver.getInstance().setPrepared(false);
        // 停止播放
        sendMessageToStop();
        // 注销相关
        unregister();

        super.onDestroy();
    }

    /**
     * 注销相关
     */
    private void unregister() {
        // 注销U盘状态变化监听器
        UsbStateManager.getInstance().unregisterUsbStateChangeListener(this);
        // 注销歌曲文件解析监听器
        MusicInfoParser.getInstance().unregisterParseMusicInfoListener(mParseMusicInfoListener);
        // 注销实体按钮上下一曲长按事件监听器
        MusicKeyLongClickModel.getInstance().setOnMusicKeyLongClickListener(null);
        // 注销中控按钮监听器
        unregisterKeyPressListener();
    }

    @Override
    public void onUsbMounted(int usbFlag) {// U盘插入动作
    }

    @Override
    public void onUsbUnMounted(int usbFlag) {// U盘拔出动作
        if (UsbStateManager.getInstance().hasNoUsbMounted()) {//
            // 没有挂载U盘，说明当前拔出的是最后一个U盘，此时不管有没有播放都应该退出应用
            // 停止MusicPlayService
            stopSelf();
            return;
        }

        // 正在播放的音乐Uri
        String currentPlayingMusicUri = PreferencesUtil.getInstance().getCurrentPlayingMusicUri();
        if (!TextUtils.isEmpty(currentPlayingMusicUri)) {
            switch (usbFlag) {
                case MultimediaConstants.FLAG_USB1:// 如果拔出的是U盘1
                    if (currentPlayingMusicUri.startsWith(MultimediaConstants.PATH_USB1)) {//
                        // 正在播放U盘1的音乐
                        // 停止MusicPlayService
                        stopSelf();
                    }
                    break;
                case MultimediaConstants.FLAG_USB2:// 如果拔出的是U盘2
                    if (currentPlayingMusicUri.startsWith(MultimediaConstants.PATH_USB2)) {//
                        // 正在播放U盘2的音乐
                        // 停止MusicPlayService
                        stopSelf();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onStartKeyLongClick(int keyCode) {// 开始长按按键
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
    }

    @Override
    public void onReceiveKeyLongClickEvent(int keyCode) {
    }

    private static class MusicPlayHandler extends Handler {
        private static WeakReference<MusicPlayService> mReference;

        public MusicPlayHandler(MusicPlayService service) {
            mReference = new WeakReference<MusicPlayService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mReference.get() == null) {
                return;
            }

            switch (msg.what) {
                case MusicConstants.MSG_SERVICE_PLAY:// 播放指定位置
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        mReference.get().play(bundle.getString("musicUri"), bundle.getInt
                                ("progress"), bundle.getBoolean("autoStart"), bundle.getBoolean
                                ("saveState"));
                    }
                    break;
                case MusicConstants.MSG_SERVICE_START:// 开始播放
                    mReference.get().startByService((Boolean) msg.obj);
                    break;
                case MusicConstants.MSG_SERVICE_PAUSE:// 暂停播放
                    mReference.get().pauseByService((Boolean) msg.obj);
                    break;
                case MusicConstants.MSG_SERVICE_STOP:// 停止播放
                    mReference.get().stopByService();
                    break;
                case MusicConstants.MSG_SERVICE_PREVIOUS:// 上一曲
                    mReference.get().previousByService();
                    break;
                case MusicConstants.MSG_SERVICE_NEXT:// 下一曲
                    mReference.get().nextByService((Boolean) msg.obj);
                    break;
                case MusicConstants.MSG_SERVICE_UPDATE_PROGRESS:// 更新播放进度
                    // 获取播放进度
                    int position = mReference.get().getProgress();
                    // 保存当前播放的进度值
                    mReference.get().saveProgress(position);
                    // 更新播放进度回调
                    if (mReference.get().musicPlayCallback != null) {
                        if (position >= 0) {// 进度合法
                            mReference.get().musicPlayCallback.onUpdatePlayProgress(position);
                        }
                    }
                    // 接着发送消息
                    mReference.get().sendMessageToUpdatePlayProgress(200);// 时间设短些，防止跳秒
                    break;
                case MusicConstants.MSG_SERVICE_FADE_UP:// 音量渐变上升
                    mReference.get().mCurrentVolumeRatio += mReference.get().volume_step_plus;
                    if (mReference.get().mCurrentVolumeRatio < mReference.get().highest_ratio) {
                        mReference.get().mHandler.sendEmptyMessageDelayed(MusicConstants
                                .MSG_SERVICE_FADE_UP, mReference.get().fade_up_delayMillis);
                    } else {
                        mReference.get().mCurrentVolumeRatio = mReference.get().highest_ratio;
                    }
                    mReference.get().setMusicVolume(AppUtil.formatOneDecimal(mReference.get()
                            .mCurrentVolumeRatio));
                    break;
                case MusicConstants.MSG_SERVICE_FADE_DOWN:// 音量渐变降低
                    mReference.get().mCurrentVolumeRatio -= mReference.get().volume_step_sub;
                    if (mReference.get().mCurrentVolumeRatio > mReference.get().lowest_ratio) {
                        mReference.get().mHandler.sendEmptyMessageDelayed(MusicConstants
                                .MSG_SERVICE_FADE_DOWN, mReference.get().fade_down_delayMillis);
                    } else {
                        mReference.get().mCurrentVolumeRatio = mReference.get().lowest_ratio;
                    }
                    mReference.get().setMusicVolume(AppUtil.formatOneDecimal(mReference.get()
                            .mCurrentVolumeRatio));
                    break;
                case MusicConstants.MSG_SERVICE_SUB:// 快退
                    if (mReference.get().sendMessageToAdjustBackward) {
                        mReference.get().slightAdjustProgressBackward();
                        mReference.get().mHandler.sendEmptyMessageDelayed(MusicConstants
                                .MSG_SERVICE_SUB, 125);
                    }
                    break;
                case MusicConstants.MSG_SERVICE_PLUS:// 快进
                    if (mReference.get().sendMessageToAdjustForward) {
                        mReference.get().slightAdjustProgressForward();
                        mReference.get().mHandler.sendEmptyMessageDelayed(MusicConstants
                                .MSG_SERVICE_PLUS, 125);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public class MusicPlayBinder extends Binder {
        public MusicPlayService getService() {
            return MusicPlayService.this;
        }
    }
}