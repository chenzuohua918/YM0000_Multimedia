package com.semisky.ym_multimedia.ymbluetooth.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;

import com.semisky.ym_multimedia.ymbluetooth.data.CallLogRecords;
import com.semisky.ym_multimedia.ymbluetooth.data.Contacts;
import com.semisky.ym_multimedia.ymbluetooth.func.CharacterParser;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class PhoneDBManager {
    private Context mContext;
//    private PhoneDBHelper mPhoneDBHelper;
//    private SQLiteDatabase mPhoneDB;

    private PhoneDBHelper helper;
    private SQLiteDatabase db;

    private static final String TAG = "PhoneDBManager";

    public PhoneDBManager(Context context) {
        helper = PhoneDBHelper.getInstance(context);
        db = helper.getWritableDatabase();
    }

    /**
     * 保存联系人（增）
     * @param contactsList
     */
    public void saveContacts(List<Contacts> contactsList) {

        synchronized (helper) {
            // 看数据库是否关闭
            if (!db.isOpen()) {
                db = helper.getWritableDatabase();
            }
            // 开始事务
            db.beginTransaction();
            try {
                String sql = "INSERT INTO " + PhoneDBHelper.CONTACTS_TABLE+"(name,number) VALUES(?,?)";
                SQLiteStatement stat = db.compileStatement(sql);
                for (Contacts contacts : contactsList) {
//            BtLogger.e(TAG, "getName＝"+contacts.getName());
//            BtLogger.e(TAG, "getNumber＝" + contacts.getNumber());
                    if(contacts.getName() != null){
                        stat.bindString(1, contacts.getName());
                    }
                    if(contacts.getNumber() != null){
                        stat.bindString(2, contacts.getNumber());
                    }
                    stat.executeInsert();
                }
                db.setTransactionSuccessful(); // 设置事务成功完成
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * 保存通话记录（增）
     * @param callLogRecordsList
     */
    public void saveCallLogRecords(List<CallLogRecords> callLogRecordsList) {
        synchronized (helper) {
            // 看数据库是否关闭
            if (!db.isOpen()) {
                db = helper.getWritableDatabase();
            }
            // 开始事务
            db.beginTransaction();
            try {
                String sql = "INSERT INTO " + PhoneDBHelper.CALL_LOG_TABLE+"(name,number,type,datetime) VALUES(?,?,?,?)";
                SQLiteStatement stat = db.compileStatement(sql);
                for (CallLogRecords callLogRecords : callLogRecordsList) {
//            BtLogger.e(TAG, "getName＝"+contacts.getName());
//            BtLogger.e(TAG, "getNumber＝" + contacts.getNumber());
                    if(callLogRecords.getName() != null){
                        stat.bindString(1, callLogRecords.getName());
                    }
                    if(callLogRecords.getNumber() != null){
                        stat.bindString(2, callLogRecords.getNumber());
                    }
                    stat.bindString(3, callLogRecords.getType()+"");
                    stat.bindString(4, callLogRecords.getDateTime()+"");
                    stat.executeInsert();
                }
                db.setTransactionSuccessful(); // 设置事务成功完成
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    public void insertCallLogRecords(CallLogRecords callLogRecords) {
        synchronized (helper) {
            // 看数据库是否关闭
            if (!db.isOpen()) {
                db = helper.getWritableDatabase();
            }
            // 开始事务
            db.beginTransaction();
            try {
                String sql = "INSERT INTO " + PhoneDBHelper.CALL_LOG_TABLE+"(name,number,type,datetime) VALUES(?,?,?,?)";
                SQLiteStatement stat = db.compileStatement(sql);
//            BtLogger.e(TAG, "getName＝"+contacts.getName());
//            BtLogger.e(TAG, "getNumber＝" + contacts.getNumber());
                if(callLogRecords.getName() != null){
                    stat.bindString(1, callLogRecords.getName());
                }
                if(callLogRecords.getNumber() != null){
                    stat.bindString(2, callLogRecords.getNumber());
                }
                stat.bindString(3, callLogRecords.getType()+"");
                stat.bindString(4, callLogRecords.getDateTime() + "");
                stat.executeInsert();
                db.setTransactionSuccessful(); // 设置事务成功完成
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }


    /**
     * 删除全部联系人数据（删）
     */
    public void deleteAllContacts() {
        BtLogger.d(TAG, "判断联系人数据表是否存在 = " + tabIsExist(PhoneDBHelper.CONTACTS_TABLE));
        //先判断联系人表是否存在,再删除
        if(tabIsExist(PhoneDBHelper.CONTACTS_TABLE)) {
            synchronized (helper) {
                if (!db.isOpen()) {
                    db = helper.getWritableDatabase();
                }
                db.beginTransaction();
                try {
                    db.execSQL("DELETE FROM " + PhoneDBHelper.CONTACTS_TABLE);
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                    db.close();
                }
            }
        }
    }

    /**
     * 删除全部通话记录数据（删）
     */
    public void deleteAllCallLogRecords() {
        BtLogger.d(TAG, "判断通话记录数据表是否存在 = " + tabIsExist(PhoneDBHelper.CALL_LOG_TABLE));
        //先判断通话记录表是否存在,再删除
        if(tabIsExist(PhoneDBHelper.CALL_LOG_TABLE)) {
            synchronized (helper) {
                if (!db.isOpen()) {
                    db = helper.getWritableDatabase();
                }
                db.beginTransaction();
                try {
                    db.execSQL("DELETE FROM " + PhoneDBHelper.CALL_LOG_TABLE);
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                    db.close();
                }
            }
        }
    }

    /**
     * 根据电话号码删除通话记录
     * @param number
     */
    public void deleteSingleCallLogRecord(String number){
        BtLogger.d(TAG, "判断通话记录数据表是否存在 = " + tabIsExist(PhoneDBHelper.CALL_LOG_TABLE));
        //先判断通话记录表是否存在,再删除
        if(tabIsExist(PhoneDBHelper.CALL_LOG_TABLE)) {
            synchronized (helper) {
                if (!db.isOpen()) {
                    db = helper.getWritableDatabase();
                }
                db.beginTransaction();
                try {
                    db.execSQL("DELETE FROM " + PhoneDBHelper.CALL_LOG_TABLE + " where number like '%" + number + "'");
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                    db.close();
                }
            }
        }
    }


    /**
     * 根据姓名查找电话号码（查）
     * @param name
     * @return
     */
    public String getContactsNumByName(String name) {

        String phoneNum = null;
        synchronized (helper) {
            if (!db.isOpen()) {
                helper.getWritableDatabase();
            }
            String sql = "select * from " + PhoneDBHelper.CONTACTS_TABLE + " where name = '" + name + "'";
            Cursor cursor = db.rawQuery(sql, null);
            try {
                while (cursor != null && cursor.moveToNext()) {
                    phoneNum = cursor.getString(cursor.getColumnIndexOrThrow("number"));
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return phoneNum;
    }

    /**
     * 根据号码查找联系人姓名（查）
     * @param number
     * @return
     */
    public String getContactsNameByNumber(String number) {
        String name = null;
        synchronized (helper) {
            if (!db.isOpen()) {
                helper.getWritableDatabase();
            }
            String sql = "select * from " + PhoneDBHelper.CONTACTS_TABLE + " where number like '%" + number + "'";
            Cursor cursor = db.rawQuery(sql, null);
            try {
                while (cursor != null && cursor.moveToNext()) {
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return name;
    }

    /**
     * 获取游标
     */
    public Cursor queryTheCursor(String table) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + table,null);
        return cursor;
    }

    /**
     * 获取所有联系人（查）
      * @return
     */
    public List<Contacts> getContactsList() {

        List<Contacts> contactsList = new ArrayList<Contacts>();
        synchronized (helper) {
            if (!db.isOpen()) {
                db = helper.getWritableDatabase();
            }
            Cursor cursor = queryTheCursor(PhoneDBHelper.CONTACTS_TABLE);
            try {
                while (cursor.moveToNext()) {
                    Contacts contacts = new Contacts();
                    String name = cursor.getString(cursor.getColumnIndex("name")).trim();
                    contacts.setName(name);
                    contacts.setNumber(cursor.getString(cursor.getColumnIndex("number")));
                    //拼音排序
                    String pinyin = CharacterParser.getInstance().getSelling(name);
                    contacts.setSpell(pinyin.toUpperCase());
                    String sortChar = "";
                    if (pinyin.length() > 0) {
                        sortChar = pinyin.substring(0, 1).toUpperCase();
                    }
                    if (sortChar.matches("[A-Z]")) {
                        contacts.setSortChar(sortChar+"@_^#_@"+name);
                    }else{
                        contacts.setSortChar("#");
                    }
                    contactsList.add(contacts);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        Collections.sort(contactsList, new Comparator<Contacts>() {
            @Override
            public int compare(Contacts o1, Contacts o2) {
                if (o1.getSortChar().equals("@")
                        || o2.getSortChar().equals("#")) {
                    return -1;
                } else if (o1.getSortChar().equals("#")
                        || o2.getSortChar().equals("@")) {
                    return 1;
                } else {
//                    return o1.getSortChar().compareTo(o2.getSortChar());
                    return Collator.getInstance(java.util.Locale.CHINESE).compare(o1.getSortChar(), o2.getSortChar());
                }
            }
        });
        return contactsList;
    }

    /**
     * 获取全部通话记录（查）
     * @return
     */
    public List<CallLogRecords> getCallLogRecordsList() {
        Set<CallLogRecords> callLogRecordsSet = new HashSet<CallLogRecords>();
        List<CallLogRecords> callLogRecordsList = new ArrayList<CallLogRecords>();
        synchronized (helper) {
            if (!db.isOpen()) {
                db = helper.getWritableDatabase();
            }
            Cursor cursor = queryTheCursor(PhoneDBHelper.CALL_LOG_TABLE);
            try {
                while (cursor.moveToNext()) {
                    CallLogRecords callLogRecords = new CallLogRecords();
                    //联系人
                    callLogRecords.setName(cursor.getString(cursor.getColumnIndex("name")));
                    //号码
                    callLogRecords.setNumber(cursor.getString(cursor.getColumnIndex("number")));
                    SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss\nyyyy/MM/dd");
                    String dateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("datetime"));
                    long dateTime = 0;
                    if(dateTimeStr != null && !"null".equals(dateTimeStr)){
                        //保存比较用的时间值
                        dateTime = Long.parseLong(dateTimeStr);
                    }
                    callLogRecords.setDateTime(dateTime);
                    Date date = new Date(dateTime);
                    //呼叫时间
                    callLogRecords.setTime(timeFormat.format(date));
                    //呼叫类型
                    callLogRecords.setType(Integer.parseInt(cursor.getString(cursor.getColumnIndex("type"))));
                    callLogRecordsSet.add(callLogRecords);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        Iterator<CallLogRecords> iterator = callLogRecordsSet.iterator();
        callLogRecordsList.clear();
        while(iterator.hasNext()) {
            callLogRecordsList.add(iterator.next());
        }
        BtLogger.d(TAG, "callLogRecordsList size = " + callLogRecordsList.size());
        //按通话时间排序
        Collections.sort(callLogRecordsList, new Comparator<CallLogRecords>() {

            public int compare(CallLogRecords callLogRecords1, CallLogRecords callLogRecords2) {
                int compareName = callLogRecords2.getDateTime().compareTo(callLogRecords1.getDateTime());
                return compareName;
            }
        });
        return callLogRecordsList;
    }

    /**
     * 通过呼叫类型获取通话记录（查）
     * @return
     */
    public List<CallLogRecords> getCallLogRecordsListByType(int type) {
        Set<CallLogRecords> callLogRecordsSet = new HashSet<CallLogRecords>();
        List<CallLogRecords> callLogRecordsList = new ArrayList<CallLogRecords>();
        synchronized (helper) {
            if (!db.isOpen()) {
                db = helper.getWritableDatabase();
            }
            String sql = "select * from " + PhoneDBHelper.CALL_LOG_TABLE + " where type = '" + type + "'";
            if(type == 0){
                sql = "select * from " + PhoneDBHelper.CALL_LOG_TABLE;
            }
            Cursor cursor = db.rawQuery(sql, null);
            try {
                while (cursor.moveToNext()) {
                    CallLogRecords callLogRecords = new CallLogRecords();
                    //联系人
                    callLogRecords.setName(cursor.getString(cursor.getColumnIndex("name")));
                    //号码
                    callLogRecords.setNumber(cursor.getString(cursor.getColumnIndex("number")));
                    SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss\nyyyy/MM/dd");
                    String dateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("datetime"));
                    long dateTime = 0;
                    if(dateTimeStr != null && !"null".equals(dateTimeStr)){
                        //保存比较用的时间值
                        dateTime = Long.parseLong(dateTimeStr);
                    }
                    callLogRecords.setDateTime(dateTime);
                    Date date = new Date(dateTime);
                    //呼叫时间
                    callLogRecords.setTime(timeFormat.format(date));
                    //呼叫类型
                    callLogRecords.setType(Integer.parseInt(cursor.getString(cursor.getColumnIndex("type"))));
                    callLogRecordsSet.add(callLogRecords);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        Iterator<CallLogRecords> iterator = callLogRecordsSet.iterator();
        callLogRecordsList.clear();
        while(iterator.hasNext()) {
            callLogRecordsList.add(iterator.next());
        }
        BtLogger.d(TAG, "callLogRecordsList size = " + callLogRecordsList.size());
        //按通话时间排序
        Collections.sort(callLogRecordsList, new Comparator<CallLogRecords>() {

            public int compare(CallLogRecords callLogRecords1, CallLogRecords callLogRecords2) {
                int compareName = callLogRecords2.getDateTime().compareTo(callLogRecords1.getDateTime());
                return compareName;
            }
        });
        return callLogRecordsList;
    }

    /**
     * 判断某个表是否存在
     * @param tabName
     * @return
     */
    public boolean tabIsExist(String tabName){
        boolean result = false;
        if(tabName == null){
            return false;
        }
        synchronized (helper) {
            try {
                if (!db.isOpen()) {
                    db = helper.getWritableDatabase();
                }
                String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tabName.trim() + "' ";
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor.moveToNext()) {
                    int count = cursor.getInt(0);
                    if (count > 0) {
                        result = true;
                    }
                }

            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return result;
    }
}
