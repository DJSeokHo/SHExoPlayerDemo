package com.swein.shexoplayerdemo.exoplayer.player;

import android.content.Context;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.swein.shexoplayerdemo.R;
import com.swein.shexoplayerdemo.exoplayer.constants.PlayerConstants;
import com.swein.shexoplayerdemo.exoplayer.player.controller.PlayerControllerViewHolder;
import com.swein.shexoplayerdemo.framework.util.animation.AnimationUtil;
import com.swein.shexoplayerdemo.framework.util.date.DateUtil;
import com.swein.shexoplayerdemo.framework.util.debug.log.ILog;
import com.swein.shexoplayerdemo.framework.util.timer.TimerUtil;
import com.swein.shexoplayerdemo.framework.util.view.ViewUtil;

import java.util.Timer;

/**
 * 播放器容器类
 * 用于控制播放器，比如播放，暂停，停止，改变UI，等等。。。
 */
public class PlayerViewHolder {

    /*
        播放器容器的委托(接口)
     */
    public interface PlayerViewHolderDelegate {
        void onCloseClicked();
        void onFullScreenClicked();
        void onPIPClicked();
        void onPlayerFinishPlay();
    }

    /*
        PIP(画中画)播放器的委托(接口)
     */
    public interface FloatingPlayerViewHolderDelegate {
        void onActionDown(MotionEvent event);
        void onActionMove(MotionEvent event);
    }

    private final static String TAG = "PlayerViewHolder";

    // 停止播放
    private final static int PLAYER_STATE_STOP = 1;

    // 播放
    private final static int PLAYER_STATE_PLAYING = 3;

    // 播放结束时
    private final static int PLAYER_STATE_FINISHED = 4;

    private View view;

    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView exoPlayerView;
    private MediaSource videoSource;

    private PlayerViewHolderDelegate playerViewHolderDelegate;
    private FloatingPlayerViewHolderDelegate floatingPlayerViewHolderDelegate;

    private PlayerControllerViewHolder playerControllerViewHolder;
    private FrameLayout frameLayoutControllerContainer;

    private View viewCover;

    private PlayerConstants.PlayerState playerState = PlayerConstants.PlayerState.STOP;

    public static boolean enterFloatingWindow = false;

    private String url;
    private PlayerConstants.URLType urlType = PlayerConstants.URLType.HLS;

    private PlayerConstants.Mode mode = PlayerConstants.Mode.NORMAL;

    private FrameLayout frameLayoutProgress;

    /**
     * 播放器时间监听，必须有
     */
    private Player.EventListener eventListener = new Player.EventListener() {

        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
            /*
            这里不需要，我就没有管
             */
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            ILog.iLogDebug(TAG, "onTracksChanged");
             /*
            这里不需要，我就没有管
             */
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            ILog.iLogDebug(TAG, "onLoadingChanged " + isLoading);
             /*
            这里不需要，我就没有管
             */
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            /*
             * playWhenReady 意思是播放器的状态
             */
            ILog.iLogDebug(TAG, "onPlayerStateChanged " + playWhenReady + " " + playbackState);

            if(!playWhenReady) {
                /*
                不播放，停止更新进度条和时间
                 */
                stopSync();
                return;
            }

            if(playWhenReady && PLAYER_STATE_PLAYING == playbackState) {
                 /*
                开始播放，开始更新进度条和时间
                 */
                startSync();
                return;
            }

            if(playWhenReady && PLAYER_STATE_STOP == playbackState) {
                /*
                停止播放，停止更新进度条和时间
                 */
                stopSync();
                return;
            }

            if(playWhenReady && PLAYER_STATE_FINISHED == playbackState) {
                /*
                播放完的时候，停止更新进度条和时间，重置进度条和时间，改变UI
                 */
                playerState = PlayerConstants.PlayerState.STOP;
                playerControllerViewHolder.updateControllerUI(playerState);
                frameLayoutControllerContainer.startAnimation(AnimationUtil.show(view.getContext()));
                frameLayoutControllerContainer.setVisibility(View.VISIBLE);
                if(urlType != PlayerConstants.URLType.RTMP) {
                    setSeekBar(0, 0);
                }

                stopSync();
                updateTime(0, 0);
                playerViewHolderDelegate.onPlayerFinishPlay();
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            /*
            这里不需要，我就没有管
             */
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
             /*
            这里不需要，我就没有管
             */
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            ILog.iLogDebug(TAG, "onPlayerError");
            hideProgress();
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            ILog.iLogDebug(TAG, "onPositionDiscontinuity");
             /*
            这里不需要，我就没有管
             */
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
             /*
            这里不需要，我就没有管
             */
        }

        @Override
        public void onSeekProcessed() {
             /*
            这里不需要，我就没有管
            拉进度条，播放器处理完成时，你可以用
            播放器的 simpleExoPlayer.getCurrentPosition() 这个方法来获取当前进度
            进度是long类型，是当前所处的帧的位置
             */
            ILog.iLogDebug(TAG, "onSeekProcessed");
            ILog.iLogDebug(TAG, simpleExoPlayer.getCurrentPosition());
        }
    };

