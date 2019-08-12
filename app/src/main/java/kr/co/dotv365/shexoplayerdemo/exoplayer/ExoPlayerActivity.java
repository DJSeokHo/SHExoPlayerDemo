package kr.co.dotv365.shexoplayerdemo.exoplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import kr.co.dotv365.shexoplayerdemo.R;
import kr.co.dotv365.shexoplayerdemo.constants.Constants;
import kr.co.dotv365.shexoplayerdemo.exoplayer.constants.PlayerConstants;
import kr.co.dotv365.shexoplayerdemo.exoplayer.player.PlayerViewHolder;
import kr.co.dotv365.shexoplayerdemo.framework.util.debug.log.ILog;
import kr.co.dotv365.shexoplayerdemo.framework.util.display.DisplayUtils;
import kr.co.dotv365.shexoplayerdemo.framework.util.theme.ThemeUtil;
import kr.co.dotv365.shexoplayerdemo.framework.util.thread.ThreadUtil;

public class ExoPlayerActivity extends AppCompatActivity {

    private final static String TAG = "ExoPlayerActivity";


    private FrameLayout frameLayoutPlayerContainer;

    private boolean isFullScreen = false;

    private PlayerViewHolder vodPlayerViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player);

        ThemeUtil.setWindowStatusBarColor(this, Color.TRANSPARENT);

        findView();

        initVODPlayer();
    }

    private void findView() {
        frameLayoutPlayerContainer = findViewById(R.id.frameLayoutPlayerContainer);
    }

    private void initVODPlayer() {

        vodPlayerViewHolder = new PlayerViewHolder(this);
        vodPlayerViewHolder.setDelegate(new PlayerViewHolder.VODPlayerViewHolderDelegate() {
            @Override
            public void onCloseClicked() {
                frameLayoutPlayerContainer.removeAllViews();
                finish();
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
        });

        frameLayoutPlayerContainer.removeAllViews();
        frameLayoutPlayerContainer.addView(vodPlayerViewHolder.getView());

        vodPlayerViewHolder.setTitle("Title");
        vodPlayerViewHolder.setUrl(Constants.MP4_VOD_URL, PlayerConstants.URLType.MP4);
//        vodPlayerViewHolder.setUrl(Constants.HLS_VOD_URL, PlayerConstants.URLType.HLS);
//        vodPlayerViewHolder.setUrl(Constants.RTMP_URL, PlayerConstants.URLType.RTMP);

        vodPlayerViewHolder.initPlayer();
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

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // set full
            frameLayoutPlayerContainer.setLayoutParams(layoutParams);

            if(vodPlayerViewHolder != null) {
                vodPlayerViewHolder.setFullScreen();
            }
        }
        else {

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            // set dp
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dipToPx(this, 200));
            frameLayoutPlayerContainer.setLayoutParams(layoutParams);

            if(vodPlayerViewHolder != null) {
                vodPlayerViewHolder.setNormalScreen();
            }
        }
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
        if(vodPlayerViewHolder != null) {
            vodPlayerViewHolder.resumePlay();
        }
    }

    private void pausePlay() {
        if(vodPlayerViewHolder != null) {
            vodPlayerViewHolder.pause();
        }
    }

    private void destroyPlayer() {
        if(vodPlayerViewHolder != null) {
            vodPlayerViewHolder.stopWithReset();
            vodPlayerViewHolder.destroy();
            vodPlayerViewHolder = null;
        }
    }

    @Override
    protected void onDestroy() {
        destroyPlayer();
        super.onDestroy();
    }

}
