package com.swein.shexoplayerdemo.exoplayer.constants;

public class PlayerConstants {

    /**
     * 播放器的状态
     */
    public enum PlayerState {
        PLAY, PAUSE, STOP
    }

    /**
     * 播放器播放链接的类型
     */
    public enum URLType {
        MP4, HLS, RTMP
    }

    /**
     * 正常模式，画中画模式
     */
    public enum Mode {
        NORMAL, PIP
    }
}
