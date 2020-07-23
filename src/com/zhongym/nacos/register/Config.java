package com.zhongym.nacos.register;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

public class Config extends ArrayList<String>{

    public List<Long> get(){
        return null;
    }

    public static String sourceServerAddr = "192.168.2.33:8848";
//    public static String sourceServerAddr = "test-admin.mall.perfectdiary.com:8848";
    public static String targetServerAddr = "127.0.0.1:8848";
    public static List<String> serviceNameList = new ArrayList<>();

    public static void main(String[] args) throws NoSuchMethodException, JsonProcessingException {
//        Method get = Config.class.getMethod("get");
//        Type genericReturnType = get.getGenericReturnType();
//        System.out.println(genericReturnType);

        Object o = new ObjectMapper().readValue("{\"id\":1}", Object.class);
        System.out.println(o);
    }
}
