package com.swein.shexoplayerdemo.framework.util.activity;

import android.content.Context;
import android.content.Intent;

public class ActivityUtil {

    public static void startNewActivityWithoutFinish(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        packageContext.startActivity(intent);
    }

}
