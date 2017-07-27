package com.semisky.ym_multimedia.ymbluetooth.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.semisky.ym_multimedia.BaseFragment;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.semisky.ym_multimedia.ymbluetooth.EventMsg.EventContacts;
import com.semisky.ym_multimedia.ymbluetooth.adapter.AdpContactsList;
import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.data.Contacts;
import com.semisky.ym_multimedia.ymbluetooth.data.DataStatic;
import com.semisky.ym_multimedia.ymbluetooth.func.ControlManager;
import com.semisky.ym_multimedia.ymbluetooth.func.FuncBTOperate;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luoyin on 16/9/26.
 */
public class BTContacts extends BaseFragment {
    private final String TAG = "BTContacts";
    private Context mContext;
    private Activity mActivity;
    private View mRootView;
    private boolean mInSync = false;
    private ListView mListView;
    private AdpContactsList mAdpContactsList;
    private List<Contacts> mContactsList = new ArrayList<Contacts>();

    private ControlManager mControlManager;
    private FuncBTOperate mFuncBTOperate;

    private View mYMSynchroBtn;
    private View mYMSearchBtn;
    private View mYMDialBtn;

    //搜索相关控件
    private String mSearchText = "";
    private TextView mSearchTextTV;
    private View mSearchKeypadLL, mNumKeypadLL;
    private int[] mKeyLetterIDs = {R.id.btn_key_0, R.id.btn_key_1, R.id.btn_key_2, R.id.btn_key_3
            , R.id.btn_key_4, R.id.btn_key_5, R.id.btn_key_6, R.id.btn_key_7 , R.id.btn_key_8, R.id.btn_key_9
            , R.id.btn_key_a, R.id.btn_key_b, R.id.btn_key_c, R.id.btn_key_d, R.id.btn_key_e, R.id.btn_key_f
            , R.id.btn_key_g, R.id.btn_key_h, R.id.btn_key_i, R.id.btn_key_j, R.id.btn_key_k, R.id.btn_key_l
            , R.id.btn_key_m, R.id.btn_key_n, R.id.btn_key_o, R.id.btn_key_p, R.id.btn_key_q, R.id.btn_key_r
            , R.id.btn_key_s, R.id.btn_key_t, R.id.btn_key_u, R.id.btn_key_v, R.id.btn_key_w, R.id.btn_key_x
            , R.id.btn_key_y, R.id.btn_key_z, R.id.btn_key_comma, R.id.btn_key_period, R.id.btn_key_num_hide
            , R.id.btn_key_space, R.id.btn_key_shift, R.id.btn_key_hide, R.id.btn_key_del};
    private View[] mKeyLetterBTNs = new View[mKeyLetterIDs.length];
//    private String[] mLetters = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
//            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
//            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
//            "U", "V", "W", "X", "Y", "Z",
//            "a", "b", "c", "d", "e", "f", "g", "h", "i", "J",
//            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
//            "U", "V", "W", "X", "Y", "Z" };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden) {
            if (mFuncBTOperate != null) {
                //设置标题
                mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.nl_bt_title, R.string.ym_bt_contacts, -1);
            }
            BtLogger.d(TAG, "onHiddenChanged-mAdpContactsList="+mAdpContactsList);
            if (mAdpContactsList != null) {
                mAdpContactsList.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BtLogger.d(TAG, "onCreate");
        EventBus.getDefault().register(this);
        mActivity = getActivity();
        mContext = getActivity().getApplicationContext();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        refreshRightText();
    }

