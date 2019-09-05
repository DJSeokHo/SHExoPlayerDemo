package com.swein.shexoplayerdemo.framework.util.intent;

import android.content.Context;
import android.content.Intent;

public class IntentUtil {

    public static void intentStartActionBackToHome(Context context) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        context.startActivity(intent);

    }

}
