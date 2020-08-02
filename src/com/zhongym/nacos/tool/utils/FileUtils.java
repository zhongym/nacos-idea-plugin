package com.zhongym.nacos.tool.utils;

import java.io.File;

/**
 * @author Yuanmao.Zhong
 */
public class FileUtils {

    public static String getResourceDir() {
        return System.getProperty("user.home") + File.separator + "nacos-idea-plugin" + File.separator + "resource" + File.separator;
    }

    public static String getConfigDir() {
        return System.getProperty("user.home") + File.separator + "nacos-idea-plugin" + File.separator + "config";
    }

    public static String getLogDir(String serverName) {
        return System.getProperty("user.home") + File.separator + "nacos-idea-plugin" + File.separator + serverName;

    }
}
