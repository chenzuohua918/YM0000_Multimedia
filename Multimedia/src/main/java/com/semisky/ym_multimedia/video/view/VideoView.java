/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.semisky.ym_multimedia.video.view;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

import com.semisky.ym_multimedia.common.utils.AppUtil;
import com.semisky.ym_multimedia.common.utils.Logger;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 * <p>
 * 
 * <em>Note: VideoView does not retain its full state when going into the
 * background.</em> In particular, it does not restore the current play state,
 * play position, selected tracks, or any subtitle tracks added via
 * {@link #addSubtitleSource addSubtitleSource()}. Applications should save and
 * restore these on their own in
 * {@link android.app.Activity#onSaveInstanceState} and
 * {@link android.app.Activity#onRestoreInstanceState}.
 * <p>
 * Also note that the audio session id (from {@link #getAudioSessionId}) may
 * change from its previously returned value when the VideoView is restored.
 */
public class VideoView extends SurfaceView implements MediaPlayerControl,
		android.media.SubtitleController.Anchor {
	private String TAG = "VideoView";
	// settable by the client
	private Uri mUri;
	private Map<String, String> mHeaders;

	// all possible internal states
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;

	// VIEW_SUBTITLE
	private static final int SUBTITLE_DISPLAY_NONE = -1;
	private static final int SUBTITLE_DISPLAY_OFF = 0;
	private static final int SUBTITLE_DISPLAY_ON = 1;
	private static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;

	private static final int SUBTITLE_INTERNAL_PGS = 3;

	// mCurrentState is a VideoView object's current state.
	// mTargetState is the state that a method caller intends to reach.
	// For instance, regardless the VideoView object's current state,
	// calling pause() intends to bring the object to a target state
	// of STATE_PAUSED.
	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;

	// All the stuff we need for playing and showing a video
	private SurfaceHolder mSurfaceHolder = null;
	private MediaPlayer mMediaPlayer = null;
	private int mAudioSession;
	private int mVideoWidth;
	private int mVideoHeight;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private MediaController mMediaController;
	private OnCompletionListener mOnCompletionListener;
	private MediaPlayer.OnPreparedListener mOnPreparedListener;
	private int mCurrentBufferPercentage;
	private OnErrorListener mOnErrorListener;
	private OnInfoListener mOnInfoListener;
	private int mSeekWhenPrepared; // recording the seek position while
									// preparing
	private boolean mCanPause;
	private boolean mCanSeekBack;
	private boolean mCanSeekForward;

	// VIEW_SUBTITLE
	private android.widget.Subtitle mSubtitle;
	private boolean mSubtitleOn = true;
	private int mSubtitleFontSize = 24;
	private android.widget.Subtitle.OnSubtitleListener mOnSubtitleListener;
	private ContentResolver mContentResolver;
	/** Subtitle rendering widget overlaid on top of the video. */
	private android.media.SubtitleTrack.RenderingWidget mSubtitleWidget;

	/** Listener for changes to subtitle data, used to redraw when needed. */
	private android.media.SubtitleTrack.RenderingWidget.OnChangedListener mSubtitlesChangedListener;

	public VideoView(Context context) {
		super(context);
		initVideoView();

		// VIEW_SUBTITLE
		mContentResolver = context.getContentResolver();
	}

	public VideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		initVideoView();

		// VIEW_SUBTITLE
		mContentResolver = context.getContentResolver();
	}

	public VideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initVideoView();

		// VIEW_SUBTITLE
		mContentResolver = context.getContentResolver();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Anter

		// //Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec)
		// + ", "
		// // + MeasureSpec.toString(heightMeasureSpec) + ")");
		//
		// int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
		// int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
		// if (mVideoWidth > 0 && mVideoHeight > 0) {
		//
		// int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		// int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		// int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		// int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
		//
		// if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode ==
		// MeasureSpec.EXACTLY) {
		// // the size is fixed
		// width = widthSpecSize;
		// height = heightSpecSize;
		//
		// // for compatibility, we adjust size based on aspect ratio
		// if ( mVideoWidth * height < width * mVideoHeight ) {
		// //Log.i("@@@", "image too wide, correcting");
		// width = height * mVideoWidth / mVideoHeight;
		// } else if ( mVideoWidth * height > width * mVideoHeight ) {
		// //Log.i("@@@", "image too tall, correcting");
		// height = width * mVideoHeight / mVideoWidth;
		// }
		// } else if (widthSpecMode == MeasureSpec.EXACTLY) {
		// // only the width is fixed, adjust the height to match aspect ratio
		// if possible
		// width = widthSpecSize;
		// height = width * mVideoHeight / mVideoWidth;
		// if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize)
		// {
		// // couldn't match aspect ratio within the constraints
		// height = heightSpecSize;
		// }
		// } else if (heightSpecMode == MeasureSpec.EXACTLY) {
		// // only the height is fixed, adjust the width to match aspect ratio
		// if possible
		// height = heightSpecSize;
		// width = height * mVideoWidth / mVideoHeight;
		// if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
		// // couldn't match aspect ratio within the constraints
		// width = widthSpecSize;
		// }
		// } else {
		// // neither the width nor the height are fixed, try to use actual
		// video size
		// width = mVideoWidth;
		// height = mVideoHeight;
		// if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize)
		// {
		// // too tall, decrease both width and height
		// height = heightSpecSize;
		// width = height * mVideoWidth / mVideoHeight;
		// }
		// if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
		// // too wide, decrease both width and height
		// width = widthSpecSize;
		// height = width * mVideoHeight / mVideoWidth;
		// }
		// }
		// } else {
		// // no size yet, just adopt the given spec sizes
		// }
		// setMeasuredDimension(width, height);

		int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
		int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}

	@Override
	public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(VideoView.class.getName());
	}

	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(VideoView.class.getName());
	}

	public int resolveAdjustedSize(int desiredSize, int measureSpec) {
		return getDefaultSize(desiredSize, measureSpec);
	}

	private void initVideoView() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		mPendingSubtitleTracks = new Vector<Pair<InputStream, MediaFormat>>();
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;

		// Anter
		mVolumeHandler = new VolumeHandler(this);
		mGestureDetector = new GestureDetector(getContext(),
				new GestureDetector.SimpleOnGestureListener() {
					final float FLING_MIN_VELOCITY = 5f;
					float startX = -1;

					@Override
					public boolean onSingleTapUp(MotionEvent e) {// 抬起，手指离开触摸屏时触发(长按、滚动、滑动时，不会触发这个手势)
						return false;
					}

					@Override
					public void onShowPress(MotionEvent e) {// 短按，手指按下后片刻后抬起，会触发这个手势，如果迅速抬起则不会（实测，手指按下后既不抬起也不移动，七八十毫秒后触发）
					}

					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2,
							float distanceX, float distanceY) {// 滚动，手指按下后移动
						if (startX < 0) {
							startX = e1.getX();
						}
						if (Math.abs(distanceX) > Math.abs(distanceY)) {
							if (e2.getX() - startX > 5
									&& Math.abs(distanceX) > FLING_MIN_VELOCITY) {// 右滑快进
								if (onSlidingListener != null) {
									onSlidingListener.onSlidingRight(Math
											.abs(distanceX));
								}
								startX = e2.getX();// 实时记录开始位置，防止手指来回滑动只快进或快退
							} else if (e2.getX() - startX < -5
									&& Math.abs(distanceX) > FLING_MIN_VELOCITY) {// 左滑快退
								if (onSlidingListener != null) {
									onSlidingListener.onSlidingLeft(Math
											.abs(distanceX));
								}
								startX = e2.getX();// 实时记录开始位置，防止手指来回滑动只快进或快退
							}
						}
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {// 长按，手指按下后既不抬起也不移动，过一段时间（大概800毫秒）后触发
					}

					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {// 滑动，手指按下后快速移动并抬起，会先触发滚动手势，跟着触发一个滑动手势
						return true;
					}

					@Override
					public boolean onDown(MotionEvent e) {
						startX = -1;
						return true;
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {// 双击，手指在触摸屏上迅速点击第二下时触发
						if (onSlidingListener != null) {
							onSlidingListener.onVideoViewDoubleTap();
						}
						return super.onDoubleTap(e);
					}

					@Override
					public boolean onDoubleTapEvent(MotionEvent e) {// 双击的按下跟抬起各触发一次
						return super.onDoubleTapEvent(e);
					}

					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {// 单击确认，即很快的按下并抬起，但并不连续点击第二下
						return super.onSingleTapConfirmed(e);
					}
				});
	}

	public void setVideoPath(String path) {
		setVideoURI(Uri.parse(path));
	}

	public void setVideoURI(Uri uri) {
		setVideoURI(uri, null);
	}

	/**
	 * @hide
	 */
	public void setVideoURI(Uri uri, Map<String, String> headers) {
		// VIEW_SUBTITLE
		if (mSubtitle != null) {
			mSubtitle.stop();
			mSubtitle = null;
		}

		mUri = uri;
		mHeaders = headers;
		mSeekWhenPrepared = 0;
		openVideo();
		requestLayout();
		invalidate();
	}

	/**
	 * Adds an external subtitle source file (from the provided input stream.)
	 * 
	 * Note that a single external subtitle source may contain multiple or no
	 * supported tracks in it. If the source contained at least one track in it,
	 * one will receive an {@link MediaPlayer#MEDIA_INFO_METADATA_UPDATE} info
	 * message. Otherwise, if reading the source takes excessive time, one will
	 * receive a {@link MediaPlayer#MEDIA_INFO_SUBTITLE_TIMED_OUT} message. If
	 * the source contained no supported track (including an empty source file
	 * or null input stream), one will receive a
	 * {@link MediaPlayer#MEDIA_INFO_UNSUPPORTED_SUBTITLE} message. One can find
	 * the total number of available tracks using
	 * {@link MediaPlayer#getTrackInfo()} to see what additional tracks become
	 * available after this method call.
	 * 
	 * @param is
	 *            input stream containing the subtitle data. It will be closed
	 *            by the media framework.
	 * @param format
	 *            the format of the subtitle track(s). Must contain at least the
	 *            mime type ({@link MediaFormat#KEY_MIME}) and the language (
	 *            {@link MediaFormat#KEY_LANGUAGE}) of the file. If the file
	 *            itself contains the language information, specify "und" for
	 *            the language.
	 */
	public void addSubtitleSource(InputStream is, MediaFormat format) {
		if (mMediaPlayer == null) {
			mPendingSubtitleTracks.add(Pair.create(is, format));
		} else {
			try {
				mMediaPlayer.addSubtitleSource(is, format);
			} catch (IllegalStateException e) {
				mInfoListener.onInfo(mMediaPlayer,
						MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE, 0);
			}
		}
	}

	private Vector<Pair<InputStream, MediaFormat>> mPendingSubtitleTracks;

	public void stopPlayback() {
		// VIEW_SUBTITLE
		if (mSubtitle != null) {
			mSubtitle.stop();
			mSubtitle = null;
		}

		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
	}

	private void openVideo() {
		if (mUri == null || mSurfaceHolder == null) {
			// not ready for playback just yet, will try again later
			return;
		}
		// Tell the music playback service to pause
		// TODO: these constants need to be published somewhere in the
		// framework.
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		mContext.sendBroadcast(i);

		// we shouldn't clear the target state, because somebody might have
		// called start() previously
		release(false);
		try {
			mMediaPlayer = new MediaPlayer();
			// TODO: create SubtitleController in MediaPlayer, but we need
			// a context for the subtitle renderers
			final Context context = getContext();
			final android.media.SubtitleController controller = new android.media.SubtitleController(
					context, mMediaPlayer.getMediaTimeProvider(), mMediaPlayer);
			controller.registerRenderer(new android.media.WebVttRenderer(
					context));
			mMediaPlayer.setSubtitleAnchor(controller, this);

			if (mAudioSession != 0) {
				mMediaPlayer.setAudioSessionId(mAudioSession);
			} else {
				mAudioSession = mMediaPlayer.getAudioSessionId();
			}
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnInfoListener(mInfoListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mCurrentBufferPercentage = 0;
			mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			// VIEW_SUBTITLE
			mSubtitle = new android.widget.Subtitle();
			mSubtitle.setOnSubtitleListener(mSubtitleListener);
			if (mSubtitleOn == true) {
				String scheme = mUri.getScheme();
				if (scheme != null && scheme.equals("file")) {
					mSubtitle.start(mMediaPlayer, mUri.getPath(),
							mSubtitleFontSize, mSubtitle.DISPLAY_VIEW);
				} else {
					Cursor cursor = mContentResolver.query(mUri,
							new String[] { "_data" }, null, null, null);
					if (cursor != null) {
						if (cursor.moveToFirst()) {
							mSubtitle.start(mMediaPlayer, cursor.getString(0),
									mSubtitleFontSize, mSubtitle.DISPLAY_VIEW);
						}
						cursor.close();
					} else {
						Log.e(TAG, "Can't found a file path.");
					}
				}
			}

			for (Pair<InputStream, MediaFormat> pending : mPendingSubtitleTracks) {
				try {
					mMediaPlayer.addSubtitleSource(pending.first,
							pending.second);
				} catch (IllegalStateException e) {
					mInfoListener.onInfo(mMediaPlayer,
							MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE, 0);
				}
			}

			// we don't set the target state here either, but preserve the
			// target state that was there before.
			mCurrentState = STATE_PREPARING;
			attachMediaController();
		} catch (IOException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} finally {
			mPendingSubtitleTracks.clear();
		}
	}

	public void setMediaController(MediaController controller) {
		if (mMediaController != null) {
			mMediaController.hide();
			// VIEW_SUBTITLE
			mMediaController.hideSubtitle();
		}
		mMediaController = controller;
		attachMediaController();
	}

	private void attachMediaController() {
		if (mMediaController != null) {
			if (mUri != null) {
				String scheme = mUri.getScheme();
				if (scheme != null
						&& (scheme.equals("rtsp") || scheme.equals("http"))) {
					mMediaController.specifyStreamingMedia();
				}
			}
			if (mMediaPlayer != null) {
				mMediaController.setMediaPlayer(this);
				View anchorView = this.getParent() instanceof View ? (View) this
						.getParent() : this;
				mMediaController.setAnchorView(anchorView);
				mMediaController.setEnabled(isInPlaybackState());

				// VIEW_SUBTITLE
				if ((mSubtitle != null)
						&& (mSubtitle.getSubtitleType() != android.widget.Subtitle.TYPE_NONE)) {
					mMediaController.setSubtitleView(mSubtitleFontSize);
				}
			}
		}
	}

	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				requestLayout();
			}
		}
	};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			mCurrentState = STATE_PREPARED;

			// Get the capabilities of the player for this stream
			android.media.Metadata data = mp.getMetadata(
					MediaPlayer.METADATA_ALL,
					MediaPlayer.BYPASS_METADATA_FILTER);

			if (data != null) {
				mCanPause = !data.has(android.media.Metadata.PAUSE_AVAILABLE)
						|| data.getBoolean(android.media.Metadata.PAUSE_AVAILABLE);
				mCanSeekBack = !data
						.has(android.media.Metadata.SEEK_BACKWARD_AVAILABLE)
						|| data.getBoolean(android.media.Metadata.SEEK_BACKWARD_AVAILABLE);
				mCanSeekForward = !data
						.has(android.media.Metadata.SEEK_FORWARD_AVAILABLE)
						|| data.getBoolean(android.media.Metadata.SEEK_FORWARD_AVAILABLE);
			} else {
				mCanPause = mCanSeekBack = mCanSeekForward = true;
			}

			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
			if (mMediaController != null) {
				mMediaController.setEnabled(true);
			}
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();

			int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be
													// changed after seekTo()
													// call
			if (seekToPosition != 0) {
				seekTo(seekToPosition);

				// VIEW_SUBTITLE
				if (mMediaController != null) {
					mMediaController.clearSubtitle();
				}
			}
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				// Log.i("@@@@", "video size: " + mVideoWidth +"/"+
				// mVideoHeight);
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				if (mSurfaceWidth == mVideoWidth
						&& mSurfaceHeight == mVideoHeight) {
					// We didn't actually change the size (it was already at the
					// size
					// we need), so we won't get a "surface changed" callback,
					// so
					// start the video here instead of in the callback.
					if (mTargetState == STATE_PLAYING) {
						start();
						if (mMediaController != null) {
							mMediaController.show();
						}
					} else if (!isPlaying()
							&& (seekToPosition != 0 || getCurrentPosition() > 0)) {
						if (mMediaController != null) {
							// Show the media controls when we're paused into a
							// video and make 'em stick.
							mMediaController.show(0);
						}
					}
				}
			} else {
				// We don't know the video size yet, but should start anyway.
				// The video size might be reported to us later.
				if (mTargetState == STATE_PLAYING) {
					start();
				}
			}
		}
	};

	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			mTargetState = STATE_PLAYBACK_COMPLETED;
			// VIEW_SUBTITLE
			if (mSubtitle != null) {
				mSubtitle.stop();
				mSubtitle = null;
			}
			if (mMediaController != null) {
				mMediaController.hide();
				mMediaController.hideSubtitle();
			}
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
		}
	};

	private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
		public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
			if (mOnInfoListener != null) {
				mOnInfoListener.onInfo(mp, arg1, arg2);
			}
			return true;
		}
	};

	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			Log.d(TAG, "Error: " + framework_err + "," + impl_err);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			// VIEW_SUBTITLE
			if (mSubtitle != null) {
				mSubtitle.stop();
				mSubtitle = null;
			}
			if (mMediaController != null) {
				mMediaController.hide();
				// VIEW_SUBTITLE
				mMediaController.hideSubtitle();
			}

			/* If an error handler has been supplied, use it and finish. */
			if (mOnErrorListener != null) {
				if (mOnErrorListener.onError(mMediaPlayer, framework_err,
						impl_err)) {
					return true;
				}
			}

			/*
			 * Otherwise, pop up an error dialog so the user knows that
			 * something bad has happened. Only try and pop up the dialog if
			 * we're attached to a window. When we're going away and no longer
			 * have a window, don't bother showing the user an error.
			 */
			if (getWindowToken() != null) {
				Resources r = mContext.getResources();
				int messageId;

				if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
					messageId = com.android.internal.R.string.VideoView_error_text_invalid_progressive_playback;
				} else {
					messageId = com.android.internal.R.string.VideoView_error_text_unknown;
				}

				new AlertDialog.Builder(mContext)
						.setMessage(messageId)
						.setPositiveButton(
								com.android.internal.R.string.VideoView_error_button,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										/*
										 * If we get here, there is no onError
										 * listener, so at least inform them
										 * that the video is over.
										 */
										if (mOnCompletionListener != null) {
											mOnCompletionListener
													.onCompletion(mMediaPlayer);
										}
									}
								}).setCancelable(false).show();
			}
			return true;
		}
	};
	/*
	 * private MediaPlayer.OnInfoListener mInfoListener = new
	 * MediaPlayer.OnInfoListener() { public boolean onInfo(MediaPlayer mp, int
	 * what, int extra) { if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START ||
	 * what == MediaPlayer.MEDIA_INFO_BUFFERING_END) { if (mOnInfoListener !=
	 * null) { if (mOnInfoListener.onInfo(mp, what, extra)) { return true; } } }
	 * return false; } };
	 */
	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mCurrentBufferPercentage = percent;
		}
	};

	// VIEW_SUBTITLE
	private android.widget.Subtitle.OnSubtitleListener mSubtitleListener =

	new android.widget.Subtitle.OnSubtitleListener() {

		public void onSubtitle(int index, String subtitle) {
			if (mOnSubtitleListener != null) {
				mOnSubtitleListener.onSubtitle(index, subtitle);
			} else if (mMediaController != null) {
				mMediaController.setSubtitle(index, subtitle);
			}
		}

		public void onPGSCaption(int OffsetX, int OffsetY, int Srcwidth,
				int SrcHeight, int Dstwidth, int DstHeight, int StreamSize,
				int[] StreamData) {
			if (mOnSubtitleListener != null) {
				mOnSubtitleListener.onPGSCaption(OffsetX, OffsetY, Srcwidth,
						SrcHeight, Dstwidth, DstHeight, StreamSize, StreamData);
			} else if (mMediaController != null) {
				mMediaController.setPGSCaption(OffsetX, OffsetY, Srcwidth,
						SrcHeight, Dstwidth, DstHeight, StreamSize, StreamData);
			}
		}
	};

	/**
	 * Register a callback to be invoked when the media file is loaded and ready
	 * to go.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	/**
	 * Register a callback to be invoked when the end of a media file has been
	 * reached during playback.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnCompletionListener(OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	/**
	 * Register a callback to be invoked when an error occurs during playback or
	 * setup. If no listener is specified, or if the listener returned false,
	 * VideoView will inform the user of any errors.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;
	}

	/**
	 * Register a callback to be invoked when an informational event occurs
	 * during playback or setup.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnInfoListener(OnInfoListener l) {
		mOnInfoListener = l;
	}

	// VIEW_SUBTITLE
	/**
	 * Register a callback to be invoked when updated a subtitle
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnSubtitleListener(
			android.widget.Subtitle.OnSubtitleListener l) {
		mOnSubtitleListener = l;
	}

	/*
	 * release the media player in any state
	 */
	private void release(boolean cleartargetstate) {
		// VIEW_SUBTITLE
		if (mSubtitle != null) {
			mSubtitle.stop();
			mSubtitle = null;
		}

		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mPendingSubtitleTracks.clear();
			mCurrentState = STATE_IDLE;
			if (cleartargetstate) {
				mTargetState = STATE_IDLE;
			}
		}
	}

	// Anter
	/** 滑动监听回调接口 */
	public interface OnSlidingListener {
		// 左滑
		void onSlidingLeft(float distanceX);

		// 右滑
		void onSlidingRight(float distanceX);

		// 双击VideoView
		void onVideoViewDoubleTap();
	}

	private OnSlidingListener onSlidingListener;
	private GestureDetector mGestureDetector;

	public void setOnSlidingListener(OnSlidingListener onSlidingListener) {
		this.onSlidingListener = onSlidingListener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}

		// Anter
		// return true;
		return mGestureDetector.onTouchEvent(ev);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
				&& keyCode != KeyEvent.KEYCODE_VOLUME_UP
				&& keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
				&& keyCode != KeyEvent.KEYCODE_VOLUME_MUTE
				&& keyCode != KeyEvent.KEYCODE_MENU
				&& keyCode != KeyEvent.KEYCODE_CALL
				&& keyCode != KeyEvent.KEYCODE_ENDCALL;

		/*
		 * // Test Media Info if (keyCode == KeyEvent.KEYCODE_MENU) { Log.d(TAG,
		 * "***************************************************"); Log.d(TAG,
		 * "    TEST Media Info"); Log.d(TAG,
		 * "***************************************************");
		 * 
		 * if (mMediaPlayer != null) { String str_val = null; int int_val = 0;
		 * 
		 * str_val =
		 * mMediaPlayer.getStringParameter(android.media.MediaParameterKeys.
		 * KEY_PARAMETER_GET_AUDIO_CODECTYPE); Log.d(TAG, "Audio Codec Type: " +
		 * str_val);
		 * 
		 * str_val =
		 * mMediaPlayer.getStringParameter(android.media.MediaParameterKeys.
		 * KEY_PARAMETER_GET_VIDEO_CODECTYPE); Log.d(TAG, "Video Codec Type: " +
		 * str_val);
		 * 
		 * int_val =
		 * mMediaPlayer.getIntParameter(android.media.MediaParameterKeys.
		 * KEY_PARAMETER_GET_VIDEO_FRAMERATE); Log.d(TAG, "Video Framerate: " +
		 * int_val/1000.0);
		 * 
		 * int_val =
		 * mMediaPlayer.getIntParameter(android.media.MediaParameterKeys.
		 * KEY_PARAMETER_GET_AUDIO_SAMPLERATE); Log.d(TAG, "Audio Samplerate: "
		 * + int_val);
		 * 
		 * int_val =
		 * mMediaPlayer.getIntParameter(android.media.MediaParameterKeys
		 * .KEY_PARAMETER_GET_BITRATE ); Log.d(TAG, "Bitrate: " + int_val + " ("
		 * + int_val/1000.0 + " kbps" + ")"); } }
		 */

		if (isInPlaybackState() && isKeyCodeSupported
				&& mMediaController != null) {
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				} else {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
				if (!mMediaPlayer.isPlaying()) {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				}
				return true;
				// VIEW_SUBTITLE
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				if (mMediaPlayer.isPlaying() && mSubtitle != null) {
					mSubtitle.setTimeShiftPTS(500);
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				if (mMediaPlayer.isPlaying() && mSubtitle != null) {
					mSubtitle.setTimeShiftPTS(-500);
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MENU) {
				/*
				 * // Test Multi-audio selection boolean ret =
				 * changeAudioTrack(); if (ret != true) Log.d(TAG,
				 * "Can't change the audio track. ret = " + ret);
				 */
			} else {
				toggleMediaControlsVisiblity();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private void toggleMediaControlsVisiblity() {
		if (mMediaController.isShowing()) {
			mMediaController.hide();
		} else {
			mMediaController.show();
		}
	}

	@Override
	public void start() {
		if (isInPlaybackState()) {
			mMediaPlayer.start();
			mCurrentState = STATE_PLAYING;
		}
		mTargetState = STATE_PLAYING;

		// VIEW_SUBTITLE
		if (mMediaController != null) {
			mMediaController.show();
		}

	}

	@Override
	public void pause() {
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				mCurrentState = STATE_PAUSED;
			}
		}
		mTargetState = STATE_PAUSED;
	}

	public void suspend() {
		release(false);
	}

	public void resume() {
		openVideo();
	}

	@Override
	public int getDuration() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getDuration();
		}

		return -1;
	}

	@Override
	public int getCurrentPosition() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	@Override
	public void seekTo(int msec) {
		if (isInPlaybackState()) {
			String scheme = mUri.getScheme();
			if (scheme != null && scheme.equals("rtsp")) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.seekTo(msec);
					mSeekWhenPrepared = 0;
					return;
				}
			} else {
				mMediaPlayer.seekTo(msec);
			}
			// VIEW_SUBTITLE
			int cur = mMediaPlayer.getCurrentPosition();
			if (mSubtitle != null) {
				mSubtitle.seek(cur);
			}

			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	@Override
	public boolean isPlaying() {
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	@Override
	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	private boolean isInPlaybackState() {
		return (mMediaPlayer != null && mCurrentState != STATE_ERROR
				&& mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

	@Override
	public boolean canPause() {
		return mCanPause;
	}

	@Override
	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	@Override
	public boolean canSeekForward() {
		return mCanSeekForward;
	}

	// VIEW_SUBTITLE
	/**
	 * @hide
	 */
	public void setSubtitle(boolean onoff, int fontsize) {
		if (isInPlaybackState()
				&& mSubtitle != null
				&& mSubtitle.getSubtitleType() != android.widget.Subtitle.TYPE_NONE) {
			if (mMediaController != null) {
				if (mSubtitleOn != onoff) {
					Log.d(TAG, "mMediaController.setSubtitleOnoff " + onoff);
					mMediaController.setSubtitleOnoff(onoff);
				}

				if (mSubtitleFontSize != fontsize) {
					Log.d(TAG, "mMediaController.setSubtitleFontSize "
							+ fontsize);
					mMediaController.setSubtitleFontSize(fontsize);
				}
			}
		}

		mSubtitleOn = onoff;
		mSubtitleFontSize = fontsize;
	}

	/**
	 * @hide
	 */
	public int getSubtitleType() {
		if (isInPlaybackState() == false || mSubtitle == null) {
			return android.widget.Subtitle.TYPE_NONE;
		}

		return mSubtitle.getSubtitleType();
	}

	/**
	 * @hide
	 */
	public void setSubtitlePosition(int position) {
		if (isInPlaybackState()
				&& mSubtitle != null
				&& mSubtitle.getSubtitleType() != android.widget.Subtitle.TYPE_NONE) {
			if (mMediaController != null) {
				mMediaController.setSubtitlePosition(position);
			}
		}
	}

	/**
	 * @hide
	 */
	public void changeSubtitleClass(int classcount, int[] classindex) {
		if (isInPlaybackState()
				&& mSubtitle != null
				&& mSubtitle.getSubtitleType() != android.widget.Subtitle.TYPE_NONE) {
			mSubtitle.changeSubtitle(classcount, classindex);
		}

		return;
	}

	/**
	 * @hide
	 */
	public int setSubtitleTimeShiftPTS(int msec) {
		if (isInPlaybackState()
				&& mSubtitle != null
				&& mSubtitle.getSubtitleType() != android.widget.Subtitle.TYPE_NONE) {
			return mSubtitle.setTimeShiftPTS(msec);
		}

		return 0;
	}

	@Override
	public int getAudioSessionId() {
		if (mAudioSession == 0) {
			MediaPlayer foo = new MediaPlayer();
			mAudioSession = foo.getAudioSessionId();
			foo.release();
		}
		return mAudioSession;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		if (mSubtitleWidget != null) {
			mSubtitleWidget.onAttachedToWindow();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		if (mSubtitleWidget != null) {
			mSubtitleWidget.onDetachedFromWindow();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (mSubtitleWidget != null) {
			measureAndLayoutSubtitleWidget();
		}
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);

		if (mSubtitleWidget != null) {
			final int saveCount = canvas.save();
			canvas.translate(getPaddingLeft(), getPaddingTop());
			mSubtitleWidget.draw(canvas);
			canvas.restoreToCount(saveCount);
		}
	}

	/**
	 * Forces a measurement and layout pass for all overlaid views.
	 * 
	 */
	private void measureAndLayoutSubtitleWidget() {
		final int width = getWidth() - getPaddingLeft() - getPaddingRight();
		final int height = getHeight() - getPaddingTop() - getPaddingBottom();

		mSubtitleWidget.setSize(width, height);
	}

	/** @hide */
	@Override
	public void setSubtitleWidget(
			android.media.SubtitleTrack.RenderingWidget subtitleWidget) {
		if (mSubtitleWidget == subtitleWidget) {
			return;
		}

		final boolean attachedToWindow = isAttachedToWindow();
		if (mSubtitleWidget != null) {
			if (attachedToWindow) {
				mSubtitleWidget.onDetachedFromWindow();
			}

			mSubtitleWidget.setOnChangedListener(null);
		}

		mSubtitleWidget = subtitleWidget;

		if (subtitleWidget != null) {
			if (mSubtitlesChangedListener == null) {
				mSubtitlesChangedListener = new android.media.SubtitleTrack.RenderingWidget.OnChangedListener() {
					@Override
					public void onChanged(
							android.media.SubtitleTrack.RenderingWidget renderingWidget) {
						invalidate();
					}
				};
			}

			setWillNotDraw(false);
			subtitleWidget.setOnChangedListener(mSubtitlesChangedListener);

			if (attachedToWindow) {
				subtitleWidget.onAttachedToWindow();
				requestLayout();
			}
		} else {
			setWillNotDraw(true);
		}

		invalidate();
	}

	/** @hide */
	@Override
	public Looper getSubtitleLooper() {
		return Looper.getMainLooper();
	}

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			// Anter
			if (surfaceHolderCallback != null) {
				surfaceHolderCallback.surfaceChanged(holder, format, w, h);
			}

			mSurfaceWidth = w;
			mSurfaceHeight = h;
			boolean isValidState = (mTargetState == STATE_PLAYING);
			boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			if (mMediaPlayer != null && isValidState && hasValidSize) {
				if (mSeekWhenPrepared != 0) {
					seekTo(mSeekWhenPrepared);
					// VIEW_SUBTITLE
					if (mMediaController != null) {
						mMediaController.clearSubtitle();
					}
				}
				start();
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			// Anter
			if (surfaceHolderCallback != null) {
				surfaceHolderCallback.surfaceCreated(holder);
			}

			mSurfaceHolder = holder;
			openVideo();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// Anter
			if (surfaceHolderCallback != null) {
				surfaceHolderCallback.surfaceDestroyed(holder);
			}

			// after we return from this we can't use the surface any more
			mSurfaceHolder = null;
			if (mMediaController != null) {
				mMediaController.hide();
				// VIEW_SUBTITLE
				mMediaController.hideSubtitle();
			}
			release(true);
		}
	};

	// Anter

	public interface SurfaceHolderCallback {
		void surfaceCreated(SurfaceHolder holder);

		void surfaceChanged(SurfaceHolder holder, int format, int w, int h);

		void surfaceDestroyed(SurfaceHolder holder);
	}

	private SurfaceHolderCallback surfaceHolderCallback;

	public void setSurfaceHolderCallback(SurfaceHolderCallback callback) {
		this.surfaceHolderCallback = callback;
	}

	private float mCurrentVolumeRatio = 1.0f;// 音量大小比例（MediaPlayer的音量控制范围0.0——1.0）
	private float lowest_ratio = 0.0f;// 音量最低比例
	private float highest_ratio = 1.0f;// 音量最高比例
	private float volume_step_sub = 0.1f;// 每次音量减小比例（尽量不要太小，否则设置音量过于频繁）
	private float volume_step_plus = 0.1f;// 每次音量增大比例（尽量不要太小，否则设置音量过于频繁）
	private int fade_down_delayMillis = 150;// 每次音量减小时间间隔
	private int fade_up_delayMillis = 150;// 每次音量增大时间间隔
	private VolumeHandler mVolumeHandler;

	private static final int MSG_FADEUP = 0;// 声音渐变增大
	private static final int MSG_FADEDOWN = 1;// 声音渐变降低

	private static class VolumeHandler extends Handler {
		private static WeakReference<VideoView> mReference;

		public VolumeHandler(VideoView videoView) {
			mReference = new WeakReference<VideoView>(videoView);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mReference.get() == null) {
				return;
			}

			switch (msg.what) {
			case MSG_FADEUP:// 声音渐变上升
				mReference.get().mCurrentVolumeRatio += mReference.get().volume_step_plus;
				if (mReference.get().mCurrentVolumeRatio < mReference.get().highest_ratio) {
					mReference.get().mVolumeHandler.sendEmptyMessageDelayed(
							MSG_FADEUP, mReference.get().fade_up_delayMillis);
				} else {
					mReference.get().mCurrentVolumeRatio = mReference.get().highest_ratio;
				}
				mReference
						.get()
						.setVideoVolume(
								AppUtil.formatOneDecimal(mReference.get().mCurrentVolumeRatio));
				break;
			case MSG_FADEDOWN:// 声音渐变降低
				mReference.get().mCurrentVolumeRatio -= mReference.get().volume_step_sub;
				if (mReference.get().mCurrentVolumeRatio > mReference.get().lowest_ratio) {
					mReference.get().mVolumeHandler.sendEmptyMessageDelayed(
							MSG_FADEDOWN,
							mReference.get().fade_down_delayMillis);
				} else {
					mReference.get().mCurrentVolumeRatio = mReference.get().lowest_ratio;
				}
				mReference
						.get()
						.setVideoVolume(
								AppUtil.formatOneDecimal(mReference.get().mCurrentVolumeRatio));
				break;
			default:
				break;
			}
		}
	}

	/** 设置视频音量，范围（0.0——1.0） */
	private void setVideoVolume(float volume) {
		if (mMediaPlayer != null) {
			mMediaPlayer.setVolume(volume, volume);
			Logger.logI("VideoView-----------------------设置音量比例 = " + volume);
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

	/** 设置渐变最低音量 */
	public void setLowestVolumeRatio(float lowest_ratio) {
		this.lowest_ratio = lowest_ratio;
	}

	/** 获取当前渐变最低音量 */
	public float getLowestVolumeRatio() {
		return lowest_ratio;
	}

	/** 删除队列中的消息 */
	public void removeMessages(int what) {
		mVolumeHandler.removeMessages(what);
	}

	/** 删除队列中所有的消息 */
	public void removeAllMessages() {
		mVolumeHandler.removeCallbacksAndMessages(null);
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
		removeAllMessages();
		sendEmptyMessage(MSG_FADEUP);
	}

	/** 从0开始音量渐变上升 */
	public void fadeUpVolumeFromZero() {
		removeAllMessages();
		setVideoVolume(mCurrentVolumeRatio = 0f);
		sendEmptyMessage(MSG_FADEUP);
	}

	/** 开始渐变调低音量 */
	public void fadeDownVolume() {
		removeAllMessages();
		sendEmptyMessage(MSG_FADEDOWN);
	}

	/** 关闭媒体声音 */
	public void muteVolume() {
		removeAllMessages();
		setVideoVolume(mCurrentVolumeRatio = 0f);
	}
}