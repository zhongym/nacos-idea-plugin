package com.zhongym.nacos.tool;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.zhongym.nacos.tool.ui.MainDialog;

import java.awt.*;

/**
 * @author Yuanmao.Zhong
 */
public class NacosAction extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        MainDialog dialog = MainDialog.getInstance();
        dialog.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) screenSize.getWidth() / 4;
        int y = (int) screenSize.getHeight() / 15;
        dialog.setLocation(x, y);
        dialog.setVisible(true);
    }
}
