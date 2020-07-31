package com.zhongym.nacos.register.config;

import com.zhongym.nacos.register.constants.IpEnum;
import com.zhongym.nacos.register.utils.LogPrinter;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuanmao.Zhong
 */
public class Config implements Serializable {
    private static Config config;

    public ConfigItem<Integer> nacosPort = new IntConfigItem("注册中心端口", 8848);
    public ConfigItem<String> nacosDbUser = new StrConfigItem("注册中心数据库用户名", "root");
    public ConfigItem<String> nacosDbPassword = new StrConfigItem("注册中心数据库密码", "mall123456");
    public ConfigItem<String> nacosDbUrl = new StrConfigItem("注册中心数据库jdbc连接", "jdbc:mysql://mall-mysql:3306/mall_config?characterEncoding=utf8");
    public ConfigItem<Integer> gatewayPort = new IntConfigItem("网关端口", 9999);
    public ConfigItem<String> sourceServerAddr = new StrConfigItem("源注册中心地址", "192.168.2.33:8848");

    public static String getLocalNacos() {
        return IpEnum.getLoopbackAddress() + ":" + Config.getInstance().nacosPort.getValue();
    }

    public static String getRemoteNacos() {
        return Config.getInstance().sourceServerAddr.getValue();
    }

    public static String getGatewayUrl() {
        return "http://" + IpEnum.getLoopbackAddress() + ":" + Config.getInstance().gatewayPort.getValue();
    }

    private class IntConfigItem extends ConfigItem<Integer> {

        public IntConfigItem(String title, Integer value) {
            super(title, value);
        }

        @Override
        Integer convert(String value) {
            return Integer.valueOf(value);
        }
    }

    private class StrConfigItem extends ConfigItem<String> {
        public StrConfigItem(String title, String value) {
            super(title, value);
        }

        @Override
        String convert(String value) {
            return value;
        }
    }

    public abstract class ConfigItem<T> implements Serializable {
        private final String title;
        private T value;

        public ConfigItem(String title, T value) {
            this.title = title;
            this.value = value;
        }


        public String getTitle() {
            return title;
        }

        public void setting(String value) {
            this.value = convert(value);
        }

        public void setValue(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public String getValueString() {
            return value.toString();
        }

        abstract T convert(String value);

        private Config getConfig() {
            return Config.this;
        }
    }

    private Config() {
    }

    public List<ConfigItem<?>> getAllItem() {
        List<ConfigItem<?>> items = new ArrayList<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getType().isAssignableFrom(ConfigItem.class)) {
                try {
                    items.add((ConfigItem<?>) field.get(this));
                } catch (IllegalAccessException e) {
                     LogPrinter.print(e);
                }
            }
        }
        return items;
    }

    public void save() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(new File(Config.class.getName())));) {
            outputStream.writeObject(this);
        } catch (Exception e) {
             LogPrinter.print(e);
        }
    }

    public static Config getInstance() {
        if (config == null) {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(new File(Config.class.getName())));) {
                config = (Config) inputStream.readObject();
            } catch (Exception e) {
                 LogPrinter.print(e);
            }
        }
        if (config == null) {
            return new Config();
        }
        return config;
    }

    public static void main(String[] args) throws IOException {
        Config instance = Config.getInstance();
        System.out.println(instance.nacosPort.value);
        instance.nacosPort.setting("97877799");
        instance.save();

        ConfigItem nacosPort = instance.nacosPort;
        nacosPort.setting("1");
        System.out.println(nacosPort.getValue());


    }
}
