package com.zhongym.nacos.register.utils;

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
        LogPrinter.consumer = consumer;
    }

    public static synchronized void print(String log) {
        list.add(" log: " + log);
        if (consumer != null) {
            String t = list.toString();
            ThreadHelper.onUIThread(() -> {
                consumer.accept(t);
            });
        }
        System.out.println(log);
    }

    public static synchronized void printServerLog(String log) {
//        list.add(" log: " + log);
//        if (consumer != null) {
//            String t = list.toString();
//            ThreadHelper.onUIThread(() -> {
//                consumer.accept(t);
//            });
//        }
        System.out.println(log);
    }

    public static synchronized void print(Exception e) {
        list.add(" exception: " + e.getMessage());
        if (consumer != null) {
//            StringWriter out = new StringWriter();
//            e.printStackTrace(new PrintWriter(out));
//            consumer.accept(out.toString());
            String t = list.toString();
            ThreadHelper.onUIThread(() -> {
                consumer.accept(t);
            });
        }
        e.printStackTrace();
    }
}
