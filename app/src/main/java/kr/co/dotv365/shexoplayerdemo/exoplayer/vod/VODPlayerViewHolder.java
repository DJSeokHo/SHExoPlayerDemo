package kr.co.dotv365.shexoplayerdemo.exoplayer.vod;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.audio.AudioListener;
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
import kr.co.dotv365.shexoplayerdemo.framework.util.animation.AnimationUtil;
import kr.co.dotv365.shexoplayerdemo.framework.util.debug.log.ILog;
import kr.co.dotv365.shexoplayerdemo.framework.util.view.ViewUtil;

public class VODPlayerViewHolder {

    public interface VODPlayerViewHolderDelegate {

    }

    private final static String TAG = "VODPlayerActivity";

    private View view;

    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView exoPlayerView;

    private VODPlayerViewHolderDelegate vodPlayerViewHolderDelegate;

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
            if(isLoading) {
                showProgress();
            }
            else {
                hideProgress();
            }
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            ILog.iLogDebug(TAG, "onPlayerStateChanged " + playWhenReady + " " + playbackState);

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

    public VODPlayerViewHolder(Context context) {
        view = ViewUtil.inflateView(context, R.layout.view_holder_vod_player, null);
        findView();
        // rtmp
//        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
//        // This is the MediaSource representing the media to be played.
//        MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory).createMediaSource(Uri.parse(Constants.RTMP_URL));
    }

    private void findView() {
        frameLayoutProgress = view.findViewById(R.id.frameLayoutProgress);
    }

    public void setDelegate(VODPlayerViewHolderDelegate vodPlayerViewHolderDelegate) {
        this.vodPlayerViewHolderDelegate = vodPlayerViewHolderDelegate;
    }

    public void initPlayer(String url) {

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

        // This is the MediaSource representing the media to be played.
        // mp4
//        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
//                .createMediaSource(Uri.parse(Constants.VOD_URL));

        // m3u8
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(view.getContext(), Util.getUserAgent(view.getContext(), view.getContext().getString(R.string.app_name)));
        MediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));

        // Prepare the player with the source.
        simpleExoPlayer.prepare(videoSource);
    }


    public void play() {
        simpleExoPlayer.setPlayWhenReady(true);
    }

    public void pause() {
        simpleExoPlayer.setPlayWhenReady(false);
    }

    public void stop() {
        simpleExoPlayer.stop(false);
    }

    public void stopWithReset() {
        simpleExoPlayer.stop(true);
    }

    public void seekTo(long ms) {
        simpleExoPlayer.seekTo(ms);
    }

    public long getDuration() {
        return simpleExoPlayer.getDuration();
    }

    public long getCurrentPosition() {
        return simpleExoPlayer.getCurrentPosition();
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
        }
    }

    public void showProgress() {

        if(frameLayoutProgress.getVisibility() == View.VISIBLE) {
            return;
        }

        frameLayoutProgress.startAnimation(AnimationUtil.show(view.getContext()));
        frameLayoutProgress.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {

        if(frameLayoutProgress.getVisibility() == View.GONE) {
            return;
        }

        frameLayoutProgress.startAnimation(AnimationUtil.hide(view.getContext()));
        frameLayoutProgress.setVisibility(View.GONE);
    }
}
