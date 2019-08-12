package kr.co.dotv365.shexoplayerdemo.framework.util.intent;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;

/**
 * Created by seokho on 03/01/2017.
 */

public class IntentUtil {

    public static void intentStartActionBackToHome(Context context) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        context.startActivity(intent);

    }

    public static void intentStartActivityWithComponentNameWithoutFinish(Activity activity, Class<?> cls) {

        ComponentName componentName = new ComponentName(activity, cls);
        Intent intent = new Intent();
        intent.setComponent(componentName);
        activity.startActivity(intent);

    }

    public static void intentStartActivityWithComponentNameWithFinish(Activity activity, Class<?> cls) {

        ComponentName componentName = new ComponentName(activity, cls);
        Intent intent = new Intent();
        intent.setComponent(componentName);
        activity.startActivity(intent);
        activity.finish();
    }

    public static PendingIntent getPendingIntentWithClass(Context context, Class<?> cls) {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, cls);
        resultIntent.putExtra("fromAppUINotificationCenter", "turnOffScrshtRecordVideoMode");
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(cls);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        1,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        return resultPendingIntent;
    }

}
