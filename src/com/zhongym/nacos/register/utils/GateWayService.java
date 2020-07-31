package com.zhongym.nacos.register.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.zhongym.nacos.register.config.Config;
import com.zhongym.nacos.register.constants.ServerStatusEnum;

import java.util.LinkedHashMap;

/**
 * @author Yuanmao.Zhong
 */
public class GateWayService {

    private static ServerStatusEnum statusEnum = ServerStatusEnum.UP;

    public static ServerStatusEnum getServerStatus() {
        String url = Config.getGatewayUrl() + "/actuator/health";
        try {
            String res = HttpUtil.get(url, 2000);
            LinkedHashMap map = JSONObject.parseObject(res, LinkedHashMap.class);
            if ("UP".equals(map.get("status").toString())) {
                return ServerStatusEnum.UP;
            }
        } catch (Exception e) {
            LogPrinter.print(e);
        }
        return ServerStatusEnum.DOWN;
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

    public static void main(String[] args) {
        ServerStatusEnum serverStatus = GateWayService.getServerStatus();
        System.out.println(serverStatus);
    }
}