    /*
    视频类监听，必须要
     */
    private VideoListener videoListener = new VideoListener() {

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            ILog.iLogDebug(TAG, "onVideoSizeChanged " + width + " " + height);
        }

        @Override
        public void onSurfaceSizeChanged(int width, int height) {

        }

        @Override
        public void onRenderedFirstFrame() {
            ILog.iLogDebug(TAG, "onRenderedFirstFrame");
//            ILog.iLogDebug(TAG, "onRenderedFirstFrame");
//            ILog.iLogDebug(TAG, simpleExoPlayer.getDuration());
//            ILog.iLogDebug(TAG, simpleExoPlayer.getCurrentPosition());
//            ILog.iLogDebug(TAG, simpleExoPlayer.getContentDuration());
//            ILog.iLogDebug(TAG, simpleExoPlayer.getTotalBufferedDuration());
//
//            ILog.iLogDebug(TAG, "getVideoFormat " + (simpleExoPlayer.getVideoFormat() == null));
//            ILog.iLogDebug(TAG, "width " + simpleExoPlayer.getVideoFormat().width);
//            ILog.iLogDebug(TAG, "height " + simpleExoPlayer.getVideoFormat().height);

            /*
            读取完成时，开始渲染第一帧画面时，
            隐藏读取UI
            设置进度条的当前时间和总时长
            如果是直播流RTMP的话，就不需要进度条了。
             */
            hideProgress();

            if(urlType == PlayerConstants.URLType.RTMP) {
                return;
            }

            setSeekBar((int)simpleExoPlayer.getDuration(), (int)getCurrentPosition());
        }
    };

    /*
     * 这里我没有使用播放器的音量控制，需要的话可以自己实现
     * 我就只是用是系统按钮来控制音量
     */
    private AudioListener audioListener = new AudioListener() {

        @Override
        public void onAudioSessionId(int audioSessionId) {

        }

        @Override
        public void onAudioAttributesChanged(AudioAttributes audioAttributes) {

        }

        @Override
        public void onVolumeChanged(float volume) {

        }
    };

    // 计时器
    private Timer timer;

    /**
     * 构造方法
     * @param context 上下文
     */
    public PlayerViewHolder(Context context) {
        view = ViewUtil.inflateView(context, R.layout.view_holder_player, null);
        findView();
        initPlayerController();
        setListener();
    }

    /**
     * 绑定控件
     */
    private void findView() {
        frameLayoutProgress = view.findViewById(R.id.frameLayoutProgress);
        frameLayoutControllerContainer = view.findViewById(R.id.frameLayoutControllerContainer);

        /*
        用于处理点击事件
         */
        viewCover = view.findViewById(R.id.viewCover);
    }

    /**
     * 初始化并载入播放器控制容器
     */
    private void initPlayerController() {
        playerControllerViewHolder = new PlayerControllerViewHolder(view.getContext(), new PlayerControllerViewHolder.PlayerControllerViewHolderDelegate() {
            @Override
            public void onButtonCloseClicked() {
                playerViewHolderDelegate.onCloseClicked();
            }

            @Override
            public void onButtonPlayClicked() {
                switch (playerState) {
                    case PLAY:
                        stopWithReset();
                        break;

                    case STOP:
                        reloadPlay();
                        toggleController();
                        break;
                }

                /*
                更新播放器控制器UI
                 */
                playerControllerViewHolder.updateControllerUI(playerState);
            }

            @Override
            public void onButtonPlayBigClicked() {
                switch (playerState) {
                    case PLAY:
                        pause();
                        break;

                    case PAUSE:
                        resumePlay();
                        toggleController();
                        break;

                    case STOP:
                        reloadPlay();
                        toggleController();
                        break;
                }

                 /*
                更新播放器控制器UI
                 */
                playerControllerViewHolder.updateControllerUI(playerState);
            }

            @Override
            public void onButtonFullScreenClicked() {
                /*
                传递给ExoPlayerActivity来处理全屏
                 */
                playerViewHolderDelegate.onFullScreenClicked();
            }

            @Override
            public void onButtonPIPClicked() {
                toggleController();
                 /*
                传递给ExoPlayerActivity来处理PIP模式
                 */
                playerViewHolderDelegate.onPIPClicked();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTime(seekBar.getProgress(), getDuration());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopSync();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ILog.iLogDebug(TAG, seekBar.getProgress());
                seekTo(seekBar.getProgress());
                showProgress();
            }
        });

        /*
        加载播放器控制界面 到 播放器控制界面容器
         */
        frameLayoutControllerContainer.addView(playerControllerViewHolder.getView());

    }

    /**
     * 切换 PIP模式或正常模式下的播放器控制界面UI
     * @param mode
     */
    public void setMode(PlayerConstants.Mode mode) {
        this.mode = mode;

        playerControllerViewHolder.setMode(mode);

    }

    /**
     * 改变全屏时播放器控制界面的UI
     */
    public void setFullScreen() {
        playerControllerViewHolder.setFullScreen();
    }

    /**
     * 改变正常时播放器控制界面的UI
     */
    public void setNormalScreen() {
        playerControllerViewHolder.setNormalScreen();
    }

    public PlayerConstants.Mode getMode() {
        return mode;
    }

    /**
     * viewCover 是用来接收和处理点击事件的一个view
     * 可以在xml里确认
     * 播放器容器，播放器，播放器控制器容器，播放器控制器，点击层
     * 的层级关系
     *
     * 分离各层，单独控制，是核心
     */
    private void setListener() {

        viewCover.setOnTouchListener(new View.OnTouchListener() {

            long currentTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if(mode == PlayerConstants.Mode.PIP) {
                            currentTime = System.currentTimeMillis();
                            floatingPlayerViewHolderDelegate.onActionDown(event);
                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if(mode == PlayerConstants.Mode.PIP) {
                            floatingPlayerViewHolderDelegate.onActionMove(event);
                            return true;
                        }
                       break;

                    case MotionEvent.ACTION_UP:
                        ILog.iLogDebug(TAG, "touch view cover");

                        if(mode == PlayerConstants.Mode.PIP) {
                            if(System.currentTimeMillis() - currentTime > 500) {
                                currentTime = 0;
                                return true;
                            }
                        }

                        toggleController();
                        return true;
                }

                return false;
            }
        });


    }

    /**
     * 设置地址
     * @param url 播放的链接的地址
     * @param urlType 播放的链接的类型
     */
    public void setUrl(String url, PlayerConstants.URLType urlType) {
        this.url = url;
        this.urlType = urlType;

    }

    /**
     * 设置委托(接口)
     */
    public void setDelegate(PlayerViewHolderDelegate playerViewHolderDelegate) {
        this.playerViewHolderDelegate = playerViewHolderDelegate;
    }

    /**
     * 设置PIP模式的委托(接口)
     * @param floatingPlayerViewHolderDelegate
     */
    public void setFloatingDelegate(FloatingPlayerViewHolderDelegate floatingPlayerViewHolderDelegate) {
        this.floatingPlayerViewHolderDelegate = floatingPlayerViewHolderDelegate;
    }

    /**
     * 初始化EXO播放器
     */
    public void initPlayer() {

        exoPlayerView = view.findViewById(R.id.exoPlayerView);

        exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

        DefaultRenderersFactory rendererFactory = new DefaultRenderersFactory(view.getContext());

        ILog.iLogDebug(TAG, "renderers factory finish");

        //Minimum Video you want to buffer while Playinguo
        int minimum_buffer_duration = 2000;

        //Max Video you want to buffer during PlayBack
        int max_buffer_duration = 5000;

        //Min Video you want to buffer before start Playing it
        int minimum_playback_start_buffer = 1000;

        //Min video You want to buffer when user resumes video
        int minimum_playback_resume_buffer = 2000;

        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 16))
