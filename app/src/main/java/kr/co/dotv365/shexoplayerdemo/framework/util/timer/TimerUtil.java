package kr.co.dotv365.shexoplayerdemo.framework.util.timer;

import android.os.CountDownTimer;

import java.util.Timer;
import java.util.TimerTask;

import kr.co.dotv365.shexoplayerdemo.framework.util.thread.ThreadUtil;

/**
 * Created by seokho on 09/02/2018.
 */

public class TimerUtil {

    public static Timer createTimerTask(long delay, long period, final Runnable runnable) {

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ThreadUtil.startUIThread(0, new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                    }
                });
            }
        };

        timer.scheduleAtFixedRate(task, delay, period);

        return timer;
    }

    public static void cancelTimerTask(Timer timer) {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
