package com.semisky.ym_multimedia.radio.model;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.semisky.ym_multimedia.common.utils.AppUtil;
import com.semisky.ym_multimedia.radio.utils.RadioUtil;
import com.semisky.ym_multimedia.radio.utils.ProtocolUtil;
import com.semisky.ym_multimedia.radio.utils.RadioConstants;

/**
 * 收音机声音控制Model
 * 
 * @author Anter
 * 
 */
public class RadioVolumeModel {
	private Context mContext;
	private static RadioVolumeModel instance;
	private final VolumeHandler mVolumeHandler = new VolumeHandler(this);

	private int max_radio_vol = 31;// 最大音量31
	private float mCurrentVolumeRatio = 1.0f;// 音量大小比例
	public float lowest_vol = 0.0f;// 音量最低比例
	public float highest_vol = 1.0f;// 音量最高比例
	public static float volume_step_sub = 0.1f;// 每次音量减小比例（尽量不要太小，否则设置音量过于频繁）
	public static float volume_step_plus = 0.1f;// 每次音量增大比例（尽量不要太小，否则设置音量过于频繁）
	public static int fade_down_delayMillis = 150;// 每次音量减小时间间隔
	public static int fade_up_delayMillis = 150;// 每次音量增大时间间隔

	public RadioVolumeModel(Context context) {
		this.mContext = context;
	}

	public static RadioVolumeModel getInstance(Context context) {
		if (instance == null) {
			instance = new RadioVolumeModel(context);
		}
		return instance;
	}

	public VolumeHandler getHandler() {
		return mVolumeHandler;
	}

	private static class VolumeHandler extends Handler {
		private static WeakReference<RadioVolumeModel> mReference;

		public VolumeHandler(RadioVolumeModel model) {
			mReference = new WeakReference<RadioVolumeModel>(model);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mReference.get() == null) {
				return;
			}
			
			switch (msg.what) {
			case RadioConstants.OPEN_RADIO_VOLUME:
				ProtocolUtil.getInstance(mReference.get().mContext)
						.openRadioVolDoubleInsure();
				break;
			case RadioConstants.CLOSE_RADIO_VOLUME:
				ProtocolUtil.getInstance(mReference.get().mContext)
						.closeRadioVolDoubleInsure();
				break;
			case RadioConstants.FADE_DOWM:// 声音渐渐降低
				mReference.get().mCurrentVolumeRatio -= volume_step_sub;
				if (mReference.get().mCurrentVolumeRatio > mReference.get().lowest_vol) {
					mReference.get().mVolumeHandler.sendEmptyMessageDelayed(
							RadioConstants.FADE_DOWM, fade_down_delayMillis);
				} else {
					mReference.get().mCurrentVolumeRatio = mReference.get().lowest_vol;
				}
				ProtocolUtil.getInstance(mReference.get().mContext).setVolumn(
						android.media.AudioSystem.STREAM_RADIO,
						AppUtil.roundOff(mReference.get().mCurrentVolumeRatio
								* mReference.get().max_radio_vol), 0);
				break;
			case RadioConstants.FADE_UP:// 声音渐渐增大
				mReference.get().mCurrentVolumeRatio += volume_step_plus;
				if (mReference.get().mCurrentVolumeRatio < mReference.get().highest_vol) {
					mReference.get().mVolumeHandler.sendEmptyMessageDelayed(
							RadioConstants.FADE_UP, fade_up_delayMillis);
				} else {
					mReference.get().mCurrentVolumeRatio = mReference.get().highest_vol;
				}
				ProtocolUtil.getInstance(mReference.get().mContext).setVolumn(
						android.media.AudioSystem.STREAM_RADIO,
						AppUtil.roundOff(mReference.get().mCurrentVolumeRatio
								* mReference.get().max_radio_vol), 0);
				break;
			default:
				break;
			}
		}
	}

	/** 获取当前渐变音量比例 */
	public float getCurrentVolumeRatio() {
		return mCurrentVolumeRatio;
	}

	/** 设置当前渐变音量比例 */
	public void setCurrentVolumeRatio(float ratio) {
		this.mCurrentVolumeRatio = ratio;
	}

	/** 设置收音机最大音量 */
	public void setMaxRadioVolume(int volume) {
		this.max_radio_vol = volume;
	}

	/** 设置渐变最低音量 */
	public void setLowestVolume(float lowest_vol) {
		this.lowest_vol = lowest_vol;
	}

	/** 获取当前渐变最低音量 */
	public float getLowestVolume() {
		return lowest_vol;
	}

	/** 删除队列中的消息 */
	public void removeMessages(int what) {
		mVolumeHandler.removeMessages(what);
	}

	/** 发送消息 */
	public void sendEmptyMessage(int what) {
		mVolumeHandler.sendEmptyMessage(what);
	}

	/** 延迟发送消息 */
	public void sendEmptyMessageDelayed(int what, long delayMillis) {
		mVolumeHandler.sendEmptyMessageDelayed(what, delayMillis);
	}

	/** 开始渐变调高音量 */
	public void fadeUpVolume() {
		removeMessages(RadioConstants.FADE_DOWM);
		removeMessages(RadioConstants.FADE_UP);
		sendEmptyMessage(RadioConstants.FADE_UP);
	}

	/** 开始渐变调低音量 */
	public void fadeDownVolume() {
		removeMessages(RadioConstants.FADE_UP);
		removeMessages(RadioConstants.FADE_DOWM);
		sendEmptyMessage(RadioConstants.FADE_DOWM);
	}
}
