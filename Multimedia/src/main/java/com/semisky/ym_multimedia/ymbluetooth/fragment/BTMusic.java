package com.semisky.ym_multimedia.ymbluetooth.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.semisky.ym_multimedia.BaseFragment;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.broadcom.bt.avrcp.BluetoothAvrcpController;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventMusic;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.func.ControlManager;
import com.semisky.ym_multimedia.ymbluetooth.func.FuncBTOperate;
import com.ypy.eventbus.EventBus;

/**
 * Created by luoyin on 16/9/26.
 */
public class BTMusic extends BaseFragment {
    private final String TAG = "BTMusic";
    private Context mContext;
    private View mRootView;
    private TextView mTitleTV,mAlbumTV,mArtistTV;
    private ImageView mPrevIV;
    private ImageView mNextIV;
    private ImageView mPlayIV;
    private ImageView mPauseIV;

    private ControlManager mControlManager;

    private FuncBTOperate mFuncBTOperate;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        BtLogger.e(TAG, "BTMusic-onHiddenChanged-=" + hidden);
        if(!hidden && mFuncBTOperate != null){
            //设置标题
            mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.nl_bt_title, R.string.ym_bt_music, -1);
//            //初始化蓝牙音乐相关数据
//            mFuncBTOperate.notifyBTService(6);
//            //播放音乐
//            mFuncBTOperate.startPlayerForLoss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BtLogger.e(TAG, "BTMusic-onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        BtLogger.e(TAG, "BTMusic-onPause");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        BtLogger.e(TAG, "BTMusic-onCreate");
        mContext = getActivity().getApplicationContext();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(EventMusic eventMusic) {
        BtLogger.d(TAG, "onEventMainThread－getMethod = " + eventMusic.getMethod());
        switch (eventMusic.getMethod()){
            case 0:
                updateUI(eventMusic);
                break;
            case 1:
                syncPlayStatus(eventMusic);
                break;
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        BtLogger.e(TAG, "BTMusic-onCreateView");
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.ym_bt_func_music, null);
        }
        initViews();
        initData();
        return mRootView;
    }


    private void initViews() {
        mTitleTV = (TextView) mRootView.findViewById(R.id.tv_music_title);
        mAlbumTV = (TextView) mRootView.findViewById(R.id.tv_music_album);
        mArtistTV = (TextView) mRootView.findViewById(R.id.tv_music_artist);

        mPrevIV = (ImageView) mRootView.findViewById(R.id.iv_bt_music_prev);
        mPrevIV.setOnClickListener(mNaviOCListener);
        mNextIV = (ImageView) mRootView.findViewById(R.id.iv_bt_music_next);
        mNextIV.setOnClickListener(mNaviOCListener);
        mPlayIV = (ImageView) mRootView.findViewById(R.id.iv_bt_music_play);
        mPlayIV.setOnClickListener(mNaviOCListener);
        mPauseIV = (ImageView) mRootView.findViewById(R.id.iv_bt_music_pause);
        mPauseIV.setOnClickListener(mNaviOCListener);
    }

    private void initData() {
        mControlManager = ControlManager.getInstance(mContext, null);
        mFuncBTOperate = FuncBTOperate.getInstance(mContext);
        //设置标题
        mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.nl_bt_title, R.string.ym_bt_music, -1);
        //初始化蓝牙音乐相关数据
        mFuncBTOperate.notifyBTService(6);
    }

    private boolean mIsRepeat = false;
    private Handler mRepeatHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    mIsRepeat = false;
                    break;
            }
        }
    };

    private void avoidRepeat(){
        mIsRepeat = true;
        mRepeatHandler.removeMessages(0);
        mRepeatHandler.sendEmptyMessageDelayed(0, 500);
    }

    private View.OnClickListener mNaviOCListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //未连接蓝牙则不响应
            if(mControlManager != null
                    && mControlManager.getConnectionState(DataStatic.mCurrentBT) != BluetoothHfDevice.STATE_CONNECTED){
                return;
            }
            if(mIsRepeat){
                return;
            }
            avoidRepeat();
            switch (v.getId()){
                case R.id.iv_bt_music_prev:
//                    Toast.makeText(getActivity(), "上一首", Toast.LENGTH_SHORT).show();
                    mFuncBTOperate.notifyBTService(10);
                    break;
                case R.id.iv_bt_music_next:
//                    Toast.makeText(getActivity(), "下一首", Toast.LENGTH_SHORT).show();
                    mFuncBTOperate.notifyBTService(9);
                    break;
                case R.id.iv_bt_music_play:
                    mFuncBTOperate.notifyBTService(7);
//                    Toast.makeText(getActivity(), "播放", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.iv_bt_music_pause:
                    mFuncBTOperate.notifyBTService(8);
//                    Toast.makeText(getActivity(), "暂停", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void updateUI(EventMusic eventMusic){
        mTitleTV.setText(eventMusic.getTitle());
        mAlbumTV.setText(eventMusic.getAlbum());
        mArtistTV.setText(eventMusic.getArtist());
    }

    private void syncPlayStatus(EventMusic eventMusic){
        BtLogger.e(TAG, "syncPlayStatus-="+eventMusic.getPlayStatus());
        switch (eventMusic.getPlayStatus()){
            case BluetoothAvrcpController.PLAY_STATUS_PLAYING:
                BtLogger.e(TAG, "PLAY_STATUS_PLAYING");
                mPlayIV.setVisibility(View.GONE);
                mPauseIV.setVisibility(View.VISIBLE);
                break;
            case BluetoothAvrcpController.PLAY_STATUS_PAUSED:
            case BluetoothAvrcpController.PLAY_STATUS_STOPPED:
                BtLogger.e(TAG, "PLAY_STATUS_STOPPED");
                mPlayIV.setVisibility(View.VISIBLE);
                mPauseIV.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void initModel() {}

    @Override
    public void initLeftViews() {}

    @Override
    public void initRightViews() {}

    @Override
    public void initMiddleViews() {}

    @Override
    public int getSystemUITitleResId() {
        return R.string.ym_bt_music;
    }

    @Override
    public void setListener() {}

    @Override
    public void register() {}

    @Override
    public void unregister() {}
}
