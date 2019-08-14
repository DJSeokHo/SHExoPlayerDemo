package com.swein.shexoplayerdemo.exoplayer;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.swein.shexoplayerdemo.R;
import com.swein.shexoplayerdemo.constants.Constants;
import com.swein.shexoplayerdemo.exoplayer.constants.PlayerConstants;
import com.swein.shexoplayerdemo.exoplayer.player.PlayerViewHolder;
import com.swein.shexoplayerdemo.framework.util.debug.log.ILog;
import com.swein.shexoplayerdemo.framework.util.display.DisplayUtils;
import com.swein.shexoplayerdemo.framework.util.intent.IntentUtil;
import com.swein.shexoplayerdemo.framework.util.size.DensityUtil;
import com.swein.shexoplayerdemo.framework.util.theme.ThemeUtil;
import com.swein.shexoplayerdemo.framework.util.thread.ThreadUtil;

public class ExoPlayerActivity extends AppCompatActivity {

    private final static String TAG = "ExoPlayerActivity";


    private FrameLayout frameLayoutPlayerContainer;
    private FrameLayout frameLayoutOtherContainer;

    private boolean isFullScreen = false;

    private PlayerViewHolder playerViewHolder;

    private WindowManager.LayoutParams layoutParams;
    private WindowManager windowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player);

        ThemeUtil.setWindowStatusBarColor(this, Color.TRANSPARENT);

        findView();

        initPlayer();
    }

    private void findView() {
        frameLayoutPlayerContainer = findViewById(R.id.frameLayoutPlayerContainer);
        frameLayoutOtherContainer = findViewById(R.id.frameLayoutOtherContainer);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DisplayUtils.getScreenWidthPx(this), (int)(DisplayUtils.getScreenWidthPx(this) * Constants.RATE));
        frameLayoutPlayerContainer.setLayoutParams(layoutParams);
    }

    private void initPlayer() {

        playerViewHolder = new PlayerViewHolder(this);

        playerViewHolder.setFloatingDelegate(new PlayerViewHolder.FloatingPlayerViewHolderDelegate() {

            private float lastX;
            private float lastY;
            private float nowX;
            private float nowY;
            private float tranX;
            private float tranY;

            @Override
            public void onActionDown(MotionEvent event) {
                lastX = event.getRawX();
                lastY = event.getRawY();
            }

            @Override
            public void onActionMove(MotionEvent event) {
                nowX = event.getRawX();
                nowY = event.getRawY();

                tranX = nowX - lastX;
                tranY = nowY - lastY;

                layoutParams.x += tranX;
                layoutParams.y += tranY;

                // update floating  window position
                windowManager.updateViewLayout(playerViewHolder.getView(), layoutParams);

                lastX = nowX;
                lastY = nowY;
            }
        });

        playerViewHolder.setDelegate(new PlayerViewHolder.PlayerViewHolderDelegate() {
            @Override
            public void onCloseClicked() {
                if(playerViewHolder.getMode() == PlayerConstants.Mode.NORMAL) {
                    frameLayoutPlayerContainer.removeAllViews();
                    finish();
                }
                else {
                    windowManager.removeView(playerViewHolder.getView());
                    finish();
                }
            }

            @Override
            public void onFullScreenClicked() {
                if(isFullScreen) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    ThreadUtil.startUIThread(3000, new Runnable() {
                        @Override
                        public void run() {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        }
                    });
                }
                else{
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }

            @Override
            public void onPIPClicked() {
                if(playerViewHolder.getMode() == PlayerConstants.Mode.NORMAL) {
                    PlayerViewHolder.enterFloatingWindow = true;
                    playerViewHolder.setMode(PlayerConstants.Mode.FLOATTING);
                    createFloatingWindow();
                }
                else {
                    PlayerViewHolder.enterFloatingWindow = false;
                    playerViewHolder.setMode(PlayerConstants.Mode.NORMAL);
                    openDetail();
                }
            }

            @Override
            public void onPlayerFinishPlay() {
                if(playerViewHolder.getMode() == PlayerConstants.Mode.FLOATTING) {
                    playerViewHolder.setMode(PlayerConstants.Mode.NORMAL);
                    openDetail();
                }
            }
        });

        playerViewHolder.setMode(PlayerConstants.Mode.NORMAL);

        frameLayoutPlayerContainer.removeAllViews();
        frameLayoutPlayerContainer.addView(playerViewHolder.getView());

        playerViewHolder.setTitle("Title");

//        playerViewHolder.setUrl(Constants.MP4_VOD_URL, PlayerConstants.URLType.MP4);
//        playerViewHolder.setUrl(Constants.HLS_VOD_URL, PlayerConstants.URLType.HLS);
        playerViewHolder.setUrl(Constants.RTMP_URL, PlayerConstants.URLType.RTMP);

        playerViewHolder.initPlayer();
    }

    /**
     * need add android:configChanges="orientation|keyboard|layoutDirection|screenSize" in the <activity> tag
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isFullScreen = true;
        }
        else{
            isFullScreen = false;
        }

        toggleFullScreen();
    }

    private void toggleFullScreen() {

        if(isFullScreen) {

            frameLayoutOtherContainer.setVisibility(View.GONE);

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DisplayUtils.getScreenWidthPx(this), DisplayUtils.getScreenHeightPx(this));
            // set full
            frameLayoutPlayerContainer.setLayoutParams(layoutParams);

            if(playerViewHolder != null) {
                playerViewHolder.setFullScreen();
            }
        }
        else {

            frameLayoutOtherContainer.setVisibility(View.VISIBLE);

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            // set dp
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DisplayUtils.getScreenWidthPx(this), (int)(DisplayUtils.getScreenWidthPx(this) * Constants.RATE));
            frameLayoutPlayerContainer.setLayoutParams(layoutParams);

            if(playerViewHolder != null) {
                playerViewHolder.setNormalScreen();
            }
        }
    }

    private void openDetail() {

        windowManager.removeViewImmediate(playerViewHolder.getView());

        // reset player size
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        playerViewHolder.getView().setLayoutParams(layoutParams);

        frameLayoutPlayerContainer.removeAllViews();
        frameLayoutPlayerContainer.addView(playerViewHolder.getView());

        Intent intent = new Intent(getApplicationContext(), ExoPlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void createFloatingWindow() {

        ILog.iLogDebug(TAG, "createFloatingWindow");

        IntentUtil.intentStartActionBackToHome(ExoPlayerActivity.this);

        ThreadUtil.startThread(new Runnable() {
            @Override
            public void run() {

                windowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);

                // TYPE_SYSTEM_ALERT allow receive event
                // TYPE_SYSTEM_OVERLAY over system
                layoutParams = new WindowManager.LayoutParams();
                layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

                // FLAG_NOT_TOUCH_MODAL not block event pass to behind
                // FLAG_NOT_FOCUSABLE
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

                // floating window position
                layoutParams.gravity = Gravity.CENTER;

                layoutParams.x = 0;
                layoutParams.y = 0;

                // floating window size
                layoutParams.width = DensityUtil.dip2px(ExoPlayerActivity.this, 250);
                layoutParams.height = DensityUtil.dip2px(ExoPlayerActivity.this, (int)(250 * Constants.RATE));

                // floating window background
                layoutParams.format = PixelFormat.TRANSPARENT;

                ThreadUtil.startUIThread(0, new Runnable() {
                    @Override
                    public void run() {
                        frameLayoutPlayerContainer.removeAllViews();
                        windowManager.addView(playerViewHolder.getView(), layoutParams);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ILog.iLogDebug(TAG, "onResume");
        resumePlay();
    }

    @Override
    protected void onPause() {
        ILog.iLogDebug(TAG, "onPause");
        pausePlay();
        super.onPause();
    }

    @Override
    protected void onStop() {
        ILog.iLogDebug(TAG, "onStop");
        pausePlay();
        super.onStop();
    }

    private void resumePlay() {
        if(playerViewHolder != null && !PlayerViewHolder.enterFloatingWindow) {
            playerViewHolder.resumePlay();
        }
    }

    private void pausePlay() {
        if(playerViewHolder != null && !PlayerViewHolder.enterFloatingWindow) {
            playerViewHolder.pause();
        }
    }

    private void destroyPlayer() {
        if(playerViewHolder != null) {
            playerViewHolder.stopWithReset();
            playerViewHolder.destroy();
            playerViewHolder = null;
        }
    }

    @Override
    protected void onDestroy() {
        destroyPlayer();
        super.onDestroy();
    }

}
