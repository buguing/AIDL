package com.wellee.connect;

import android.os.IInterface;

public interface IServiceCommand<T extends IInterface> {

    void serviceConnected(T iInterface) throws Exception;
}
