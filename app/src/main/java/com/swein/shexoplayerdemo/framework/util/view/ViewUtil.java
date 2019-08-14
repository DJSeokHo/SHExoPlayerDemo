package com.swein.shexoplayerdemo.framework.util.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewUtil {

    public static View inflateView(Context context, int resource, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(resource, viewGroup);
    }
}
