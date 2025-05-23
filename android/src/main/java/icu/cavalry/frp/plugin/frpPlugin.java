package icu.cavalry.frp.plugin;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

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
    private static final String TAG = "frpPlugin";
    private static final int REQUEST_VPN_PERMISSION = 1234;
    private PluginCall pendingCall;
    private static final String FRPC_EXCEPTION = "FrpcRuntimeException";
    private static final String FRPS_EXCEPTION = "FrpsRuntimeException";
    private boolean debugMode = false;

    private ActivityResultLauncher<Intent> vpnPermissionLauncher;


    @Override
    public void load() {
        super.load(); // load the plugin
        try {
            implementation = new Frpc(this, getContext()); // Plugin是经过反射加载的，所以需要手动调用load方法
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(FRPC_EXCEPTION, "初始化获取ApplicationInfo失败:" + e.getMessage());
            notifyListeners(FRPC_EXCEPTION, new JSObject().put("message", "初始化获取ApplicationInfo失败" + e));
        }
        vpnPermissionLauncher = bridge.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    PluginCall savedCall = getSavedCall();
                    if (savedCall == null) return;

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        savedCall.resolve(new JSObject().put("status", "granted"));
                    } else {
                        savedCall.reject("VPN permission denied");
                    }
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
        try {
            JSObject result = implementation.startFrpc();
            call.resolve(result);
        } catch (FrpcRuntimeException e) {
            call.reject(e.getMessage());
            notifyListeners("FrpcRuntimeException", new JSObject().put("message", e.getMessage()));
        }
    }

   @PluginMethod
   public void testStartFrpc(PluginCall call){
        JSObject result = implementation.testStartFrpc();
        call.resolve(result);
   }

    @PluginMethod
    public void enableDebugMode(PluginCall call) {
        JSObject ok = new JSObject().put("enabled", true);
        this.debugMode = true; // 插件内部 flag
        call.resolve(ok);
    }


    @Override
    public void onFrpOutput(String line) {
        JSObject data = new JSObject();
        data.put("FrpLine", line);
        if (line.contains("[I]")) {
            notifyListeners("[I]", data);
        } else if (line.contains("[W]")) {
            notifyListeners("[W]", data);
        } else if (line.contains("[E]")) {
            notifyListeners("[E]", data);
        } else if (line.contains("[D]")) {
            notifyListeners("[D]", data);
        }
        notifyListeners("frpOutput", data);
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
            Log.d(TAG,"intent: " + String.valueOf( intent == null));
            if (intent != null) {
                // 用户尚未授权，跳转授权页面
                saveCall(call);
                startActivityForResult(call, intent, REQUEST_VPN_PERMISSION);
                return;
//                call.resolve(new JSObject().put("status", "requested"));
            }

            // 已授权，直接启动 VPN 服务
            getContext().startService(new Intent(getContext(), DummyVpnService.class));
            // 已授权
            call.resolve(new JSObject().put("status", "ignored"));
        } else {
            call.reject("VpnService not supported on this Android version");
        }
    }

    @PluginMethod
    public void requestVpnPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = VpnService.prepare(getContext());
            if (intent != null) {
                // 需要用户授权，保存 call，发起请求
                this.pendingCall = call;
                getActivity().startActivityForResult(intent, REQUEST_VPN_PERMISSION);
            } else {
                // 已授权
                call.resolve(new JSObject().put("granted", true));
            }
        } else {
            call.reject("VPN not supported");
        }
    }

//    @Override
//    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d(TAG,"handleOnActivityResult: " + requestCode + " " + resultCode);
//        if (requestCode == REQUEST_VPN_PERMISSION && pendingCall != null) {
//            if (resultCode == Activity.RESULT_OK) {
//                pendingCall.resolve(new JSObject().put("granted", true));
//            } else {
//                pendingCall.reject("User denied VPN permission");
//            }
//            pendingCall = null; // 清理引用
//        }
//        super.handleOnActivityResult(requestCode, resultCode, data);
//    }
}
