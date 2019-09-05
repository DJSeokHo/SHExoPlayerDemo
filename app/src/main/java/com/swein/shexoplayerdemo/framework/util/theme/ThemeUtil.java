package com.swein.shexoplayerdemo.framework.util.theme;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

public class ThemeUtil {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setWindowStatusBarColor(Activity activity, int color) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

}
