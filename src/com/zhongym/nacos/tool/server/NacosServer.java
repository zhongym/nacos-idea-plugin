package com.zhongym.nacos.tool.server;

import com.alibaba.nacos.client.naming.NacosNamingService;
import com.alibaba.nacos.client.naming.core.HostReactor;
import com.zhongym.nacos.tool.config.Config;
import com.zhongym.nacos.tool.constants.ServerEnum;
import com.zhongym.nacos.tool.constants.ServerStatusEnum;
import com.zhongym.nacos.tool.utils.FileUtils;
import com.zhongym.nacos.tool.utils.LogPrinter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Yuanmao.Zhong
 */
public class NacosServer extends BaseServer {

    private static NacosServer server;

    public static NacosServer getServer() {
        if (server == null) {
            server = new NacosServer();
        }
        return server;
    }

    @Override
    public ServerEnum getServerName() {
        return ServerEnum.NACOS;
    }

    @Override
    public ServerStatusEnum getServerStatus() {
        return NacosService.getServerStatus(Config.getLocalNacos());
    }

    @Override
    protected List<String> getStartParams() {
        List<String> params = new ArrayList<>();
        params.add(" -Xms128m -Xmx128m");
        params.add(" -Dnacos.home=" + FileUtils.getLogDir("nacos"));
        params.add(" -Dserver.tomcat.basedir=" + FileUtils.getLogDir("nacosTomcat"));
        params.add(" -Dlogging.level.root=debug");
        params.add(" -Dserver.port=" + Config.getInstance().nacosPort.getValue());
        params.add(" -DnacosDbUrl=" + Config.getInstance().nacosDbUrl.getValue());
        params.add(" -DnacosDbUser=" + Config.getInstance().nacosDbUser.getValue());
        params.add(" -DnacosDbPassword=" + Config.getInstance().nacosDbPassword.getValue());
        return params;
    }

    @Override
    protected String getStopScript() {
        return "stop-nacos.bat";
    }

    @Override
    protected void onDestroy() {
        try {
            NacosNamingService instance = NacosService.getInstance(Config.getLocalNacos());
            HostReactor hostReactor = getValue(instance, "hostReactor", HostReactor.class);
            if (hostReactor != null) {
                ScheduledExecutorService executor = getValue(hostReactor, "executor", ScheduledExecutorService.class);
                if (executor != null) {
                    executor.shutdownNow();
                }
            }
            ScheduledExecutorService executor = getValue(instance.getBeatReactor(), "executorService", ScheduledExecutorService.class);
            if (executor != null) {
                executor.shutdownNow();
            }
        } catch (Exception e) {
            LogPrinter.print(e);
        }
        server = null;
    }

    private static <T> T getValue(Object o, String fieldN, Class<T> c) {
        try {
            Field field = o.getClass().getDeclaredField(fieldN);
            field.setAccessible(true);
            return (T) field.get(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
