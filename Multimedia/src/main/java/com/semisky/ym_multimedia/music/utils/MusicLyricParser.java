package com.semisky.ym_multimedia.music.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.multimedia.model.MediaDataModel;
import com.semisky.ym_multimedia.multimedia.utils.PreferencesUtil;
import com.semisky.ym_multimedia.music.view.LrcEntry;
import com.semisky.ym_multimedia.music.view.LrcView;

import java.io.File;
import java.util.List;

public class MusicLyricParser {
    private static MusicLyricParser instance;
    private String mFlag;
    private OnLyricParseListener onLyricParseListener;

    private MusicLyricParser() {
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new MusicLyricParser();
        }
    }

    public static MusicLyricParser getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    public void setOnLyricParseListener(OnLyricParseListener listener) {
        this.onLyricParseListener = listener;
    }

    public String getFlag() {
        return mFlag;
    }

    public void setFlag(String mFlag) {
        this.mFlag = mFlag;
    }

    /**
     * 加载歌词显示
     *
     * @param context
     * @param lrcView  歌词自定义View
     * @param musicUri 歌曲Uri
     */
    public void loadLyric(Context context, final LrcView lrcView, final String musicUri,
                          MediaDataModel model) {
        lrcView.reset();

        setFlag(musicUri);
        // 获取暂存的歌词Uri，不用每次打开音乐界面都查询一遍数据库。（MusicPlayService中prepare时会重置记忆的歌词Uri）
        String lyricUri = PreferencesUtil.getInstance().getCurrentPlayingMusicLyricUri();
        // 如果记忆的歌词Uri为空，说明从未播放过歌曲或者已经切换了别的歌曲或者歌词文件被删除或被移动了，此时查询数据库获取歌词Uri；否则使用记忆的歌词Uri
        if (TextUtils.isEmpty(lyricUri) || !new File(lyricUri).exists()) {//
            // 切换歌曲会将歌词记忆置空，所以不用添加切换歌曲的逻辑
            // 查询数据库获取歌词Uri
            lyricUri = model.getLyricUri(musicUri);
            // 保存歌词Uri
            PreferencesUtil.getInstance().setCurrentPlayingMusicLyricUri(lyricUri);
            if (TextUtils.isEmpty(lyricUri)) {// 最终还是发现U盘中没有当前歌曲对应的歌词文件，则停止往下执行
                // 无歌词文件回调
                if (onLyricParseListener != null) {
                    onLyricParseListener.onLyricNotExists(musicUri);
                }
                return;
            }
        }
        // 开启子线程加载歌词
        new AsyncTask<File, Integer, List<LrcEntry>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Logger.logD("MusicLyricParser-----------------------开始加载歌词");
            }

            @Override
            protected List<LrcEntry> doInBackground(File... params) {
                return LrcEntry.parseLrc(params[0]);
            }

            @Override
            protected void onPostExecute(List<LrcEntry> lrcEntries) {
                if (getFlag() == musicUri) {// 因为是异步加载，需要防错乱
                    if (lrcEntries == null || lrcEntries.size() <= 0) {
                        Logger.logE("MusicLyricParser-----------------------加载歌词失败");
                        // 解析歌词文件失败回调
                        if (onLyricParseListener != null) {
                            onLyricParseListener.onParseLyricFail(musicUri);
                        }
                        return;
                    }
                }

                if (getFlag() == musicUri) {// 因为是异步加载，需要防错乱
                    Logger.logD("MusicLyricParser-----------------------加载歌词成功");
                    lrcView.onLrcLoaded(lrcEntries);
                    setFlag(null);
                    // 解析歌词文件成功回调
                    if (onLyricParseListener != null) {
                        onLyricParseListener.onParseLyricSuccess(musicUri);
                    }
                }
            }

        }.execute(new File(lyricUri));
    }

    public interface OnLyricParseListener {
        // 对应歌曲的歌词文件不存在
        void onLyricNotExists(String musicUri);

        // 解析歌词文件失败
        void onParseLyricFail(String musicUri);

        // 解析歌词文件成功
        void onParseLyricSuccess(String musicUri);
    }
}
