package com.zhongym.nacos.register;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.zhongym.nacos.register.ui.GenDialog;

/**
 * @author Yuanmao.Zhong
 */
public class NacosRegisterAction extends AnAction {

    private static GenDialog dialog;

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (dialog == null) {
            dialog = new GenDialog();
        }
        dialog.pack();
        dialog.setVisible(true);
    }
}
