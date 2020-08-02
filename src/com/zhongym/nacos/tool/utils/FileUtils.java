package com.zhongym.nacos.tool.utils;

import java.io.File;

/**
 * @author Yuanmao.Zhong
 */
public class FileUtils {
    public static void main(String[] args) {
        System.out.println(System.getProperty("java.io.tmpdir"));
    }

    public static String getConfigDir() {
        return System.getProperty("user.home") + File.separator + "nacos-idea-plugin" + File.separator + "config";
    }

    public static String getLogDir(String serverName) {
        return System.getProperty("user.home")+ File.separator  + "nacos-idea-plugin" + File.separator + serverName;

    }
}
