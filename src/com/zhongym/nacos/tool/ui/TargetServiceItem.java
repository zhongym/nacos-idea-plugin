package com.zhongym.nacos.tool.ui;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhongym.nacos.tool.config.Config;
import com.zhongym.nacos.tool.utils.MyIconLoader;
import com.zhongym.nacos.tool.server.NacosService;
import com.zhongym.nacos.tool.utils.ThreadHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;


/**
 * @author Yuanmao.Zhong
 */
public class TargetServiceItem {
    private JPanel panel;
    private JLabel nameField;
    private JLabel hostField;
    private JLabel iconField;
    private JPanel textPanel;

    public TargetServiceItem(MainDialog dialog, String serviceName, List<Instance> insList) {
        String host = insList.stream().findFirst().map(i -> i.getIp() + ":" + i.getPort()).orElse("没有实例");
        boolean healthy = insList.stream().anyMatch(Instance::isHealthy);
        if (healthy) {
            iconField.setIcon(MyIconLoader.getIcon("send-l-b.png"));
        } else {
            iconField.setIcon(MyIconLoader.getIcon("addr-icon.png"));
        }
        nameField.setText(serviceName);
        hostField.setText(host);

        textPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        textPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //右键
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                    JMenuItem item = new JMenuItem(MyIconLoader.getIcon("list-sel01.jpg"));
                    item.setText("移除");
                    item.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ThreadHelper.async(() -> {
                                try {
                                    for (Instance instance : insList) {
                                        NacosService.getInstance(Config.getLocalNacos(), Config.getLocalNameSpace()).deregisterInstance(serviceName, instance);
                                    }
                                    ThreadHelper.onUIThread(() -> {
                                        dialog.removeTargetServiceItem(TargetServiceItem.this);
                                    });
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

    public JPanel getPanel() {
        return panel;
    }

}
