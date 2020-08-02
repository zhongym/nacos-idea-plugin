package com.zhongym.nacos.tool.ui;

import com.zhongym.nacos.tool.config.Config;
import com.zhongym.nacos.tool.constants.ServerStatusEnum;
import com.zhongym.nacos.tool.server.IServer;
import com.zhongym.nacos.tool.utils.LogPrinter;
import com.zhongym.nacos.tool.utils.MyIconLoader;
import com.zhongym.nacos.tool.utils.ThreadHelper;

import javax.swing.*;
import javax.swing.border.Border;

/**
 * @author Yuanmao.Zhong
 */
public class ServerItem {
    private JPanel containerPanel;
    private JLabel stateLabel;
    private JButton startButton;

    private final IServer server;

    public ServerItem(IServer server) {
        this.server = server;
        //边框标题
        Border border = BorderFactory.createTitledBorder(server.getServerName().getTitle());
        containerPanel.setBorder(border);
        //监听事件
        this.startButton.addActionListener(e -> {
            triggerServer();
        });
        //刷新状态
        ThreadHelper.async(this::flushState);
        Integer interval = Config.getInstance().serverFlushInterval.getValue();
        if (interval > 0) {
            ThreadHelper.scheduleAtFixedRate(this::flushState, interval);
        }
    }

    private void triggerServer() {
        startButton.setEnabled(false);
        ThreadHelper.async(server::trigger);
        ThreadHelper.delay(this::flushState, 5);
        ThreadHelper.delayOnUIThread(() -> {
            startButton.setEnabled(true);
        }, 15);
    }

    private void flushState() {
        LogPrinter.print("刷新" + server.getServerName().getTitle() + "状态");
        ServerStatusEnum statusEnum = server.getServerStatus();
        ThreadHelper.onUIThread(() -> {
            stateLabel.setIcon(MyIconLoader.getIcon(statusEnum.getIcon()));
            if (ServerStatusEnum.UP.equals(statusEnum)) {
                startButton.setText("一键关闭");
            } else {
                startButton.setText("一键启动");
            }
        });
    }

    public JPanel getContainerPanel() {
        return containerPanel;
    }
}
