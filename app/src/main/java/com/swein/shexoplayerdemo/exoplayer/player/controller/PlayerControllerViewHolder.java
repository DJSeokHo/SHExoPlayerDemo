package com.swein.shexoplayerdemo.exoplayer.player.controller;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.swein.shexoplayerdemo.R;
import com.swein.shexoplayerdemo.exoplayer.constants.PlayerConstants;
import com.swein.shexoplayerdemo.framework.util.view.ViewUtil;

public class PlayerControllerViewHolder {

    public interface PlayerControllerViewHolderDelegate {

        void onButtonCloseClicked();
        void onButtonPlayClicked();
        void onButtonPlayBigClicked();
        void onButtonFullScreenClicked();
        void onButtonPIPClicked();

        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);
        void onStartTrackingTouch(SeekBar seekBar);
        void onStopTrackingTouch(SeekBar seekBar);
    }


    private View view;
    private PlayerControllerViewHolderDelegate playerControllerViewHolderDelegate;

    private ImageButton imageButtonClose;
    private ImageButton imageButtonPlay;
    private ImageButton imageButtonPlayBig;
    private ImageButton imageButtonFullScreen;
    private ImageButton imageButtonPIP;
    private SeekBar seekBar;
    private TextView textViewTitle;
    private TextView textViewTime;


    public PlayerControllerViewHolder(Context context, PlayerControllerViewHolderDelegate playerControllerViewHolderDelegate) {
        this.playerControllerViewHolderDelegate = playerControllerViewHolderDelegate;
        view = ViewUtil.inflateView(context, R.layout.view_holder_player_controller, null);

        findView();

        setListener();
    }

    private void findView() {
        imageButtonClose = view.findViewById(R.id.imageButtonClose);
        imageButtonPlay = view.findViewById(R.id.imageButtonPlay);
        imageButtonPlayBig = view.findViewById(R.id.imageButtonPlayBig);
        imageButtonFullScreen = view.findViewById(R.id.imageButtonFullScreen);
        imageButtonPIP = view.findViewById(R.id.imageButtonPIP);

        textViewTitle = view.findViewById(R.id.textViewTitle);
        textViewTime = view.findViewById(R.id.textViewTime);

        seekBar = view.findViewById(R.id.seekBar);
    }

    private void setListener() {
        imageButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerControllerViewHolderDelegate.onButtonCloseClicked();
            }
        });

        imageButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerControllerViewHolderDelegate.onButtonPlayClicked();
            }
        });

        imageButtonPlayBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerControllerViewHolderDelegate.onButtonPlayBigClicked();
            }
        });

        imageButtonFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerControllerViewHolderDelegate.onButtonFullScreenClicked();
            }
        });

        imageButtonPIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerControllerViewHolderDelegate.onButtonPIPClicked();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                playerControllerViewHolderDelegate.onProgressChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                playerControllerViewHolderDelegate.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playerControllerViewHolderDelegate.onStopTrackingTouch(seekBar);
            }
        });
    }

    public void updateControllerUI(PlayerConstants.PlayerState playerState) {

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

    public void setMode(PlayerConstants.Mode mode) {
        if(mode == PlayerConstants.Mode.NORMAL) {
            imageButtonFullScreen.setVisibility(View.VISIBLE);
        }
        else {
            imageButtonFullScreen.setVisibility(View.GONE);
        }
    }

    public void setSeekBar(int max, int progress) {
        seekBar.setMax(max);
        seekBar.setProgress(progress);
    }

    public void setRTMPType() {
        seekBar.setEnabled(false);
        textViewTime.setVisibility(View.GONE);
    }

    public void setVODType() {
        seekBar.setEnabled(true);
        textViewTime.setVisibility(View.VISIBLE);
    }

    public void syncSeekBar(int progress) {
        seekBar.setProgress(progress);
    }

    public void setTitle(String title) {
        textViewTitle.setText(title);
    }

    public void syncTime(String current, String total) {
        textViewTime.setText(String.format(view.getContext().getString(R.string.player_time), current, total));
    }

    public void setFullScreen() {
        imageButtonFullScreen.setImageResource(R.drawable.icon_normal_screen);
    }

    public void setNormalScreen() {
        imageButtonFullScreen.setImageResource(R.drawable.icon_full_screen);
    }

    public View getView() {
        return view;
    }
}
