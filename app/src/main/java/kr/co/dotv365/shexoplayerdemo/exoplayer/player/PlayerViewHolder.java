package kr.co.dotv365.shexoplayerdemo.exoplayer.player;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
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
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import java.util.Timer;

import kr.co.dotv365.shexoplayerdemo.R;
import kr.co.dotv365.shexoplayerdemo.exoplayer.constants.PlayerConstants;
import kr.co.dotv365.shexoplayerdemo.framework.util.animation.AnimationUtil;
import kr.co.dotv365.shexoplayerdemo.framework.util.date.DateUtil;
import kr.co.dotv365.shexoplayerdemo.framework.util.debug.log.ILog;
import kr.co.dotv365.shexoplayerdemo.framework.util.thread.ThreadUtil;
import kr.co.dotv365.shexoplayerdemo.framework.util.timer.TimerUtil;
import kr.co.dotv365.shexoplayerdemo.framework.util.view.ViewUtil;

public class PlayerViewHolder {

    public interface PlayerViewHolderDelegate {
        void onCloseClicked();
        void onFullScreenClicked();
        void onPIPClicked();
    }

    public interface FloatingPlayerViewHolderDelegate {
        void onActionDown(MotionEvent event);
        void onActionMove(MotionEvent event);
    }

    private final static String TAG = "PlayerViewHolder";

    private final static int PLAYER_STATE_STOP = 1;
    private final static int PLAYER_STATE_PLAYING = 3;
    private final static int PLAYER_STATE_FINISHED = 4;

    private View view;

    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView exoPlayerView;
    private MediaSource videoSource;

    private PlayerViewHolderDelegate playerViewHolderDelegate;
    private FloatingPlayerViewHolderDelegate floatingPlayerViewHolderDelegate;

    private FrameLayout frameLayoutController;
    private ImageButton imageButtonClose;
    private ImageButton imageButtonPlay;
    private ImageButton imageButtonPlayBig;
    private ImageButton imageButtonFullScreen;
    private ImageButton imageButtonPIP;
    private SeekBar seekBar;
    private TextView textViewTitle;
    private TextView textViewTime;

    private View viewCover;

    private PlayerConstants.PlayerState playerState = PlayerConstants.PlayerState.STOP;

    private String url;
    private PlayerConstants.URLType urlType = PlayerConstants.URLType.HLS;

    private PlayerConstants.Mode mode = PlayerConstants.Mode.NORMAL;

    private FrameLayout frameLayoutProgress;

    private Player.EventListener eventListener = new Player.EventListener() {

        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            ILog.iLogDebug(TAG, "onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            ILog.iLogDebug(TAG, "onLoadingChanged " + isLoading);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            ILog.iLogDebug(TAG, "onPlayerStateChanged " + playWhenReady + " " + playbackState);

            if(!playWhenReady) {
                stopSync();
                return;
            }

            if(playWhenReady && PLAYER_STATE_PLAYING == playbackState) {
                startSync();
                return;
            }

            if(playWhenReady && PLAYER_STATE_STOP == playbackState) {
                stopSync();
                return;
            }

            if(playWhenReady && PLAYER_STATE_FINISHED == playbackState) {
                // play finished
                playerState = PlayerConstants.PlayerState.STOP;
                updateControllerUI();
                frameLayoutController.startAnimation(AnimationUtil.show(view.getContext()));
                frameLayoutController.setVisibility(View.VISIBLE);
                setSeekBar(0, 0);
                stopSync();
                updateTime(0, 0);
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            ILog.iLogDebug(TAG, "onPlayerError");
            hideProgress();
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            ILog.iLogDebug(TAG, "onPositionDiscontinuity");
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {
            ILog.iLogDebug(TAG, "onSeekProcessed");
            ILog.iLogDebug(TAG, simpleExoPlayer.getCurrentPosition());
        }
    };

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
            ILog.iLogDebug(TAG, simpleExoPlayer.getDuration());
            ILog.iLogDebug(TAG, simpleExoPlayer.getCurrentPosition());
            ILog.iLogDebug(TAG, simpleExoPlayer.getContentDuration());
            ILog.iLogDebug(TAG, simpleExoPlayer.getTotalBufferedDuration());

            hideProgress();

            if(urlType == PlayerConstants.URLType.RTMP) {
                return;
            }

            setSeekBar((int)simpleExoPlayer.getDuration(), (int)getCurrentPosition());
        }
    };

