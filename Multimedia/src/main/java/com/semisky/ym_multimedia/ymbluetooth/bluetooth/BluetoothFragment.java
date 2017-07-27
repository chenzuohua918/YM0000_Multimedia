package com.semisky.ym_multimedia.ymbluetooth.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.*;//导包
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;

import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.semisky.ym_multimedia.BaseFragment;
import com.semisky.ym_multimedia.common.controller.FragmentSwitchController;
import com.semisky.ym_multimedia.common.utils.CommonConstants;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager;
import com.semisky.ym_multimedia.ymbluetooth.fragment.BTDevices;
import com.semisky.ym_multimedia.ymbluetooth.fragment.BTSettings;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;
//import android.view.BottomBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.broadcom.bt.avrcp.BluetoothAvrcpController;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventMain;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;
import com.semisky.ym_multimedia.ymbluetooth.fragment.BTContacts;
import com.semisky.ym_multimedia.ymbluetooth.fragment.BTDial;
import com.semisky.ym_multimedia.ymbluetooth.fragment.BTMusic;
import com.semisky.ym_multimedia.ymbluetooth.fragment.BTCallLog;
import com.semisky.ym_multimedia.ymbluetooth.func.ControlManager;
import com.semisky.ym_multimedia.ymbluetooth.func.FuncBTOperate;
import com.semisky.ym_multimedia.ymbluetooth.widget.MutilRadioGroup;
import com.ypy.eventbus.EventBus;
import com.semisky.ym_multimedia.R;

import java.lang.reflect.Field;

