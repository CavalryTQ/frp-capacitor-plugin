package icu.cavalry.frp.plugin.utils;

public enum LinuxISAPath {
    ARM64("/arm64-v8a"),
    ARM32("/armeabi-v7a"),
    X86_64("/x86_64");

    private final String ISAPath;

    LinuxISAPath(String value) {
        this.ISAPath = value;
    }

    public String getISAPath() {
        return ISAPath;
    }
}
