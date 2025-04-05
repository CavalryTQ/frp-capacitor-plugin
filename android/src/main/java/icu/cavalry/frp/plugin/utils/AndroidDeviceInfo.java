package icu.cavalry.frp.plugin.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

public class AndroidDeviceInfo {

    private final Context context; // 持有 ApplicationContext
    private final String manufacturer;  // 设备制造商
    private final String model;         // 设备型号
    private final String brand;         // 设备品牌
    private final String device;        // 设备名称
    private final String hardware;      // 硬件名称
    private final String product;       // 产品名称
    private final String androidVersion;// Android 版本
    private final String apiLevel;      // API 级别
    private final String abi;           // 主要 CPU 架构
    private final String serialNumber;  // 设备序列号（部分设备需要权限）
    private final String androidId;     // Android ID（系统唯一标识符）
    private final String packageName;   // 应用包名
    private final String versionName;   // 应用版本名称
    private final int versionCode;      // 应用版本号

    private final PackageInfo packageInfo; // 包信息
    private PackageManager packageManager; // 包管理器

    private static volatile AndroidDeviceInfo instance; // 单例

    private AndroidDeviceInfo(Context context) { // 私有化构造器实行单例模式
        this.context = context.getApplicationContext();
        this.manufacturer = Build.MANUFACTURER;
        this.model = Build.MODEL;
        this.brand = Build.BRAND;
        this.device = Build.DEVICE;
        this.hardware = Build.HARDWARE;
        this.product = Build.PRODUCT;
        this.androidVersion = Build.VERSION.RELEASE;
        this.apiLevel = String.valueOf(Build.VERSION.SDK_INT);
        this.abi = Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "unknown";
        this.serialNumber = getDeviceSerial();
        this.androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        PackageInfo packageInfo = getPackageInfo(context);
        this.packageInfo = packageInfo;
        this.packageName = context.getPackageName();
        this.versionName = packageInfo != null ? packageInfo.versionName : "unknown";
        this.versionCode = packageInfo != null ? packageInfo.versionCode : -1;

        this.packageManager = context.getPackageManager();

    }

    public static void init(Context context) { // 初始化单例,保证只传入一次context
        if (instance == null) {
            synchronized (AndroidDeviceInfo.class) { // 避免重复初始化
                if (instance == null) {
                    instance = new AndroidDeviceInfo(context);
                }
            }
        }
    }


    public static AndroidDeviceInfo initInfo(Context context) {
        initInfo(context);
        return instance;
    }

    /**
     * 决定使用双重校验锁式单例而不用静态内部类的原因是构造器需要传入上下文参数，ApplicationProvider.getApplicationContext();限制在本包路径下获取。
     * @return
     */
    public static AndroidDeviceInfo getInstance() { // 双重校验锁式单例
        if (instance == null) {
            throw new IllegalStateException("AndroidDeviceInfo not initialized. Call init(Context) first.");
        }
        return instance;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getBrand() {
        return brand;
    }

    public String getDevice() {
        return device;
    }

    public String getHardware() {
        return hardware;
    }

    public String getProduct() {
        return product;
    }

    public String getApiLevel() {
        return apiLevel;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public String getAbi() {
        return abi;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAndroidId() {
        return androidId;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    private String getDeviceSerial() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                return Build.getSerial();
            } catch (SecurityException e) {
                return "Permission Denied";
            }
        } else {
            return Build.SERIAL;
        }
    }

    private PackageInfo getPackageInfo(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "AndroidDeviceInfo{" +
                "manufacturer='" + manufacturer + '\'' +
                ", model='" + model + '\'' +
                ", brand='" + brand + '\'' +
                ", device='" + device + '\'' +
                ", hardware='" + hardware + '\'' +
                ", product='" + product + '\'' +
                ", androidVersion='" + androidVersion + '\'' +
                ", apiLevel='" + apiLevel + '\'' +
                ", abi='" + abi + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", androidId='" + androidId + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                '}';
    }

    public PackageManager getPackageManager() {
        return packageManager;
    }
}