    private AudioListener audioListener = new AudioListener() {

        @Override
        public void onAudioSessionId(int audioSessionId) {

        }

        @Override
        public void onAudioAttributesChanged(AudioAttributes audioAttributes) {

        }

        @Override
        public void onVolumeChanged(float volume) {
            ILog.iLogDebug(TAG, "onVolumeChanged " + volume);
        }
    };

    private Timer timer;

    public PlayerViewHolder(Context context) {
        view = ViewUtil.inflateView(context, R.layout.view_holder_player, null);
        findView();
        setListener();
    }

    private void findView() {
        frameLayoutProgress = view.findViewById(R.id.frameLayoutProgress);
        frameLayoutController = view.findViewById(R.id.frameLayoutController);
        imageButtonClose = view.findViewById(R.id.imageButtonClose);
        imageButtonPlay = view.findViewById(R.id.imageButtonPlay);
        imageButtonPlayBig = view.findViewById(R.id.imageButtonPlayBig);
        imageButtonFullScreen = view.findViewById(R.id.imageButtonFullScreen);
        imageButtonPIP = view.findViewById(R.id.imageButtonPIP);

        textViewTitle = view.findViewById(R.id.textViewTitle);
        textViewTime = view.findViewById(R.id.textViewTime);

        viewCover = view.findViewById(R.id.viewCover);
    }

    public void setMode(PlayerConstants.Mode mode) {
        this.mode = mode;

        if(mode == PlayerConstants.Mode.NORMAL) {
            imageButtonFullScreen.setVisibility(View.VISIBLE);
        }
        else {
            imageButtonFullScreen.setVisibility(View.GONE);
        }
    }

    public PlayerConstants.Mode getMode() {
        return mode;
    }

