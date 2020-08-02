package com.zhongym.nacos.tool.server;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.zhongym.nacos.tool.config.Config;
import com.zhongym.nacos.tool.constants.ServerEnum;
import com.zhongym.nacos.tool.constants.ServerStatusEnum;
import com.zhongym.nacos.tool.utils.LogPrinter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yuanmao.Zhong
 */
public class GateWayServer extends BaseServer {

    private static GateWayServer server;

    public static GateWayServer getServer() {
        if (server == null) {
            server = new GateWayServer();
        }
        return server;
    }

    @Override
    public ServerEnum getServerName() {
        return ServerEnum.GATE_WAY;
    }

    @Override
    public ServerStatusEnum getServerStatus() {
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


    @Override
    protected List<String> getStartParams() {
        List<String> params = new ArrayList<>(5);
        params.add("-Xms128m -Xmx128m");
        params.add("-Dserver.port=" + Config.getInstance().gatewayPort.getValue());
        return params;
    }

    @Override
    protected String getStopScript() {
        return "stop-gateway.bat";
    }

    @Override
    protected void onDestroy() {
        server = null;
    }
}
