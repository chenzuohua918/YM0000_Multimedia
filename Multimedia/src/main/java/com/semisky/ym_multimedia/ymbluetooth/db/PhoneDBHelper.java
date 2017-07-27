package com.semisky.ym_multimedia.ymbluetooth.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.semisky.ym_multimedia.ymbluetooth.tests.BtLogger;


/**
 * Created by Administrator on 2016/9/2.
 */
public class PhoneDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "glphonebook.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TB_NAME = "name";
    private static final String TB_ID = "_id";
    private static final String TB_NUMBER = "number";
    private static final String TAG = "PhoneDBHelper";

    public static final String CONTACTS_TABLE = "glcontactstable";
    public static final String CALL_LOG_TABLE = "glcalllogtable";

    /*私有的静态对象，为整个应用程序提供一个sqlite操作的静态实例，并保证只能通过下面的静态方法getHelper(Context context)获得，
         * 防止使用时绕过同步方法改变它*/
    private static PhoneDBHelper instance;//这里主要解决死锁问题,是static就能解决死锁问题
    /**
     * 私有的构造函数，只能自己使用，防止绕过同步方法生成多个实例，
     * @param context
     */
    private PhoneDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    /**
     * 为应用程序提供一个单一的入口，保证应用程序使用同一个对象操作数据库，不会因为对象不同而使同步方法失效
     * @param context 上下文
     * @return  instance
     */
    public static PhoneDBHelper getInstance(Context context){
        if(instance==null)
            instance=new PhoneDBHelper(context);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        BtLogger.e(TAG, "onCreate-mTable=" + CONTACTS_TABLE);
        //以下任一语句都正确
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + CONTACTS_TABLE
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, number VARCHAR, type VARCHAR, datetime VARCHAR)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + CALL_LOG_TABLE
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, number VARCHAR, type VARCHAR, datetime VARCHAR)");
//        String sql = "Create table " + CHANNEL_TABLE + "("
//                + TB_ID + " integer primary key autoincrement,"
//                + TB_NAME + " text, "
//                + TB_NUMBER + " text );";
//        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        BtLogger.e(TAG, "onUpgrade-mTable="+CONTACTS_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE  IF EXISTS " + CONTACTS_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE  IF EXISTS " + CALL_LOG_TABLE);
        onCreate(sqLiteDatabase);
    }
}