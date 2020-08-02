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
    private static LinkedList<String> list = new LinkedList<String>() {
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
        list.clear();
        LogPrinter.consumer = consumer;
    }

    public static synchronized void print(String log) {
        list.add(" log: " + log);
        flushUI();
        System.out.println(log);
    }

    public static synchronized void printServerLog(String log) {
        list.add(" log: " + log);
        flushUI();
        System.out.println(log);
    }

    public static synchronized void print(Exception e) {
        list.add(" exception: " + e.getMessage());
        flushUI();
        e.printStackTrace();
    }

    public static void destroy() {
        list.clear();
    }

    private static int lastTime = 0;
    private static DateTimeFormatter hHmmss = DateTimeFormatter.ofPattern("HHmmss");

    private static void flushUI() {
        //控制ui刷新频率
        Integer current = Integer.valueOf(LocalTime.now().format(hHmmss));
        if (current - lastTime < 1) {
            return;
        }
        lastTime = current;
        if (consumer != null) {
            String t = list.toString();
            ThreadHelper.onUIThread(() -> {
                consumer.accept(t);
            });
        }
    }
}
