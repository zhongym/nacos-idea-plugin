package com.zhongym.nacos.tool.constants;

@SuppressWarnings("all")
public enum ServerEnum {
    NACOS("nacos-server", "注册中心"),
    GATE_WAY("mall-gateway", "网关服务"),
    AUTH("mall-auth", "授权中心");

    private final String code;
    private final String title;

    ServerEnum(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }
}