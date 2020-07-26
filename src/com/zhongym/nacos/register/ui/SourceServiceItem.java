package com.zhongym.nacos.register.ui;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zhongym.nacos.register.utils.MyIconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    public SourceServiceItem(String serviceName, List<Instance> insList, Consumer<SourceServiceItem> clickCallBack) {
        String host = insList.stream().findFirst().map(i -> i.getIp() + ":" + i.getPort()).orElse("没有实例");
        boolean healthy = insList.stream().anyMatch(Instance::isHealthy);
        if (healthy) {
            iconField.setIcon(MyIconLoader.getIcon("send-p-icon1.png"));
        } else {
            iconField.setIcon(MyIconLoader.getIcon("tip-icon.png"));
        }
        updateIcon();
        nameField.setText(serviceName);
        hostField.setText(host);

        textPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        textPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(!isSelected());
                clickCallBack.accept(SourceServiceItem.this);
            }
        });
    }

    private void updateIcon() {
        if (isSelected) {
            selectStateField.setIcon(MyIconLoader.getIcon("layer-icon03.png"));
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
