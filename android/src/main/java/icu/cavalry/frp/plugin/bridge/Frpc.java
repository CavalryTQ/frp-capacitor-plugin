package icu.cavalry.frp.plugin.bridge;

import android.util.Log;

import com.getcapacitor.JSObject;

public class Frpc extends FrpService {
    private static final String TAG = "FRPC";

    public Frpc(FrpListener listener) {
        super(listener);
    }

    public String getPlatform() {
        return "Android";
    }

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    @Override
    public JSObject startFrpc() {
        Log.d(TAG, "startFrpc!!!!!!!!!!!!!!!!");
        // 未来细化frpc客户端启动实现逻辑，比如处理不同配置路径


        return null;
    }



}
