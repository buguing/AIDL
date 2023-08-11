package com.wellee.aidclient;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wellee.aidl.IAidlInterface;
import com.wellee.aidlclient.R;
import com.wellee.connect.IServiceConnection;
import com.wellee.connect.ServiceBinder;
import com.wellee.connect.ServiceManger;

/**
 * AIDL重连
 */
public class MainActivity extends AppCompatActivity {

    private EditText etNum1;
    private EditText etNum2;
    private EditText etResult;
    private IAidlInterface iAidlInterface;
    private ServiceBinder<IAidlInterface> service;

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
        Log.d("AIDL-client", "invoke bindService");
        service = ServiceManger.bind(this, "com.wellee.aidlservice", IAidlInterface.class, "com.wellee.aidlservice.RemoteService");
        service.addServiceConnection(serviceConnection);
    }

    private final IServiceConnection<IAidlInterface> serviceConnection = new IServiceConnection<IAidlInterface>() {
        @Override
        public void serviceConnected(IAidlInterface iInterface) {
            iAidlInterface = iInterface;
        }

        @Override
        public void serviceDisconnected(ComponentName name) {
            Log.d("AIDL-client", "serviceDisconnected " + name.getClassName());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (service != null) {
            service.removeServiceConnection(serviceConnection);
            service.destroy();
        }
    }
}
