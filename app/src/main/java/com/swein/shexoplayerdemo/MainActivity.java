package com.swein.shexoplayerdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.swein.shexoplayerdemo.exoplayer.ExoPlayerActivity;
import com.swein.shexoplayerdemo.framework.util.activity.ActivityUtil;
import com.swein.shexoplayerdemo.framework.util.toast.ToastUtil;

public class MainActivity extends FragmentActivity {

    private final static int ACTION_MANAGE_OVERLAY_PERMISSION_CODE = 101;

    private boolean closeFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonPlayer).setOnClickListener(v -> openPlayer());
    }

    private void openPlayer() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_CODE);
            }
            else {
                ActivityUtil.startNewActivityWithoutFinish(MainActivity.this, ExoPlayerActivity.class);
            }

        }
        else {
            ActivityUtil.startNewActivityWithoutFinish(MainActivity.this, ExoPlayerActivity.class);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_CODE) {
            if (Settings.canDrawOverlays(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted
                ActivityUtil.startNewActivityWithoutFinish(MainActivity.this, ExoPlayerActivity.class);
            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getKeyCode() != KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event);
        }

        if (!closeFlag) {
            ToastUtil.showShortToastNormal(this, getString(R.string.local_touch_back_twice_to_exit));
            closeFlag = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    closeFlag = false;
                }
            }, 3000);
        }
        else {

            finish();
        }
        return false;
    }

}
