package icu.cavalry.frp.plugin.bridge;

import com.getcapacitor.JSObject;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class FrpService implements FrpMapper{
    FrpThread frpThread;
    public interface FrpListener {
        void onFrpOutput(String line);
    }
    private FrpListener listener;


    public FrpService(FrpListener listener) {
        this.listener = listener;
    }

    public abstract JSObject startFrpc(); // 具体到 Frpc类实现，Frps同理
    @Override
    public  JSObject start(List<String> command, File dir, Map<String, String> envp){
        frpThread = FrpThread.create(command, dir, envp, line -> {
            if (listener != null) {
                listener.onFrpOutput(line);
            }
        });

        frpThread.start();

        JSObject result = new JSObject();
        result.put("status", "started");
        return result;
    }

    @Override
    public JSObject stop() {
        return null;
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
}
