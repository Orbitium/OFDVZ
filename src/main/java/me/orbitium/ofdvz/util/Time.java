package me.orbitium.ofdvz.util;

import java.time.LocalDateTime;

public class Time {

    public static int dateToInt() {
        int v = 0;
        LocalDateTime localDateTime = LocalDateTime.now();

        v += localDateTime.getDayOfMonth() * 86400;
        v += localDateTime.getHour() * 3600;
        v += localDateTime.getMinute() * 60;
        v += localDateTime.getSecond();

        return v;
    }


}
