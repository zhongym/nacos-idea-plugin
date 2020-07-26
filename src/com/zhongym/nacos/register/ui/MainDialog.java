package com.zhongym.nacos.register.ui;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhongym.nacos.register.Config;
import com.zhongym.nacos.register.constants.ServerStatusEnum;
import com.zhongym.nacos.register.utils.GateWayService;
import com.zhongym.nacos.register.utils.MyIconLoader;
import com.zhongym.nacos.register.utils.NacosService;
import com.zhongym.nacos.register.utils.ThreadHelper;
import com.zhongym.nacos.register.constants.IpEnum;
import com.zhongym.nacos.register.constants.StateEnum;

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
    private final List<SourceServiceItem> sourceCheckBoxList = new ArrayList<>();

    /**
     * 目标服务控件
     */
    private JPanel targetServicePanel;
    private JComboBox targetStateComboBox;
    private JComboBox targetIpComboBox;


    public MainDialog() {
        setTitle("Nacos注册");
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

        flushTargetServicePanel();
    }

    private void initBaseServerState() {
        //初始化nacos状态
        ThreadHelper.async(this::updateNacosStatus);
        ThreadHelper.scheduleAtFixedRate(this::updateNacosStatus, 10);
        nacosStartButton.addActionListener(e -> {
            ThreadHelper.async(() -> {
                NacosService.triggerLocalNacos();
                updateNacosStatus();
            });
        });

        //初始化gateway状态
        ThreadHelper.async(this::updateGatewayStatus);
        ThreadHelper.scheduleAtFixedRate(this::updateGatewayStatus, 10);
        gatewayStartButton.addActionListener(e -> {
            ThreadHelper.async(() -> {
                GateWayService.trigger();
                updateGatewayStatus();
            });
        });
    }

    private void updateGatewayStatus() {
        System.out.println("刷新网关状态.....");
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
        System.out.println("刷新nacos状态.....");
        ServerStatusEnum statusEnum = NacosService.getServerStatus(Config.targetServerAddr);
        ThreadHelper.onUIThread(() -> {
            nacosStateLabel.setIcon(MyIconLoader.getIcon(statusEnum.getIcon()));
            if (ServerStatusEnum.UP.equals(statusEnum)) {
                nacosStartButton.setText("一键关闭");
            } else {
                nacosStartButton.setText("一键启动");
            }
        });
    }

    private void flushSourceServicePanel() {
        ServerStatusEnum serverStatus = NacosService.getServerStatus(Config.sourceServerAddr);
        sourceNacosStateLabel.setIcon(MyIconLoader.getIcon(serverStatus.getIcon()));

        boolean filterJob = sourceFilterJobCheckBox.isSelected();
        boolean filterHealthy = sourceFilterHealthyCheckBox.isSelected();
        ThreadHelper.async(() -> {
            Map<String, List<Instance>> allService;
            try {
                allService = NacosService.getAllService(Config.sourceServerAddr);
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
                    SourceServiceItem item = new SourceServiceItem(serviceName, insList, (itemObj) -> {
                        updateSourceStateForSelect();
                    });
                    sourceServicePanel.add(item.getPanel());
                    sourceCheckBoxList.add(item);
                });
                updateSourceStateForSelect();
                //重新调整窗口大小
                this.pack();
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


    private void flushTargetServicePanel() {
        StateEnum stateEnum = (StateEnum) targetStateComboBox.getSelectedItem();
        IpEnum ipEnum = (IpEnum) targetIpComboBox.getSelectedItem();

        ThreadHelper.async(() -> {
            Map<String, List<Instance>> allService;
            try {
                allService = NacosService.getAllService(Config.targetServerAddr);
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
            });
        });
    }


    private void register() {
        showSourceNacosLog("开始注册.....");
        ThreadHelper.async(() -> {
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

    private void showSourceNacosLog(String tip) {
        sourceLogLabel.setText(tip);
        sourceLogLabel.setVisible(false);
        sourceLogLabel.setVisible(true);
    }


    private void sleep(int n) {
        try {
            Thread.sleep(n);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void onClose() {
        // add your code here if necessary
        dispose();
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
