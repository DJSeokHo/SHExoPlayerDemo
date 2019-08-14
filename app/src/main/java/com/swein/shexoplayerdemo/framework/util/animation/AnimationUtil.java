package com.swein.shexoplayerdemo.framework.util.animation;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.swein.shexoplayerdemo.R;

public class AnimationUtil {

    public static Animation show(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.fade_in);
    }

    public static Animation hide(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.fade_out);
    }

    public static void shakeView(Context context, View view) {
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        view.startAnimation(shake);
    }

    public static void jellyView(Context context, View view) {
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.jelly);
        view.startAnimation(shake);
    }
}
