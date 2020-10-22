package com.example.tools.util;

import java.lang.reflect.Field;

/**
 * Generate a POJO from a string. The field is separated by specifed breaker in the string.
 * The order of declared fields should be same as the order in the string.
 *
 * @author wanglihao
 */
public class LogDataFactory {

    public static <T> T extract(String line, Class<T> clazz) throws Exception {
        return extract(line, clazz, "\t");
    }

    public static <T> T extract(String line, Class<T> clazz, String filedBreak) throws Exception {
        T object = clazz.getConstructor().newInstance();
        Field[] fields = clazz.getDeclaredFields();
        String[] columns = line.split(filedBreak);
        assert (fields.length == columns.length);
        for (int i = 0; i < fields.length; i++) {
            Class fieldClass = fields[i].getType();
            fields[i].setAccessible(true);
            Object value = null;
            if (fieldClass == String.class) {
                value = columns[i];
            } else if (fieldClass == Integer.class) {
                try {
                    value = Integer.valueOf(columns[i]);
                } catch (NumberFormatException e) {
                    value = 0;
                }
            } else if (fieldClass == Long.class) {
                try {
                    value = Long.valueOf(columns[i]);
                } catch (NumberFormatException e) {
                    value = 0L;
                }
            }
            fields[i].set(object, value);
        }
        return object;
    }
}
