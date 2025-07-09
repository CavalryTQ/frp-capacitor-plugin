package icu.cavalry.frp.plugin;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import icu.cavalry.frp.plugin.bridge.FrpService;
import icu.cavalry.frp.plugin.bridge.Frpc;
import icu.cavalry.frp.plugin.bridge.FrpcRuntimeException;
import icu.cavalry.frp.plugin.service.DummyVpnService;

@CapacitorPlugin(name = "frp")
public class frpPlugin extends Plugin implements FrpService.FrpListener{
    private Frpc implementation;

    private static final String TAG = "frpPlugins";
    private static final int REQUEST_VPN_PERMISSION = 1234;
    private String pendingCallId;
    private String pendingCallFrpcId = "";
    private String pendingCallFrpsId = "";
    private static final String FRPC_EXCEPTION = "FrpcRuntimeException";
    private static final String FRPS_EXCEPTION = "FrpsRuntimeException";
    private boolean debugMode = false;
    public static frpPlugin instance; // 静态实例引用

    private ActivityResultLauncher<Intent> vpnPermissionLauncher;
    private ActivityResultLauncher<String> notificationPermissionLauncher;


    @Override
    public void load() {
        super.load(); // load the plugin
        FrpService.setStaticListener(this);
        instance = this;
        try {
            implementation = new Frpc(getContext()); // Plugin是经过反射加载的，所以需要手动调用load方法
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(FRPC_EXCEPTION, "初始化获取ApplicationInfo失败:" + e.getMessage());
            notifyListeners(FRPC_EXCEPTION, new JSObject().put("message", "初始化获取ApplicationInfo失败" + e));
        }
        vpnPermissionLauncher = bridge.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "onActivityResult: " + result.getResultCode());
                    PluginCall savedCall = bridge.getSavedCall(pendingCallId); // ✅ 从 Plugin 基类获取保存的 call
                    Log.d(TAG, "savedCall: " + savedCall.getData());
                    Log.d(TAG, "savedCallId: " + savedCall.getCallbackId());
                    if (savedCall == null) {
                        Log.e(TAG, "VPN 权限请求返回但未找到对应的 PluginCall");
                        return;
                    };

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        JSObject JSobjResult = new JSObject().put("status", "granted");
                        Log.d(TAG, "VPN permission granted");
                        savedCall.resolve(JSobjResult);
                    } else {
                        Log.d(TAG, "VPN permission denied");
                        Log.w(TAG, "VPN 权限未授权或取消，ResultCode: " + result.getResultCode());
                        savedCall.reject("VPN permission denied");
                    }
                    bridge.releaseCall(savedCall); // ✅ 释放 call，避免内存泄漏
                }
        );

        notificationPermissionLauncher = bridge.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    PluginCall pendingNotificationCall = bridge.getSavedCall(pendingCallId);
                    if (pendingNotificationCall == null) {
                        Log.e(TAG, "Notification 权限请求返回但未找到对应的 PluginCall");
                        return;
                    };

                    if (isGranted) {
                        JSObject result = new JSObject().put("status", "granted");
                        notifyListeners("notificationPermissionGranted", result);
                        Log.d(TAG, "Notification permission granted");
                        pendingNotificationCall.resolve(result);
                    } else {
                        Log.d(TAG, "Notification permission denied");
                        pendingNotificationCall.reject("Notification permission denied");
                    }

                    bridge.releaseCall(pendingNotificationCall); // 释放引用
                }
        );
    }
    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");
        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void startFrpc(PluginCall call) {
        pendingCallFrpcId = call.getCallbackId();
        bridge.saveCall(call);
        try {
             implementation.startFrpc(call);
//            call.resolve(result);
           // call.reject("FrpcRuntimeException", result);
        } catch (FrpcRuntimeException e){
            notifyListeners("FrpcRuntimeException", new JSObject().put("message", e.getMessage()));
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void stopFrpc(PluginCall call) {
        try {
//            call.resolve();
            implementation.stopFrpc(call);
        } catch (FrpcRuntimeException e){
            call.reject(e.getMessage());
            notifyListeners("FrpcRuntimeException", new JSObject().put("message", e.getMessage()));
        }
    }

    @PluginMethod
    public void getStatus(PluginCall call) {
        try {
//            call.resolve();
            implementation.getStatus(call);
        } catch (FrpcRuntimeException e){
            call.reject(e.getMessage());
            notifyListeners("FrpcRuntimeException", new JSObject().put("message", e.getMessage()));
        }
    }

   @PluginMethod
   public void testStartFrpc(PluginCall call){
        JSObject result = new JSObject().put("status", "running"); // implementation.testStartFrpc();
        call.resolve(result);
   }

    @PluginMethod
    public void enableDebugMode(PluginCall call) {
        JSObject ok = new JSObject().put("enabled", true);
        this.debugMode = true; // 插件内部 flag
        call.resolve(ok);
    }


    @PluginMethod
    public void isBatteryOptimizationIgnored(PluginCall call) {
        boolean ignored = true;
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        ignored = pm.isIgnoringBatteryOptimizations(getContext().getPackageName());
        JSObject result = new JSObject();
        result.put("ignored", ignored);
        call.resolve(result);
    }

    @PluginMethod
    public void requestIgnoreBatteryOptimizations(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            boolean isIgnoring = pm.isIgnoringBatteryOptimizations(getContext().getPackageName());

            if (!isIgnoring) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                getActivity().startActivity(intent);
                call.resolve(new JSObject().put("status", "requested"));
            } else {
                call.resolve(new JSObject().put("status", "already_ignored"));
            }
        } else {
            call.resolve(new JSObject().put("status", "not_applicable"));
        }
    }

    @PluginMethod
    public void startDummyVpn(PluginCall call) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = VpnService.prepare(getContext());
