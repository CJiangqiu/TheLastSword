package net.the_last_sword.agent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Agent 日志文件写入器：将 Agent 日志直接写入独立文件
public class AgentLogWriter {

    private static BufferedWriter writer = null;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static boolean initialized = false;

    //初始化日志文件
    private static synchronized void initialize() {
        if (initialized) return;
        initialized = true;

        try {
            //创建 logs 目录（如果不存在）
            Path logsDir = Paths.get("logs");
            if (!Files.exists(logsDir)) {
                Files.createDirectories(logsDir);
            }

            //创建日志文件
            Path logFile = logsDir.resolve("TheLastSwordAgent.log");
            writer = new BufferedWriter(new FileWriter(logFile.toFile(), false)); //覆盖模式

            //写入文件头
            writer.write("========================================\n");
            writer.write("The Last Sword - Agent Log\n");
            writer.write("Started at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
            writer.write("========================================\n\n");
            writer.flush();

        } catch (IOException e) {
            //静默失败，避免影响 Agent 启动
            writer = null;
        }
    }

    //写入日志
    public static synchronized void log(String level, String message) {
        initialize();

        if (writer == null) return;

        try {
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            writer.write(String.format("[%s] [%s] %s\n", timestamp, level, message));
            writer.flush(); //立即刷新，确保日志不丢失
        } catch (IOException e) {
            //忽略写入错误
        }
    }

    //写入带异常的日志
    public static synchronized void log(String level, String message, Throwable throwable) {
        log(level, message);

        if (writer == null || throwable == null) return;

        try {
            writer.write("  Exception: " + throwable.getClass().getName() + ": " + throwable.getMessage() + "\n");

            //写入堆栈跟踪（前 10 行）
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            int limit = Math.min(stackTrace.length, 10);
            for (int i = 0; i < limit; i++) {
                writer.write("    at " + stackTrace[i].toString() + "\n");
            }
            if (stackTrace.length > 10) {
                writer.write("    ... " + (stackTrace.length - 10) + " more\n");
            }

            writer.flush();
        } catch (IOException e) {
            //忽略写入错误
        }
    }

    //关闭日志文件
    public static synchronized void close() {
        if (writer != null) {
            try {
                writer.write("\n========================================\n");
                writer.write("Agent log closed at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
                writer.write("========================================\n");
                writer.close();
            } catch (IOException e) {
                //忽略关闭错误
            }
            writer = null;
        }
    }

    private AgentLogWriter() {}
}