public class BluetoothFragment extends BaseFragment implements
		View.OnClickListener, UsbStateManager.OnUsbStateChangeListener {
	private final String TAG = "BluetoothFragment";
	private Context mContext;
	// 菜单
	private MutilRadioGroup mMenuMain;
	private RadioButton mMenuDail;
	private RadioButton mMenuRecord;
	private RadioButton mMenuContacts;
	private RadioButton mMenuMusic;
	private RadioButton mMenuPair;
	private RadioButton mMenuSettings;
	// fragment相关
	// private FragmentManager mFragmentManager;
	private Fragment mFrgDail;
	private Fragment mFrgCallLog;
	private Fragment mFrgContacts;
	private Fragment mFrgMusic;
	private Fragment mFrgPair;
	private Fragment mFrgSettings;
	private Fragment mFrgCurrent;

	private TextView mDateTV;
	private TextView mTitleTV;
	// private TextView mBTInfoTV;

	private FuncBTOperate mFuncBTOperate;
	private ControlManager mControlManager;
	// private View mBackV;

	// 系统底部
	// private BottomBar mBottomBar;
	private String mBottomStr = "";
	private int mStatus = 0;
	private boolean mIsSaveInstanceState = false;
	private boolean mSyncBottoming = false;
	private View mRootView;
	//右上角多媒体手机切换图标布局
	private View mTopRightV;

	//主界面入口方式
	private int mEntyMode = 0;

	public void setmEntyMode(int mEntyMode) {
		this.mEntyMode = mEntyMode;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		BtLogger.e(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// super.onCreate(savedInstanceState);
		// if (mRootView == null) {
		// mRootView = inflater.inflate(R.layout.ym_bt_main, container, false);
		// }
		mRootView = inflater.inflate(R.layout.ym_bt_main, container, false);
		// 这个为解决The specified child already has a parent崩溃
		// ViewGroup p = (ViewGroup) mRootView.getParent();
		// if (p != null) {
		// p.removeAllViewsInLayout();
		// }
		mContext = getActivity().getApplicationContext();
		createView(inflater, container, savedInstanceState);
		initViews();
		initData();
//		if(getArguments() != null){
//			BtLogger.e(TAG, "bluetooth---onCreateView22222 "
//					+ getArguments().getInt(CommonConstants.KEY_GOTO_BT));
//			mEntyMode = getArguments().getInt(CommonConstants.KEY_GOTO_BT);
//		}
		return mRootView;
	}

	@Override
	public void onDestroy() {
		BtLogger.e(TAG, "onDestroy");
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onStart() {
		super.onStart();
		BtLogger.e(TAG, "onStart");
	}

	// @Override
	// protected void onNewIntent(Intent intent) {
	// super.onNewIntent(intent);
	// BtLogger.e(TAG, "onNewIntent");
	// setIntent(intent);
	// }

	@Override
	public void onResume() {
		super.onResume();
		// 注册相关
		register();
		mIsSaveInstanceState = false;
		DataStatic.sIsBluetoothUI = true;
		BtLogger.e(TAG, "onResume=" + mFrgCurrent);
		//改变标题和刷新数据
		if (mFrgCurrent != null) {
			mFrgCurrent.onHiddenChanged(false);
		}
		// 断开连接后进入更新界面,然后再退回后台连接成功,在进入fragment不匹配菜单
		if (mMenuMain.getCheckedRadioButtonId() == R.id.rb_menu_dail) {
			setMenuEnabled(true);
		}
		// 以下暂时无用,留以后备用（增加菜单匹配，避免从别的应用切换回来时菜单和界面不对应）
		switch (mMenuMain.getCheckedRadioButtonId()) {
			case R.id.rb_menu_dail:
				switchMenu(mFrgDail);
				break;
			case R.id.rb_menu_call_log:
				switchMenu(mFrgCallLog);
				break;
			case R.id.rb_menu_contacts:
				switchMenu(mFrgContacts);
				break;
			case R.id.rb_menu_music:
				switchMenu(mFrgMusic);
				break;
			case R.id.rb_menu_pair:
				switchMenu(mFrgPair);
				break;
			case R.id.rb_menu_settings:
				switchMenu(mFrgSettings);
				break;
		}
		// refreshBottomBarText();
		// 每次返回应用就自动重连
		if (mFuncBTOperate != null) {
			mFuncBTOperate.delayedConnectBTDevice(1000);
		}
		// 如果是从左下角点击进入,这个应该放在onHiddenChanged之后
		// 右上角菜单进入蓝牙音乐
		if (mEntyMode == 2) {
			switchMenu(mFrgMusic);
			// 恢复联系人界面，隐藏搜索键盘等
			hideSearchUI();
		}else if (mEntyMode == 1) {
			// 方控拨号进入拨号界面
			switchMenu(mFrgDail);
			// 恢复联系人界面，隐藏搜索键盘等
			hideSearchUI();
		}else if (mEntyMode == 0) {
			switch (DataStatic.sCurrentIF){
				case 0:
				case 1:
					switchMenu(mFrgDail);
					break;
				case 2:
					switchMenu(mFrgCallLog);
					break;
				case 3:
					switchMenu(mFrgContacts);
					break;
				case 4:
					switchMenu(mFrgMusic);
					break;
				case 5:
					switchMenu(mFrgSettings);
					break;
				case 6:
					switchMenu(mFrgPair);
					break;
			}
			// 恢复联系人界面，隐藏搜索键盘等
			hideSearchUI();
		}
		// 方控响应注册，恢复方控控制
		registerIKeyPressInterface();
		// 只要进入蓝牙就抢焦点
		if (mFuncBTOperate != null){
			mFuncBTOperate.lossRegain(true);
		}
		//显示配对提示弹框
		if (mFuncBTOperate != null && mControlManager != null
				&& mControlManager.getConnectionState(DataStatic.mCurrentBT) != BluetoothHfDevice.STATE_CONNECTED){
			String btSelfName = mControlManager.getBTName();
			mFuncBTOperate.showmBTPairDialog(getActivity(), R.string.ym_bt_pair_tips,
					R.string.ym_bt_pair_name, btSelfName, R.string.ym_bt_pair_password, "0000");
		}
	}

	private void autoPlayMusic() {
		// 蓝牙菜单高亮则恢复蓝牙音乐（必须处于蓝牙界面才播放）
		if (mMenuMusic.isChecked()) {
			boolean isBluetooth = mControlManager.btMusicIsForeground();
			if (isBluetooth) {
				// 在重新进入并恢音乐后才执行播放
				if (mFuncBTOperate != null)
					mFuncBTOperate.startPlayerForLoss();
			}
		}
	}

	@Override
	public void onStop() {
		BtLogger.e(TAG, "onStop");
		DataStatic.sIsBluetoothUI = false;
		mEntyMode = 0;
		// 注销相关
		unregister();
		super.onStop();
	}

	// @Override
	// public void onAttachFragment(Fragment fragment) {
	// super.onAttachFragment(fragment);
	// if(fragment instanceof BTDial){
	// mFrgDail = fragment;
	// }else if(fragment instanceof BTCallLog){
	// mFrgCallLog = fragment;
	// }else if(fragment instanceof BTContacts){
	// mFrgContacts = fragment;
	// }else if(fragment instanceof BTMusic){
	// mFrgMusic = fragment;
	// }else if(fragment instanceof BTDevices){
	// mFrgPair = fragment;
	// }
	// }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// 设置菜单问题，解决中英文切换无变化bug
		mMenuDail.setText(mContext.getResources().getText(R.string.ym_bt_dail));
		mMenuRecord.setText(mContext.getResources().getText(
				R.string.ym_bt_call_log));
		mMenuContacts.setText(mContext.getResources().getText(
				R.string.ym_bt_contacts));
		mMenuMusic.setText(mContext.getResources().getText(R.string.ym_bt_music));
		mMenuPair.setText(mContext.getResources().getText(R.string.ym_bt_list));
	}

	public void onEventMainThread(EventMain eventMain) {
		switch (eventMain.getMethod()) {
		case 0:
//			mMenuEnable = eventMain.isEnabled();
			// 同步菜单状态
			break;
		case 1:
			// 同步左下角信息
			syncBottomBTInfo(eventMain);
			break;
		case 2:
			showSearchUI();
			break;
		case 3:
			highlightMusic();
			break;
		case 4:
			registerIKeyPressInterface();
			break;
		case 5:
			unRegisterIKeyPressInterface();
			break;
		case 6:
			switchMenu(mFrgDail);
			break;
		case 7:
			switchMenu(mFrgCallLog);
			break;
		case 8:
			switchMenu(mFrgContacts);
			break;
		case 9:
			switchMenu(mFrgMusic);
			break;
		case 10:
			switchMenu(mFrgPair);
			break;
		case 11:
			dialNumber(eventMain.getMusicName());
			break;
		case 12:
			// 只有a2dp连接成功才能执行播放音乐
			autoPlayMusic();
			break;
		}
	}

	private void dialNumber(String number) {
		// 号码不为null或空则执行拨号处理
		if (number != null && !number.equals("")) {
			// 拨打
			mControlManager.dial(number);
		}
	}

	private void initViews() {
		// 系统底部
		// mBottomBar = (BottomBar) mRootView.findViewById(R.id.bottomBar);
		// //设置返回键不可见
		// mBottomBar.setBackBtnVisible(true);
		// refreshBottomBarText();
		// 设置时间同步
		mDateTV = (TextView) mRootView.findViewById(R.id.tv_bt_date);
		new TimeThread().start();
		// 标题
		mTitleTV = (TextView) mRootView.findViewById(R.id.tv_bt_title);
		// 左下角蓝牙信息
		// mBTInfoTV = (TextView) mRootView.findViewById(R.id.tv_bt_info);
		// 页脚菜单响应
		mRootView.findViewById(R.id.ll_bt_status).setOnClickListener(
				mOCListener);
		mRootView.findViewById(R.id.btn_home).setOnClickListener(mOCListener);
		// mBackV = mRootView.findViewById(R.id.btn_back);
		// mBackV.setOnClickListener(mOCListener);

		// mFragmentManager = getChildFragmentManager();
		mFrgDail = new BTDial();
		mFrgCallLog = new BTCallLog();
		mFrgContacts = new BTContacts();
		mFrgMusic = new BTMusic();
		mFrgPair = new BTDevices();
		mFrgSettings = new BTSettings();

		mMenuMain = (MutilRadioGroup) mRootView.findViewById(R.id.rg_menu);
		mMenuDail = (RadioButton) mRootView.findViewById(R.id.rb_menu_dail);
		mMenuRecord = (RadioButton) mRootView
				.findViewById(R.id.rb_menu_call_log);
		mMenuContacts = (RadioButton) mRootView
				.findViewById(R.id.rb_menu_contacts);
		mMenuMusic = (RadioButton) mRootView.findViewById(R.id.rb_menu_music);
		mMenuPair = (RadioButton) mRootView.findViewById(R.id.rb_menu_pair);
		mMenuSettings = (RadioButton) mRootView
				.findViewById(R.id.rb_menu_settings);
		//右上角布局
		mTopRightV = mRootView
				.findViewById(R.id.linear_top_right);

		// 只为点击音
		mMenuDail.setOnClickListener(mOCListener);
		mMenuRecord.setOnClickListener(mOCListener);
		mMenuContacts.setOnClickListener(mOCListener);
		mMenuMusic.setOnClickListener(mOCListener);
		mMenuPair.setOnClickListener(mOCListener);
		mMenuSettings.setOnClickListener(mOCListener);

		mMenuMain.setOnCheckedChangeListener(mRGOnCheckedChangeListener);
	}

	private MutilRadioGroup.OnCheckedChangeListener mRGOnCheckedChangeListener = new MutilRadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(MutilRadioGroup radioGroup, int checkedId) {

			switch (checkedId) {
			case R.id.rb_menu_dail:
				switchMenu(mFrgDail);
                //改变图标大小的，发现是切图有问题，不需要程序处理
//				Drawable drawable=getResources().getDrawable(R.drawable.ym_bt_menu_dial_selected); //获取图片
//				drawable.setBounds(0, 13, 40, 40);  //设置图片参数
//				mMenuDail.setCompoundDrawables(null, drawable, null, null);
				break;
			case R.id.rb_menu_call_log:
				switchMenu(mFrgCallLog);
				break;
			case R.id.rb_menu_contacts:
				switchMenu(mFrgContacts);
				break;
			case R.id.rb_menu_music:
				switchMenu(mFrgMusic);
				break;
			case R.id.rb_menu_pair:
				switchMenu(mFrgPair);
				break;
			case R.id.rb_menu_settings:
				switchMenu(mFrgSettings);
				break;
			}
		}
	};

	private void initData() {
		mFuncBTOperate = FuncBTOperate.getInstance(mContext);
		BtLogger.e(TAG, "禁用菜单位置－－－1");
		// 通知主界面变化--蓝牙未连接时其他菜单设置为灰色不可点击
		if (mFuncBTOperate != null)
			mFuncBTOperate.notifyMain(0, false);
		mControlManager = ControlManager.getInstance(mContext, null);
		// 同步显示左下角蓝牙信息，必须放在mControlManager初始化之后
		if (mFuncBTOperate != null)
			mFuncBTOperate.notifyMain(1, false, -1, "");
	}

	private void highlightMusic() {
		BtLogger.e(TAG, "highlightMusic=");
		// 高亮并调整蓝牙菜单
		switchMenu(mFrgMusic);
		mMenuMusic.setChecked(true);
		mMenuMusic.setEnabled(true);
	}

	private void setMenuEnabled(boolean enabled) {
		// boolean enabled = eventMain.isEnabled();
		BtLogger.e(TAG, "切换主界面setMenuEnabled－enabled=" + enabled);
		// enabled = true;
		if (!enabled) {
			// 断开蓝牙默认配对界面
			switchMenu(mFrgPair);
			// 断开蓝牙时清空拨号界面电话号码
			if (mControlManager != null) {
				mControlManager.notifyDial(0);
			}
			mMenuPair.setChecked(true);
			// 恢复联系人界面，隐藏搜索键盘等
			hideSearchUI();
		} else if (mFrgCurrent == mFrgContacts) {
			// 这里的作用不太明确
			switchMenu(mFrgContacts);
			mMenuContacts.setChecked(true);
		} else {
			// 连接蓝牙默认拨号界面
			// switchMenu(mFrgDail);
			// mMenuDail.setChecked(true);
		}
		mMenuDail.setEnabled(enabled);
		mMenuContacts.setEnabled(enabled);
		mMenuRecord.setEnabled(enabled);
		mMenuMusic.setEnabled(enabled);
	}

	/**
	 * 同步主页左下角蓝牙信息
	 * 
	 * @param eventMain
	 */
	private void syncBottomBTInfo(EventMain eventMain) {
		if (mSyncBottoming) {
			return;
		}
		mSyncBottoming = true;
		boolean enabled = eventMain.isEnabled();
		long playStatus = eventMain.getPlayStatus();
		String musicName = eventMain.getMusicName();
		BtLogger.e(TAG, "syncBottomBTInfo－enabled=" + enabled);
		BtLogger.e(TAG, "syncBottomBTInfo－playStatus=" + playStatus);
		BtLogger.e(TAG, "syncBottomBTInfo－musicName=" + musicName);
		// 设置左下角蓝牙信息(按理说应该是pause，实际stop就是pause)
		if (playStatus == BluetoothAvrcpController.PLAY_STATUS_PLAYING
				&& !"".equals(musicName.trim())) {
			// mBTInfoTV.setText(musicName);
			// mBottomBar.setFirstText(musicName);
			mBottomStr = musicName;
			// 蓝牙音乐播放
			mStatus = 1;
		} else if (DataStatic.mCurrentBT == null || !enabled) {
			if (mControlManager != null) {
				String btSelfName = mControlManager.getBTName();
				BtLogger.e(TAG, "syncBottomBTInfo－btSelfName=" + btSelfName);
				if (btSelfName != null) {
					// mBTInfoTV.setText(btSelfName);
					// mBottomBar.setFirstText(btSelfName);
					mBottomStr = btSelfName;
					// 未连接蓝牙,自身名字
					mStatus = 2;
				}
			}
		} else {
			// mBTInfoTV.setText(DataStatic.mCurrentBT.getName());
			// mBottomBar.setFirstText(DataStatic.mCurrentBT.getName());
			mBottomStr = DataStatic.mCurrentBT.getName();
			// 连接中蓝牙的名字
			mStatus = 3;
		}
		BtLogger.e(TAG, "syncBottomBTInfo－mBottomStr=" + mBottomStr);
		// refreshBottomBarText();
		mSyncBottoming = false;
	}

	// private void refreshBottomBarText(){
	// if(mBottomBar != null && !mIsSaveInstanceState){
	// //设置蓝牙图标
	// mBottomBar.setIcon(BottomBar.ICON_BLUETOOTH);
	// mBottomBar.setFirstText(mBottomStr);
	// mBottomBar.setSecondText("");
	// mBottomBar.setThirdText("");
	// BtLogger.e(TAG, "this.getPackageName()=" +
	// getActivity().getPackageName());
	// BtLogger.e(TAG, "this.getComponentName().getClassName()=" +
	// getActivity().getComponentName().getClassName());
	// mBottomBar.setPackageName(getActivity().getPackageName());
	// mBottomBar.setClassName(getActivity().getComponentName().getClassName());
	// mBottomBar.setCustomBackFunc(true);
	// mBottomBar.setOnBottomBarListener(new BottomBar.OnBottomBarListener() {
	// @Override
	// public void onIconClick(View view) {
	// //设置蓝牙永久可见
	// //
	// mControlManager.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
	// }
	//
	// @Override
	// public void onMenuClick(View view) {
	// goHome();
	// }
	//
	// @Override
	// public void onBackClick(View view) {
	// //如果处于搜索才隐藏键盘
	// if(!mMenuMain.isShown()){
	// hideSearchUI();
	// //仅返回键时才需要同步标题
	// mFuncBTOperate.notifyContactsList(4);
	// }else{
	// //不能关闭应用，只能回到主界面
	// // mActivity.finish();
	// goHome();
	// }
	//
	// }
	//
	// @Override
	// public void onLeftLinearClick(View view) {
	// // 左边整个LinearLayout点击事件（需先设置mBottomBar.setCustomLeftLinearFunc(true);才有效）
	// }
	// });
	// BtLogger.e(TAG, "refreshBottomBarText-mBottomStr=" + mBottomStr);
	// }
	// }

	private void showSearchUI() {
		mMenuMain.setVisibility(View.GONE);
		// mBackV.setVisibility(View.VISIBLE);
		// mBottomBar.setBackBtnVisible(true);
	}

	private void hideSearchUI() {
		mMenuMain.setVisibility(View.VISIBLE);
		// mBackV.setVisibility(View.GONE);
		// mBottomBar.setBackBtnVisible(false);
		if (mFuncBTOperate != null)
			mFuncBTOperate.notifyContactsList(2);
	}

	private View.OnClickListener mOCListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.ll_bt_status:
				// Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show();
				break;
			case R.id.btn_home:
				// simulateKey(KeyEvent.KEYCODE_BACK);
				goHome();
				break;
			case R.id.btn_back:
				hideSearchUI();
				break;
			}
		}
	};

	private void goHome() {
		Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);

		mHomeIntent.addCategory(Intent.CATEGORY_HOME);
		mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		getActivity().overridePendingTransition(R.anim.ym_bt_fade_in,
				R.anim.ym_bt_fade_out);
		startActivity(mHomeIntent);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mIsSaveInstanceState = true;
	}

	/**
	 * 当第一次从一个Activity启动Fragment，然后再去启动子Fragment的时候，存在指向Activity的变量，
	 * 但当退出这些Fragment之后回到Activity，
	 * 然后再进入Fragment的时候，这个变量变成null，这就很容易明了为什么抛出的异常是activity has been destroyed，
	 * Fragment在Detached之后都会被reset掉，但是它并没有对ChildFragmentManager做reset，
	 * 所以会造成ChildFragmentManager的状态错误。
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		try {
			Field childFragmentManager = Fragment.class
					.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private void switchMenu(Fragment frgShow) {
		// EventBus响应时,在onSaveInstanceState之后不能操作fragment,否则会报错
		if (mIsSaveInstanceState) {
			return;
		}
		//隐藏右上角菜单
		mTopRightV.setVisibility(View.GONE);
		if(frgShow == mFrgDail){
			DataStatic.sCurrentIF = 1;
			mMenuDail.setChecked(true);
		}else if(frgShow == mFrgCallLog){
			DataStatic.sCurrentIF = 2;
			mMenuRecord.setChecked(true);
		}else if(frgShow == mFrgContacts){
			DataStatic.sCurrentIF = 3;
			mMenuContacts.setChecked(true);
		}else if(frgShow == mFrgMusic){
			//只有蓝牙音乐时才显示右上方菜单
			mTopRightV.setVisibility(View.VISIBLE);
			DataStatic.sCurrentIF = 4;
			mMenuMusic.setChecked(true);
		}else if(frgShow == mFrgSettings){
			DataStatic.sCurrentIF = 5;
			mMenuSettings.setChecked(true);
		}else if(frgShow == mFrgPair){
			DataStatic.sCurrentIF = 6;
			mMenuPair.setChecked(true);
		}
		BtLogger.e(TAG, "switchMenu－frgShow=" + mFrgCurrent);
		// 仅切换才能使用
		if (mFrgCurrent != frgShow) {
			mFrgCurrent = frgShow;
			FragmentManager childFragmentManager = getChildFragmentManager();
			FragmentTransaction fragmentTransaction = childFragmentManager
					.beginTransaction().setCustomAnimations(
							android.R.anim.fade_in, R.anim.ym_bt_fade_out);
			if (!frgShow.isAdded()) { // 先判断是否被add过
				fragmentTransaction.hide(mFrgDail).hide(mFrgCallLog)
						.hide(mFrgContacts).hide(mFrgMusic).hide(mFrgPair)
						.hide(mFrgSettings)
						.add(R.id.fragment_container, frgShow).show(frgShow)
						.commit(); // 隐藏当前的fragment，add下一个到Activity中
			} else {
				fragmentTransaction.hide(mFrgDail).hide(mFrgCallLog)
						.hide(mFrgContacts).hide(mFrgMusic).hide(mFrgPair)
						.hide(mFrgSettings).show(frgShow).commit(); // 隐藏当前的fragment，显示下一个
			}

			// int showTitleResId = -1;
			// if(frgShow == mFrgDail){
			// showTitleResId = R.string.ym_bt_func_dial;
			// }else if(frgShow == mFrgCallLog){
			// showTitleResId = R.string.ym_bt_func_call_log;
			// }else if(frgShow == mFrgContacts){
			// showTitleResId = R.string.ym_bt_func_contacts;
			// }else if(frgShow == mFrgMusic){
			// showTitleResId = R.string.ym_bt_func_music;
			// }else if(frgShow == mFrgPair){
			// showTitleResId = R.string.ym_bt_func_devices;
			// }
			// setmTitle(showTitleResId);
			// //设置标题
			// mFuncBTOperate.sendCurrentPosition(mActivity, R.string.bt_title,
			// showTitleResId, -1);
		}
	}

	private void setmTitle(int menu) {
		String title = getString(R.string.nl_bt_title);
		if (menu != -1) {
			title += ">" + getString(menu);
		}
		mTitleTV.setText(title);
	}

	@Override
	public void initModel() {

	}

	@Override
	public void initLeftViews() {

	}

	@Override
	public void initRightViews() {

	}

	private ImageButton ib_music, ib_video, ib_radio;// 右上角按钮

	@Override
	public void initMiddleViews() {
		ib_music = (ImageButton) mRootView.findViewById(R.id.bt_ib_music);
		ib_video = (ImageButton) mRootView.findViewById(R.id.bt_ib_video);
		ib_radio = (ImageButton) mRootView.findViewById(R.id.bt_ib_radio);
	}

	@Override
	public int getSystemUITitleResId() {
		return R.string.ym_bt_music;
	}

	@Override
	public void setListener() {
		ib_music.setOnClickListener(this);
		ib_video.setOnClickListener(this);
		ib_radio.setOnClickListener(this);
	}

	@Override
	public void register() {
		// 注册U盘状态变化监听器
		UsbStateManager.getInstance().registerUsbStateChangeListener(this);
	}

	@Override
	public void unregister() {
		// 注销U盘状态变化监听器
		UsbStateManager.getInstance().unregisterUsbStateChangeListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.bt_ib_music:// 点击多媒体音乐按钮
				FragmentSwitchController.getInstance().switchFragment(
						CommonConstants.FLAG_MUSIC, null);
				break;
			case R.id.bt_ib_video:// 点击视频按钮
				FragmentSwitchController.getInstance().switchFragment(
						CommonConstants.FLAG_VIDEO, null);
				break;
			case R.id.bt_ib_radio:// 切换到收音机
				FragmentSwitchController.getInstance().switchFragment(
						CommonConstants.FLAG_RADIO, null);
				break;
		}
	}

	private class TimeThread extends Thread {
		@Override
		public void run() {
			do {
				try {
					Message msg = new Message();
					msg.what = 1;
					mHandler.sendMessage(msg);
					Thread.sleep(1000000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				long sysTime = System.currentTimeMillis();
				// CharSequence sysTimeStr = DateFormat.format("HH:mm",
				// sysTime);
				CharSequence sysTimeStr = DateFormat
						.format("yyyy-M-d", sysTime);
				mDateTV.setText(sysTimeStr);
				break;
			default:
				break;
			}
		}
	};

	private boolean isRepeat = false;

	private Handler mRepeatHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				isRepeat = false;
				break;
			}
		}
	};

	private void avoidRepeat() {
		isRepeat = true;
		mRepeatHandler.removeMessages(0);
		mRepeatHandler.sendEmptyMessageDelayed(0, 600);
	}

	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent event) {
	// BtLogger.e(TAG, "onKeyUp-keyCode=" + keyCode);
	// if(DataStatic.mCurrentBT != null && !isRepeat) {
	// avoidRepeat();
	// //以下这个不需要判断
	// // int a2dpState =
	// mControlManager.getConnectionA2dpState(DataStatic.mCurrentBT);
	// // if (a2dpState == BluetoothProfile.STATE_CONNECTED) { //2
	// switch (keyCode) {
	// // //前一曲
	// // case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
	// // //TUNE左旋
	// // case KeyEvent.KEYCODE_F1:
	// // mFuncBTOperate.notifyBTService(10);
	// // break;
	// // //后一曲
	// // case KeyEvent.KEYCODE_MEDIA_NEXT:
	// // //TUNE右旋
	// // case KeyEvent.KEYCODE_F2:
	// // mFuncBTOperate.notifyBTService(9);
	// // break;
	// //拨打
	// case KeyEvent.KEYCODE_F3:
	// //如果是拨号界面才处理
	// if(mFrgCurrent == mFrgDail || mFrgCurrent instanceof BTDial){
	// ((BTDial)mFrgDail).BTCallDail();
	// }
	// break;
	// //暂停播放
	// case KeyEvent.KEYCODE_F5:
	// if(mFuncBTOperate.getAvrcpState() ==
	// BluetoothAvrcpController.PLAY_STATUS_PLAYING){
	// //暂停
	// mFuncBTOperate.notifyBTService(8);
	// }else{
	// //播放
	// mFuncBTOperate.notifyBTService(7);
	// }
	// break;
	// }
	// // }
	// }
	// return super.onKeyUp(keyCode, event);
	// }

	private Handler mKeyHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			// 上一曲
			case 0:
				if (mFuncBTOperate != null)
					mFuncBTOperate.notifyBTService(10);
				break;
			// 下一曲
			case 1:
				if (mFuncBTOperate != null)
					mFuncBTOperate.notifyBTService(9);
				break;
			}
		}
	};

	public final static int KEY_PREV = 8;// 上一曲 8
	public final static int KEY_NEXT = 9;// , // 下一曲9
	public final static int KEY_SEEK_INC = 10; // 10 TUNE右旋,下一曲
	public final static int KEY_SEEK_DEC = 11;// ,// 11 TUNE左旋,上一曲
	
	// Anter
	public final static int KEY_POWER = 19;// Power按键
	
	private boolean DEBUG = true;

	private IKeyPressInterface mIKeyPressInterface = new IKeyPressInterface.Stub() {
		public void onKeyPressed(int keyCode, int mode) {
			BtLogger.d(TAG, "onKeyPressed() key: " + keyCode + ",mode: " + mode);
			switch (keyCode) {
			// 下一曲
			case KEY_NEXT:
			case KEY_SEEK_INC:
				mKeyHandler.sendEmptyMessage(1);
				break;
			// 上一曲
			case KEY_PREV:
			case KEY_SEEK_DEC:
				mKeyHandler.sendEmptyMessage(0);
				break;
				
			// Anter
			case KEY_POWER:
				if (mFuncBTOperate.getAvrcpState() == BluetoothAvrcpController.PLAY_STATUS_PLAYING) {
					// 暂停
					mFuncBTOperate.notifyBTService(8);
				} else {
					// 播放
					mFuncBTOperate.notifyBTService(7);
				}
				break;
			}
		}

		public String onGetAppInfo() {// 返回各应用名字标记
			return "btmusic";
		}
	};

	// 注册旋钮响应
	private void registerIKeyPressInterface() {// 注册自定义KEY
		if (DEBUG)
			BtLogger.d(TAG, "registerIKeyPressInterface()");
		if (mIKeyPressInterface != null) {
			ProtocolManager.getInstance().registerKeyPressListener(
					mIKeyPressInterface);
		}

	}

	// 注销旋钮响应
	private void unRegisterIKeyPressInterface() {// 反注册自定义KEY
		if (DEBUG)
			BtLogger.d(TAG, "unRegisterIKeyPressInterface()");
		if (mIKeyPressInterface != null) {
			ProtocolManager.getInstance().unregisterKeyPressListener(
					mIKeyPressInterface);
		}
	}

	@Override
	public void onUsbMounted(int usbFlag) {

	}

	@Override
	public void onUsbUnMounted(int usbFlag) {
		BtLogger.d(TAG, "onUsbUnMounted()");
		//如果蓝牙连接成功则不响应U盘拔出操作
		if(mControlManager != null
				&& mControlManager.getConnectionState(DataStatic.mCurrentBT) == BluetoothHfDevice.STATE_CONNECTED){
			return;
		}
		// 没有挂载U盘，说明当前拔出的是最后一个U盘，此时不管有没有播放都应该退出应用
		if (UsbStateManager.getInstance().hasNoUsbMounted()) {
			// 退出Activity
			finishActivityAndBackHome();
		}
	}
}
