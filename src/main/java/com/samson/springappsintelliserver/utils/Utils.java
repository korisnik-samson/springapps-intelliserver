package com.samson.springappsintelliserver.utils;

public class Utils {
    public static int calculateTime(boolean isDay, int t) {
        if (!isDay) return 1000 * 60 * 60 * t;

        else return 1000 * 60 * 60 * 24 * t;
    }
}

