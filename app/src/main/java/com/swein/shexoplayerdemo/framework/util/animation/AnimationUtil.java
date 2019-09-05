package com.swein.shexoplayerdemo.framework.util.animation;

import android.content.Context;
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

}
