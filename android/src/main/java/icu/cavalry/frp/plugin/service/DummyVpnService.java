package icu.cavalry.frp.plugin.service;

import android.content.Intent;
import android.net.VpnService;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;

import icu.cavalry.frp.plugin.bridge.FrpService;
import icu.cavalry.frp.plugin.frpPlugin;

public class DummyVpnService extends VpnService {

    private static final String TAG = "DummyVpnService";
    private ParcelFileDescriptor vpnInterface;
    // 添加 Binder（允许插件主动访问服务）
    private final IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        public DummyVpnService getService() {
            return DummyVpnService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting Dummy VPN");

        Builder builder = new Builder();
        builder.setSession("Frp")
                .addAddress("10.0.0.2", 32)
                .addRoute("192.0.2.0", 24); //  添加路由

        try {
            vpnInterface = builder.establish(); // 建立虚拟接口
            frpPlugin.emitVpnStateChangedEvent(true);
        } catch (Exception e) {
            Log.e(TAG, "VPN establish failed", e);
        }

        return START_STICKY; // 保持运行
    }

    @Override
    public void onRevoke() {
        super.onRevoke();
        Log.d("DummyVpnService", "VPN 被系统/用户断开");
        frpPlugin.emitVpnStateChangedEvent(false);
        disconnect(); // 调用 disconnect 释放资源
    }
    @Override
    public void onDestroy() {
        Log.i(TAG, "Dummy VPN stopped");
        super.onDestroy();
        frpPlugin.emitVpnStateChangedEvent(false);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder; // 返回自定义 Binder
    }

    // 主动断开 VPN 接口的方法
    public void disconnect() {
        Log.i(TAG, "Disconnecting VPN manually");
        try {
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
            stopSelf(); // 主动停止服务
        } catch (IOException e) {
            Log.e(TAG, "关闭 VPN 接口失败", e);
        }
    }
}