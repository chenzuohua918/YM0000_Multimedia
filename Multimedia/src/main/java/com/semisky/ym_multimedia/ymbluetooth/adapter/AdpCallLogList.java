package com.semisky.ym_multimedia.ymbluetooth.adapter;

import android.content.Context;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.data.CallLogRecords;
import com.semisky.ym_multimedia.ymbluetooth.func.FuncBTOperate;

import java.util.List;


public class AdpCallLogList extends BaseAdapter {

    private List<CallLogRecords> mCallLogList;
    private Context mContext;
    private FuncBTOperate mFuncBTOperate;

    private int mHighlightItem = 0;

    public void setmHighlightItem(int mHighlightItem) {
        this.mHighlightItem = mHighlightItem;
    }

    public AdpCallLogList(Context context, List<CallLogRecords> callLogList) {
        mContext = context;
        this.mCallLogList = callLogList;
        mFuncBTOperate = FuncBTOperate.getInstance(mContext);
    }

    public void setCallLogList(List<CallLogRecords> callLogList) {
        mCallLogList = callLogList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCallLogList == null ? 0 : mCallLogList.size();
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
                    R.layout.ym_bt_item_call_log, null);
            contactsHolder = new ContactsHolder(convertView);
        } else {
            contactsHolder = (ContactsHolder) convertView.getTag();
        }

        contactsHolder.mLogTime.setText("" + mCallLogList.get(position).getTime());
        String number = mCallLogList.get(position).getNumber();
        if(number != null && !"".equals(number.trim())){
            String name = mFuncBTOperate.getContactsNameByNumber(number);
            if(name != null && !"".equals(name.trim())){
                contactsHolder.mLogPhone.setText("" + name.trim());
            }else{
                contactsHolder.mLogPhone.setText("" + number.trim());
            }
        }
//        contactsHolder.mLogPhone.setTextColor(mContext.getResources().getColor(R.color.white));
//        contactsHolder.mCallType.setVisibility(View.INVISIBLE);
        //如果显示类型为0，则显示全部，否则只显示showType类型
        switch (mCallLogList.get(position).getType()) {
            case CallLog.Calls.INCOMING_TYPE:
                contactsHolder.mCallType.setBackgroundResource(R.drawable.ym_bt_selector_call_log_in);
                break;
            case CallLog.Calls.OUTGOING_TYPE:
                contactsHolder.mCallType.setBackgroundResource(R.drawable.ym_bt_selector_call_log_out);
//                contactsHolder.mCallType.setVisibility(View.VISIBLE);
                break;
            case CallLog.Calls.MISSED_TYPE:
                contactsHolder.mCallType.setBackgroundResource(R.drawable.ym_bt_selector_call_log_miss);
//                contactsHolder.mLogPhone.setTextColor(mContext.getResources().getColor(R.color.yellow));
                break;
            default:
                break;
        }
        //高亮
        if(mHighlightItem == position){
            contactsHolder.mItemBg.setBackgroundResource(R.color.ym_bt_item_bg_pressed);
        }else{
            contactsHolder.mItemBg.setBackgroundResource(R.color.ym_bt_item_bg_normal);
        }
        return convertView;
    }

    private class ContactsHolder {
        public TextView mLogTime;
        public TextView mLogName;
        public TextView mLogPhone;
        public ImageView mCallType;
        public View mItemBg;

        public ContactsHolder(View view) {
            mLogTime = (TextView) view
                    .findViewById(R.id.tv_log_time);
            mLogName = (TextView) view
                    .findViewById(R.id.tv_log_name);
            mLogPhone = (TextView) view
                    .findViewById(R.id.tv_log_phone);
            mCallType = (ImageView) view
                    .findViewById(R.id.iv_log_mark);
            mItemBg = view.findViewById(R.id.bt_call_log_item_bg);
            view.setTag(this);
        }
    }
}