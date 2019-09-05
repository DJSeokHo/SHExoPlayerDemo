package com.swein.shexoplayerdemo.exoplayer.player.controller;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.swein.shexoplayerdemo.R;
import com.swein.shexoplayerdemo.framework.util.view.ViewUtil;

public class PlayerControllerViewHolder {

    public interface PlayerControllerViewHolderDelegate {
        void onButtonCloseClicked();
        void onButtonPlayClicked();
        void onButtonPlayBigClicked();
        void onButtonFullScreenClicked();
        void onButtonPIPClicked();
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

            }
        });

        imageButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        imageButtonPlayBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        imageButtonFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        imageButtonPIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    public View getView() {
        return view;
    }
}
