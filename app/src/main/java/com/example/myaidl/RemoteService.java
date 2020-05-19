package com.example.myaidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * @author : liwei
 * 创建日期 : 2019/10/24 13:36
 * 邮   箱 : liwei@worken.cn
 * 功能描述 :
 */
public class RemoteService extends Service {

    public RemoteService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private IBinder binder = new IAidlInterface.Stub() {
        @Override
        public int add(int num1, int num2) throws RemoteException {
            Log.d("RemoteService", "add方法：收到了客户端的请求 ，输入的" +
                    "num1 ->" + num1 + "\n num2 ->" + num2);
            return num1 + num2;
        }
    };
}
