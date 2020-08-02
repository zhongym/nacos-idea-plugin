package com.zhongym.nacos.tool.ui;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhongym.nacos.tool.config.Config;
import com.zhongym.nacos.tool.constants.IpEnum;
import com.zhongym.nacos.tool.constants.ServerStatusEnum;
import com.zhongym.nacos.tool.constants.StateEnum;
import com.zhongym.nacos.tool.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yuanmao.Zhong
 */
public class MainDialog extends JDialog {
    private static MainDialog dialog;

    public static MainDialog getInstance() {
        if (dialog == null) {
            dialog = new MainDialog();
        }
        return dialog;
    }

    private JPanel contentPane;

    /**
     * 公共按钮
     */
    private JButton settingButton;
    private JButton closeButton;
    private JButton bgButton;
    private JTextPane logTextPane;
    private JScrollPane logScrollPane;

    /**
     * 基础服务控件
     */
    private JPanel nacosPanel;
    private JLabel nacosStateLabel;
    private JButton nacosStartButton;

    private JPanel gatewayPanel;
    private JLabel gatewayStateLabel;
    private JButton gatewayStartButton;

    /**
     * 源服务控件
     */
    private JScrollPane sourceScrollPane;
    private JPanel sourceServicePanel;
    private JCheckBox sourceFilterJobCheckBox;
    private JCheckBox sourceFilterHealthyCheckBox;
    private JButton sourceAllButton;
    private JButton sourceRegisterButton;
    private JButton sourceUnAllButton;
    private JLabel sourceLogLabel;
    private JLabel sourceNacosStateLabel;
    private JTextField sourceServerNameField;
    private JButton freshenSourceButton;
    private final List<SourceServiceItem> sourceCheckBoxList = new ArrayList<>();

    /**
     * 目标服务控件
     */
    private JScrollPane targetScollPane;
    private JPanel targetServicePanel;
    private JComboBox targetStateComboBox;
    private JComboBox targetIpComboBox;
    private JTextField targetServerNameField;
    private JButton freshenTargetButton;
    private JButton targetDeregisterButton;

    public MainDialog() {
        setTitle("Nacos Tool");
        setContentPane(contentPane);
        setModal(true);
        //初始化资源
        ThreadHelper.init();
        LogPrinter.init(s -> {
            logTextPane.setText(s);
            logTextPane.setCaretPosition(logTextPane.getStyledDocument().getLength() - 1);
        });

        //初始化nacos和gateway状态
        initBaseServerState();

        //目标控件初始化
        initTargetNacos();

        //源控件初始化
        initSourceNacos();

        //公共按钮
        bgButton.addActionListener(e -> {
            MainDialog.this.setVisible(false);
        });
        closeButton.addActionListener(e -> onClose());
        settingButton.addActionListener(e -> {
            ConfigDialog configDialog = new ConfigDialog(MainDialog.this);
            configDialog.setLocation((int) (MainDialog.this.getLocation().getX() + MainDialog.this.getLocation().getX() / 2),
                    (int) (MainDialog.this.getLocation().getY() + MainDialog.this.getLocation().getY() / 2));
            configDialog.pack();
            configDialog.setVisible(true);
        });
    }

