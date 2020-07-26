package com.zhongym.nacos.register.ui;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhongym.nacos.register.Config;
import com.zhongym.nacos.register.NacosService;
import com.zhongym.nacos.register.ThreadHelper;
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
public class GenDialog2 extends JDialog {
    private JPanel contentPane;


    private JButton 设置Button;
    /**
     * 关闭按钮
     */
    private JButton buttonCancel;
    /**
     * 后台运行按钮
     */
    private JButton buttonBg;

    /**
     * 基础服务控件
     */
    private JPanel nacosPanel;
    private JPanel gatewayPanel;
    private JButton 一键启动Button;
    private JButton 一键启动Button1;


    /**
     * 源服务控件
     */
    private JPanel sourceServicePanel;
    private JCheckBox sourceFilterJobCheckBox;
    private JCheckBox sourceFilterHealthyCheckBox;
    private JButton sourceAllButton;
    private JButton sourceRegisterButton;
    private JButton sourceUnAllButton;
    private JLabel sourceStateLabel;
    private final List<SourceServiceItem> sourceCheckBoxList = new ArrayList<>();

    /**
     * 目标服务控件
     */
    private JPanel targetServicePanel;
    private JComboBox targetStateComboBox;
    private JComboBox targetIpComboBox;
    private final List<JCheckBox> targetCheckBoxList = new ArrayList<>();

    public GenDialog2() {
        setTitle("Nacos注册");
        setContentPane(contentPane);
        setModal(true);

        //目标控件初始化
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

        //源控件初始化
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
            GenDialog2.this.pack();
        });
        sourceUnAllButton.addActionListener(e -> {
            for (SourceServiceItem checkBox : sourceCheckBoxList) {
                checkBox.setSelected(false);
            }
            updateSourceStateForSelect();
            GenDialog2.this.pack();
        });

        sourceRegisterButton.addActionListener(e -> register());


        //公共按钮
        buttonBg.addActionListener(e -> {
            GenDialog2.this.setVisible(false);
        });
        buttonCancel.addActionListener(e -> onCancel());


        //获取所有源服务名称列表
        flushSourceServicePanel();
        flushTargetServicePanel();
    }

    private void flushSourceServicePanel() {
        boolean filterJob = sourceFilterJobCheckBox.isSelected();
        boolean filterHealthy = sourceFilterHealthyCheckBox.isSelected();
        ThreadHelper.async(() -> {
            Map<String, List<Instance>> allService;
            try {
                allService = NacosService.getAllService(Config.sourceServerAddr);
            } catch (Exception e) {
                ThreadHelper.onUIThread(() -> {
                    sourceStateLabel.setText(e.getMessage());
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
            showSourceState("等待操作....");
        } else {
            showSourceState("已选择" + selectCount + "个服务");
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
                targetCheckBoxList.clear();
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
                    TargetServiceItem item = new TargetServiceItem(GenDialog2.this, serviceName, insList);
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
        showSourceState("开始注册.....");
        ThreadHelper.async(() -> {
            List<String> serviceNames = sourceCheckBoxList.stream()
                    .filter(SourceServiceItem::isSelected)
                    .map(SourceServiceItem::getServiceName)
                    .collect(Collectors.toList());
            NacosService.registerInstance(serviceNames, msg -> {
                GenDialog2.this.sleep(100);
                ThreadHelper.onUIThread(() -> {
                    showSourceState("注册中... " + msg);
                });
            });
            ThreadHelper.onUIThread(() -> {
                showSourceState("注册完成，等待界面刷新");
            });
            ThreadHelper.delayOnUIThread(() -> {
                flushTargetServicePanel();
                showSourceState("界面刷新完成");
            }, 3);
        });
    }

    public void removeTargetServiceItem(TargetServiceItem item) {
        targetServicePanel.remove(item.getPanel());
        targetServicePanel.setVisible(false);
        targetServicePanel.setVisible(true);
        this.pack();
    }

    private void showSourceState(String tip) {
        sourceStateLabel.setText(tip);
        sourceStateLabel.setVisible(false);
        sourceStateLabel.setVisible(true);
    }

    private void unRegister() {
//        showState("开始注销.....");
//        ThreadHelper.async(() -> {
//            List<String> serviceNames = sourceCheckBoxList.stream()
//                    .filter(AbstractButton::isSelected)
//                    .map(AbstractButton::getText)
//                    .collect(Collectors.toList());
//            NacosService.unRegisterInstance(serviceNames, msg -> {
//                ThreadHelper.onUIThread(() -> {
//                    showState("注销中..." + msg);
//                });
//            });
//            ThreadHelper.onUIThread(() -> {
//                flushTargetServicePanel();
//                showState("注销完成");
//            });
//        });
    }

    private void sleep(int n) {
        try {
            Thread.sleep(n);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GenDialog2 dialog = new GenDialog2();
            dialog.pack();
            dialog.setVisible(true);
            System.exit(0);
        });
    }

}
