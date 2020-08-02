package com.zhongym.nacos.tool.ui;

import com.zhongym.nacos.tool.config.Config;

import javax.swing.*;

/**
 * @author Yuanmao.Zhong
 */
public class ConfigSettingItem {
    private final Config.ConfigItem<?> item;
    private JPanel panel;
    private JLabel titleField;
    private JTextField valueTextField;

    public ConfigSettingItem(Config.ConfigItem<?> item) {
        this.item = item;
        titleField.setText(item.getTitle() + ":");
        valueTextField.setText(item.getValueString());
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setting() {
        String text = valueTextField.getText();
        if (text != null && !text.equals(item.getValueString())) {
            item.setting(text);
        }
    }
}