    private void initSourceNacos() {
        sourceFilterJobCheckBox.setSelected(true);
        sourceFilterJobCheckBox.addActionListener(e -> {
            flushSourceServicePanel();
        });

        sourceFilterHealthyCheckBox.setSelected(true);
        sourceFilterHealthyCheckBox.addActionListener(e -> {
            flushSourceServicePanel();
        });

        sourceAllButton.addActionListener(e -> {
            for (SourceServiceItem checkBox : sourceCheckBoxList) {
                checkBox.setSelected(true);
            }
            updateSourceStateForSelect();
            MainDialog.this.pack();
        });
        sourceUnAllButton.addActionListener(e -> {
            for (SourceServiceItem checkBox : sourceCheckBoxList) {
                checkBox.setSelected(false);
            }
            updateSourceStateForSelect();
            MainDialog.this.pack();
        });
        freshenSourceButton.addActionListener(e -> {
            flushSourceServicePanel();
        });
        sourceServerNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                //按回车键执行相应操作;
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    flushSourceServicePanel();
                }
            }
        });
        sourceRegisterButton.addActionListener(e -> register());
        //初始化源注册中心面板
        flushSourceServicePanel();
    }

    private void initTargetNacos() {
        targetStateComboBox.removeAllItems();
        for (StateEnum stateEnum : StateEnum.values()) {
            targetStateComboBox.addItem(stateEnum);
        }
        targetStateComboBox.addActionListener(e -> {
            flushTargetServicePanel();
        });

        targetIpComboBox.removeAllItems();
        for (IpEnum ipEnum : IpEnum.values()) {
            targetIpComboBox.addItem(ipEnum);
        }
        targetIpComboBox.addActionListener(e -> {
            flushTargetServicePanel();
        });
        targetServerNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                //按回车键执行相应操作;
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    flushTargetServicePanel();
                }
            }
        });
        freshenTargetButton.addActionListener(e -> {
            flushTargetServicePanel();
        });
        targetDeregisterButton.addActionListener(e -> {
            targetDeregister();
        });

        flushTargetServicePanel();
    }

    private void initBaseServerState() {
        //初始化nacos状态
        ThreadHelper.async(this::updateNacosStatus);
        //定时刷新状态
        if (Config.getInstance().nacosFlushInterval.getValue() > 0) {
            ThreadHelper.scheduleAtFixedRate(() -> {
                try {
                    updateNacosStatus();
                } catch (Exception e) {
                    LogPrinter.print(e);
                }
            }, Config.getInstance().nacosFlushInterval.getValue());
        }
        nacosStartButton.addActionListener(e -> {
            nacosStartButton.setEnabled(false);
            ThreadHelper.async(NacosService::triggerLocalNacos);
            ThreadHelper.delay(() -> {
                updateNacosStatus();
            }, 5);
            ThreadHelper.delayOnUIThread(() -> {
                nacosStartButton.setEnabled(true);
            }, 15);
        });

        //初始化gateway状态
        ThreadHelper.async(this::updateGatewayStatus);
        if (Config.getInstance().gatewayFlushInterval.getValue() > 0) {
            ThreadHelper.scheduleAtFixedRate(() -> {
                try {
                    updateGatewayStatus();
                } catch (Exception e) {
                    LogPrinter.print(e);
                }
            }, Config.getInstance().gatewayFlushInterval.getValue());
        }
        gatewayStartButton.addActionListener(e -> {
            gatewayStartButton.setEnabled(false);

            ThreadHelper.async(GateWayService::trigger);
            ThreadHelper.delay(() -> {
                updateGatewayStatus();
            }, 5);
            ThreadHelper.delayOnUIThread(() -> {
                gatewayStartButton.setEnabled(true);
            }, 15);
        });
    }

    private void updateGatewayStatus() {
        LogPrinter.print("刷新网关状态.....");
        ServerStatusEnum statusEnum = GateWayService.getServerStatus();
        ThreadHelper.onUIThread(() -> {
            gatewayStateLabel.setIcon(MyIconLoader.getIcon(statusEnum.getIcon()));
            if (ServerStatusEnum.UP.equals(statusEnum)) {
                gatewayStartButton.setText("一键关闭");
            } else {
                gatewayStartButton.setText("一键启动");
            }
        });
    }

    private void updateNacosStatus() {
        LogPrinter.print("刷新nacos状态.....");
        ServerStatusEnum statusEnum = NacosService.getServerStatus(Config.getLocalNacos());
        ThreadHelper.onUIThread(() -> {
            nacosStateLabel.setIcon(MyIconLoader.getIcon(statusEnum.getIcon()));
            nacosStateLabel.setVisible(false);
            nacosStateLabel.setVisible(true);
            if (ServerStatusEnum.UP.equals(statusEnum)) {
                nacosStartButton.setText("一键关闭");
            } else {
                nacosStartButton.setText("一键启动");
            }
        });
    }

    public void flushSourceServicePanel() {
        LogPrinter.print("刷新源注册中心服务列表......");
        boolean filterJob = sourceFilterJobCheckBox.isSelected();
        boolean filterHealthy = sourceFilterHealthyCheckBox.isSelected();
        String filterName = Optional.ofNullable(sourceServerNameField.getText()).map(String::trim).orElse("");
        ThreadHelper.async(() -> {
            ServerStatusEnum serverStatus = NacosService.getServerStatus(Config.getRemoteNacos());
            ThreadHelper.onUIThread(() -> {
                sourceNacosStateLabel.setIcon(MyIconLoader.getIcon(serverStatus.getIcon()));
            });
            Map<String, List<Instance>> allService = NacosService.getAllService(Config.getRemoteNacos(),
                    filterHealthy ? StateEnum.HEALTH : StateEnum.ALL, IpEnum.ALL, filterName, filterJob);
            ThreadHelper.onUIThread(() -> {
                sourceCheckBoxList.clear();
                sourceServicePanel.removeAll();
                sourceServicePanel.setLayout(new GridLayout(10, 2));
                allService.forEach((serviceName, insList) -> {
                    SourceServiceItem item = new SourceServiceItem(MainDialog.this, serviceName, insList);
                    sourceServicePanel.add(item.getPanel());
                    sourceCheckBoxList.add(item);
                });
                updateSourceStateForSelect();
                sourceServicePanel.setVisible(false);
                sourceServicePanel.setVisible(true);
                sourceScrollPane.getVerticalScrollBar().setValue(0);
                //重新调整窗口大小
                this.pack();
                LogPrinter.print("刷新源注册中心服务列表完成......");
            });
        });
    }

    public void updateSourceStateForSelect() {
        long selectCount = sourceCheckBoxList.stream().filter(SourceServiceItem::isSelected).count();
        if (selectCount == 0) {
            showSourceNacosLog("等待操作....");
        } else {
            showSourceNacosLog("已选择" + selectCount + "个服务");
        }
    }


    public void flushTargetServicePanel() {
        LogPrinter.print("刷新本机注册中心服务列表......");
        StateEnum stateEnum = (StateEnum) targetStateComboBox.getSelectedItem();
        IpEnum ipEnum = (IpEnum) targetIpComboBox.getSelectedItem();
        String filterName = Optional.ofNullable(targetServerNameField.getText()).map(String::trim).orElse("");
        ThreadHelper.async(() -> {
            Map<String, List<Instance>> allService = NacosService.getAllService(Config.getLocalNacos(), stateEnum, ipEnum, filterName, false);
            ThreadHelper.onUIThread(() -> {
                targetServicePanel.removeAll();
                targetServicePanel.setLayout(new GridLayout(10, 2));
                allService.forEach((serviceName, insList) -> {
                    TargetServiceItem item = new TargetServiceItem(MainDialog.this, serviceName, insList);
                    targetServicePanel.add(item.getPanel());
                });
                targetServicePanel.setVisible(false);
                targetServicePanel.setVisible(true);
                targetScollPane.getVerticalScrollBar().setValue(0);
                //重新调整窗口大小
                this.pack();
                LogPrinter.print("刷新本机注册中心服务列表完成......");
            });
        });
    }

    public void targetDeregister() {
        LogPrinter.print("注销本机注册中心服务......");
        StateEnum stateEnum = (StateEnum) targetStateComboBox.getSelectedItem();
        IpEnum ipEnum = (IpEnum) targetIpComboBox.getSelectedItem();
        String filterName = Optional.ofNullable(targetServerNameField.getText()).map(String::trim).orElse("");
        ThreadHelper.async(() -> {
            Map<String, List<Instance>> allService = NacosService.getAllService(Config.getLocalNacos(), stateEnum, ipEnum, filterName, false);
            allService.forEach((serviceName, insList) -> {
                LogPrinter.print("注销中..." + serviceName);
                for (Instance instance : insList) {
                    try {
                        NacosService.getInstance(Config.getLocalNacos()).deregisterInstance(serviceName, instance);
                    } catch (NacosException e) {
                        LogPrinter.print(e);
                    }
                }
            });
            LogPrinter.print("注销完成，等待界面刷新");
            ThreadHelper.delayOnUIThread(this::flushTargetServicePanel, 3);
        });
    }

    private void register() {
        showSourceNacosLog("开始注册.....");
        ThreadHelper.async(() -> {
            ServerStatusEnum serverStatus = NacosService.getServerStatus(Config.getLocalNacos());
            if (ServerStatusEnum.DOWN.equals(serverStatus)) {
                ThreadHelper.onUIThread(() -> {
                    showSourceNacosLog("本机注册中心没启动，此操作中止");
                });
                ThreadHelper.delayOnUIThread(this::updateSourceStateForSelect, 4);
                return;
            }
            List<String> serviceNames = sourceCheckBoxList.stream()
                    .filter(SourceServiceItem::isSelected)
                    .map(SourceServiceItem::getServiceName)
                    .collect(Collectors.toList());
            NacosService.registerInstance(serviceNames, msg -> {
                MainDialog.this.sleep(100);
                ThreadHelper.onUIThread(() -> {
                    showSourceNacosLog("注册中... " + msg);
                });
            });
            ThreadHelper.onUIThread(() -> {
                showSourceNacosLog("注册完成，等待界面刷新");
            });
            ThreadHelper.delayOnUIThread(() -> {
                flushTargetServicePanel();
                showSourceNacosLog("界面刷新完成");
            }, 3);
        });
    }

    public void removeTargetServiceItem(TargetServiceItem item) {
        targetServicePanel.remove(item.getPanel());
        targetServicePanel.setVisible(false);
        targetServicePanel.setVisible(true);
        this.pack();
    }

    public void showSourceNacosLog(String tip) {
        sourceLogLabel.setText(tip);
        sourceLogLabel.setVisible(false);
        sourceLogLabel.setVisible(true);
        LogPrinter.print(tip);
    }


    private void sleep(int n) {
        try {
            Thread.sleep(n);
        } catch (InterruptedException e) {
            LogPrinter.print(e);
        }
    }

    private void onClose() {
        // add your code here if necessary
        ThreadHelper.async(() -> {
            destroy();
        });
        int s = 6;
        new Thread() {
            @Override
            public void run() {
                MainDialog.this.sleep(s * 1000);
                dispose();
            }
        }.start();
        JOptionPane.showMessageDialog(this, "开始销毁资源，" + s + "秒钟后关闭窗口", "关闭操作",
                JOptionPane.WARNING_MESSAGE, MyIconLoader.getIcon("state-down.png"));
    }

    private void destroy() {
        LogPrinter.print("开始销毁资源,5秒钟后关闭窗口");
        LogPrinter.print("关闭注册中心.....");
        NacosService.stopLocalNacos();
        LogPrinter.print("关闭网关.....");
        GateWayService.close();
        LogPrinter.print("销毁注册中心.....");
        NacosService.destroy();
        LogPrinter.print("销毁线程.....");
        ThreadHelper.destroy();
        LogPrinter.print("销毁日志.....");
        LogPrinter.destroy();

        MainDialog.dialog = null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainDialog dialog = new MainDialog();
            dialog.pack();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (int) screenSize.getWidth() / 2 - dialog.getWidth();
            int y = (int) screenSize.getHeight() / 15;
            dialog.setLocation(x, y);
            dialog.setVisible(true);
            System.exit(0);
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        logTextPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }

            @Override
            public void setSize(Dimension d) {
                if (d.width < getParent().getSize().width) {
                    d.width = getParent().getSize().width;
                }
                d.width += 100;
                super.setSize(d);
            }

        };
    }
}