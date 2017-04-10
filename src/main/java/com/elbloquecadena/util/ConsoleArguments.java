package com.elbloquecadena.util;

import java.util.HashMap;
import java.util.Map;

public class ConsoleArguments {

    private final Map<String, String> map = new HashMap<>();

    public ConsoleArguments(String[] args) {
        if (args != null && args.length > 0)
            parseArguments(args);
    }

    private void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String current = args[i];
            String next = null;
            if (i + 1 < args.length) {
                String nn = args[i + 1];
                if (!nn.startsWith("-")) {
                    next = nn;
                    i++;
                }
            } else {
                next = "true";
            }
            map.put(current, next);
        }
    }

    public boolean contains(String key) {
        return map.containsKey(key);
    }

    public String getString(String key, String defaultValue) {
        String val = map.get(key);
        if (val != null) {
            return val;
        } else {
            return defaultValue;
        }
    }

    public int getInteger(String key, int defaultValue) {
        String val = map.get(key);
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (Exception e) {
            }
        }
        return defaultValue;
    }

    @Override
    public String toString() {
        return map.toString();
    }

}