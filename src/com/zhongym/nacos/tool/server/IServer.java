package com.zhongym.nacos.tool.server;

import com.zhongym.nacos.tool.constants.ServerEnum;
import com.zhongym.nacos.tool.constants.ServerStatusEnum;

/**
 * @author Yuanmao.Zhong
 */
public interface IServer {
    /**
     * 服务名
     *
     * @return StateEnum
     */
    ServerEnum getServerName();

    /**
     * 服务状态
     *
     * @return StateEnum
     */
    ServerStatusEnum getServerStatus();

    /**
     * 根据状态来判断是启动，还是停止
     */
    void trigger();

    /**
     * 启动服务
     */
    void start();


    /**
     * 停止服务
     */
    void stop();

    /**
     * 销毁
     */
    void destroy();

}
