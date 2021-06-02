package com.zhongym.nacos.tool.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.alibaba.nacos.client.naming.net.NamingProxy;
import com.zhongym.nacos.tool.config.Config;
import com.zhongym.nacos.tool.config.Namespace;
import com.zhongym.nacos.tool.constants.IpEnum;
import com.zhongym.nacos.tool.constants.ServerStatusEnum;
import com.zhongym.nacos.tool.constants.StateEnum;
import com.zhongym.nacos.tool.utils.LogPrinter;
import com.zhongym.nacos.tool.utils.MyException;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Yuanmao.Zhong
 */
public class NacosService {
    public static Map<String, NacosNamingService> nacosServiceMap = new HashMap<>();

    static {
        System.setProperty("com.alibaba.nacos.client.naming.ctimeout", "400");
    }

    public static NacosNamingService getInstance(String nacosAddr, String namespace) {
        NacosNamingService nacosService = nacosServiceMap.get(getKey(nacosAddr, namespace));
        if (nacosService == null) {
            try {
                Properties properties = new Properties();
                properties.setProperty(PropertyKeyConst.SERVER_ADDR, nacosAddr);
                if (namespace != null && namespace.length() > 0) {
                    properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
                }

                nacosService = new NacosNamingService(properties);
                if (!"UP".equals(nacosService.getServerStatus())) {
                    throw new MyException("注册中心状态DOWN");
                }
            } catch (Exception e) {
                LogPrinter.print("连接注册中心失败");
                LogPrinter.print(e);
            }
            nacosServiceMap.put(getKey(nacosAddr, namespace), nacosService);
        }
        return nacosService;
    }

    public static List<Namespace> getNamespaces(String nacosAddr) {
        try {
            NacosNamingService instance = getInstance(nacosAddr, null);
            Field field = instance.getClass().getDeclaredField("serverProxy");
            field.setAccessible(true);
            NamingProxy serverProxy = (NamingProxy) field.get(instance);
            String json = serverProxy.reqAPI("/nacos/v1/console/namespaces", new LinkedHashMap<>(), "GET");
            JSONObject result = JSONObject.parseObject(json);
            if (result.getIntValue("code") != 200) {
                throw new MyException("查询命名空间失败:" + result.getString("message"));
            }
            return result.getJSONArray("data").toJavaList(Namespace.class);
        } catch (Exception e) {
            e.printStackTrace();
            LogPrinter.print("查询命名空间失败:" + e.getMessage());
            return new ArrayList<>();
        }
    }


    public static ServerStatusEnum getServerStatus(String nacosAddr) {
        try {
            NamingService nacosService = getInstance(nacosAddr, null);
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
                List<Instance> instanceList = getInstance(Config.getRemoteNacos(), Config.getRemoteNameSpace()).getAllInstances(serviceName, Config.getNacosGropName());
                for (Instance instance : instanceList) {
                    getInstance(Config.getLocalNacos(), Config.getLocalNameSpace()).registerInstance(serviceName, Config.getNacosGropName(), instance);
                }
            }
        } catch (Exception e) {
            callBack.accept(e.getMessage());
            LogPrinter.print(e);
        }
    }

    public static Map<String, List<Instance>> getAllService(String nacosAddr, String namespace) {
        try {
            Map<String, List<Instance>> map = new LinkedHashMap<>();
            List<String> serviceNameList = getInstance(nacosAddr, namespace).getServicesOfServer(1, 1000, Config.getNacosGropName()).getData();
            for (String serviceName : serviceNameList) {
                List<Instance> instanceList = getInstance(nacosAddr, namespace).getAllInstances(serviceName, Config.getNacosGropName());
                map.put(serviceName, instanceList);
            }
            return map;
        } catch (NacosException e) {
            LogPrinter.print(e);
            return new HashMap<>();
        }
    }

    public static Map<String, List<Instance>> getAllService(String nacosAddr, String namespace, StateEnum stateEnum, IpEnum ipEnum, String filterName, boolean filterJob) {
        Map<String, List<Instance>> allService = NacosService.getAllService(nacosAddr, namespace);
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

    private static String getKey(String nacosAddr, String namespace) {
        return nacosAddr + namespace;
    }
}
