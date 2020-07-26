package com.zhongym.nacos.register.utils;

import com.zhongym.nacos.register.constants.ServerStatusEnum;

/**
 * @author Yuanmao.Zhong
 */
public class GateWayService {

    private static ServerStatusEnum statusEnum = ServerStatusEnum.UP;

    public static ServerStatusEnum getServerStatus() {
        return statusEnum;
    }

    public static void trigger() {
        if (ServerStatusEnum.UP.equals(getServerStatus())) {
            close();
        } else {
            start();
        }
    }

    public static void start() {
        System.out.println("启动网关服务.....");
        statusEnum = ServerStatusEnum.UP;

    }

    public static void close() {
        System.out.println("关闭网关服务.....");
        statusEnum = ServerStatusEnum.DOWN;
    }
}
