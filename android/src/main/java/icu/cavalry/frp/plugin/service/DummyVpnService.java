package icu.cavalry.frp.plugin.service;

import android.content.Intent;
import android.net.VpnService;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;

public class DummyVpnService extends VpnService {

    private static final String TAG = "DummyVpnService";
    private ParcelFileDescriptor vpnInterface;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting Dummy VPN");

        Builder builder = new Builder();
        builder.setSession("Frp")
                .addAddress("10.0.0.2", 32)
                .addRoute("192.0.2.0", 24); //  添加路由

        try {
            vpnInterface = builder.establish(); // 建立虚拟接口
        } catch (Exception e) {
            Log.e(TAG, "VPN establish failed", e);
        }

        return START_STICKY; // 保持运行
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Dummy VPN stopped");
        try {
            if (vpnInterface != null) vpnInterface.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // 不支持绑定
    }
}