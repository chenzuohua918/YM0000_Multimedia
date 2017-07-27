package com.semisky.ym_multimedia.photo.utils;

import java.util.Locale;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.semisky.ym_multimedia.R;

public class PhotoUtil {
	/**
	 * 显示图片
	 * 
	 * @param context
	 * @param url
	 * @param imageView
	 */
	public static void displayImageByGlide(Context context, String url,
			ImageView imageView) {
		if (url.toLowerCase(Locale.getDefault()).endsWith(".gif")) {// 动图
			Glide.with(context.getApplicationContext()).load(url).asGif()
					.diskCacheStrategy(DiskCacheStrategy.SOURCE)
					.error(R.drawable.icon_photo_defaut).into(imageView);
		} else {
			Glide.with(context.getApplicationContext()).load(url)
					// 仅仅缓存最终图像，即降低分辨率后的（或者转换后的）
					.diskCacheStrategy(DiskCacheStrategy.RESULT)
					.error(R.drawable.icon_photo_defaut).into(imageView);
		}
	}

}
