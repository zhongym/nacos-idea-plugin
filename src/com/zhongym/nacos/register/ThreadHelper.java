package com.zhongym.nacos.register;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Yuanmao.Zhong
 */
public class ThreadHelper {
    private static final ExecutorService executor = Executors.newFixedThreadPool(5,(r) -> new Thread(r, "async-task-thread"));

    public static void async(Runnable task) {
        executor.submit(task);
    }

    @SuppressWarnings("all")
    public static void onUIThread(Runnable task) {
        SwingUtilities.invokeLater(() -> {
            task.run();
        });
    }
}
