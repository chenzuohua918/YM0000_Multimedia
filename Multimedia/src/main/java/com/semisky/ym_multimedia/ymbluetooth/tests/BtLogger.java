package com.semisky.ym_multimedia.ymbluetooth.tests;

import android.util.Log;

/**
 * Created by luoyin on 2017/4/10.
 */

public class BtLogger {

    /**
     * log开关
     */
    public static final boolean DEBUG = true;


    public static void i(String name, String message) {
        if (DEBUG) {
            Log.i(name, message);
        }
    }

    public static void e(String name, String message) {
        if (DEBUG) {
            Log.e(name, message);
        }
    }

    public static void e(String name, String message, Throwable tr) {
        if (DEBUG) {
            Log.e(name, message, tr);
        }
    }

    public static void d(String name, String message) {
        if (DEBUG) {
            Log.d(name, message);
        }
    }

    public static void w(String name, String message) {
        if (DEBUG) {
            Log.w(name, message);
        }
    }


}
