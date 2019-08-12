package kr.co.dotv365.shexoplayerdemo;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import kr.co.dotv365.shexoplayerdemo.exoplayer.ExoPlayerActivity;
import kr.co.dotv365.shexoplayerdemo.framework.util.activity.ActivityUtil;
import kr.co.dotv365.shexoplayerdemo.framework.util.debug.log.ILog;
import kr.co.dotv365.shexoplayerdemo.framework.util.toast.ToastUtil;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private boolean closeFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonVODPlayer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.startNewActivityWithoutFinish(MainActivity.this, ExoPlayerActivity.class);
            }
        });
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
