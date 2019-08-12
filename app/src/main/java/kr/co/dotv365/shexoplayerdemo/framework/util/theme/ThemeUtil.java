package kr.co.dotv365.shexoplayerdemo.framework.util.theme;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

public class ThemeUtil {

    /**
     * must > API 19
     * put this before setContentView()
     *
     * and add
     * android:fitsSystemWindows="true"
     * to your root layout of xml file
     *
     * @param activity activity
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void transWindow(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    /**
     * must > API 19
     * put this before setContentView()
     *
     * and add
     * android:fitsSystemWindows="true"
     * to your root layout of xml file
     *
     *
     * @param activity
     * @param colorResId
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setWindowStatusBarColorResource(Activity activity, int colorResId) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(colorResId));
    }

    /**
     * must > API 19
     * put this before setContentView()
     *
     * and add
     * android:fitsSystemWindows="true"
     * to your root layout of xml file
     *
     *
     * @param activity
     * @param color
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setWindowStatusBarColor(Activity activity, int color) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

}
