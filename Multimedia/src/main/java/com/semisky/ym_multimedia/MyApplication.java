package com.semisky.ym_multimedia;

import android.app.Application;

import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.multimedia.model.MediaDataModelDBImp;
import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;

/**
 * Application类
 * 
 * @author Anter
 * 
 */
public class MyApplication extends Application {
	private static MyApplication instance;

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.logD("MyApplication-----------------------onCreate");
		instance = this;
		// 清除数据库中的多媒体数据
		MediaDataModelDBImp.getInstance(this).deleteAllMediaUri(
				MultimediaConstants.FLAG_USB1);
		MediaDataModelDBImp.getInstance(this).deleteAllMediaUri(
				MultimediaConstants.FLAG_USB2);
	}

	public static MyApplication getInstance() {
		return instance;
	}
}