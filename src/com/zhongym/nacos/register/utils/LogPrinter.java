package com.zhongym.nacos.register.utils;

/**
 * @author Yuanmao.Zhong
 */
public class LogPrinter {

    public static void print(String log) {
        System.out.println(log);
    }

    public static void print(Exception e) {
        System.out.println(e.getMessage());
    }
}
