package icu.cavalry.frp.plugin.bridge;

import com.getcapacitor.JSObject;

import java.io.File;
import java.util.List;
import java.util.Map;

interface FrpMapper {
    String FRP_VERSION = "0.62.0";
    String FRP_DOWNLOAD_URL = "https://github.com/fatedier/frp/releases/download/v" + FRP_VERSION + "/frp_" + FRP_VERSION + "_linux_amd64.tar.gz";
    String FRP_DOWNLOAD_PATH = "frp_" + FRP_VERSION + "_linux_amd64.tar.gz";

    JSObject start(List<String> command, File dir, Map<String, String> envp);
    JSObject stop();
    JSObject restart();
    JSObject getStatus();
    JSObject getConfig();
}
