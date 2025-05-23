package icu.cavalry.frp.plugin.bridge;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.getcapacitor.JSObject;

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

    private File libFrpcso;
    private File configTomlFile;
    public Frpc(FrpListener listener, Context context) throws PackageManager.NameNotFoundException {
        super(listener);
        this.context = context;
        info = AndroidDeviceInfo.initInfo(context);
//        appInfo = context.getApplicationInfo();
          try {
              appInfo = info.getPackageManager().getApplicationInfo(info.getPackageName(), PackageManager.GET_SHARED_LIBRARY_FILES);  // 获取 frpc 运行libs目录前获取 ApplicationInfo
              libFrpcso = new File(appInfo.nativeLibraryDir, "libfrpc.so");
              configTomlFile = new File(context.getFilesDir(), "frpc.toml"); // 获取 frpc 运行的配置文件
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

    public JSObject testStartFrpc() {
        JSObject result = new JSObject();
        Log.d("Frpc", "开始执行 testStartFrpc()");

        if (this.appInfo == null) {
            Log.d("Frpc", "appInfo is null");
            result.put("code", 1);
            result.put("message", "appInfo is null");
            return result;
        }

        String nativeLibraryDir = this.appInfo.nativeLibraryDir;
        System.out.println(nativeLibraryDir);
        if (nativeLibraryDir == null) {
            Log.d("Frpc", "nativeLibraryDir is null");
            result.put("code", 1);
            result.put("message", "nativeLibraryDir is null");
            return result;
        }

        File soFile = new File(nativeLibraryDir, "libfrpc.so");
        if (!soFile.exists()) {
            Log.d("Frpc", "libfrpc.so not found in nativeLibraryDir");
            result.put("code", 1);
            result.put("message", "libfrpc.so not found");
            return result;
        }

        Log.d("Frpc", "libfrpc.so found at: " + soFile.getAbsolutePath());

        try {
            // ✅ 正确配置路径：app 私有目录 /data/data/<package>/files/frpc.toml
            File configFile = new File(context.getFilesDir() + FrpcConfigFilePath, "frpc.toml");

            List<String> command = new ArrayList<>();
            command.add(soFile.getAbsolutePath()); // 添加 frpc 的绝对路径
            command.add("-c");
            command.add(configFile.getAbsolutePath());

            File workingDir = new File(nativeLibraryDir);

            Log.d("Frpc", "启动命令: " + String.join(" ", command));
            Log.d("Frpc", "工作目录: " + workingDir.getAbsolutePath());

//            startFrpcService(command, workingDir);
//            start(command, this.libFrpcso.getParentFile(), null);
            startFrpService(context, command, workingDir);
            result.put("code", 0);
            result.put("message", "frpc 启动测试成功");
        } catch (Exception e) {
            Log.d("Frpc", "启动 frpc 出错: " + e.getMessage(), e);
            result.put("code", 1);
            result.put("message", "启动出错: " + e.getMessage());
        }

        return result;
    }

public JSObject testStopFrpc() {
    JSObject result = new JSObject();


    return result;
}


    @Override
    public JSObject startFrpc() throws FrpcRuntimeException{
        // TODO: 拓展多配置启动，完善配置路径管理 2025.04.14
        JSObject result = new JSObject();
        // 确保 nativeLibraryDir 不为空并且路径里包含源文件 libfrpc.so
        if (verifyEvnBeforeStart()){
            ArrayList<String> command = new ArrayList<>();
            command.add(this.libFrpcso.getAbsolutePath());
            command.add("-c");
            command.add(this.configTomlFile.getAbsolutePath());

//            start(command, this.libFrpcso.getParentFile(), null); // 启动命令
//            result.put("code", 0);
//            result.put("message", "frpc 启动成功");
           try {
                startFrpcService(command, Objects.requireNonNull(this.libFrpcso.getParentFile()));
                result.put("code", 1);
                result.put("message", "frpc 启动成功");
           } catch (Exception e) {
               Log.e(TAG, "启动前台服务失败: " + e.getMessage(), e);
               result.put("code", 0);
               result.put("message", "启动服务失败: " + e.getMessage());
               throw e; // 继续抛,给插件notifyListeners发给js前端
           }
        }
        return result;
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

    public JSObject stopFrpc() throws FrpcRuntimeException{
        JSObject result = new JSObject();
            result = stop();
            result.put("code", 0);
            result.put("message", "frpc 停止成功");
        return result;
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
        return true;
    }


}
