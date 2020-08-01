package com.zhongym.nacos.register.ui;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhongym.nacos.register.config.Config;
import com.zhongym.nacos.register.constants.IpEnum;
import com.zhongym.nacos.register.constants.ServerStatusEnum;
import com.zhongym.nacos.register.constants.StateEnum;
import com.zhongym.nacos.register.utils.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Yuanmao.Zhong
 */
public class MainDialog extends JDialog {
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
    private JPanel sourceServicePanel;
    private JCheckBox sourceFilterJobCheckBox;
    private JCheckBox sourceFilterHealthyCheckBox;
    private JButton sourceAllButton;
    private JButton sourceRegisterButton;
    private JButton sourceUnAllButton;
    private JLabel sourceLogLabel;
    private JLabel sourceNacosStateLabel;
    private JButton freshenSourceButton;
    private final List<SourceServiceItem> sourceCheckBoxList = new ArrayList<>();

    /**
     * 目标服务控件
     */
    private JPanel targetServicePanel;
    private JComboBox targetStateComboBox;
    private JComboBox targetIpComboBox;
    private JButton freshenTargetButton;

    public MainDialog() {
        setTitle("Mall Tool");
        setContentPane(contentPane);
        setModal(true);

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

        LogPrinter.init(s -> {
            logTextPane.setText(s);
            logTextPane.setCaretPosition(logTextPane.getStyledDocument().getLength());
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
        sourceRegisterButton.addActionListener(e -> register());

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
        freshenTargetButton.addActionListener(e -> {
            flushTargetServicePanel();
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
        ServerStatusEnum serverStatus = NacosService.getServerStatus(Config.getRemoteNacos());
        sourceNacosStateLabel.setIcon(MyIconLoader.getIcon(serverStatus.getIcon()));

        boolean filterJob = sourceFilterJobCheckBox.isSelected();
        boolean filterHealthy = sourceFilterHealthyCheckBox.isSelected();
        ThreadHelper.async(() -> {
            Map<String, List<Instance>> allService;
            try {
                allService = NacosService.getAllService(Config.getRemoteNacos());
            } catch (Exception e) {
                ThreadHelper.onUIThread(() -> {
                    showSourceNacosLog(e.getMessage());
                });
                return;
            }

            ThreadHelper.onUIThread(() -> {
                sourceCheckBoxList.clear();
                sourceServicePanel.removeAll();
                sourceServicePanel.setLayout(new GridLayout(10, 2));
                allService.forEach((serviceName, insList) -> {
                    if (filterJob && serviceName.contains("-job")) {
                        return;
                    }
                    if (filterHealthy && insList.stream().noneMatch(Instance::isHealthy)) {
                        return;
                    }
                    SourceServiceItem item = new SourceServiceItem(MainDialog.this, serviceName, insList, (itemObj) -> {
                        updateSourceStateForSelect();
                    });
                    sourceServicePanel.add(item.getPanel());
                    sourceCheckBoxList.add(item);
                });
                updateSourceStateForSelect();
                //重新调整窗口大小
                this.pack();
                LogPrinter.print("刷新源注册中心服务列表完成......");
            });
        });
    }

    private void updateSourceStateForSelect() {
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

        ThreadHelper.async(() -> {
            Map<String, List<Instance>> allService;
            try {
                allService = NacosService.getAllService(Config.getLocalNacos());
            } catch (Exception e) {
                ThreadHelper.onUIThread(() -> {
//                    stateField.setText(e.getMessage());
                });
                return;
            }

            ThreadHelper.onUIThread(() -> {
                targetServicePanel.removeAll();
                targetServicePanel.setLayout(new GridLayout(10, 2));
                allService.forEach((serviceName, insList) -> {
                    if (StateEnum.HEALTH.equals(stateEnum) && (insList.isEmpty() || insList.stream().noneMatch(Instance::isHealthy))) {
                        return;
                    }
                    if (StateEnum.DEATH.equals(stateEnum) && insList.stream().anyMatch(Instance::isHealthy)) {
                        return;
                    }
                    if (IpEnum.LOCAL.equals(ipEnum) && (insList.isEmpty() || insList.stream().noneMatch(i -> IpEnum.isLocalIp(i.getIp())))) {
                        return;
                    }
                    if (IpEnum.REMOTE.equals(ipEnum) && (insList.isEmpty() || insList.stream().anyMatch(i -> IpEnum.isLocalIp(i.getIp())))) {
                        return;
                    }
                    TargetServiceItem item = new TargetServiceItem(MainDialog.this, serviceName, insList);
                    targetServicePanel.add(item.getPanel());
                });
                targetServicePanel.setVisible(false);
                targetServicePanel.setVisible(true);
                //重新调整窗口大小
                this.pack();
                LogPrinter.print("刷新本机注册中心服务列表完成......");
            });
        });
    }


    private void register() {
        showSourceNacosLog("开始注册.....");
        LogPrinter.print("开始注册.....");
        ThreadHelper.async(() -> {
            List<String> serviceNames = sourceCheckBoxList.stream()
                    .filter(SourceServiceItem::isSelected)
                    .map(SourceServiceItem::getServiceName)
                    .collect(Collectors.toList());
            NacosService.registerInstance(serviceNames, msg -> {
                MainDialog.this.sleep(100);
                ThreadHelper.onUIThread(() -> {
                    showSourceNacosLog("注册中... " + msg);
                    LogPrinter.print("注册中... " + msg);
                });
            });
            ThreadHelper.onUIThread(() -> {
                showSourceNacosLog("注册完成，等待界面刷新");
                LogPrinter.print("注册完成，等待界面刷新");
            });
            ThreadHelper.delayOnUIThread(() -> {
                flushTargetServicePanel();
                showSourceNacosLog("界面刷新完成");
                LogPrinter.print("界面刷新完成");
            }, 3);
        });
    }

    public void removeTargetServiceItem(TargetServiceItem item) {
        targetServicePanel.remove(item.getPanel());
        targetServicePanel.setVisible(false);
        targetServicePanel.setVisible(true);
        this.pack();
    }

    private void showSourceNacosLog(String tip) {
        sourceLogLabel.setText(tip);
        sourceLogLabel.setVisible(false);
        sourceLogLabel.setVisible(true);
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
        dispose();
    }

    private void destroy() {
        NacosService.stopLocalNacos();
        GateWayService.close();
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

}
