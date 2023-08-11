package com.wellee.connect;

import android.content.ComponentName;

public interface IServiceConnection<T> {

    void serviceConnected(T iInterface);

    void serviceDisconnected(ComponentName name);
}
