package com.swein.shexoplayerdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import com.swein.shexoplayerdemo.exoplayer.ExoPlayerActivity;
import com.swein.shexoplayerdemo.framework.util.activity.ActivityUtil;
import com.swein.shexoplayerdemo.framework.util.toast.ToastUtil;

public class MainActivity extends AppCompatActivity {

    private final static int ACTION_MANAGE_OVERLAY_PERMISSION_CODE = 101;

    private boolean closeFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonPlayer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.startNewActivityWithoutFinish(MainActivity.this, ExoPlayerActivity.class);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }
    }

    /**
     * 检查浮窗权限，用于PIP(画中画)
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        if (!Settings.canDrawOverlays(MainActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_CODE);
        }
    }

    /**
     * 检查浮窗权限的结果，不同意权限的话就直接退出，你也可以修改成自己的逻辑
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted
                finish();
            }
        }
    }

    /**
     * 连续按两次回退键就退出app
     */
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
