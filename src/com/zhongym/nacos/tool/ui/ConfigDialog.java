package com.zhongym.nacos.tool.ui;

import com.zhongym.nacos.tool.config.Config;
import com.zhongym.nacos.tool.utils.LogPrinter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuanmao.Zhong
 */
public class ConfigDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel settingItemPanel;

    private final List<ConfigSettingItem> settingItemList = new ArrayList<>();

    public ConfigDialog(Window window) {
        super(window);
        setTitle("配置参数");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        //初始化设置项
        List<Config.ConfigItem<?>> allItem = Config.getInstance().getAllItem();
        settingItemPanel.setLayout(new GridLayout(0, 1));
        for (Config.ConfigItem<?> configItem : allItem) {
            ConfigSettingItem settingItem = new ConfigSettingItem(configItem);
            settingItemPanel.add(settingItem.getPanel());
            settingItemList.add(settingItem);
        }

        //按钮事件
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
    }

    private void onOK() {
        //保存设置
        LogPrinter.print("保存设置");
        settingItemList.forEach(ConfigSettingItem::setting);
        Config.getInstance().save();
        dispose();
    }

    private void onCancel() {
        dispose();
    }

}
