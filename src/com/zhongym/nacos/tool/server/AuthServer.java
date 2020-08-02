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
public class AuthServer extends BaseServer {

    private static AuthServer server;

    public static AuthServer getServer() {
        if (server == null) {
            server = new AuthServer();
        }
        return server;
    }

    @Override
    public ServerEnum getServerName() {
        return ServerEnum.AUTH;
    }

    @Override
    public ServerStatusEnum getServerStatus() {
        String url = Config.getAuthUrl() + "/actuator/health";
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
    public List<String> getStartParams() {
        List<String> params = new ArrayList<>(5);
        params.add(" -Xms128m -Xmx128m");
        params.add(" -Dserver.port=" + Config.getInstance().authPort.getValue());
        return params;
    }

    @Override
    protected String getStopScript() {
        return "stop-auth.bat";
    }

    @Override
    protected void onDestroy() {
        server = null;
    }

    public static void main(String[] args) {
        getServer().start();
    }
}
