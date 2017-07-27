package com.semisky.ym_multimedia.ymbluetooth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.semisky.ym_multimedia.R;
import com.semisky.ym_multimedia.ymbluetooth.data.Contacts;

import java.util.List;


public class AdpContactsList extends BaseAdapter {

    private List<Contacts> mContactsList;
    private Context mContext;

    private int mHighlightItem = 0;

    public void setmHighlightItem(int mHighlightItem) {
        this.mHighlightItem = mHighlightItem;
    }

    public AdpContactsList(Context context, List<Contacts> contactsList) {
        mContext = context;
        mContactsList = contactsList;
    }

    public void setContactsList(List<Contacts> contactsList) {
        mContactsList = contactsList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mContactsList == null ? 0 : mContactsList.size();
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
                    R.layout.ym_bt_item_contacts, null);
            contactsHolder = new ContactsHolder(convertView);
        } else {
            contactsHolder = (ContactsHolder) convertView.getTag();
        }

        contactsHolder.mCtsName.setText(""+mContactsList.get(position).getName());
        contactsHolder.mCtsPhone.setText(""+mContactsList.get(position).getNumber());
        //高亮
        if(mHighlightItem == position){
            contactsHolder.mItemBg.setBackgroundResource(R.color.ym_bt_item_bg_pressed);
        }else{
            contactsHolder.mItemBg.setBackgroundResource(R.color.ym_bt_item_bg_normal);
        }
        return convertView;
    }

    private class ContactsHolder {
        public TextView mCtsName;
        public TextView mCtsPhone;
        public View mItemBg;

        public ContactsHolder(View view) {
            mCtsName = (TextView) view
                    .findViewById(R.id.tv_contacts_name);
            mCtsPhone = (TextView) view
                    .findViewById(R.id.tv_contacts_phone);
            mItemBg = view.findViewById(R.id.bt_contacts_item_bg);
            view.setTag(this);
        }
    }
}