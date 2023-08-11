package com.wellee.connect;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import com.wellee.connect.utils.HandlerUtils;
import com.wellee.connect.utils.LogUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class ServiceBinder <T extends IInterface> implements ServiceConnection {

    private final Context context;
    private final ClassLoader classLoader;
    private final Method binderMethod;
    private final String packageName;
    private final int reconnectTime;
    private final Class<T> serviceClazz;
    private T t;
    private Object proxyObject;
    private volatile boolean connected = false;
    private final Set<IServiceConnection<T>> serviceConnections = new HashSet<>();
    private final LinkedBlockingQueue<IServiceCommand<T>> iServiceCommands = new LinkedBlockingQueue<>();
    private Intent intent;
    private boolean isDestroy;

    public ServiceBinder(Context context, Class<T> aidlClazz, String packageName, String remoteService, int reconnectTime) {
        this(context, ServiceBinder.class.getClassLoader(), aidlClazz, packageName, remoteService, reconnectTime);
    }

    public ServiceBinder(Context context, ClassLoader classLoader, Class<T> aidlClazz, String packageName, String remoteService, int reconnectTime) {
        try {
            String stubClassName = aidlClazz.getName() + "$Stub";
            Class<?> stubClass = Class.forName(stubClassName, true, classLoader);
            this.binderMethod = stubClass.getMethod("asInterface", IBinder.class);
        } catch (Throwable e) {
            throw new RuntimeException("无法获取绑定方法");
        }
        this.classLoader = classLoader;
        this.serviceClazz = aidlClazz;
        this.context = context;
        this.packageName = packageName;
        this.reconnectTime = reconnectTime;
        this.intent = new Intent();
        this.intent.setComponent(new ComponentName(packageName, remoteService));
        this.doConnect();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            Object serverObject = this.binderMethod.invoke(null, service);
            t = this.serviceClazz.cast(serverObject);
            this.connected = true;
            LogUtils.i("服务已连接[%s]%s", name, t);
            while (!iServiceCommands.isEmpty()) {
                IServiceCommand<T> command = iServiceCommands.poll();
                try {
                    command.serviceConnected(t);
                } catch (Throwable e) {
                    LogUtils.w("error callback:%s", command);
                }
            }
            for (IServiceConnection<T> iServiceConnection : serviceConnections) {
                iServiceConnection.serviceConnected(t);
            }
        } catch (Throwable e) {
            LogUtils.eTag("绑定服务失败[%s:%s]", e, this.packageName, this.serviceClazz);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        LogUtils.i("服务断开%s", name);
        this.connected = false;
        t = null;
        for (IServiceConnection<T> iServiceConnection : serviceConnections) {
            iServiceConnection.serviceDisconnected(name);
        }
        doConnect();
    }

    public T getObject() {
        if (null == proxyObject) {
            proxyObject = Proxy.newProxyInstance(classLoader, new Class[]{this.serviceClazz}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (!connected) {
                        throw new RemoteException("服务未连接[" + packageName + ":" + serviceClazz + "]");
                    } else {
                        return method.invoke(t, args);
                    }
                }
            });
        }
        return serviceClazz.cast(proxyObject);
    }

    public void doConnect() {
        if (isDestroy) {
            return;
        }
        if (connected) {
            return;
        }

        boolean bind = context.bindService(intent, this, Activity.BIND_AUTO_CREATE);
        if (!bind) {
            LogUtils.eTag("绑定服务失败[%s:%s]", this.packageName, this.serviceClazz);
            if (this.reconnectTime < 0) {
                return;
            }
            HandlerUtils.delayTask(this.reconnectTime, new Runnable() {
                @Override
                public void run() {
                    doConnect();
                }
            });
        } else {
            LogUtils.i("服务成功绑定[%s:%s]", this.packageName, this.serviceClazz);
            HandlerUtils.delayTask(2, new Runnable() {
                @Override
                public void run() {
                    doConnect();
                }
            });
        }
    }

    public void addServiceConnection(IServiceConnection<T> serviceConnection) {
        if (null != t) {
            serviceConnection.serviceConnected(t);
        } else {
            serviceConnection.serviceDisconnected(new ComponentName(this.packageName, this.serviceClazz.getName()));
        }
        this.serviceConnections.add(serviceConnection);
    }

    public void removeServiceConnection(IServiceConnection<T> serviceConnection) {
        this.serviceConnections.remove(serviceConnection);
    }

    public void exec(IServiceCommand<T> command) throws Exception {
        if (!connected) {
            iServiceCommands.add(command);
        } else {
            command.serviceConnected(t);
        }
    }

    public void removeExec(IServiceCommand command) {
        iServiceCommands.remove(command);
    }

    public void destroy() {
        isDestroy = true;
    }
}
