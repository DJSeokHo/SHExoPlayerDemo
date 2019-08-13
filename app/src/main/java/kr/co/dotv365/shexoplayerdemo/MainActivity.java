package kr.co.dotv365.shexoplayerdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import java.util.HashMap;

import kr.co.dotv365.shexoplayerdemo.constants.Constants;
import kr.co.dotv365.shexoplayerdemo.exoplayer.ExoPlayerActivity;
import kr.co.dotv365.shexoplayerdemo.framework.util.activity.ActivityUtil;
import kr.co.dotv365.shexoplayerdemo.framework.util.debug.log.ILog;
import kr.co.dotv365.shexoplayerdemo.framework.util.eventsplitshot.eventcenter.EventCenter;
import kr.co.dotv365.shexoplayerdemo.framework.util.eventsplitshot.subject.ESSArrows;
import kr.co.dotv365.shexoplayerdemo.framework.util.toast.ToastUtil;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private final static int ACTION_MANAGE_OVERLAY_PERMISSION_CODE = 101;

    private boolean closeFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initESS();

        findViewById(R.id.buttonVODPlayer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.startNewActivityWithoutFinish(MainActivity.this, ExoPlayerActivity.class);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }
    }

    private void initESS() {
        EventCenter.getInstance().addEventObserver(ESSArrows.EXIT_APP, this, new EventCenter.EventRunnable() {
            @Override
            public void run(String arrow, Object poster, HashMap<String, Object> data) {
                finish();
            }
        });
    }

    private void removeESS() {
        EventCenter.getInstance().removeAllObserver(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        if (!Settings.canDrawOverlays(MainActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent,ACTION_MANAGE_OVERLAY_PERMISSION_CODE);
        }
        else {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted
                finish();
            }
            else {

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

    @Override
    protected void onDestroy() {
        removeESS();
        super.onDestroy();
    }
}
