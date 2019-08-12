package kr.co.dotv365.shexoplayerdemo.exoplayer;

import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import kr.co.dotv365.shexoplayerdemo.R;
import kr.co.dotv365.shexoplayerdemo.constants.Constants;
import kr.co.dotv365.shexoplayerdemo.exoplayer.vod.VODPlayerViewHolder;
import kr.co.dotv365.shexoplayerdemo.framework.util.debug.log.ILog;
import kr.co.dotv365.shexoplayerdemo.framework.util.display.DisplayUtils;
import kr.co.dotv365.shexoplayerdemo.framework.util.theme.ThemeUtil;

public class ExoPlayerActivity extends AppCompatActivity {

    private final static String TAG = "ExoPlayerActivity";


    private FrameLayout frameLayoutPlayerContainer;

    private boolean isFullScreen = false;

    private VODPlayerViewHolder vodPlayerViewHolder;

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

        vodPlayerViewHolder = new VODPlayerViewHolder(this);

        frameLayoutPlayerContainer.removeAllViews();
        frameLayoutPlayerContainer.addView(vodPlayerViewHolder.getView());

        vodPlayerViewHolder.initPlayer(Constants.VOD_URL);
        vodPlayerViewHolder.play();
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
        }
        else {

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);


            // set dp
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dipToPx(this, 200));
            frameLayoutPlayerContainer.setLayoutParams(layoutParams);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        ILog.iLogDebug(TAG, "onResume");
        if(vodPlayerViewHolder != null) {
            vodPlayerViewHolder.play();
        }
    }

    @Override
    protected void onPause() {
        ILog.iLogDebug(TAG, "onPause");
        if(vodPlayerViewHolder != null) {
            vodPlayerViewHolder.pause();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        ILog.iLogDebug(TAG, "onStop");
        if(vodPlayerViewHolder != null) {
            vodPlayerViewHolder.pause();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(vodPlayerViewHolder != null) {
            vodPlayerViewHolder.stopWithReset();
            vodPlayerViewHolder.destroy();
            vodPlayerViewHolder = null;
        }
        super.onDestroy();
    }

}
