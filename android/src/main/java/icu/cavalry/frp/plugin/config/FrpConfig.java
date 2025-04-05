package icu.cavalry.frp.plugin.config;


// frp config类
public class FrpConfig {

    private Integer type = null; // 模式类型 0: frpc, 1: frps
    private FrpcConfig frpcConfig;
    private FrpsConfig frpsConfig;

    // 定义 FrpcConfig
    private final class FrpcConfig extends FrpConfig {
        private String LIBS_PATH = this.getLIBS();
        private String LIBS_ISA_PATH = this.getLIBS();
        public String getLIBS_ISA_PATH() {
            return LIBS_ISA_PATH;
        }
    }

    // 定义 FrpsConfig
    private static final class FrpsConfig extends FrpConfig{
        private String LIBS_ISA_PATH = this.getLIBS();
    }
    public static class FrpConfigTypeException extends RuntimeException {
        public FrpConfigTypeException(String message) {
            super(message);
        }
    }

    // 只允许设置一次 type，并确保只能是 0 或 1
    public void setType(int type) {
        if (this.type != null) {
            throw new IllegalStateException("type 已经被设置，无法更改！");
        }
        if ((type != 0 && type != 1)) {
            throw new FrpConfigTypeException("意外的值：0: frpc, 1: frps");
        }

        this.type = type;

        // 根据 type 初始化对应的内部类
        // 获取当前运行系统ISA

        if (type == 0) {
            this.frpcConfig = new FrpcConfig();
        } else {
            this.frpsConfig = new FrpsConfig();
        }
    }

    public Integer getType() {
        return type;
    }

    public String getLIBS() {
        return "/libs";
    }

    public FrpsConfig getFrpsConfig() {
        return frpsConfig;
    }

    public FrpcConfig getFrpcConfig() {
        return frpcConfig;
    }

    // 获取当前运行系统ISA

}