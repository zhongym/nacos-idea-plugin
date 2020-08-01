package com.zhongym.nacos.register.utils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.zhongym.nacos.register.config.Config;
import com.zhongym.nacos.register.constants.ServerStatusEnum;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Yuanmao.Zhong
 */
public class NacosService {
    public static Map<String, NamingService> nacosServiceMap = new HashMap<>();

    static {
        System.setProperty("com.alibaba.nacos.client.naming.ctimeout", "400");
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

    public static void unRegisterInstance(List<String> serviceNameList, Consumer<String> callBack) {
        try {
            for (String serviceName : serviceNameList) {
                callBack.accept(serviceName);
                List<Instance> instanceList = getInstance(Config.getRemoteNacos()).getAllInstances(serviceName);
                for (Instance instance : instanceList) {
                    getInstance(Config.getLocalNacos()).deregisterInstance(serviceName, instance);
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

    public static void triggerLocalNacos() {
        if (ServerStatusEnum.UP.equals(getServerStatus(Config.getLocalNacos()))) {
            LogPrinter.print("关闭nacos........");
            stopLocalNacos();
        } else {
            LogPrinter.print("启动nacos........");
            new Thread() {
                @Override
                public void run() {
                    startLocalNacos();
                }
            }.start();
            LogPrinter.print("开启新线程完成........");
        }
    }

    public static void stopLocalNacos() {
        String path = Optional.ofNullable(NacosService.class.getClassLoader().getResource("stop-nacos.bat"))
                .map(URL::getPath)
                .map(s -> {
                    if (s.startsWith("/")) {
                        return s.substring(1);
                    }
                    return s;
                })
                .orElseThrow(() -> new RuntimeException("找不到指定文件"));
        try {
            int i = CommandLineUtils.executeCommandLine(new Commandline(path), LogPrinter::print, LogPrinter::print, -1);
        } catch (CommandLineException e) {
            LogPrinter.print(e);
        }
    }

    private static void startLocalNacos() {
        String path = Optional.ofNullable(NacosService.class.getClassLoader().getResource("nacos-server.jar"))
                .map(URL::getPath)
                .map(s -> {
                    if (s.startsWith("/")) {
                        return s.substring(1);
                    }
                    return s;
                })
                .orElseThrow(() -> new RuntimeException("找不到指定文件"));
        StringBuffer commands = new StringBuffer();
        commands.append("java -jar ")
                .append(" -Xms128m -Xmx128m")
                .append(" -Dlogging.level.root=debug")
                .append(" -Dserver.port=").append(com.zhongym.nacos.register.config.Config.getInstance().nacosPort.getValue())
                .append(" -DnacosDbUrl=").append(com.zhongym.nacos.register.config.Config.getInstance().nacosDbUrl.getValue())
                .append(" -DnacosDbUser=").append(com.zhongym.nacos.register.config.Config.getInstance().nacosDbUser.getValue())
                .append(" -DnacosDbPassword=").append(com.zhongym.nacos.register.config.Config.getInstance().nacosDbPassword.getValue())
                .append(" ").append(path);
        String command = commands.toString();
        LogPrinter.print("启动命令：" + command);
        try {
            int i = CommandLineUtils.executeCommandLine(new Commandline(command), LogPrinter::printServerLog, LogPrinter::printServerLog, -1);
        } catch (CommandLineException e) {
            LogPrinter.print(e);
        }
    }

    public static void main(String[] args) {
        startLocalNacos();
    }
}
