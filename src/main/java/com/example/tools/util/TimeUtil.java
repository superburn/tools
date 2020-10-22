package com.example.tools.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
public class TimeUtil {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static List<Date> getTimeListStep(String startTime, String endTime, int step) {
        int timeInterval = step * 60 * 1000;

        Date startDate = null;
        Date endDate = null;
        try {
            startDate = sdf.parse(startTime);
            endDate = sdf.parse(endTime);
        } catch (ParseException pe) {
            log.error("TimeUtil getTimeListStep error,startTime:{},endTime:{},step:{}", startTime, endTime, step, pe);
        }

        List<Date> list = new ArrayList<>();

        if (startDate != null && endDate != null) {
            Calendar begin = (new Calendar.Builder()).setInstant(startDate.getTime()
                        / timeInterval * timeInterval).build();
            Calendar end = (new Calendar.Builder()).setInstant(endDate.getTime()
                        / timeInterval * timeInterval).build();

            while (true) {
                list.add(begin.getTime());
                if (begin.equals(end)) {
                    break;
                }
                begin.add(Calendar.MINUTE, step);
            }
        }
        return list;
    }

    public static Date addDate(Date date, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, amount);
        return calendar.getTime();
    }
}
