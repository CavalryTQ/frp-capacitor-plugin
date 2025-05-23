package icu.cavalry.frp.plugin.bridge;

import android.util.Log;

public class FrpcRuntimeException extends RuntimeException {
    private static final String TAG = "FrpcRuntimeException";
    public FrpcRuntimeException(String message) {
        super(message); // JDK 22前super必须是第一条语句
        Log.e(TAG, message);
    }
}