//    private void refreshRightText(){
//        mContactsSyncTV.setVText(mContext.getResources().getText(R.string.synchro));
//        mContactsSearchTV.setVText(mContext.getResources().getText(R.string.ym_bt_search));
//    }

    public void onEventMainThread(EventContacts eventContacts) {
        switch (eventContacts.getMethod()){
            case 0:
                syncContacts();
                break;
            case 1:
                clearList();
                break;
            case 2:
                hideSearchUI();
                break;
            case 3:
                mInSync = false;
                break;
            case 4:
                //设置标题
//                mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.bt_title, R.string.bt_contacts, -1);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        BtLogger.d(TAG, "BTContacts-onCreateView");
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.ym_bt_func_contacts, null);
        }
        initViews();
        initSearch();
        initData();
        syncContacts();
        return mRootView;
    }

    private void initViews() {
        mListView = (ListView) mRootView.findViewById(R.id.lv_contacts_list);
        mListView.setOnItemClickListener(mOIClistener);
        mAdpContactsList = new AdpContactsList(getActivity(), mContactsList);
        mListView.setAdapter(mAdpContactsList);
        //右边条响应
//        mContactsSyncTV = (VerticalTextView) mRootView.findViewById(R.id.tv_contacts_synchro);
//        mContactsSearchTV = (VerticalTextView) mRootView.findViewById(R.id.tv_contacts_search);
//        mContactsSyncTV.setOnClickListener(mOCListener);
//        mContactsSearchTV.setOnClickListener(mOCListener);
        //以下控件与搜索相关
        //搜索键盘
        mSearchKeypadLL = mRootView.findViewById(R.id.ll_search_keypad);
        //数字键区域
        mNumKeypadLL = mRootView.findViewById(R.id.keypad_row_0);
        //搜索拼音显示框
        mSearchTextTV = (TextView) mRootView.findViewById(R.id.tv_search_text);
        mSearchTextTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchKeypadLL.setVisibility(View.VISIBLE);
                refreshSearchIcon(false);
            }
        });
        for (int i = 0; i < mKeyLetterBTNs.length; i++) {
            mKeyLetterBTNs[i] = mRootView.findViewById(mKeyLetterIDs[i]);
            mKeyLetterBTNs[i].setOnClickListener(mLetterOCListener);
        }
        //数字键初始化
        initNumKeypad();
        //字母键初始化
        initCapsKeypad();
        //标点空格初始化
        initPunctuation();
        //长按全删
        mRootView.findViewById(R.id.btn_key_del).setOnLongClickListener(ol);

        mYMSynchroBtn = mRootView.findViewById(R.id.ym_contacts_synchro);
        mYMSearchBtn = mRootView.findViewById(R.id.ym_contacts_search);
        mYMDialBtn = mRootView.findViewById(R.id.ym_contacts_dial);
        mYMSynchroBtn.setOnClickListener(mOnYMMenuCListener);
        mYMSearchBtn.setOnClickListener(mOnYMMenuCListener);
        mYMDialBtn.setOnClickListener(mOnYMMenuCListener);
    }

    private void initNumKeypad(){
        //数字键初始化
        for (int i = 0; i < 10; i++) {
            int asc = 48+i;
            String key = Character.toString((char)asc);
            ((Button)mKeyLetterBTNs[i]).setText(key);
        }
    }

    private void initPunctuation(){
        //空格
        String key = Character.toString((char)32);
        //逗号
        key = Character.toString((char)44);
        ((Button)mKeyLetterBTNs[36]).setText(key);
        //句号
        key = Character.toString((char)46);
        ((Button)mKeyLetterBTNs[37]).setText(key);
        //数字键隐藏显示
        ((Button)mKeyLetterBTNs[38]).setText("123");
    }

    private void initCapsKeypad(){
        int base = 97-10;
        if(mCapsLock){
            base = 65-10;
        }
        //字母键初始化
        for (int i = 10; i < 36; i++) {
            int asc = base+i;
            String key = Character.toString((char)asc);
            ((Button)mKeyLetterBTNs[i]).setText(key);
        }
    }

    private View.OnClickListener mOnYMMenuCListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //未连接蓝牙则不响应
            if(mControlManager != null
                    && mControlManager.getConnectionState(DataStatic.mCurrentBT) != BluetoothHfDevice.STATE_CONNECTED){
                return;
            }
            if(mInSync){
                return;
            }
            switch (view.getId()){
                case R.id.ym_contacts_synchro:
                    mInSync = true;
                    mFuncBTOperate.showmBTClearDialog(getActivity(), R.string.nl_bt_cautions, R.string.nl_bt_sync_contacts, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            clearList();
                            //清除联系人相关数据需要开线程
                            syncClearAndPullContacts();
                            mFuncBTOperate.dismissmBTClearDialog();
                        }
                    });
                    break;
                case R.id.ym_contacts_search:
//                    Toast.makeText(mContext, "搜索还未完成", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(mContext, SearchActivity.class);
//                    mActivity.startActivity(intent);
                    showSearchUI();
                    break;
                case R.id.ym_contacts_dial:
                    if(mContactsList.size() > position) {
                        String dialNumber = mContactsList.get(position).getNumber();
                        BtLogger.e(TAG, "通话记录界面－拨打" + dialNumber);
                        mControlManager.dial(dialNumber);
                    }
                    break;
            }


        }
    };

    private void initData() {
        mControlManager = ControlManager.getInstance(mContext, null);
        mFuncBTOperate = FuncBTOperate.getInstance(mContext);
        //设置标题
        mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.nl_bt_title, R.string.ym_bt_contacts, -1);