//                .setBufferDurationsMs(
//                        1000,
//                        15000,
//                        500,
//                        0)
                .setBufferDurationsMs(
                        minimum_buffer_duration,        //Minimum Video you want to buffer while Playing
                        max_buffer_duration,            //Max Video you want to buffer during PlayBack
                        minimum_playback_start_buffer,  //Min Video you want to buffer before start Playing it
                        minimum_playback_resume_buffer) //Min video You want to buffer when user resumes video
                .setTargetBufferBytes(C.LENGTH_UNSET)
                .setPrioritizeTimeOverSizeThresholds(true)
                .createDefaultLoadControl();

        ILog.iLogDebug(TAG, "load control finish");

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        ILog.iLogDebug(TAG, "track selection finish");

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(view.getContext(), rendererFactory, trackSelector, loadControl);

        simpleExoPlayer.addListener(eventListener);
        simpleExoPlayer.addVideoListener(videoListener);
        simpleExoPlayer.addAudioListener(audioListener);

        exoPlayerView.setPlayer(simpleExoPlayer);
        exoPlayerView.setUseController(false);

        createMediaSource();

        ILog.iLogDebug(TAG, "create media source finish");

        resumePlay();

        ILog.iLogDebug(TAG, "resume play finish");

        playerControllerViewHolder.updateControllerUI(playerState);
        toggleController();

        if(urlType == PlayerConstants.URLType.RTMP) {
            playerControllerViewHolder.setRTMPType();
        }
        else {
            playerControllerViewHolder.setVODType();
        }
    }

    /**
     * 开始同步进度条和时间
     */
    private void startSync() {

        if(urlType == PlayerConstants.URLType.RTMP) {
            return;
        }

        timer = TimerUtil.createTimerTask(0, 1000, new Runnable() {
            @Override
            public void run() {
                ILog.iLogDebug(TAG, getCurrentPosition());

                playerControllerViewHolder.syncSeekBar((int)getCurrentPosition());
                updateTime(getCurrentPosition(), getDuration());
            }
        });
    }

    public int getVideoWidth() {

        Format format = simpleExoPlayer.getVideoFormat();
        if(format != null) {
            return format.width;
        }
        else {
            return 0;
        }
    }

    public int getVideoHeight() {
        Format format = simpleExoPlayer.getVideoFormat();
        if(format != null) {
            return format.height;
        }
        else {
            return 0;
        }
    }

    private void updateTime(long currentMS, long totalMS) {

        if(urlType == PlayerConstants.URLType.RTMP) {
            // rtmp has no time
            currentMS = 0;
            totalMS = 0;
        }

        String current = DateUtil.getDateFromMilliSeconds(currentMS);
        String total = DateUtil.getDateFromMilliSeconds(totalMS);
        playerControllerViewHolder.syncTime(current, total);
    }

    /**
     * 停止同步进度条和时间
     */
    private void stopSync() {

        if(urlType == PlayerConstants.URLType.RTMP) {
            return;
        }

        TimerUtil.cancelTimerTask(timer);
    }

    /**
     * 设置进度条位置
     */
    private void setSeekBar(int max, int progress) {
        playerControllerViewHolder.setSeekBar(max, progress);
    }

    /**
     * 设置播放器的标题
     */
    public void setTitle(String title) {
        playerControllerViewHolder.setTitle(title);
    }

    /**
     * 建立视频源
     */
    private void createMediaSource() {

        switch (urlType) {
            case MP4: {
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(view.getContext(), Util.getUserAgent(view.getContext(), view.getContext().getString(R.string.app_name)));
                videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
                break;
            }
            case HLS: {
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(view.getContext(), Util.getUserAgent(view.getContext(), view.getContext().getString(R.string.app_name)));
                videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
                break;
            }
            case RTMP: {
                RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
                // This is the MediaSource representing the media to be played.
                videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory).createMediaSource(Uri.parse(url));
                break;
            }
        }

        // Prepare the player with the source.
        simpleExoPlayer.prepare(videoSource);
    }

    /**
     * 播放器重新读取
     */
    private void reloadPlay() {
        createMediaSource();
        resumePlay();
    }

    /**
     * 播放器恢复播放
     */
    public void resumePlay() {
        simpleExoPlayer.setPlayWhenReady(true);
        playerState = PlayerConstants.PlayerState.PLAY;
    }

    /**
     * 播放器暂停播放
     */
    public void pause() {
        simpleExoPlayer.setPlayWhenReady(false);
        playerState = PlayerConstants.PlayerState.PAUSE;
    }

    /**
     * 播放器停止播放
     */
    public void stopWithReset() {
        simpleExoPlayer.stop(true);
        playerState = PlayerConstants.PlayerState.STOP;
        videoSource = null;
    }

    private void seekTo(long ms) {
        ILog.iLogDebug(TAG, "seek to " + ms);
        simpleExoPlayer.seekTo(ms);
    }

    /**
     * 获取视频总长
     * long类型，意思是视频的总帧数
     */
    private long getDuration() {
        return simpleExoPlayer.getDuration();
    }

    /**
     * 获取当前帧数
     */
    private long getCurrentPosition() {
        if(simpleExoPlayer != null) {
            return simpleExoPlayer.getCurrentPosition();
        }

        return 0;
    }

    public PlayerConstants.PlayerState getPlayerState() {
        return playerState;
    }

    private void toggleController() {

        if(frameLayoutControllerContainer.getVisibility() == View.VISIBLE) {

            frameLayoutControllerContainer.startAnimation(AnimationUtil.hide(view.getContext()));
            frameLayoutControllerContainer.setVisibility(View.GONE);
        }
        else {
            frameLayoutControllerContainer.startAnimation(AnimationUtil.show(view.getContext()));
            frameLayoutControllerContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showProgress() {

        if(frameLayoutProgress.getVisibility() == View.VISIBLE) {
            return;
        }

        frameLayoutProgress.startAnimation(AnimationUtil.show(view.getContext()));
        frameLayoutProgress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {

        if(frameLayoutProgress.getVisibility() == View.GONE) {
            return;
        }

        frameLayoutProgress.startAnimation(AnimationUtil.hide(view.getContext()));
        frameLayoutProgress.setVisibility(View.GONE);
    }
    public View getView() {
        return view;
    }

    /**
     * 销毁播放器，取消计时器
     * 养成好习惯，手动释放资源，方便内存回收
     * 养成好习惯，手动释放资源，方便内存回收
     * 养成好习惯，手动释放资源，方便内存回收
     *
     * 说三遍才会印象深刻
     *
     */
    public void destroy() {

        if(simpleExoPlayer != null) {

            simpleExoPlayer.getCurrentPosition();

            if(eventListener != null) {
                simpleExoPlayer.removeListener(eventListener);
                eventListener = null;
            }

            if(videoListener != null) {
                simpleExoPlayer.removeVideoListener(videoListener);
                videoListener = null;
            }

            if(audioListener != null) {
                simpleExoPlayer.removeAudioListener(audioListener);
                audioListener = null;
            }

            TimerUtil.cancelTimerTask(timer);

            simpleExoPlayer.release();
            simpleExoPlayer = null;
            videoSource = null;
        }

        if(playerControllerViewHolder != null) {
            frameLayoutControllerContainer.removeView(playerControllerViewHolder.getView());
            playerControllerViewHolder = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        ILog.iLogDebug(TAG, "finalize");
        super.finalize();
    }
}
