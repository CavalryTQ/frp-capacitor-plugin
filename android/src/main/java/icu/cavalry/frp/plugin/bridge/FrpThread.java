package icu.cavalry.frp.plugin.bridge;

import android.os.Build;

import java.io.*;
import java.util.List;
import java.util.Map;

public class FrpThread extends Thread {
    private final List<String> command; // 命令行参数
    private final File dir; // 执行命令的目录
    private final Map<String, String> envp; // 环境变量
    private final OutputCallback outputCallback; // 输出回调

    private Process process; // 进程对象

    protected interface OutputCallback {
        void onOutput(String line);
    }

    protected FrpThread(List<String> command, File dir, Map<String, String> envp, OutputCallback callback) {
        this.command = command;
        this.dir = dir;
        this.envp = envp;
        this.outputCallback = callback;
    }


    protected static FrpThread create(List<String> command, File dir, Map<String, String> envp, OutputCallback callback){
        return new FrpThread(command, dir, envp, callback);
    }

    @Override
    public void run() {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(dir);
            builder.redirectErrorStream(true);

            Map<String, String> environment = builder.environment();
            if (envp != null) {
                environment.putAll(envp);
            }

            process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while (!isInterrupted() && (line = reader.readLine()) != null) {
                    outputCallback.onOutput(line);
                }
            } catch (InterruptedIOException e) {
                outputCallback.onOutput("Thread interrupted: " + e.getMessage());
            }

            int exitCode = process.waitFor();
            outputCallback.onOutput("Process exited with code: " + exitCode);

        } catch (Exception e) {
            outputCallback.onOutput("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        } finally {
            stopProcess();
        }
    }

    public void stopProcess() {
        if (process != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    process.destroyForcibly(); // destroy() 也可以按需使用
                }
            } catch (Exception e) {
                outputCallback.onOutput("Error stopping process: " + e.getMessage());
            }
        }
    }
}
