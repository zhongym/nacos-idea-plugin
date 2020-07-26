package com.zhongym.nacos.register.constants;

/**
 * @author Yuanmao.Zhong
 */

public enum ServerStatusEnum {
    /**
     * 启动
     */
    UP("state-up.png"),
    /**
     * 闭关
     */
    DOWN("state-down.png");

    private final String icon;

    ServerStatusEnum(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }
}
