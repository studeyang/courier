package com.github.open.courier.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 日期工具类
 *
 * @author Courier
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String yyyyMMdd = "yyyyMMdd";

    public static final String yyyyMMdd_HHmmss = "yyyyMMdd HH:mm:ss";

    private static final String TIME_REGEX = "([0-1][0-9]|2[0-3]):[0-5]\\d:[0-5]\\d";


    private static final Map<String, DateTimeFormatter> dateTimeFormatterMap = new ConcurrentHashMap<>();


    public static DateTimeFormatter getDateTimeFormatter(String format) {
        return dateTimeFormatterMap.computeIfAbsent(format, DateTimeFormatter::ofPattern);
    }


    public static String formatDate(LocalDateTime sourceDate) {
        return formatDate(sourceDate, DEFAULT_FORMAT);
    }


    public static String formatDate(LocalDateTime sourceDate, String targetFormat) {
        return getDateTimeFormatter(targetFormat).format(sourceDate);
    }


    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }


    //-----------------------------以下是非java8时间工具类的实现---------------------------------------------

    public static String formatDate(Date date) {
        return formatDate(date, DEFAULT_FORMAT);
    }


    public static String formatDate(Date date, String format) {

        SimpleDateFormat sdf = new SimpleDateFormat(format);

        if (date == null) {
            return StringUtils.EMPTY;
        }

        return sdf.format(date);
    }


    @SneakyThrows
    public static Date parseDate(Date date, String time) {

        if (!Pattern.matches(TIME_REGEX, time)) {
            throw new IllegalArgumentException("incorrect time: " + time);
        }

        String formatedDate = formatDate(date, yyyyMMdd) + " " + time;

        return org.apache.commons.lang3.time.DateUtils.parseDate(formatedDate, yyyyMMdd_HHmmss);
    }

    public static Date parse(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_FORMAT);
        if (date == null || date.isEmpty()) {
            return null;
        }
        return sdf.parse(date);
    }


    public static Date getStartOfBeforeDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }


    public static Date getStartOfTodayDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }


    public static Date getEndOfTodayDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }


    public static List<String> getDatesByDatePeriod(Date startTime, Date endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMdd);
        List<String> result = new ArrayList<>();
        result.add(sdf.format(startTime));
        Calendar calBegin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calBegin.setTime(startTime);
        Calendar calEnd = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calEnd.setTime(endTime);
        // 测试此日期是否在指定日期之后 并且不是同一天
        while (endTime.after(calBegin.getTime()) && !org.apache.commons.lang3.time.DateUtils.isSameDay(calBegin.getTime(), endTime)) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            result.add(sdf.format(calBegin.getTime()));
        }
        return result;

    }

    /**
     * 获取前几天的 Date 对象
     *
     * @param days 天数
     * @return 日期对象
     */
    public static Date getDateBefore(int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -days);
        return c.getTime();
    }

}




























