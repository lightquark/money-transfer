package org.lightquark.moneytransfer.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public class Config {

    private static Properties properties = new Properties();

    static {
        load();
    }

    private static void load() {
        try {
            properties.load(Config.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            log.error("Failed loading config", e);
        }
    }

    public static String getString(String key) {
        return properties.getProperty(key);
    }

    public static String getString(String key, String defaultValue) {
        String value = getString(key);
        return value != null ? value : defaultValue;
    }

    public static Integer getInteger(String key) {
        try {
            String s = properties.getProperty(key);
            return s != null ? Integer.parseInt(s) : null;
        } catch (Exception e) {
            log.warn("Failed parse property {}", key, e);
            return null;
        }
    }

    public static Integer getInteger(String key, Integer defaultValue) {
        Integer value = getInteger(key);
        return value != null ? value : defaultValue;
    }
}
