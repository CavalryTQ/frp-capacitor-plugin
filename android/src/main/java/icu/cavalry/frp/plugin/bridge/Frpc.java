package icu.cavalry.frp.plugin.bridge;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import icu.cavalry.frp.plugin.config.AndroidDeviceInfo;
import icu.cavalry.frp.plugin.service.FrpcForegroundService;

public class Frpc extends FrpService implements FrpService.FrpcService {
    private static final String TAG = "Frpc";

    private  Context context;
    private AndroidDeviceInfo info;
    private ApplicationInfo appInfo;

    private final String FrpcConfigFilePath = "/frpc";

    private final String SingleConfigName = "frpc.toml";

    private File libFrpcso;
    private File configTomlFile;


    private interface CallbackConnected {
        void onConnected(ComponentName componentName, IBinder iBinder);
    }
    private interface CallbackDisconnected {
        void onDisconnected(ComponentName componentName);
    }
    public Frpc(Context context) throws PackageManager.NameNotFoundException {
        super();
        this.context = context;
        info = AndroidDeviceInfo.initInfo(context);
//        appInfo = context.getApplicationInfo();
          try {
              appInfo = info.getPackageManager().getApplicationInfo(info.getPackageName(), PackageManager.GET_SHARED_LIBRARY_FILES);  // 获取 frpc 运行libs目录前获取 ApplicationInfo
              libFrpcso = new File(appInfo.nativeLibraryDir, "libfrpc.so");
              configTomlFile = new File(context.getFilesDir(), SingleConfigName); // 获取 frpc 运行的配置文件
          }catch (Exception e){
              Log.d(TAG, "初始化获取ApplicationInfo失败:" + e.getMessage());
              e.printStackTrace();
              throw e; // 继续抛
          }
    }


    public String echo(String value) {
        Log.d("Echo", value);
        Log.d("Echo", "Echo value: nativeLibraryDir" + this.appInfo.nativeLibraryDir);
        return value;
    }



public JSObject testStopFrpc() {
    JSObject result = new JSObject();


    return result;
}


    @Override
    public void startFrpc(PluginCall call) throws FrpcRuntimeException{
        // TODO: 拓展多配置启动，完善配置路径管理 2025.04.14
        JSObject result = new JSObject();
        // 确保 nativeLibraryDir 不为空并且路径里包含源文件 libfrpc.so
        if (verifyEvnBeforeStart()){
            String  configName = call.getData().getString("fileName");
            if ( configName !=  null && !configName.isEmpty()){
                this.configTomlFile = new File(context.getFilesDir() + FrpcConfigFilePath, configName);
            }else {
               throw new FrpcRuntimeException("请检查文件名或指定 frpc 配置文件");
            }

            ArrayList<String> command = new ArrayList<>();
            command.add(this.libFrpcso.getAbsolutePath());
            command.add("-c");
            command.add(this.configTomlFile.getAbsolutePath());

           try {
               startFrpcService(command, Objects.requireNonNull(this.libFrpcso.getParentFile()));
               Log.d(TAG, "startFrpc: 配置文件路径" + this.configTomlFile.getAbsolutePath());
 //              start(command, this.libFrpcso.getParentFile(), null);
                result.put("code", 1);
                result.put("message", "frpc 启动成功");
           } catch (Exception e) {
               Log.e(TAG, "启动前台服务失败: " + e.getMessage(), e);
//               result.put("code", 0);
//               result.put("message", "启动服务失败: " + e.getMessage());
               call.reject("启动服务失败: " + e.getMessage());
               throw e; // 继续抛,给插件notifyListeners发给js前端
           }
        }
    }


    @Override
    public void stopFrpc(PluginCall call) throws FrpcRuntimeException{
        try {
            frpcForegroundService((componentName, iBinder) -> {
                // 停止frpc
                try {
                    FrpcForegroundService service = ((FrpcForegroundService.LocalBinder) iBinder).getService();
                    service.getFrpService().stop();
                    service.onDestroy();
                    call.resolve(new JSObject().put("code", 1).put("message", "frpc 停止成功"));
                } catch (RuntimeException e) {
                    Log.e(TAG, "停止前台服务失败: " + e.getMessage(), e);
                    call.reject("停止服务失败: " + e.getMessage());
                }
            });
        }catch (RuntimeException e){
            throw new FrpcRuntimeException(e.getMessage());
        }
    }

    @Override
    public JSObject restartFrpc(PluginCall call) {
        return null;
    }

    @Override
    public void getStatus(PluginCall call) throws FrpcRuntimeException{
       try  {
//           String mode = call.getData().getString("mode");
//           getFrpcServiceStatus( mode, call);
           frpcForegroundService((componentName, iBinder) -> {
               try {
                   String mode = call.getData().getString("mode");
                   FrpcForegroundService frpcService = ((FrpcForegroundService.LocalBinder) iBinder).getService();
                   FrpService internalFrpService = frpcService.getFrpService();
                   JSObject status = internalFrpService.getStatus(mode);
                   call.resolve(status);
               } catch (RuntimeException e) {
                   Log.e(TAG, "获取服务状态失败: " + e.getMessage(), e);
                   call.reject(e.getMessage());
               }
           });

       } catch (RuntimeException e) {
//           call.reject(e.getMessage());
           throw new FrpcRuntimeException(e.getMessage());
       }
    }

    private void startFrpcService(List<String> command, File workingDirectory) {
        Intent intent = new Intent(context, FrpcForegroundService.class);

        // 添加启动参数
        intent.putStringArrayListExtra("command", new ArrayList<>(command));
        intent.putExtra("dir", workingDirectory.getAbsolutePath());
        Log.d(TAG, "启动前台服务: " + intent.getStringExtra("dir"));
        // Android 8+ 必须使用 startForegroundService 启动前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Android 8+ 必须使用 startForegroundService 启动前台服务");
            context.startForegroundService(intent);
        } else {
            Log.d(TAG, "Android 7- 使用 startService 启动前台服务");
            context.startService(intent);
        }

    }


    private boolean verifyEvnBeforeStart() throws FrpcRuntimeException {
        if (this.appInfo == null){
            Log.e(TAG, "appInfo is null");
            throw new FrpcRuntimeException("获取ApplicationInfo为null!");
        }
        if (this.libFrpcso == null){
            Log.e(TAG, "libFrpcso is null");
            throw new FrpcRuntimeException("获取libfrpc的File类为null!");
        }
        if (!this.libFrpcso.exists()){
            Log.e(TAG, "libFrpcso not exists");
            throw new FrpcRuntimeException("libfrpc.so文件不存在!");
        }
        if (!this.libFrpcso.canExecute()){
            Log.e(TAG, "libFrpcso can not execute");
            throw new FrpcRuntimeException("libfrpc.so文件不可执行!");
        }
        return true;
    }

    @Override
    public JSObject getConfig() {
        JSObject result = new JSObject();
        result.put("code", 0);
        result.put("message", "获取配置成功");
        result.put("config", this.configTomlFile.getAbsolutePath());
        return result;
    }

    private void frpcForegroundService(CallbackConnected callback) {
        Intent intent = new Intent(context, FrpcForegroundService.class);
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                // 服务连接成功
                callback.onConnected(componentName, iBinder);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                // 服务断开连接
            }
        };
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
//       return context.stopService(intent);
    }

}
