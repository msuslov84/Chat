package com.suslov.cft.chat.common.service;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public final class PropertyUtil {

    private PropertyUtil() {
    }

    public static Connection getConnection() {
        return parseConnectionSettings();
    }

    public static Connection getConnectionFromString(String host, String port) {
        int iPort = 0;
        try {
            iPort = Integer.parseInt(port);
        } catch (NumberFormatException exp) {
            log.error("Value parsing error of 'port': value '" + port + "' is incorrect");
        }
        return new Connection(host, iPort);
    }

    private static Connection parseConnectionSettings() {
        Properties props = new Properties();
        try {
            props.load(PropertyUtil.class.getResourceAsStream("/connection.properties"));
        } catch (IOException | NullPointerException e) {
            log.error("Connection properties loading error", e);
            return new Connection("", 0);
        }
        return new Connection(parseToString(props, "host"), parseToInt(props, "port"));
    }

    private static int parseToInt(Properties props, String propertyName) {
        String property = props.getProperty(propertyName);
        if (checkForNull(property, propertyName)) {
            return 0;
        }
        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException exp) {
            log.info("Value parsing error of '" + propertyName + "': value '" + property + "' is incorrect");
            return 0;
        }
    }

    private static String parseToString(Properties props, String propertyName) {
        String property = props.getProperty(propertyName);
        if (checkForNull(property, propertyName)) {
            return "";
        }
        return property;
    }

    private static boolean checkForNull(String property, String propertyName) {
        if (property == null) {
            log.error("Value parsing error of '" + propertyName + "': property is missing from " +
                    "the properties file");
            return true;
        }
        return false;
    }
}