package com.semisky.ym_multimedia.ymbluetooth.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.broadcom.bt.hfdevice.BluetoothHfDevice;
import com.semisky.ym_multimedia.R;

import com.semisky.ym_multimedia.ymbluetooth.func.ControlManager;

import java.util.List;


public class AdpBTDeviceList extends BaseAdapter {

    private List<BluetoothDevice> mBTDeviceList;
    private Context mContext;
    private boolean mPairing = false;

    private int mHighlightItem = 0;

    public void setmHighlightItem(int mHighlightItem) {
        this.mHighlightItem = mHighlightItem;
    }

    public AdpBTDeviceList(Context context, List<BluetoothDevice> btDeviceList) {
        mContext = context;
        this.mBTDeviceList = btDeviceList;
    }

    public void setBTDeviceList(List<BluetoothDevice> btDeviceList) {
        mBTDeviceList = btDeviceList;
        notifyDataSetChanged();
    }

    public boolean ismPairing() {
        return mPairing;
    }

    public void setmPairing(boolean mPairing) {
        this.mPairing = mPairing;
    }

    @Override
    public int getCount() {
        return mBTDeviceList == null ? 0 : mBTDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ContactsHolder contactsHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.ym_bt_item_device, null);
            contactsHolder = new ContactsHolder(convertView);
        } else {
            contactsHolder = (ContactsHolder) convertView.getTag();
        }

        contactsHolder.mLogName.setText("" + mBTDeviceList.get(position).getName());
        //每刷新列表就还原一次配对状态
        if(position == 0){
            mPairing = false;
        }
        int state = getItemState(mBTDeviceList.get(position));
        switch (state){
            case BluetoothHfDevice.STATE_CONNECTED:
                contactsHolder.mLogName.setTextColor(mContext.getResources().getColorStateList(R.color.ym_bt_color_list_item));
                contactsHolder.mStateIV.setBackgroundResource(R.drawable.ym_bt_selector_icon_paired);
                mPairing |= false;
//                contactsHolder.mItemBg.setBackgroundResource(R.color.bt_item_bg_pressed);
                contactsHolder.mDeviceState.setText(R.string.ym_bt_connected);
                break;
            case BluetoothHfDevice.STATE_CONNECTING:
                contactsHolder.mLogName.setTextColor(mContext.getResources().getColorStateList(R.color.ym_bt_color_press_darkgray));
                mPairing |= true;
//                contactsHolder.mItemBg.setBackgroundResource(R.color.bt_item_bg_pressed);
                contactsHolder.mDeviceState.setText(R.string.ym_bt_connecting);
                break;
            default:
                contactsHolder.mLogName.setTextColor(mContext.getResources().getColorStateList(R.color.ym_bt_color_dialog_button));
                contactsHolder.mStateIV.setBackgroundResource(R.drawable.ym_bt_selector_icon_nopaired);
                mPairing |= false;
//                contactsHolder.mItemBg.setBackgroundResource(R.color.bt_item_bg_normal);
                contactsHolder.mDeviceState.setText(R.string.ym_bt_not_connected);
                break;
        }
        //高亮
        if(mHighlightItem == position){
            contactsHolder.mItemBg.setBackgroundResource(R.color.ym_bt_item_bg_pressed);
        }else{
            contactsHolder.mItemBg.setBackgroundResource(R.color.ym_bt_item_bg_normal);
        }
//        convertView.setEnabled(false);
//        convertView.setClickable(false);
        return convertView;
    }

    private class ContactsHolder {
        public TextView mLogName;
        public TextView mDeviceState;
        public ImageView mStateIV;
        public View mItemBg;

        public ContactsHolder(View view) {
            mLogName = (TextView) view
                    .findViewById(R.id.tv_bt_name);
            mDeviceState = (TextView) view
                    .findViewById(R.id.tv_bt_state);
            view.setTag(this);
            mStateIV = (ImageView) view.findViewById(R.id.iv_bt_state);
            mItemBg = view.findViewById(R.id.bt_device_item_bg);
        }
    }

    private int getItemState(BluetoothDevice btDevice){
        ControlManager controlManager = ControlManager.getInstance(mContext, null);
        int state = controlManager.getConnectionState(btDevice);
        int bondState = btDevice.getBondState();
        return state;
    }
}