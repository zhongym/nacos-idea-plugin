package com.zhongym.nacos.register;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.zhongym.nacos.register.ui.MainDialog;

import java.awt.*;

/**
 * @author Yuanmao.Zhong
 */
public class NacosRegisterAction extends AnAction {

    private static MainDialog dialog;

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (dialog == null) {
            dialog = new MainDialog();
        }
        dialog.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) screenSize.getWidth() / 2 - dialog.getWidth();
        int y = (int) screenSize.getHeight() / 15;
        dialog.setLocation(x, y);
        dialog.setVisible(true);
    }
}
