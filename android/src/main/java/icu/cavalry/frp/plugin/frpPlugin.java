package icu.cavalry.frp.plugin;


import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import icu.cavalry.frp.plugin.bridge.FrpService;
import icu.cavalry.frp.plugin.bridge.Frpc;

@CapacitorPlugin(name = "frp")
public class frpPlugin extends Plugin implements FrpService.FrpListener {
    private final Frpc implementation = new Frpc(this);

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void startFrpc(PluginCall call) {
        JSObject result = implementation.startFrpc();
        call.resolve(result);
    }



    @Override
    public void onFrpOutput(String line) {
        JSObject data = new JSObject();
        data.put("line", line);
        notifyListeners("frpOutput", data);
    }
}
