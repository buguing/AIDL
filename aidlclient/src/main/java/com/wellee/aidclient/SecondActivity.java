package com.wellee.aidclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wellee.aidl.IAidlInterface;
import com.wellee.aidlclient.R;

/**
 * AIDL死亡代理
 */
public class SecondActivity extends AppCompatActivity {

    private EditText etNum1;
    private EditText etNum2;
    private EditText etResult;
    private IAidlInterface iAidlInterface;
    private IBinder mConnectedBinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        bindService();
    }

    private void initView() {
        etNum1 = findViewById(R.id.num1);
        etNum2 = findViewById(R.id.num2);
        etResult = findViewById(R.id.result);
    }

    private void bindService() {
        Log.d("AIDL-client", "invoke bindService");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.wellee.aidlservice", "com.wellee.aidlservice.RemoteService"));
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    private final IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d("AIDL-client", "binderDied");
            //解绑
            if (iAidlInterface != null) {
                iAidlInterface.asBinder().unlinkToDeath(deathRecipient, 0);
                iAidlInterface = null;
            }
            //断开重新绑定
            bindService();
        }
    };

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                mConnectedBinder = iBinder;
                //设置死亡代理
                iBinder.linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            iAidlInterface = IAidlInterface.Stub.asInterface(iBinder);
            Log.d("AIDL-client", "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("AIDL-client", "onServiceDisconnected");
//            iAidlInterface = null;
        }
    };

    public void calculator(View view) {
        int num1 = Integer.parseInt(etNum1.getText().toString());
        int num2 = Integer.parseInt(etNum2.getText().toString());
        try {
            int result = iAidlInterface.add(num1, num2);
            etResult.setText(String.valueOf(result));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(this, "未发现aidl服务端", Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean binderIsAlive() {
        if (mConnectedBinder != null) {
            return mConnectedBinder.isBinderAlive();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}