//        refreshRightText();
    }

    /**
     * 搜索键盘相关控件响应
     */
    private View.OnLongClickListener ol = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View view) {
            mSearchText = "";
            mSearchTextTV.setText(mSearchText);
//            mSearchTextTV.setVisibility(View.GONE);
            return false;
        }
    };

    /**
     * 搜索键盘相关控件响应
     */
    private View.OnClickListener mLetterOCListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mKeyLetterIDs.length; i++) {
                //空白键时不响应
                if(v.getId() == mKeyLetterIDs[i]){
                    int lastTextLen = mSearchText.length();
                    addSearchText(i, v);
                    showSearchContacts();
                    //上次文字不为空或当前文字不为空时响应
                    if(lastTextLen > 0 || mSearchText.length() > 0){
                        //移动到首部
//                    mListView.smoothScrollToPosition(0);
                        mListView.setSelection(0);
                    }
                }
            }
            if(mSearchText.length() == 0){
//                mSearchTextTV.setVisibility(View.GONE);
            }else{
                mSearchTextTV.setVisibility(View.VISIBLE);
            }
        }
    };

    private boolean mCapsLock = false;

    private void addSearchText(int i, View v){
        BtLogger.d(TAG, "i="+i);
        BtLogger.d(TAG, "mKeyLetterIDs.length="+mKeyLetterIDs.length);
        if(i < 38){
            String key = ((Button)v).getText().toString();
            mSearchText += key;
            mSearchTextTV.setText(mSearchText);
        }
        if(i == mKeyLetterIDs.length-1) {
            //删除
            mSearchText = mSearchText.substring(0, mSearchText.length()>0?mSearchText.length()-1:0);
            mSearchTextTV.setText(mSearchText);
        }else if(i == mKeyLetterIDs.length-2) {
            //隐藏键盘
            mSearchKeypadLL.setVisibility(View.GONE);
            BtLogger.d(TAG, "mSearchTextTV.getText().length()="+mSearchTextTV.getText().length());
            BtLogger.d(TAG, "mSearchTextTV.getText().toString().length()="+mSearchTextTV.getText().toString().length());
            //直接关闭键盘需要将图标还原出来
            if(mSearchTextTV.getText().toString().length() == 0){
                refreshSearchIcon(true);
            }
        }else if(i == mKeyLetterIDs.length-3) {
            //切换大小写
            mCapsLock = !mCapsLock;
            initCapsKeypad();
        }else if(i == mKeyLetterIDs.length-4) {
            //空格键
            mSearchText += " ";
            mSearchTextTV.setText(mSearchText);
        }else if(i == mKeyLetterIDs.length-5) {
            //隐藏显示数字键盘
            if(mNumKeypadLL.isShown()){
                mNumKeypadLL.setVisibility(View.GONE);
            }else{
                mNumKeypadLL.setVisibility(View.VISIBLE);
            }
        }

    }

    private void showSearchContacts(){
        List<Contacts> contactsList = new ArrayList<Contacts>();
        for (int i = 0; i < mContactsList.size(); i++) {
            if(getSame(mContactsList.get(i).getSpell(), mSearchText.toUpperCase())){
                contactsList.add(mContactsList.get(i));
            }
        }
        if(mAdpContactsList != null) {
            mAdpContactsList.setContactsList(contactsList);
        }
    }

    private boolean getSame(String name, String key){
        if(name.length() <= 0 || key.length() > name.length()){
            return false;
        }
        char[] nameChar = name.toCharArray();
        char[] keyChar = key.toCharArray();
        for (int i = 0; i < key.length(); i++) {
            if(nameChar[i] != keyChar[i]){
                return false;
            }
        }
        return true;
    }

    private int position = 0;

    private AdapterView.OnItemClickListener mOIClistener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(true){
                position = i;
                mAdpContactsList.setmHighlightItem(i);
                if(mAdpContactsList != null){
                    mAdpContactsList.notifyDataSetChanged();
                }
                return;
            }
            TextView nameTV = (TextView) view.findViewById(R.id.tv_contacts_name);
            TextView phoneTV = (TextView) view.findViewById(R.id.tv_contacts_phone);
            BtLogger.d(TAG, "联系人界面－拨打"+nameTV.getText().toString());
            mControlManager.dial(phoneTV.getText().toString());
        }
    };

