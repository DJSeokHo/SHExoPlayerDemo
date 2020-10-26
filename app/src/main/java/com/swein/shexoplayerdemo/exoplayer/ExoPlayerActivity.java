package com.swein.shexoplayerdemo.exoplayer;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentActivity;

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

/**
 * 播放器容器，播放器，播放器控制器容器，播放器控制器，点击层
 * 的层级关系
 *
 * Activity 承载播放器的容器
 * 播放器的容器承载播放器本身和控制器的容器
 * 控制器的容器承载播放器的控制层
 *
 * 自底向上分别是
 *
 * 1. 主activity
 * 2. 播放器容器
 * 3. 播放器
 * 4. 播放器控制界面的容器
 * 5. 播放器控制界面
 *
 * 分离各层，单独控制，是核心
 */
public class ExoPlayerActivity extends FragmentActivity {

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

        /*
        初始化播放器的尺寸，16：9
         */
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DisplayUtils.getScreenWidthPx(this), (int)(DisplayUtils.getScreenWidthPx(this) * Constants.RATE));
        frameLayoutPlayerContainer.setLayoutParams(layoutParams);
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {

        playerViewHolder = new PlayerViewHolder(this);

        /*
        实现PIP(画中画)的悬浮窗委托(接口)，用匿名对象实现接口
         */
        playerViewHolder.setFloatingDelegate(new PlayerViewHolder.FloatingPlayerViewHolderDelegate() {

            /*
            PIP在手机桌面上需要可以拖动，所以需要记录一些坐标
            这里原理就不再叙述
             */
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

                // 更新悬浮窗在桌面上的位置
                windowManager.updateViewLayout(playerViewHolder.getView(), layoutParams);

                lastX = nowX;
                lastY = nowY;
            }
        });

        /*
        实现播放器的委托，用匿名对象实现接口
         */
        playerViewHolder.setDelegate(new PlayerViewHolder.PlayerViewHolderDelegate() {
            @Override
            public void onCloseClicked() {
                /*
                关闭播放器，退出activity
                 */
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
                /*
                横屏全画面和竖屏切换
                 */
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(ExoPlayerActivity.this)) {
                        if (! Settings.canDrawOverlays(ExoPlayerActivity.this)) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            // 这里没有添加返回结果的检查，即没有在onActivityResult里做检查，如果有需要自行添加
                            startActivityForResult(intent,999);
                        }
                    }
                    else {
                         /*
                            进入PIP(画中画)模式或者退出PIP模式
                             */
                        togglePIP();
                    }
                }
                else {
                    togglePIP();
                }

            }

            @Override
            public void onPlayerFinishPlay() {
                /*
                当播放器播放完成后，自动退出PIP模式
                 */
                if(playerViewHolder.getMode() == PlayerConstants.Mode.PIP) {
                    playerViewHolder.setMode(PlayerConstants.Mode.NORMAL);
                    openDetail();
                }
            }
        });

        playerViewHolder.setMode(PlayerConstants.Mode.NORMAL);

        frameLayoutPlayerContainer.removeAllViews();
        frameLayoutPlayerContainer.addView(playerViewHolder.getView());

        playerViewHolder.setTitle("Title");

        /*
        这里作为测试，你可以在这里控制播放器的播放内容
        不用我说，你也知道。。。这里只能3选1，要么是RTMP的直播流，要么是MP4的文件，要么是HLS的流文件
         */
        playerViewHolder.setUrl(Constants.MP4_VOD_URL, PlayerConstants.URLType.MP4);
//        playerViewHolder.setUrl(Constants.HLS_VOD_URL, PlayerConstants.URLType.HLS);
//        playerViewHolder.setUrl(Constants.RTMP_URL, PlayerConstants.URLType.RTMP);

        playerViewHolder.initPlayer();
    }

    private void togglePIP() {
        if(playerViewHolder.getMode() == PlayerConstants.Mode.NORMAL) {
            PlayerViewHolder.enterFloatingWindow = true;
            playerViewHolder.setMode(PlayerConstants.Mode.PIP);
            createFloatingWindow();
        }
        else {
            PlayerViewHolder.enterFloatingWindow = false;
            playerViewHolder.setMode(PlayerConstants.Mode.NORMAL);
            openDetail();
        }
    }

    /**
     * 横竖屏监听
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

    /**
     * 横屏时就全屏播放，并且隐藏系统状态栏
     * 竖屏时就显示状态栏
     */
    private void toggleFullScreen() {

        if(isFullScreen) {

            frameLayoutOtherContainer.setVisibility(View.GONE);

            /*
            全屏观看，隐藏顶部系统状态栏
             */
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

    /**
     * 退出PIP(画中画模式)
     * 回到正常的activity画面
     */
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

    /**
     * 进入PIP(画中画模式)
     */
    private void createFloatingWindow() {

        ILog.iLogDebug(TAG, "createFloatingWindow");

        IntentUtil.intentStartActionBackToHome(ExoPlayerActivity.this);

        /*
        ThreadUtil 是线程池的封装工具类
        先startThread开启线程，完成你想要的操作后
        在Runnable内部继续用startUIThread来回到主线程更新UI
        只有这么好用了，谁用谁知道。
         */
        ThreadUtil.startThread(new Runnable() {
            @Override
            public void run() {

                windowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);

                layoutParams = new WindowManager.LayoutParams();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                }
                else {
                    layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                }

                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

                layoutParams.gravity = Gravity.CENTER;

                layoutParams.x = 0;
                layoutParams.y = 0;


                /*
                画中画模式，播放器窗口的大小，我这里长是250dp，宽就按16:9自动计算，你可以改成你想要的大小
                 */
                layoutParams.width = DensityUtil.dip2px(ExoPlayerActivity.this, 250);
                layoutParams.height = DensityUtil.dip2px(ExoPlayerActivity.this, (int)(250 * Constants.RATE));

                layoutParams.format = PixelFormat.TRANSPARENT;

                /*
                ThreadUtil 是线程池的封装工具类
                先startThread开启线程，完成你想要的操作后
                在Runnable内部继续用startUIThread来回到主线程更新UI
                只有这么好用了，谁用谁知道。
                 */
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

    /**
     * 继续播放
     * 用于响应activity的生命周期
     */
    private void resumePlay() {
        if(playerViewHolder != null && !PlayerViewHolder.enterFloatingWindow) {
            playerViewHolder.resumePlay();
        }
    }

    /**
     * 暂停播放
     * 用于响应activity的生命周期
     */
    private void pausePlay() {
        if(playerViewHolder != null && !PlayerViewHolder.enterFloatingWindow) {
            playerViewHolder.pause();
        }
    }

    /**
     * 销毁播放器
     *
     * 养成好习惯，手动释放资源，方便内存回收
     * 养成好习惯，手动释放资源，方便内存回收
     * 养成好习惯，手动释放资源，方便内存回收
     *
     * 说三遍才会印象深刻
     *
     */
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
