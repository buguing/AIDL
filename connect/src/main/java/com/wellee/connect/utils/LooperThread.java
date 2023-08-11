package com.wellee.connect.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class LooperThread extends Thread {

    private Handler handler;

    public LooperThread(ThreadGroup group, String name) {
        super(group, name);
    }

    @Override
    public void run() {
        Looper.prepare();
        synchronized (this) {
            handler = new RefHandler(this);
            notifyAll();
        }
        Log.i("LooperThread", "handler:" + handler);
        Looper.loop();
    }

    public Handler getHandler() {
        if (!isAlive()) {
            return null;
        }
        synchronized (this) {
            while (isAlive() && handler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return handler;
    }

    static class RefHandler extends Handler {
        private final LooperThread looperThread;

        public RefHandler(LooperThread looperThread) {
            this.looperThread = looperThread;
        }

        public LooperThread getLooperThread() {
            return looperThread;
        }
    }
}
