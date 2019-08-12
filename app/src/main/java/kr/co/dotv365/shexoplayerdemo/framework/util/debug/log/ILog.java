package kr.co.dotv365.shexoplayerdemo.framework.util.debug.log;


import android.util.Log;


import kr.co.dotv365.shexoplayerdemo.BuildConfig;


public class ILog {

    public static void iLogDebug(String tag, Object content) {
        if (BuildConfig.DEBUG) {
            Log.d("[- ILog Print -]===>>" + tag, content.toString());
        }
    }

}
