package com.zhongym.nacos.register.ui;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhongym.nacos.register.config.Config;
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

    public SourceServiceItem(MainDialog dialog, String serviceName, List<Instance> insList, Consumer<SourceServiceItem> clickCallBack) {
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
                    clickCallBack.accept(SourceServiceItem.this);
                }
                //右键
                if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0) {
                    JMenuItem item = new JMenuItem(MyIconLoader.getIcon("sc.png"));
                    item.setText("注册到本机");
                    item.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ThreadHelper.async(() -> {
                                try {
                                    for (Instance instance : insList) {
                                        NacosService.getInstance(Config.getLocalNacos()).registerInstance(serviceName, instance);
                                    }
                                    ThreadHelper.delayOnUIThread(() -> {
                                        dialog.flushTargetServicePanel();
                                    }, 3);
                                } catch (Exception ex) {
                                    ThreadHelper.onUIThread(() -> {
                                        JOptionPane.showMessageDialog(null, "操作失败", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                                    });
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
