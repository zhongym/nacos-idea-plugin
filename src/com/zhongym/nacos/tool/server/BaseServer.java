package com.zhongym.nacos.tool.server;

import com.zhongym.nacos.tool.constants.ServerStatusEnum;
import com.zhongym.nacos.tool.utils.LogPrinter;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * @author Yuanmao.Zhong
 */
public abstract class BaseServer implements IServer {

    /**
     * 根据状态来判断是启动，还是停止
     */
    @Override
    @SuppressWarnings("all")
    public void trigger() {
        ServerStatusEnum serverStatus = getServerStatus();
        if (ServerStatusEnum.UP.equals(serverStatus)) {
            stop();
        } else {
            new Thread() {
                @Override
                public void run() {
                    BaseServer.this.start();
                }
            }.start();
        }
    }

    /**
     * 启动服务
     */
    @Override
    public void start() {
        LogPrinter.print("启动" + getServerName().getTitle() + " ....");
        String path = Optional.ofNullable(this.getClass().getClassLoader().getResource(getServerName().getCode() + ".jar"))
                .map(URL::getPath)
                .map(s -> {
                    if (s.startsWith("/")) {
                        return s.substring(1);
                    }
                    return s;
                }).orElse(null);
        if (path == null) {
            LogPrinter.print("找不到" + getServerName().getTitle() + "启动jar，启动中止");
            return;
        }
        StringBuilder commands = new StringBuilder("java -Dfile.encoding=utf-8 -jar ");
        for (String param : getStartParams()) {
            commands.append(param).append(" ");
        }
        commands.append(path);
        String command = commands.toString();
        LogPrinter.print("启动命令：" + command);
        try {
            int i = CommandLineUtils.executeCommandLine(new Commandline(command), LogPrinter::printServerLog, LogPrinter::printServerLog, -1);
        } catch (CommandLineException e) {
            LogPrinter.print(e);
        }
    }

    /**
     * 启动参数
     */
    protected abstract List<String> getStartParams();


    /**
     * 停止服务
     */
    @Override
    public void stop() {
        LogPrinter.print("停止" + getServerName().getTitle() + " ....");
        String path = Optional.ofNullable(this.getClass().getClassLoader().getResource(getStopScript()))
                .map(URL::getPath)
                .map(s -> {
                    if (s.startsWith("/")) {
                        return s.substring(1);
                    }
                    return s;
                }).orElse(null);
        if (path == null) {
            LogPrinter.print("找不到" + getServerName().getTitle() + "停止脚本，启动中止");
            return;
        }
        try {
            CommandLineUtils.executeCommandLine(new Commandline(path), LogPrinter::print, LogPrinter::print, -1);
        } catch (CommandLineException e) {
            LogPrinter.print(e);
        }
    }

    protected abstract String getStopScript();


    @Override
    public void destroy() {
        LogPrinter.print("销毁" + getServerName().getTitle() + " ....");
        stop();
        onDestroy();
    }

    protected void onDestroy() {

    }

}
