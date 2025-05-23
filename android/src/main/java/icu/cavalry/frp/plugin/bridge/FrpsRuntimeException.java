package icu.cavalry.frp.plugin.bridge;

import android.util.Log;

public class FrpsRuntimeException extends RuntimeException {
    private static final String TAG = "FrpsRuntimeException";
    public FrpsRuntimeException(String message) {
        super(message);
        Log.e(TAG, message);
    }
}
