package com.semisky.ym_multimedia.multimedia.dao;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.multimedia.model.MediaDataModel;
import com.semisky.ym_multimedia.multimedia.model.MediaDataModelDBImp;
import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;
import com.semisky.ym_multimedia.multimedia.utils.OnScanMediaFileListener;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MediaScanner {
    private static final int MSG_SCAN_START = 0x01;// 通知前台U盘扫描开始
    private static final int MSG_REFRESH_USB1_LIST = 0x02;// 发送通知所有前台刷新U盘1列表消息
    private static final int MSG_REFRESH_USB2_LIST = 0x03;// 发送通知所有前台刷新U盘2列表消息
    private static final int MSG_SCANNED_FIRST_PHOTO = 0x04;// 扫描到第一个图片文件消息
    private static final int MSG_SCANNED_FIRST_MUSIC = 0x05;// 扫描到第一个音乐文件消息
    private static final int MSG_SCANNED_FIRST_VIDEO = 0x06;// 扫描到第一个视频文件消息
    private static final int MSG_SCAN_FINISH = 0x07;// 通知前台U盘扫描结束
    private static MediaScanner instance;
    private Context mContext;
    private String[] image_suffix;// 图片文件后缀数组
    private String[] music_suffix;// 音频文件后缀数组
    private String[] lyric_suffix;// 歌词文件后缀数组
    private String[] video_suffix;// 视频文件后缀数组
    private ScanRunnable usb1ScanRunnable, usb2ScanRunnable;// U盘扫描线程
    private MediaScanHandler mScanHandler;
    private MediaDataModel mMediaDataModel;// 媒体文件数据操作Model类
    private int mDelayMillis = 2000;// 动态加载时刷新间隔时间
    private String firstScannedUsb1PhotoUri, firstScannedUsb2PhotoUri;// 扫描到的第一个图片文件Uri
    private String firstScannedUsb1MusicUri, firstScannedUsb2MusicUri;// 扫描到的第一个音乐文件Uri
    private String firstScannedUsb1VideoUri, firstScannedUsb2VideoUri;// 扫描到的第一个视频文件Uri
    private List<OnScanMediaFileListener> onScanMediaFileListeners;// 多媒体数据扫描监听器

    private MediaScanner(Context context) {
        this.mContext = context;
        image_suffix = context.getResources().getStringArray(R.array.image_suffix);
        music_suffix = context.getResources().getStringArray(R.array.music_suffix);
        lyric_suffix = context.getResources().getStringArray(R.array.lyric_suffix);
        video_suffix = context.getResources().getStringArray(R.array.video_suffix);
        mScanHandler = new MediaScanHandler(this);
        onScanMediaFileListeners = new ArrayList<OnScanMediaFileListener>();
        initModel();
    }

    private static synchronized void syncInit(Context context) {
        if (instance == null) {
            instance = new MediaScanner(context);
        }
    }

    public static MediaScanner getInstance(Context context) {
        if (instance == null) {
            syncInit(context);
        }
        return instance;
    }

    /**
     * 注册媒体扫描监听器
     */
    public void registerScanMediaFileListener(OnScanMediaFileListener listener) {
        if (!onScanMediaFileListeners.contains(listener)) {
            onScanMediaFileListeners.add(listener);
        }
    }

    /**
     * 注销媒体扫描监听器
     */
    public void unregisterScanMediaFileListener(OnScanMediaFileListener listener) {
        onScanMediaFileListeners.remove(listener);
    }

    private void initModel() {
        mMediaDataModel = MediaDataModelDBImp.getInstance(mContext);
    }

    /**
     * U盘1扫描线程是否在运行
     */
    boolean isUsb1Scaning() {
        return usb1ScanRunnable != null && usb1ScanRunnable.isRunning();
    }

    /**
     * U盘2扫描线程是否在运行
     */
    boolean isUsb2Scaning() {
        return usb2ScanRunnable != null && usb2ScanRunnable.isRunning();
    }

    public String getFirstScannedUsb1PhotoUri() {
        return firstScannedUsb1PhotoUri;
    }

    public String getFirstScannedUsb2PhotoUri() {
        return firstScannedUsb2PhotoUri;
    }

    /**
     * 获取第一个扫描到的图片文件
     */
    public String getFirstScannedPhotoUri() {
        return firstScannedUsb1PhotoUri != null ? firstScannedUsb1PhotoUri :
                firstScannedUsb2PhotoUri;
    }

    /**
     * 获取第一个扫描到的音乐文件
     */
    public String getFirstScannedMusicUri() {
        return firstScannedUsb1MusicUri != null ? firstScannedUsb1MusicUri :
                firstScannedUsb2MusicUri;
    }

    /**
     * 获取第一个扫描到的视频文件
     */
    public String getFirstScannedVideoUri() {
        return firstScannedUsb1VideoUri != null ? firstScannedUsb1VideoUri :
                firstScannedUsb2VideoUri;
    }

    /**
     * 开始扫描U盘
     *
     * @param usbFlag
     */
    public void startScanUsb(int usbFlag) {
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                if (!UsbStateManager.getInstance().isUsb1Mounted()) {
                    return;
                }
                if (isUsb1Scaning()) {
                    // return;
                }
                // 开启子线程扫描
                usb1ScanRunnable = new ScanRunnable(MultimediaConstants.FLAG_USB1);
                new Thread(usb1ScanRunnable).start();
                break;
            case MultimediaConstants.FLAG_USB2:
                if (!UsbStateManager.getInstance().isUsb2Mounted()) {
                    return;
                }
                if (isUsb2Scaning()) {
                    // return;
                }
                // 开启子线程扫描
                usb2ScanRunnable = new ScanRunnable(MultimediaConstants.FLAG_USB2);
                new Thread(usb2ScanRunnable).start();
                break;
            default:
                break;
        }
    }

    /**
     * 扫描多媒体文件（递归）
     */
    void scanMediaFile(int usbFlag, File targetFile) {
        if (targetFile == null || !targetFile.exists()) {// 文件不存在
            return;
        }

        if (targetFile.isDirectory()) {// 如果是文件夹
            File[] files = targetFile.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    scanMediaFile(usbFlag, file);// 递归扫描
                }
            }
        } else {// 如果是文件
            String fileUri = targetFile.getAbsolutePath();// 文件路径
            String pathToLowerCase = fileUri.toLowerCase(Locale.getDefault());// 转小写

            // Photo
            for (String suffix : image_suffix) {// 文件后缀比较
                if (pathToLowerCase.endsWith(suffix)) {// 如果是图片文件
                    // 添加图片
                    mMediaDataModel.addPhotoUri(usbFlag, fileUri);

                    switch (usbFlag) {
                        case MultimediaConstants.FLAG_USB1:
                            if (firstScannedUsb1PhotoUri == null) {// 如果之前并未扫描到U盘1的第一个图片文件
                                // 保存扫描到U盘1的第一个图片文件Uri
                                firstScannedUsb1PhotoUri = fileUri;
                                // 回调PhotoFragment扫描到U盘1的第一个图片文件
                                sendMsgToNotifyScannedFirstPhoto(usbFlag, fileUri);
                            }
                            break;
                        case MultimediaConstants.FLAG_USB2:
                            if (firstScannedUsb2PhotoUri == null) {// 如果之前并未扫描到U盘2的第一个图片文件
                                // 保存扫描到U盘2的第一个图片文件Uri
                                firstScannedUsb2PhotoUri = fileUri;
                                // 回调PhotoFragment扫描到U盘2的第一个图片文件
                                sendMsgToNotifyScannedFirstPhoto(usbFlag, fileUri);
                            }
                            break;
                        default:
                            break;
                    }
                    return;
                }
            }

            // Music
            for (String suffix : music_suffix) {// 文件后缀比较
                if (pathToLowerCase.endsWith(suffix)) {// 如果是音乐文件
                    // 添加音乐
                    mMediaDataModel.addMusicUri(usbFlag, fileUri);

                    switch (usbFlag) {
                        case MultimediaConstants.FLAG_USB1:
                            if (firstScannedUsb1MusicUri == null) {// 如果之前并未扫描到U盘1的第一个音乐文件
                                // 保存扫描到U盘1的第一个音乐文件Uri
                                firstScannedUsb1MusicUri = fileUri;
                                // 回调MusicFragment扫描到U盘1的第一个音乐文件
                                sendMsgToNotifyScannedFirstMusic(usbFlag, fileUri);
                            }
                            break;
                        case MultimediaConstants.FLAG_USB2:
                            if (firstScannedUsb2MusicUri == null) {// 如果之前并未扫描到U盘2的第一个音乐文件
                                // 保存扫描到U盘2的第一个音乐文件Uri
                                firstScannedUsb2MusicUri = fileUri;
                                // 回调MusicFragment扫描到U盘2的第一个音乐文件
                                sendMsgToNotifyScannedFirstMusic(usbFlag, fileUri);
                            }
                            break;
                        default:
                            break;
                    }
                    return;
                }
            }

            // Lyric
            for (String suffix : lyric_suffix) {// 文件后缀比较
                if (pathToLowerCase.endsWith(suffix)) {// 如果是歌词文件
                    // 添加歌词
                    mMediaDataModel.addLyricUri(usbFlag, fileUri);
                    return;
                }
            }

            // Video
            for (String suffix : video_suffix) {// 文件后缀比较
                if (pathToLowerCase.endsWith(suffix)) {// 如果是视频文件
                    // 添加视频
                    mMediaDataModel.addVideoUri(usbFlag, fileUri);

                    switch (usbFlag) {
                        case MultimediaConstants.FLAG_USB1:
                            if (firstScannedUsb1VideoUri == null) {// 如果之前并未扫描到U盘1的第一个视频文件
                                // 保存扫描到U盘1的第一个视频文件Uri
                                firstScannedUsb1VideoUri = fileUri;
                                // 回调VideoFragment扫描到U盘1的第一个视频文件
                                sendMsgToNotifyScannedFirstVideo(usbFlag, fileUri);
                            }
                            break;
                        case MultimediaConstants.FLAG_USB2:
                            if (firstScannedUsb2VideoUri == null) {// 如果之前并未扫描到U盘2的第一个视频文件
                                // 保存扫描到U盘2的第一个视频文件Uri
                                firstScannedUsb2VideoUri = fileUri;
                                // 回调VideoFragment扫描到U盘2的第一个视频文件
                                sendMsgToNotifyScannedFirstVideo(usbFlag, fileUri);
                            }
                            break;
                        default:
                            break;
                    }
                    return;
                }
            }
        }
    }

    /**
     * 扫描开始回调
     */
    void notifyScanStart(int usbFlag) {
        Logger.logI("MediaScanner---------------通知前台扫描U盘" + (usbFlag + 1) + "开始");
        for (OnScanMediaFileListener listener : onScanMediaFileListeners) {
            if (listener != null) {
                listener.onScanStart(usbFlag);
            }
        }
    }

    /**
     * 发送扫描开始消息（为的是回调在主线程执行）
     */
    void sendMsgToNotifyScanStart(int usbFlag) {
        mScanHandler.obtainMessage(MSG_SCAN_START, usbFlag, 0).sendToTarget();
    }

    /**
     * 定时刷新回调
     */
    void notifyRefreshList(int usbFlag) {
        Logger.logI("MediaScanner---------------通知前台刷新USB" + (usbFlag + 1) + "列表");
        for (OnScanMediaFileListener listener : onScanMediaFileListeners) {
            if (listener != null) {
                listener.onNotifyRefreshList(usbFlag);
            }
        }
    }

    /**
     * 发送刷新文件列表消息
     *
     * @param usbFlag     U盘标识
     * @param delayMillis 延迟时间
     */
    void sendMsgToNotifyRefreshList(int usbFlag, int delayMillis) {
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                mScanHandler.removeMessages(MSG_REFRESH_USB1_LIST);
                mScanHandler.sendEmptyMessageDelayed(MSG_REFRESH_USB1_LIST, delayMillis);
                break;
            case MultimediaConstants.FLAG_USB2:
                mScanHandler.removeMessages(MSG_REFRESH_USB2_LIST);
                mScanHandler.sendEmptyMessageDelayed(MSG_REFRESH_USB2_LIST, delayMillis);
                break;
            default:
                break;
        }
    }

    /**
     * 扫描到第一个图片文件回调
     */
    void notifyScannedFirstPhoto(int usbFlag, String photoUri) {
        Logger.logI("MediaScanner---------------扫描到U盘" + (usbFlag + 1) + "的第一个图片文件：" + photoUri);
        for (OnScanMediaFileListener listener : onScanMediaFileListeners) {
            if (listener != null) {
                listener.onScannedFirstPhoto(usbFlag, photoUri);
            }
        }
    }

    /**
     * 发送扫描到第一个图片文件消息（为的是回调在主线程执行）
     */
    void sendMsgToNotifyScannedFirstPhoto(int usbFlag, String photoUri) {
        mScanHandler.obtainMessage(MSG_SCANNED_FIRST_PHOTO, usbFlag, 0, photoUri).sendToTarget();
    }

    /**
     * 扫描到第一个音乐文件回调
     */
    void notifyScannedFirstMusic(int usbFlag, String musicUri) {
        Logger.logI("MediaScanner---------------扫描到U盘" + (usbFlag + 1) + "的第一个音乐文件：" + musicUri);
        for (OnScanMediaFileListener listener : onScanMediaFileListeners) {
            if (listener != null) {
                listener.onScannedFirstMusic(usbFlag, musicUri);
            }
        }
    }

    /**
     * 发送扫描到第一个音乐文件消息（为的是回调在主线程执行）
     */
    void sendMsgToNotifyScannedFirstMusic(int usbFlag, String musicUri) {
        mScanHandler.obtainMessage(MSG_SCANNED_FIRST_MUSIC, usbFlag, 0, musicUri).sendToTarget();
    }

    /**
     * 扫描到第一个视频文件回调
     */
    void notifyScannedFirstVideo(int usbFlag, String videoUri) {
        Logger.logI("MediaScanner---------------扫描到U盘" + (usbFlag + 1) + "的第一个视频文件：" + videoUri);
        for (OnScanMediaFileListener listener : onScanMediaFileListeners) {
            if (listener != null) {
                listener.onScannedFirstVideo(usbFlag, videoUri);
            }
        }
    }

    /**
     * 发送扫描到第一个视频文件消息（为的是回调在主线程执行）
     */
    void sendMsgToNotifyScannedFirstVideo(int usbFlag, String videoUri) {
        mScanHandler.obtainMessage(MSG_SCANNED_FIRST_VIDEO, usbFlag, 0, videoUri).sendToTarget();
    }

    /**
     * 停止发送刷新消息
     */
    void stopSendMsgToNotifyRefreshList(int usbFlag) {
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                mScanHandler.removeMessages(MSG_REFRESH_USB1_LIST);
                break;
            case MultimediaConstants.FLAG_USB2:
                mScanHandler.removeMessages(MSG_REFRESH_USB2_LIST);
                break;
            default:
                break;
        }
    }

    /**
     * 扫描结束回调
     */
    void notifyScanFinish(int usbFlag) {
        Logger.logI("MediaScanner---------------通知前台扫描U盘" + (usbFlag + 1) + "结束");
        for (OnScanMediaFileListener listener : onScanMediaFileListeners) {
            if (listener != null) {
                listener.onScanFinish(usbFlag);
            }
        }
    }

    /**
     * 发送扫描结束消息（为的是回调在主线程执行）
     */
    void sendMsgToNotifyScanFinish(int usbFlag) {
        mScanHandler.obtainMessage(MSG_SCAN_FINISH, usbFlag, 0).sendToTarget();
    }

    /**
     * U盘挂载上
     */
    public void onUsbMounted(int usbFlag) {
        // 开始扫描U盘的媒体文件
        startScanUsb(usbFlag);
    }

    /**
     * U盘拔出
     */
    public void onUsbUnMounted(int usbFlag) {
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                // 拔出U盘，重置变量
                firstScannedUsb1PhotoUri = null;
                firstScannedUsb1MusicUri = null;
                firstScannedUsb1VideoUri = null;
                break;
            case MultimediaConstants.FLAG_USB2:
                // 拔出U盘，重置变量
                firstScannedUsb2PhotoUri = null;
                firstScannedUsb2MusicUri = null;
                firstScannedUsb2VideoUri = null;
                break;
            default:
                break;
        }
        // 清除数据库中属于该U盘的多媒体数据
        mMediaDataModel.deleteAllMediaUri(usbFlag);
    }

    private static class MediaScanHandler extends Handler {
        private static WeakReference<MediaScanner> mReference;

        public MediaScanHandler(MediaScanner scanner) {
            mReference = new WeakReference<MediaScanner>(scanner);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mReference.get() == null) {
                return;
            }

            switch (msg.what) {
                case MSG_SCAN_START:// 通知前台U盘扫描开始
                    // 扫描开始回调
                    mReference.get().notifyScanStart(msg.arg1);
                    break;
                case MSG_REFRESH_USB1_LIST:// 发送通知所有前台刷新U盘1列表消息
                    // 通知前台刷新USB1列表
                    mReference.get().notifyRefreshList(MultimediaConstants.FLAG_USB1);
                    if (mReference.get().isUsb1Scaning()) {//
                        // 临界情况：removeMessages无效（只能remove未投递的消息，已投递未执行的消息无法remove）。所以需要再加这重判断
                        // 继续发送
                        mReference.get().sendMsgToNotifyRefreshList(MultimediaConstants
                                .FLAG_USB1, mReference.get().mDelayMillis);
                    }
                    break;
                case MSG_REFRESH_USB2_LIST:// 发送通知所有前台刷新U盘2列表消息
                    // 通知前台刷新USB2列表
                    mReference.get().notifyRefreshList(MultimediaConstants.FLAG_USB2);
                    if (mReference.get().isUsb2Scaning()) {//
                        // 临界情况：removeMessages无效（只能remove未投递的消息，已投递未执行的消息无法remove）。所以需要再加这重判断
                        // 继续发送
                        mReference.get().sendMsgToNotifyRefreshList(MultimediaConstants
                                .FLAG_USB2, mReference.get().mDelayMillis);
                    }
                    break;
                case MSG_SCANNED_FIRST_PHOTO:// 扫描到第一个图片文件消息
                    mReference.get().notifyScannedFirstPhoto(msg.arg1, (String) msg.obj);
                    break;
                case MSG_SCANNED_FIRST_MUSIC:// 扫描到第一个音乐文件消息
                    mReference.get().notifyScannedFirstMusic(msg.arg1, (String) msg.obj);
                    break;
                case MSG_SCANNED_FIRST_VIDEO:// 扫描到第一个视频文件消息
                    mReference.get().notifyScannedFirstVideo(msg.arg1, (String) msg.obj);
                    break;
                case MSG_SCAN_FINISH:// 通知前台U盘扫描结束
                    // 扫描结束回调
                    mReference.get().notifyScanFinish(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 扫描所有文件线程
     */
    private class ScanRunnable implements Runnable {
        private int usbFlag;// U盘标识
        private boolean running;// 该线程是否正在执行

        public ScanRunnable(int usbFlag) {
            this.usbFlag = usbFlag;
        }

        public boolean isRunning() {
            return running;
        }

        @Override
        public void run() {
            Logger.logD("MediaScanner---------------扫描U盘" + (usbFlag + 1) + "线程开始执行");
            // 该线程开始执行
            running = true;
            // 通知前台扫描开始
            sendMsgToNotifyScanStart(usbFlag);
            // 开始定时刷新
            sendMsgToNotifyRefreshList(usbFlag, mDelayMillis);

            // 执行扫描
            switch (usbFlag) {
                case MultimediaConstants.FLAG_USB1:
                    // 扫描文件
                    scanMediaFile(MultimediaConstants.FLAG_USB1, new File(MultimediaConstants
                            .PATH_USB1));
                    break;
                case MultimediaConstants.FLAG_USB2:
                    // 扫描文件
                    scanMediaFile(MultimediaConstants.FLAG_USB2, new File(MultimediaConstants
                            .PATH_USB2));
                    break;
                default:
                    break;
            }

            // 扫描结束后插入剩余的数据到数据库
            MediaDBManager.getInstance(mContext).insertLastGroupData(usbFlag);
            // 停止定时刷新
            stopSendMsgToNotifyRefreshList(usbFlag);
            // 通知前台扫描结束
            sendMsgToNotifyScanFinish(usbFlag);
            // 该线程运行结束
            running = false;
            Logger.logD("MediaScanner---------------扫描U盘" + (usbFlag + 1) + "线程执行完毕");
        }
    }
}
