package com.wellee.connect;

import android.content.Context;
import android.os.IInterface;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceManger {

    private static Map<Context, ServiceBinder<? extends IInterface>> binderMap = new ConcurrentHashMap<>();

    public static <T extends IInterface> ServiceBinder<T> bind(Context context, String packageName, Class<T> aidlClazz, String remoteService) {
        if (null != context.getApplicationContext()) {
            context = context.getApplicationContext();
        }
        if (!binderMap.containsKey(context)) {
            binderMap.put(context, new ServiceBinder<>(context, aidlClazz, packageName, remoteService, 1));
        }
        return (ServiceBinder<T>) binderMap.get(context);
    }

    public static <T extends IInterface> T get(Context context, String packageName, Class<T> aidlClazz, String remoteService) {
        return (T) bind(context, packageName, aidlClazz, remoteService).getObject();
    }
}
