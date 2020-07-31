package com.zhongym.nacos.register.constants;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 所有
 * 本机
 * 远程
 *
 * @author Yuanmao.Zhong
 */
@SuppressWarnings("all")
public enum IpEnum {
    ALL("所有"),
    LOCAL("本机"),
    REMOTE("远程");

    private final String title;

    IpEnum(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }

    public static boolean isLocalIp(String ip) {
        try {
            String loopbackAddress = InetAddress.getLoopbackAddress().getHostAddress();
            if (ip.equals(loopbackAddress)) {
                return true;
            }
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            if (ip.equals(hostAddress)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getLoopbackAddress() {
        return InetAddress.getLoopbackAddress().getHostAddress();
    }
}
