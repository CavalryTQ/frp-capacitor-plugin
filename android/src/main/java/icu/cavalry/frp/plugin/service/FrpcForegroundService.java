package icu.cavalry.frp.plugin.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;


import androidx.core.app.NotificationCompat;

import java.io.File;
import java.util.List;
import java.util.Objects;


import icu.cavalry.frp.plugin.R;
import icu.cavalry.frp.plugin.bridge.FrpService;

public class FrpcForegroundService extends Service {
    public static final String CHANNEL_ID = "FrpcChannel";
    public static final int NOTIFICATION_ID = 1;

    private static final String TAG = "FrpcForegroundService";

    private FrpService frpService;

    @Override
    public void onCreate() {
        super.onCreate();
        frpService = new FrpService(line -> {
            // 可以通过广播或事件总线将日志传回前端
            Log.i(TAG, line);
        });
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());

        // 从 intent 里解析参数，拼接命令行
        List<String> command = intent.getStringArrayListExtra("command");
       try  {
           File dir = new File(Objects.requireNonNull(intent.getStringExtra("dir")));
           frpService.start(command, dir, null);
        } catch (NullPointerException e) {
           Log.e(TAG, "onStartCommand: ", e);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        frpService.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Frpc 运行服务", NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FRPC 正在运行")
                .setContentText("点击进入应用以查看状态")
                .setSmallIcon(R.mipmap.ic_launcher) // 换成你的图标
                .build();
    }
}