    private void setListener() {

        viewCover.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if(mode == PlayerConstants.Mode.FLOATTING) {
                            floatingPlayerViewHolderDelegate.onActionDown(event);
                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if(mode == PlayerConstants.Mode.FLOATTING) {
                            floatingPlayerViewHolderDelegate.onActionMove(event);
                            return true;
                        }
                       break;

                    case MotionEvent.ACTION_UP:
                        toggleController();
                        return true;
                }

                return false;
            }
        });

        imageButtonPIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerViewHolderDelegate.onPIPClicked();
                toggleController();
            }
        });

        imageButtonFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerViewHolderDelegate.onFullScreenClicked();
            }
        });

        imageButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerViewHolderDelegate.onCloseClicked();
            }
        });

        imageButtonPlayBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                updateControllerUI();
            }
        });

        imageButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (playerState) {
                    case PLAY:
                        stopWithReset();
                        break;

                    case STOP:
                        reloadPlay();
                        toggleController();
                        break;
                }

                updateControllerUI();
            }
        });
    }

    public void setUrl(String url, PlayerConstants.URLType urlType) {
        this.url = url;
        this.urlType = urlType;

    }

    public void setDelegate(PlayerViewHolderDelegate playerViewHolderDelegate) {
        this.playerViewHolderDelegate = playerViewHolderDelegate;

    }

    public void setFloatingDelegate(FloatingPlayerViewHolderDelegate floatingPlayerViewHolderDelegate) {
        this.floatingPlayerViewHolderDelegate = floatingPlayerViewHolderDelegate;

    }

    public void initPlayer() {

        exoPlayerView = view.findViewById(R.id.exoPlayerView);

        //initiate Player
        //Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        //Create the player
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(view.getContext(), trackSelector);

        simpleExoPlayer.addListener(eventListener);
        simpleExoPlayer.addVideoListener(videoListener);
        simpleExoPlayer.addAudioListener(audioListener);

        exoPlayerView.setPlayer(simpleExoPlayer);
        exoPlayerView.setUseController(false);
        createMediaSource();
        resumePlay();

        updateControllerUI();
        toggleController();
    }

    private void startSync() {

        if(urlType == PlayerConstants.URLType.RTMP) {
            return;
        }

        timer = TimerUtil.createTimerTask(0, 1000, new Runnable() {
            @Override
            public void run() {
                ILog.iLogDebug(TAG, getCurrentPosition());
                if(seekBar != null) {
                    seekBar.setProgress((int)getCurrentPosition());
                    updateTime(getCurrentPosition(), getDuration());
                }
            }
        });
    }

    private void updateTime(long currentMS, long totalMS) {

        if(urlType == PlayerConstants.URLType.RTMP) {
            // rtmp has no time
            currentMS = 0;
            totalMS = 0;
        }

        String current = DateUtil.getDateFromMilliSeconds(currentMS);
        String total = DateUtil.getDateFromMilliSeconds(totalMS);
        textViewTime.setText(String.format(view.getContext().getString(R.string.player_time), current, total));
    }

    private void stopSync() {

        if(urlType == PlayerConstants.URLType.RTMP) {
            return;
        }

        TimerUtil.cancelTimerTask(timer);
    }

    private void setSeekBar(int max, int progress) {

        if(seekBar == null) {
            seekBar = view.findViewById(R.id.seekBar);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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
                    toggleController();
                }
            });
        }

        if(urlType == PlayerConstants.URLType.RTMP) {
            seekBar.setEnabled(false);
            textViewTime.setVisibility(View.GONE);
            return;
        }
        else {
            seekBar.setEnabled(true);
            textViewTime.setVisibility(View.VISIBLE);
        }

        seekBar.setMax(max);
        seekBar.setProgress(progress);
    }

    public void setTitle(String title) {
        textViewTitle.setText(title);
    }

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

    public void setFullScreen() {
        imageButtonFullScreen.setImageResource(R.drawable.icon_normal_screen);
    }

    public void setNormalScreen() {
        imageButtonFullScreen.setImageResource(R.drawable.icon_full_screen);
    }

    private void reloadPlay() {
        createMediaSource();
        resumePlay();
    }

    public void resumePlay() {
        simpleExoPlayer.setPlayWhenReady(true);
        playerState = PlayerConstants.PlayerState.PLAY;

    }

    public void pause() {
        simpleExoPlayer.setPlayWhenReady(false);
        playerState = PlayerConstants.PlayerState.PAUSE;

    }

    public void stopWithReset() {
        simpleExoPlayer.stop(true);
        playerState = PlayerConstants.PlayerState.STOP;
        videoSource = null;
    }

    private void updateControllerUI() {

        switch (playerState) {

            case PLAY:
                imageButtonPlayBig.setImageResource(R.drawable.icon_pause);
                imageButtonPlay.setImageResource(R.drawable.icon_stop);
                break;

            case PAUSE:
                imageButtonPlayBig.setImageResource(R.drawable.icon_play);
                imageButtonPlay.setImageResource(R.drawable.icon_stop);
                break;

            case STOP:
                imageButtonPlayBig.setImageResource(R.drawable.icon_play);
                imageButtonPlay.setImageResource(R.drawable.icon_play);
                break;
        }
    }

    private void seekTo(long ms) {
        ILog.iLogDebug(TAG, "seek to " + ms);
        simpleExoPlayer.seekTo(ms);
    }

    private long getDuration() {
        return simpleExoPlayer.getDuration();
    }

    private long getCurrentPosition() {
        return simpleExoPlayer.getCurrentPosition();
    }

    private void toggleController() {

        if(frameLayoutController.getVisibility() == View.VISIBLE) {

            frameLayoutController.startAnimation(AnimationUtil.hide(view.getContext()));
            frameLayoutController.setVisibility(View.GONE);
        }
        else {
            frameLayoutController.startAnimation(AnimationUtil.show(view.getContext()));
            frameLayoutController.setVisibility(View.VISIBLE);
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
    }

    @Override
    protected void finalize() throws Throwable {
        ILog.iLogDebug(TAG, "finalize");
        super.finalize();
    }
}
