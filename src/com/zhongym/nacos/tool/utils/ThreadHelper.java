package com.zhongym.nacos.tool.utils;

import com.zhongym.nacos.tool.server.NacosService;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Yuanmao.Zhong
 */
public class ThreadHelper {
    private static int i = 1;
    private static ScheduledExecutorService executor;

    public static void init() {
        executor = Executors.newScheduledThreadPool(8, (r) -> new Thread(r, "async-thread-" + i++));
    }

    public static void async(Runnable task) {
        LogPrinter.print("线程池状态 " + getPoolState());
        executor.submit(task);
    }

    public static void delay(Runnable task, int seconds) {
        LogPrinter.print("线程池状态 " + getPoolState());
        executor.schedule(task, seconds, TimeUnit.SECONDS);
    }

    public static void scheduleAtFixedRate(Runnable task, int seconds) {
        LogPrinter.print("线程池状态 " + getPoolState());
        executor.scheduleAtFixedRate(task, seconds, seconds, TimeUnit.SECONDS);
    }


    public static void delayOnUIThread(Runnable task, int seconds) {
        LogPrinter.print("线程池状态 " + getPoolState());
        executor.schedule(() -> {
            onUIThread(task);
        }, seconds, TimeUnit.SECONDS);
    }


    @SuppressWarnings("all")
    public static void onUIThread(Runnable task) {
        SwingUtilities.invokeLater(() -> {
            task.run();
        });
    }


    public static void destroy() {
        LogPrinter.print("销毁线程.....");
        executor.shutdownNow();
    }

    private static String getPoolState() {
        String s = "Executor@263163c7[Running, pool size = 5, active threads = 3, queued tasks = 3, completed tasks = 0]";
        int index = s.indexOf("[");
        return s.substring(index);
    }

}
