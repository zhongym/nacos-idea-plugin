package com.zhongym.nacos.tool.config;

import java.io.Serializable;

public class Namespace implements Serializable {
    private String namespaceShowName;
    private String namespace;



    @Override
    public String toString() {
        return namespaceShowName;
    }

    public String getNamespaceShowName() {
        return namespaceShowName;
    }

    public void setNamespaceShowName(String namespaceShowName) {
        this.namespaceShowName = namespaceShowName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
