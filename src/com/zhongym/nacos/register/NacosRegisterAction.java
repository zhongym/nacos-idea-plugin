package com.zhongym.nacos.register;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.zhongym.nacos.register.ui.GenDialog;
import com.zhongym.nacos.register.ui.GenDialog2;

/**
 * @author Yuanmao.Zhong
 */
public class NacosRegisterAction extends AnAction {

    private static GenDialog2 dialog;

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (dialog == null) {
            dialog = new GenDialog2();
        }
        dialog.pack();
        dialog.setVisible(true);
    }
}
