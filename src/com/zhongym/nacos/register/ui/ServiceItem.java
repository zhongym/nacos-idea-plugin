package com.zhongym.nacos.register.ui;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;


public class ServiceItem {
    private JPanel panel;
    private JLabel nameField;
    private JLabel hostField;
    private JLabel iconField;

    public ServiceItem(String serviceName, String host, boolean healthy) {
        if (healthy) {
            iconField.setIcon(IconLoader.findIcon("icon/layer-icon03.png"));
        } else {
            iconField.setIcon(IconLoader.findIcon("icon/detail-01-state.png"));
        }
        nameField.setText(serviceName);
        hostField.setText(host);
    }

    public JPanel getPanel() {
        return panel;
    }

    public static void main(String[] args) throws InterruptedException {
        JPanel dd = new ServiceItem("mall-order", "dd", false).getPanel();
        dd.setVisible(true);
        Thread.sleep(100000);
    }
}
