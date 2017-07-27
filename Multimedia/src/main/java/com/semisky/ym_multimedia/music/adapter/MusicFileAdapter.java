package com.semisky.ym_multimedia.music.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.common.view.MarqueeTextView;
import com.semisky.ym_multimedia.multimedia.utils.PreferencesUtil;

import java.io.File;
import java.util.List;

public class MusicFileAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> showingMusicList;

    public MusicFileAdapter(Context context, List<String> showingMusicList) {
        this.mContext = context;
        this.showingMusicList = showingMusicList;
    }

    @Override
    public int getCount() {
        return showingMusicList.size();
    }

    @Override
    public Object getItem(int position) {
        return showingMusicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_music_video, null);
            holder = new ViewHolder();
            holder.item_icon = (ImageView) convertView.findViewById(R.id.item_icon);
            holder.item_name = (MarqueeTextView) convertView.findViewById(R.id.item_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String musicUrl = showingMusicList.get(position);
        if (new File(musicUrl).isDirectory()) {// 文件夹
            holder.item_icon.setImageResource(R.drawable.icon_folder);
        } else {// 音乐文件
            holder.item_icon.setImageResource(R.drawable.icon_music);
        }
        holder.item_name.setText(musicUrl.substring(musicUrl.lastIndexOf(File.separator) + 1));
        // 正在播放的歌曲设置跑马灯效果，并且背景变为蓝色
        String currentPlayingMusicUrl = PreferencesUtil.getInstance().getCurrentPlayingMusicUri();
        if (currentPlayingMusicUrl != null) {
            if (currentPlayingMusicUrl.equals(musicUrl)) {
                holder.item_name.startMarquee();
                convertView.setBackgroundResource(R.color.bg_blue_normal);
            } else {
                holder.item_name.stopMarquee();
                convertView.setBackgroundResource(R.drawable.bg_file_item_selector);
            }
        }
        return convertView;
    }

    static class ViewHolder {
        ImageView item_icon;
        MarqueeTextView item_name;
    }
}
