package com.zhongym.nacos.tool.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * @author Yuanmao.Zhong
 */
public class LogPrinter {
    private static Consumer<String> consumer;
    private static final LinkedList<String> LIST = new LinkedList<String>() {
        private int num = 1;

        @Override
        public boolean add(String s) {
            if (size() > 1000) {
                removeFirst();
            }
            return super.add(num++ + "-> thread: " + Thread.currentThread().getName() + s);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String s : this) {
                sb.append(s).append("\n");
            }
            return sb.toString();
        }
    };

    public static void init(Consumer<String> consumer) {
        LIST.clear();
        LogPrinter.consumer = consumer;
    }

    public static synchronized void print(String log) {
        LIST.add(" log: " + log);
        flushUI();
        System.out.println(log);
    }

    public static synchronized void printServerLog(String log) {
        LIST.add(" log: " + log);
        flushUI();
        System.out.println(log);
    }

    public static synchronized void print(Exception e) {
        LIST.add(" exception: " + e.getMessage());
        flushUI();
        System.out.println(e.getMessage());
    }

    public static void destroy() {
        LogPrinter.print("销毁日志.....");
        LIST.clear();
    }

    private static int lastTime = 0;
    private static final DateTimeFormatter HHMMSS = DateTimeFormatter.ofPattern("HHmmss");

    private static void flushUI() {
        //控制ui刷新频率
        int current = Integer.parseInt(LocalTime.now().format(HHMMSS));
        if (current - lastTime < 1) {
            return;
        }
        lastTime = current;
        if (consumer != null) {
            String t = LIST.toString();
            ThreadHelper.onUIThread(() -> {
                consumer.accept(t);
            });
        }
    }
}
