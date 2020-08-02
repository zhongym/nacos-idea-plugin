package com.zhongym.nacos.tool.utils;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yuanmao.Zhong
 */
public class MyIconLoader {
    private static final Map<String, ImageIcon> iconMap = new HashMap<>();

    public static ImageIcon getIcon(String iconName) {
        ImageIcon imageIcon = iconMap.get(iconName);
        if (imageIcon == null) {
            imageIcon = new ImageIcon(Objects.requireNonNull(MyIconLoader.class.getClassLoader().getResource("icon/" + iconName)));
            iconMap.put(iconName, imageIcon);
        }
        return imageIcon;
    }
}
