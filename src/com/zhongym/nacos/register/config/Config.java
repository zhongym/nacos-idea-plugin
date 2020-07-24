package com.zhongym.nacos.register.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    public static int nacosPort;
    public static int gatewayPort;
    public static String sourceServerAddr;

    static {
        Properties properties = load();
        nacosPort = Integer.parseInt(properties.getProperty("nacosPort", "8848"));
        gatewayPort = Integer.parseInt(properties.getProperty("gatewayPort", "9999"));
        sourceServerAddr = properties.getProperty("sourceServerAddr", "192.168.2.33:8848");
    }

    public static void save() {
        Properties properties = load();
        properties.setProperty("nacosPort", nacosPort + "");
        properties.setProperty("gatewayPort", gatewayPort + "");
        properties.setProperty("sourceServerAddr", sourceServerAddr);
        try {
            properties.save(new FileOutputStream(new File(Config.class.getClassLoader().getResource("config.properties").toURI())), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Properties load() {
        Properties properties = new Properties();
        try {
            properties.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;


    }

    public static void main(String[] args) {
        Config.nacosPort=3333;
        Config.save();
    }
}