//    private View.OnClickListener mOCListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            if(mInSync){
//                mFuncBTOperate.showStatus(R.string.progressing);
//                return;
//            }
//            switch (view.getId()){
//                case R.id.tv_contacts_synchro:
//                    mInSync = true;
//                    mFuncBTOperate.showmBTClearDialog(getActivity(), R.string.cautions, R.string.sync_contacts, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            clearList();
//                            //清除联系人相关数据需要开线程
//                            syncClearAndPullContacts();
//                            mFuncBTOperate.dismissmBTClearDialog();
//                        }
//                    });
//                    break;
//                case R.id.tv_contacts_search:
////                    Toast.makeText(mContext, "搜索还未完成", Toast.LENGTH_SHORT).show();
////                    Intent intent = new Intent(mContext, SearchActivity.class);
////                    mActivity.startActivity(intent);
//                    showSearchUI();
//                    break;
//            }
//        }
//    };

    private void showSearchUI(){
        //隐藏菜单
//        mFuncBTOperate.notifyMain(2, false);
        mSearchKeypadLL.setVisibility(View.VISIBLE);
//        ViewGroup.LayoutParams params = mListView.getLayoutParams();
//        params.height = FuncBase.getInstance(mContext).dp2px(400);// 当控件的高强制设成50象素
//        mListView.setLayoutParams(params); // 使设置好的布局参数应用到控件myGrid
//        mContactsSearchTV.setVisibility(View.GONE);
//        mContactsSyncTV.setVisibility(View.GONE);
        mSearchTextTV.setVisibility(View.VISIBLE);
        //设置标题
//        mFuncBTOperate.sendCurrentPosition(getActivity(), R.string.bt_title, R.string.bt_contacts, R.string.bt_search);
    }

    private void hideSearchUI(){
        mSearchKeypadLL.setVisibility(View.GONE);
//        ViewGroup.LayoutParams params = mListView.getLayoutParams();
//        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//        mListView.setLayoutParams(params); // 使设置好的布局参数应用到控件myGrid
//        mContactsSearchTV.setVisibility(View.VISIBLE);
//        mContactsSyncTV.setVisibility(View.VISIBLE);
        //将文字条隐藏，并清空数据
        mSearchTextTV.setVisibility(View.GONE);
        mSearchText = "";
        showSearchContacts();
    }

    private void syncClearAndPullContacts(){
        new Thread(){
            @Override
            public void run() {
                BtLogger.e(TAG, "同步联系人");
                mFuncBTOperate.deleteContacts();
                mFuncBTOperate.deleteAllCallLogRecords();
                mControlManager.deleteVCFFiles();
//                mControlManager.clearContactsVCF();
                //重新拉取联系人
                mFuncBTOperate.notifyBTService(0);
            }
        }.start();
    }

    public void syncContacts() {
        //蓝牙打开,且连接当前设备才同步,否则清空
        if(mControlManager.isEnabled() && !mControlManager.isNotInit()
                && mControlManager.getConnectionState(DataStatic.mCurrentBT) == BluetoothHfDevice.STATE_CONNECTED){
            BtLogger.d(TAG, "加载联系人");
            new Thread(new Runnable() {
                public void run() {
                    BtLogger.d(TAG, "读取联系人 start");
//                    mContactsList = new DBManager(mContext).getContacts();
                    mContactsList = mFuncBTOperate.getContacts();
                    BtLogger.d(TAG, "读取联系人 end");
                    mUIHandler.sendEmptyMessage(0);
                }
            }).start();
        }else{
            clearList();
        }
    }

    private Handler mUIHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    BtLogger.d(TAG,"刷新联系人");
                    if(mContactsList != null && mAdpContactsList != null) {
                        mAdpContactsList.setContactsList(mContactsList);
                    }
                    break;
            }
        }
    };

    private void clearList(){
        if(mContactsList != null && mAdpContactsList != null){
            mContactsList.clear();
            mAdpContactsList.setContactsList(mContactsList);
        }
    }

    private void refreshSearchIcon(boolean show){
        if(mSearchTextTV == null){
            return;
        }
        if(show){
            final Drawable drawable = getResources().getDrawable(R.drawable.ym_bt_search_hint);
            // 这一步必须要做,否则不会显示.
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mSearchTextTV.setCompoundDrawables(drawable, null, null, null);
        }else{
            mSearchTextTV.setCompoundDrawables(null, null, null, null);
        }
    }

    private void initSearch(){
        mSearchTextTV = (TextView) mRootView.findViewById(R.id.tv_search_text);
        refreshSearchIcon(true);
        mSearchTextTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count != 0) {
                    refreshSearchIcon(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    refreshSearchIcon(true);
                }
            }
        });
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
        return R.string.ym_bt_contacts;
    }

    @Override
    public void setListener() {}

    @Override
    public void register() {}

    @Override
    public void unregister() {}
}
