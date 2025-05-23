package icu.cavalry.frp.plugin.utils;

import androidx.annotation.NonNull;

public enum LinuxABIPath {
    ARM64("arm64-v8a", "/arm64-v8a"),
    ARM32("armeabi-v7a", "/armeabi-v7a"),
    X86_64("x86_64","/x86_64");

    @NonNull
    public static LinuxABIPath fromKey(String key) {
        for (LinuxABIPath abi : values()) {
            if (abi.key.equals(key)) {
                return abi;
            }
        }
        throw new IllegalArgumentException("Unknown ISA key: " + key);
    }
    private final String key;
    private final String abiPath;

    LinuxABIPath(String key, String value) {
        this.key = key;
        this.abiPath = value;
    }


}
