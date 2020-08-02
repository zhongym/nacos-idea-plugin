package com.zhongym.nacos.register.ui;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhongym.nacos.register.config.Config;
import com.zhongym.nacos.register.constants.ServerStatusEnum;
import com.zhongym.nacos.register.utils.LogPrinter;
import com.zhongym.nacos.register.utils.MyIconLoader;
import com.zhongym.nacos.register.utils.NacosService;
import com.zhongym.nacos.register.utils.ThreadHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Consumer;


/**
 * @author Yuanmao.Zhong
 */
public class SourceServiceItem {
    private JPanel panel;
    private JLabel nameField;
    private JLabel hostField;
    private JLabel iconField;
    private JPanel textPanel;
    private JLabel selectStateField;

    private boolean isSelected;

    public SourceServiceItem(MainDialog dialog, String serviceName, List<Instance> insList) {
        String host = insList.stream().findFirst().map(i -> i.getIp() + ":" + i.getPort()).orElse("没有实例");
        boolean healthy = insList.stream().anyMatch(Instance::isHealthy);
        if (healthy) {
            iconField.setIcon(MyIconLoader.getIcon("send-l-b.png"));
        } else {
            iconField.setIcon(MyIconLoader.getIcon("addr-icon.png"));
        }
        updateIcon();
        nameField.setText(serviceName);
        hostField.setText(host);

        textPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        textPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //左键
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == 0) {
                    setSelected(!isSelected());
                    dialog.updateSourceStateForSelect();
                }
                //右键
                if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0) {
                    JMenuItem item = new JMenuItem(MyIconLoader.getIcon("sc.png"));
                    item.setText("注册到本机");
                    item.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            dialog.showSourceNacosLog("开始注册.....");
                            ThreadHelper.async(() -> {
                                ServerStatusEnum serverStatus = NacosService.getServerStatus(Config.getLocalNacos());
                                if (ServerStatusEnum.DOWN.equals(serverStatus)) {
                                    ThreadHelper.onUIThread(() -> {
                                        dialog.showSourceNacosLog("本机注册中心没启动，此操作中止");
                                    });
                                    ThreadHelper.delayOnUIThread(dialog::updateSourceStateForSelect, 4);
                                    return;
                                }
                                try {
                                    for (Instance instance : insList) {
                                        NacosService.getInstance(Config.getLocalNacos()).registerInstance(serviceName, instance);
                                    }
                                    ThreadHelper.onUIThread(() -> {
                                        dialog.showSourceNacosLog("注册完成，等待界面刷新");
                                    });
                                    ThreadHelper.delayOnUIThread(() -> {
                                        dialog.flushTargetServicePanel();
                                        dialog.showSourceNacosLog("界面刷新完成");
                                    }, 3);
                                } catch (Exception ex) {
                                    LogPrinter.print(ex);
                                    ThreadHelper.onUIThread(() -> {
                                        dialog.showSourceNacosLog("注册失败");
                                    });
                                    ThreadHelper.delayOnUIThread(dialog::updateSourceStateForSelect, 4);
                                }
                            });
                        }
                    });

                    JPopupMenu menu = new JPopupMenu("操作");
                    menu.add(item);
                    menu.show(textPanel, 0, 0);
                }
            }
        });
    }

    private void updateIcon() {
        if (isSelected) {
            selectStateField.setIcon(MyIconLoader.getIcon("ask-check2.png"));
        } else {
            selectStateField.setIcon(null);
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean select) {
        isSelected = select;
        updateIcon();
    }

    public String getServiceName() {
        return nameField.getText();
    }
}
