package com.zhongym.nacos.register.utils;

import a.e.E;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.zhongym.nacos.register.Config;
import com.zhongym.nacos.register.constants.ServerStatusEnum;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Yuanmao.Zhong
 */
public class NacosService {
    public static Map<String, NamingService> nacosServiceMap = new HashMap<>();

    static {
        System.setProperty("com.alibaba.nacos.client.naming.ctimeout", "5000");
        System.setProperty("com.alibaba.nacos.client.naming.ctimeout", "2000");
    }

    public static NamingService getInstance(String nacosAddr) {
        NamingService nacosService = nacosServiceMap.get(nacosAddr);
        if (nacosService == null) {
            try {
                nacosService = new NacosNamingService(nacosAddr);
                if (!"UP".equals(nacosService.getServerStatus())) {
                    throw new MyException("注册中心状态DOWN");
                }
            } catch (Exception e) {
                throw new MyException("连接注册中心失败：" + e.getMessage());
            }
            nacosServiceMap.put(nacosAddr, nacosService);
        }
        return nacosService;
    }

    public static ServerStatusEnum getServerStatus(String nacosAddr) {
        try {
            NamingService nacosService = getInstance(nacosAddr);
            if (!"UP".equals(nacosService.getServerStatus())) {
                return ServerStatusEnum.DOWN;
            }
            return ServerStatusEnum.UP;
        } catch (Exception e) {
            e.printStackTrace();
            return ServerStatusEnum.DOWN;
        }
    }

    public static void registerInstance(List<String> serviceNameList, Consumer<String> callBack) {
        try {
            for (String serviceName : serviceNameList) {
                callBack.accept(serviceName);
                List<Instance> instanceList = getInstance(Config.sourceServerAddr).getAllInstances(serviceName);
                for (Instance instance : instanceList) {
                    getInstance(Config.targetServerAddr).registerInstance(serviceName, instance);
                }
            }
        } catch (Exception e) {
            callBack.accept(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void unRegisterInstance(List<String> serviceNameList, Consumer<String> callBack) {
        try {
            for (String serviceName : serviceNameList) {
                callBack.accept(serviceName);
                List<Instance> instanceList = getInstance(Config.sourceServerAddr).getAllInstances(serviceName);
                for (Instance instance : instanceList) {
                    getInstance(Config.targetServerAddr).deregisterInstance(serviceName, instance);
                }
            }
        } catch (Exception e) {
            callBack.accept(e.getMessage());
            e.printStackTrace();
        }
    }

    public static Map<String, List<Instance>> getAllService(String nacosAddr) {
        try {
            Map<String, List<Instance>> map = new LinkedHashMap<>();
            List<String> serviceNameList = getInstance(nacosAddr).getServicesOfServer(1, 1000).getData();
            for (String serviceName : serviceNameList) {
                List<Instance> instanceList = getInstance(nacosAddr).getAllInstances(serviceName);
                map.put(serviceName, instanceList);
            }
            return map;
        } catch (NacosException e) {
            e.printStackTrace();
            throw new MyException(e.getErrMsg());
        }
    }

    public static void triggerLocalNacos() {
        if (ServerStatusEnum.UP.equals(getServerStatus(Config.targetServerAddr))) {
            System.out.println("关闭nacos........");
        } else {
            System.out.println("开始nacos........");
        }
    }
}
