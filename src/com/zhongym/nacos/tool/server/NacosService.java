package com.zhongym.nacos.tool.server;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.zhongym.nacos.tool.config.Config;
import com.zhongym.nacos.tool.constants.IpEnum;
import com.zhongym.nacos.tool.constants.ServerStatusEnum;
import com.zhongym.nacos.tool.constants.StateEnum;
import com.zhongym.nacos.tool.utils.LogPrinter;
import com.zhongym.nacos.tool.utils.MyException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Yuanmao.Zhong
 */
public class NacosService {
    public static Map<String, NacosNamingService> nacosServiceMap = new HashMap<>();

    static {
        System.setProperty("com.alibaba.nacos.client.naming.ctimeout", "400");
    }

    public static NacosNamingService getInstance(String nacosAddr) {
        NacosNamingService nacosService = nacosServiceMap.get(nacosAddr);
        if (nacosService == null) {
            try {
                nacosService = new NacosNamingService(nacosAddr);
                if (!"UP".equals(nacosService.getServerStatus())) {
                    throw new MyException("注册中心状态DOWN");
                }
            } catch (Exception e) {
                LogPrinter.print("连接注册中心失败");
                LogPrinter.print(e);
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
            LogPrinter.print(e);
            return ServerStatusEnum.DOWN;
        }
    }

    public static void registerInstance(List<String> serviceNameList, Consumer<String> callBack) {
        try {
            for (String serviceName : serviceNameList) {
                callBack.accept(serviceName);
                List<Instance> instanceList = getInstance(Config.getRemoteNacos()).getAllInstances(serviceName);
                for (Instance instance : instanceList) {
                    getInstance(Config.getLocalNacos()).registerInstance(serviceName, instance);
                }
            }
        } catch (Exception e) {
            callBack.accept(e.getMessage());
            LogPrinter.print(e);
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
            LogPrinter.print(e);
            return new HashMap<>();
        }
    }

    public static Map<String, List<Instance>> getAllService(String nacosAddr, StateEnum stateEnum, IpEnum ipEnum, String filterName, boolean filterJob) {
        Map<String, List<Instance>> allService = NacosService.getAllService(nacosAddr);
        Map<String, List<Instance>> resultMap = new LinkedHashMap<>();
        allService.forEach((serviceName, insList) -> {
            if (StateEnum.HEALTH.equals(stateEnum) && (insList.isEmpty() || insList.stream().noneMatch(Instance::isHealthy))) {
                return;
            }
            if (StateEnum.DEATH.equals(stateEnum) && insList.stream().anyMatch(Instance::isHealthy)) {
                return;
            }
            if (IpEnum.LOCAL.equals(ipEnum) && (insList.isEmpty() || insList.stream().noneMatch(i -> IpEnum.isLocalIp(i.getIp())))) {
                return;
            }
            if (IpEnum.REMOTE.equals(ipEnum) && (insList.isEmpty() || insList.stream().anyMatch(i -> IpEnum.isLocalIp(i.getIp())))) {
                return;
            }
            if (filterName.length() > 0 && !serviceName.contains(filterName)) {
                return;
            }
            if (filterJob && serviceName.contains("-job")) {
                return;
            }
            resultMap.put(serviceName, insList);
        });
        return resultMap;
    }

    public static void destroy() {
        nacosServiceMap.clear();
    }

}
