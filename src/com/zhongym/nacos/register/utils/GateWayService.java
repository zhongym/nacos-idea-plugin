package com.zhongym.nacos.register.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.zhongym.nacos.register.config.Config;
import com.zhongym.nacos.register.constants.ServerStatusEnum;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * @author Yuanmao.Zhong
 */
public class GateWayService {

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
            new Thread() {
                @Override
                public void run() {
                    GateWayService.start();
                }
            }.start();
            LogPrinter.print("开启新线程完成........");
        }
    }

    public static void start() {
        LogPrinter.print("启动网关服务.....");
        String path = Optional.ofNullable(NacosService.class.getClassLoader().getResource("mall-gateway.jar"))
                .map(URL::getPath)
                .map(s -> {
                    if (s.startsWith("/")) {
                        return s.substring(1);
                    }
                    return s;
                })
                .orElseThrow(() -> new RuntimeException("找不到指定文件"));
        StringBuffer commands = new StringBuffer();
        commands.append("java -Dfile.encoding=utf-8 -jar ")
                .append(" -Xms128m -Xmx128m")
                .append(" -Dserver.port=").append(Config.getInstance().gatewayPort.getValue())
                .append(" ").append(path);
        String command = commands.toString();
        LogPrinter.print("启动命令：" + command);
        try {
            int i = CommandLineUtils.executeCommandLine(new Commandline(command), LogPrinter::printServerLog, LogPrinter::printServerLog, -1);
        } catch (CommandLineException e) {
            LogPrinter.print(e);
        }
    }

    public static void close() {
        LogPrinter.print("关闭网关服务.....");
        String path = Optional.ofNullable(NacosService.class.getClassLoader().getResource("stop-gateway.bat"))
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

    public static void main(String[] args) {
        start();
    }
}
