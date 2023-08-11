package com.wellee.connect.utils;

import android.os.Handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author : liwei
 * 创建日期 : 2019/11/1 11:25
 * 邮   箱 : liwei@worken.cn
 * 功能描述 :
 */
public class HandlerUtils {

    private static final ThreadGroup PLUGIN_THREAD_GROUP = new ThreadGroup("PG");
    private static final Map<String, LooperThread> LOOPER_THREAD_MAP = new ConcurrentHashMap<>();

    private static Handler handler = createHandler("AsyncHandler");

    public static Handler createHandler(String name) {
        synchronized (LOOPER_THREAD_MAP) {
            LooperThread looperThread = LOOPER_THREAD_MAP.get(name);
            if (null == looperThread) {
                looperThread = new LooperThread(PLUGIN_THREAD_GROUP, name);
                looperThread.start();
                LOOPER_THREAD_MAP.put(name, looperThread);
            }
            return looperThread.getHandler();
        }
    }

    public static void delayTask(int delaySeconds, final Runnable runnable) {
        handler.postDelayed(runnable, TimeUnit.SECONDS.toMillis(delaySeconds));
    }
}
