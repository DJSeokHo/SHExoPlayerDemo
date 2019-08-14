package com.swein.shexoplayerdemo.framework.util.debug.log;


import android.util.Log;

import com.swein.shexoplayerdemo.BuildConfig;


public class ILog {

    public static void iLogDebug(String tag, Object content) {
        if (BuildConfig.DEBUG) {
            Log.d("[- ILog Print -]===>>" + tag, content.toString());
        }
    }

}
