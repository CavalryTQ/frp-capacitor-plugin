package icu.cavalry.frp.plugin.bridge;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.getcapacitor.JSObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icu.cavalry.frp.plugin.service.FrpcForegroundService;

public class FrpService implements FrpMapper{ // 不再声明抽象类
    FrpThread frpThread;
    private FrpListener listener;
    private final String TAG = "FrpService";


    public interface FrpListener {
        void onFrpOutput(String line);
    }

    protected  interface FrpsService {
        default JSObject startFrps(){
            return new JSObject().put("status", "暂未未实现方法！");
        }
    }

    protected interface FrpcService {// 具体到 Frpc类实现，Frps同理
         JSObject startFrpc();
    }

    public FrpService(FrpListener listener) {
        this.listener = listener;
    }


    @Override
    public  void start(List<String> command, File dir, Map<String, String> envp){
        frpThread = FrpThread.create(command, dir, envp, line -> {
            if (listener != null) {
                listener.onFrpOutput(line);
            }
        });

        frpThread.start();
        System.out.println("started");
        Log.i(TAG, "started");
//        JSObject result = new JSObject();
//        result.put("status", "started");
//        return result;
    }


    @Override
    public JSObject stop() {
        JSObject result = new JSObject();
        if (frpThread != null) {
            frpThread.stopProcess();
            frpThread = null;
            result.put("status", "stopped");
        }else {
            result.put("status", "not started");
        }
        return result;
    }

    @Override
    public JSObject restart() {

        return null;
    }

    @Override
    public JSObject getStatus() {
        return null;
    }

    @Override
    public JSObject getConfig() {
        return null;
    }

    protected void startFrpService(Context context, List<String> command, File workingDirectory) {
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
}


