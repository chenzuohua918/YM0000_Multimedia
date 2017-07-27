package com.semisky.ym_multimedia.photo.adapter;

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
import com.semisky.ym_multimedia.photo.utils.PhotoUtil;

import java.io.File;
import java.util.List;

public class PhotoFileAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> showingPhotoList;

    public PhotoFileAdapter(Context context, List<String> showingPhotoList) {
        this.mContext = context;
        this.showingPhotoList = showingPhotoList;
    }

    @Override
    public int getCount() {
        return showingPhotoList.size();
    }

    @Override
    public Object getItem(int position) {
        return showingPhotoList.get(position);
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_photo, null);
            holder = new ViewHolder();
            holder.item_icon = (ImageView) convertView.findViewById(R.id.item_icon);
            holder.item_name = (MarqueeTextView) convertView.findViewById(R.id.item_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String photoUrl = showingPhotoList.get(position);
        if (new File(photoUrl).isDirectory()) {// 文件夹
            holder.item_icon.setImageResource(R.drawable.icon_folder);
        } else {// 图片文件
            PhotoUtil.displayImageByGlide(mContext, "file://" + photoUrl, holder.item_icon);
        }
        holder.item_name.setText(photoUrl.substring(photoUrl.lastIndexOf(File.separator) + 1));
        // 正在播放的图片设置跑马灯效果，并且背景变为蓝色
        String currentPlayingPhotoUrl = PreferencesUtil.getInstance().getCurrentPlayingPhotoUri();
        if (currentPlayingPhotoUrl != null) {
            if (currentPlayingPhotoUrl.equals(photoUrl)) {
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
