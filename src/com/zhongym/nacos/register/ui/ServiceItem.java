package com.zhongym.nacos.register.ui;

import javax.swing.*;

public class ServiceItem{
    private JPanel panel;
    private JLabel nameField;
    private JLabel hostField;
    private JRadioButton stateButton;

    public ServiceItem(String serviceName,String host,boolean healthy) {
        nameField.setText(serviceName);
        hostField.setText(host);
    }

    public JPanel getPanel() {
        return panel;
    }
}