//            Log.d(TAG,"intent: " + String.valueOf( intent == null));
            if (intent != null) {
                // 用户尚未授权，跳转授权页面
                this.pendingCallId = call.getCallbackId();
                bridge.saveCall(call);
                Log.d(TAG, "PluginCall: " + call.getData());
                vpnPermissionLauncher.launch(intent);
//                call.resolve(new JSObject().put("status", "requested"));
            }

            // 已授权，直接启动 VPN 服务
            getContext().startService(new Intent(getContext(), DummyVpnService.class));
          //  getContext().stopService(new Intent(getContext(), DummyVpnService.class))
            // 已授权
            call.resolve(new JSObject().put("status", "ignored"));
        } else {
            call.reject("VpnService not supported on this Android version");
        }
    }

    @PluginMethod
    public void stopDummyVpn(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(getContext(), DummyVpnService.class);

            ServiceConnection connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    DummyVpnService vpnService = ((DummyVpnService.LocalBinder) service).getService();
                    vpnService.disconnect();
                    getContext().unbindService(this);
                    call.resolve(new JSObject().put("status", "disconnected"));
//                    return null;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.w(TAG, "VPN service unexpectedly disconnected");
                }
            };

            getContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        } else {
            call.reject("VpnService not supported on this Android version");
        }
    }

    @PluginMethod
    public void requestVpnPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = VpnService.prepare(getContext());
            if (intent != null) {
                // 用户还未授权 VPN，发起请求
                this.pendingCallId = call.getCallbackId();
                Log.d(TAG, "PluginCall: " + call.getData());
                bridge.saveCall(call);
                vpnPermissionLauncher.launch(intent); // ✅ 使用 registerForActivityResult 启动
            } else {
                // 用户已授权
                JSObject res = new JSObject().put("granted", true);
                call.resolve(res);
            }
        } else {
            call.reject("VPN not supported");
        }
    }
    @PluginMethod
    public void requestNotificationPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.POST_NOTIFICATIONS)) {
                    // 用户已永久拒绝权限，跳转设置页面
//                    openAppSettings(call);
                    call.reject("Notification permission permanently denied. Redirected to settings.");
                    return;
                }

                // 首次请求或之前拒绝但未永久拒绝
                pendingCallId = call.getCallbackId();
                bridge.saveCall(call);
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);

            } else {
                JSObject result = new JSObject().put("status", "already_granted");
                call.resolve(result);
            }
        } else {
            JSObject result = new JSObject().put("status", "granted");
            call.resolve(result);
        }
    }

    /**
     *  打开应用设置页面
     */
    @PluginMethod
    public void openAppSettings(PluginCall call) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    // frpPlugin 方法（不暴露给前端）
    @Override
    public void onFrpOutput(String line) {
        JSObject data = new JSObject();
        data.put("FrpLine", line);
        Log.d(TAG, "onFrpOutput: " + line);
        notifyListeners("frpOutput", data);
        if (line.contains("[I]")) {
            notifyListeners("[I]", data);
        } else if (line.contains("[W]")) {
            notifyListeners("[W]", data);
        } else if (line.contains("[E]")) {
            Log.e(TAG, "onFrpOutput: " + line);
            notifyListeners("[E]", data);

        } else if (line.contains("[D]")) {
            notifyListeners("[D]", data);
        }
        if (line.contains("Process exited")){
            Log.e(TAG, "onFrpOutput: " + line);
            notifyListeners("frpExited", data);
            PluginCall savedCall = bridge.getSavedCall(pendingCallFrpcId);
            if ( savedCall != null){
                String prefix = "code: ";
                int index = line.indexOf(prefix);
                String codeStr = line.substring(index + prefix.length()).trim(); // 获取 code
                if ( index != -1 ) {
                    if (codeStr.equals("1")){
                        savedCall.reject("FrpcRuntimeException:" + line, codeStr);
                        bridge.releaseCall(savedCall);
                    }else {
                        data.put("code", codeStr);
                        savedCall.resolve(data);
                        bridge.releaseCall(savedCall);
                    }
                }
            }else {
                Log.w(TAG, "No saved call found to reject for line: " + line);
            }
        }

    }
    public static void emitVpnStateChangedEvent(boolean state) {
        if (instance != null) {
            JSObject data = new JSObject().put("status", state);
            instance.notifyListeners("vpnStateChange", data);
            Log.d("frpPlugin", "📡 VPN 状态变更事件已通知前端，状态: " + state);
        } else {
            Log.e("frpPlugin", "frpPlugin 实例未初始化，无法发送 VPN 状态变更事件");
        }
    }
    // 发出临时事件通知
    public static void emitEvent(String eventName, JSObject data) {
        if (instance != null) {
            instance.notifyListeners(eventName, data);
            Log.d("frpPlugin", " temps: " + eventName + " 已发出");
        } else {
            Log.e("frpPlugin", "frpPlugin 临时事件未初始化，无法发出事件");
        }
    }
}
