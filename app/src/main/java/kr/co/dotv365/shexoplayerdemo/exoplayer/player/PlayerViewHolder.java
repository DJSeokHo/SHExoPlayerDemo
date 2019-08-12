package kr.co.dotv365.shexoplayerdemo.exoplayer.player;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
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

import kr.co.dotv365.shexoplayerdemo.R;
import kr.co.dotv365.shexoplayerdemo.exoplayer.constants.PlayerConstants;
import kr.co.dotv365.shexoplayerdemo.framework.util.animation.AnimationUtil;
import kr.co.dotv365.shexoplayerdemo.framework.util.debug.log.ILog;
import kr.co.dotv365.shexoplayerdemo.framework.util.thread.ThreadUtil;
import kr.co.dotv365.shexoplayerdemo.framework.util.view.ViewUtil;

public class PlayerViewHolder {

    public interface VODPlayerViewHolderDelegate {
        void onCloseClicked();
        void onFullScreenClicked();
    }

    private final static String TAG = "PlayerViewHolder";

    private final static int PLAY_FINISHED = 4;

    private View view;

    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView exoPlayerView;
    private MediaSource videoSource;

    private VODPlayerViewHolderDelegate vodPlayerViewHolderDelegate;

    private FrameLayout frameLayoutController;
    private ImageButton imageButtonClose;
    private ImageButton imageButtonPlay;
    private ImageButton imageButtonPlayBig;
    private ImageButton imageButtonFullScreen;
    private ImageButton imageButtonPIP;
    private SeekBar seekBar;
    private TextView textViewTitle;
    private TextView textViewTime;

    private PlayerConstants.PlayerState playerState = PlayerConstants.PlayerState.STOP;

    private String url;
    private PlayerConstants.URLType urlType = PlayerConstants.URLType.HLS;

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

            if(playWhenReady && PLAY_FINISHED == playbackState) {
                // play finished
                playerState = PlayerConstants.PlayerState.STOP;
                updateControllerUI();
                frameLayoutController.startAnimation(AnimationUtil.show(view.getContext()));
                frameLayoutController.setVisibility(View.VISIBLE);
                setSeekBar(0, 0);
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
    }

    private void setListener() {
        imageButtonFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vodPlayerViewHolderDelegate.onFullScreenClicked();
            }
        });

        imageButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vodPlayerViewHolderDelegate.onCloseClicked();
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

    public void setDelegate(VODPlayerViewHolderDelegate vodPlayerViewHolderDelegate) {
        this.vodPlayerViewHolderDelegate = vodPlayerViewHolderDelegate;
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

        exoPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleController();
            }
        });

        updateControllerUI();
        toggleController();
    }

    private void setSeekBar(int max, int progress) {

        if(seekBar == null) {
            seekBar = view.findViewById(R.id.seekBar);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    ILog.iLogDebug(TAG, seekBar.getProgress());
                    seekTo(seekBar.getProgress());
                    showProgress();
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

        ILog.iLogDebug(TAG, "max is " + max + " current is " + progress);
        seekBar.setMax(max);
        seekBar.setProgress(progress);

    }

    public void updateTime() {

    }

    public void setTitle(String title) {
        textViewTitle.setText(title);
    }

    private void createMediaSource() {
        if(urlType == PlayerConstants.URLType.MP4) {
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(view.getContext(), Util.getUserAgent(view.getContext(), view.getContext().getString(R.string.app_name)));

            videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
        }
        else if(urlType == PlayerConstants.URLType.HLS) {
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(view.getContext(), Util.getUserAgent(view.getContext(), view.getContext().getString(R.string.app_name)));
            videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
        }
        else if(urlType == PlayerConstants.URLType.RTMP) {
            RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
            // This is the MediaSource representing the media to be played.
            videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory).createMediaSource(Uri.parse(url));
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

    public long getDuration() {
        return simpleExoPlayer.getDuration();
    }

    public long getCurrentPosition() {
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

            ThreadUtil.startUIThread(2000, new Runnable() {
                @Override
                public void run() {
                    if(playerState == PlayerConstants.PlayerState.PLAY && frameLayoutController.getVisibility() == View.VISIBLE) {
                        // auto close when playing
                        frameLayoutController.startAnimation(AnimationUtil.hide(view.getContext()));
                        frameLayoutController.setVisibility(View.GONE);
                    }
                }
            });
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