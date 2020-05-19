package com.example.aidlclient;

import androidx.appcompat.app.AppCompatActivity;

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

import com.example.myaidl.IAidlInterface;

public class MainActivity extends AppCompatActivity {

    private EditText etNum1;
    private EditText etNum2;
    private EditText etResult;
    private IAidlInterface iAidlInterface;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iAidlInterface = IAidlInterface.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("MainActivity", "onServiceConnected: ");
            iAidlInterface = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.example.myaidl", "com.example.myaidl.RemoteService"));
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    public void cacu(View view) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}
