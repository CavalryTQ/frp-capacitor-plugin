package icu.cavalry.frp.plugin.bridge;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icu.cavalry.frp.plugin.service.FrpcForegroundService;

public class FrpService implements FrpMapper{ // 不再声明抽象类
    protected FrpThread frpThread; //TODO: 临时解决线程唯一性,后期创建frpThreadMap来管理线程
    private  FrpListener listener;
    private final String TAG = "FrpService";
    public static FrpListener staticListener;

    public interface FrpListener {
        void onFrpOutput(String line);
    }

    public static void setStaticListener(FrpListener listener) {
        staticListener = listener;
    }
    protected  interface FrpsService {
        default JSObject startFrps(){
            return new JSObject().put("status", "暂未未实现方法！");
        }
    }

    protected interface FrpcService {// 具体到 Frpc类实现，Frps同理
         void startFrpc(PluginCall call);
         void stopFrpc(PluginCall call);
         JSObject restartFrpc(PluginCall call);
         void getStatus(PluginCall call);
    }

    public  FrpService(FrpListener listener) {
        this.listener = listener;
    }

    public  FrpService() {
        this.listener = staticListener;
    }


    @Override
    public  void  start(List<String> command, File dir, Map<String, String> envp){
       frpThread =  FrpThread.create(command, dir, envp, line -> {
            if (listener != null) {
                Log.d(TAG, "onFrpOutput: " + line);
                listener.onFrpOutput(line);
                if (staticListener != null) {
                    staticListener.onFrpOutput(line);
                }
            }
        });
        Log.i(TAG, "start: " + frpThread.isAlive());
        frpThread.start();
        Log.i(TAG, "started");
        Log.i(TAG, "start: " + frpThread.isAlive());
        Log.i(TAG, "start: " + frpThread.toString());
        Log.i(TAG, "start: " + frpThread.getTID());
    }


    @Override
    public void stop() throws RuntimeException{
        if (frpThread != null && frpThread.isAlive()) {
            Log.i(TAG, "beforeStop: " + frpThread.isAlive());
            Log.i(TAG, "beforeStop: " + frpThread.getTID());
            frpThread.stopProcess();
            frpThread = null;
        }else {
            throw new RuntimeException("frp process is not running");
        }

    }

    @Override
    public JSObject restart() {

        return null;
    }

    /**
     * 获取运行状态
     * @param mode Frpc/Frps
     * @return
     */
    @Override
    public JSObject getStatus(String mode) throws RuntimeException{
        JSObject result = new JSObject();
        boolean isRunning = false;
        String message = "";
        String TID  = "";

        Log.i(TAG, "getStatus: " + (this.frpThread == null)); // 为true, 即使调用了start后frpThread还是为null
        if ("frpc".equalsIgnoreCase(mode)) {
            if (this.frpThread != null && this.frpThread.isAlive()) { // TODO:  疑似线程安全问题
                isRunning = true;
                message = "frpc is running";
                TID = String.valueOf(frpThread.getTID());
            } else {
                message = "frpc is not running";
//                throw new RuntimeException("frpc process is not running");
            }
        } else if ("frps".equalsIgnoreCase(mode)) {
            // 你还没有实现 FrpsService，所以返回未实现
            message = "frps status not implemented";
        } else {
//            message = "unknown mode: " + mode;
            throw new RuntimeException("unknown mode: " + mode);
        }

        result.put("status", isRunning);
        result.put("mode", mode);
        result.put("message", message);
        result.put("tid", TID);
        return result;
    }

    @Override
    public JSObject getConfig() {
        return null;
    }

    @Override
    public JSObject setConfig(JSObject config) {
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


