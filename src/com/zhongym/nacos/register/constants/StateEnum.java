package com.zhongym.nacos.register.constants;

/**
 * 所有
 * 健康
 * 死亡
 *
 * @author Yuanmao.Zhong
 */
@SuppressWarnings("all")
public enum StateEnum {
    ALL("所有"),
    HEALTH("健康"),
    DEATH("死亡");

    private final String title;

    StateEnum(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
