package com.zhongym.nacos.register.ui;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhongym.nacos.register.Config;
import com.zhongym.nacos.register.NacosService;
import com.zhongym.nacos.register.ThreadHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.awt.Toolkit.getDefaultToolkit;

/**
 * @author Yuanmao.Zhong
 */
public class GenDialog2 extends JDialog {
    private JPanel contentPane;
    /**
     * 源服务列表
     */
    private JPanel sourceServicePanel;
    /**
     * 目标服务列表
     */
    private JPanel targetServicePanel;

    /**
     * 注册按钮
     */
    private JButton buttonOK;
    /**
     * 关闭按钮
     */
    private JButton buttonCancel;
    /**
     * 全选按钮
     */
    private JButton allButton;
    /**
     * 取消按钮
     */
    private JButton unAllButton;
    /**
     * 过滤job
     */
    private JCheckBox filterJobCheckBox;
    /**
     * 过滤非健康的
     */
    private JCheckBox filterHealthyCheckBox;
    /**
     * 注册按钮
     */
    private JButton buttonUnRegister;
    /**
     * 状态标签
     */
    private JLabel stateField;
    /**
     * 后台运行按钮
     */
    private JButton buttonBg;
    private JPanel nacosPanel;
    private JPanel gatewayPanel;
    private JButton 一键启动Button;
    private JButton 一键启动Button1;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JButton 全部选择Button;
    private JButton 注册到本机Button;
    private JButton 全部取消Button;
    private JButton 设置Button;
    /**
     * 源所有服务多选择框
     */
    private final List<JCheckBox> sourceCheckBoxList = new ArrayList<>();
    /**
     * 目标所有服务多选择框
     */
    private final List<JCheckBox> targetCheckBoxList = new ArrayList<>();

    public GenDialog2() {
        setIconImage(getDefaultToolkit().getImage("icon/1.png"));
        setTitle("Nacos注册");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        //默认过滤job
        filterJobCheckBox.setSelected(true);
        filterHealthyCheckBox.setSelected(true);

        //获取所有源服务名称列表
        flushSourceServicePanel();
        flushTargetServicePanel();

        //添加按钮事件
        buttonOK.addActionListener(e -> register());
        buttonUnRegister.addActionListener(e -> unRegister());
        allButton.addActionListener(e -> {
            for (JCheckBox checkBox : sourceCheckBoxList) {
                checkBox.setSelected(true);
            }
        });
        unAllButton.addActionListener(e -> {
            for (JCheckBox checkBox : sourceCheckBoxList) {
                checkBox.setSelected(false);
            }
        });
        buttonBg.addActionListener(e -> {
            GenDialog2.this.setVisible(false);
        });
        buttonCancel.addActionListener(e -> onCancel());


        //过滤
        filterJobCheckBox.addActionListener(e -> {
            flushSourceServicePanel();
            flushTargetServicePanel();
        });
        filterHealthyCheckBox.addActionListener(e -> {
            flushSourceServicePanel();
            flushTargetServicePanel();
        });
    }

    private void flushSourceServicePanel() {
        boolean filterJob = filterJobCheckBox.isSelected();
        boolean filterHealthy = filterHealthyCheckBox.isSelected();
        ThreadHelper.async(() -> {
            Map<String, List<Instance>> allService;
            try {
                allService = NacosService.getAllService(Config.sourceServerAddr);
            } catch (Exception e) {
                ThreadHelper.onUIThread(() -> {
                    stateField.setText(e.getMessage());
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
                    String host = insList.stream().findFirst().map(i -> i.getIp() + ":" + i.getPort()).orElse("没有实例");
                    ServiceItem item = new ServiceItem(serviceName, host,true);
                    sourceServicePanel.add(item.getPanel());
//                    sourceCheckBoxList.add(checkBox);
                });
                //重新调整窗口大小
                this.pack();
            });
        });
    }


    private void flushTargetServicePanel() {
        boolean filterJob = filterJobCheckBox.isSelected();
        boolean filterHealthy = filterHealthyCheckBox.isSelected();

        ThreadHelper.async(() -> {
            Map<String, List<Instance>> allService;
            try {
                allService = NacosService.getAllService(Config.targetServerAddr);
            } catch (Exception e) {
                ThreadHelper.onUIThread(() -> {
                    stateField.setText(e.getMessage());
                });
                return;
            }

            ThreadHelper.onUIThread(() -> {
                targetCheckBoxList.clear();
                targetServicePanel.removeAll();
                targetServicePanel.setLayout(new GridLayout(10, 2));
                allService.forEach((serviceName, insList) -> {
                    if (filterJob && serviceName.contains("-job")) {
                        return;
                    }
                    if (filterHealthy && insList.stream().noneMatch(Instance::isHealthy)) {
                        return;
                    }
                    JCheckBox checkBox = new JCheckBox(serviceName);
                    targetServicePanel.add(checkBox);
                    targetCheckBoxList.add(checkBox);
                });
                //重新调整窗口大小
                this.pack();
            });
        });
    }


    private void register() {
        showState("开始注册.....");
        ThreadHelper.async(() -> {
            List<String> serviceNames = sourceCheckBoxList.stream()
                    .filter(AbstractButton::isSelected)
                    .map(AbstractButton::getText)
                    .collect(Collectors.toList());
            NacosService.registerInstance(serviceNames, msg -> {
                GenDialog2.this.sleep(100);
                ThreadHelper.onUIThread(() -> {
                    showState("注册中... " + msg);
                });
            });
            ThreadHelper.onUIThread(() -> {
                flushTargetServicePanel();
                showState("注册完成");
            });
        });
    }


    private void showState(String tip) {
        stateField.setText(tip);
        stateField.setVisible(false);
        stateField.setVisible(true);
    }

    private void unRegister() {
        showState("开始注销.....");
        ThreadHelper.async(() -> {
            List<String> serviceNames = sourceCheckBoxList.stream()
                    .filter(AbstractButton::isSelected)
                    .map(AbstractButton::getText)
                    .collect(Collectors.toList());
            NacosService.unRegisterInstance(serviceNames, msg -> {
                ThreadHelper.onUIThread(() -> {
                    showState("注销中..." + msg);
                });
            });
            ThreadHelper.onUIThread(() -> {
                flushTargetServicePanel();
                showState("注销完成");
            });
        });
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
