package com.example.tools.util;

import org.springframework.core.env.Environment;


public class LoadPropertyUtil {

    private static Environment environment = (Environment) SpringUtils.getBean(Environment.class);

    public static Object getProperty(String key) {
        return environment.getProperty(key);
    }
}
