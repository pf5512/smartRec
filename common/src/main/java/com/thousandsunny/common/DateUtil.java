package com.thousandsunny.common;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static java.sql.Date.valueOf;
import static java.time.LocalDate.now;
import static java.time.LocalDateTime.ofInstant;
import static java.time.Period.between;
import static java.time.ZoneId.systemDefault;
import static java.util.Calendar.*;
import static org.apache.commons.lang3.time.DateUtils.*;
import static org.springframework.util.StringUtils.isEmpty;

public class DateUtil {

    public static final FastDateFormat ISO_DATETIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    public static Date truncatedDate(Date... dates) {
        if (isEmpty(dates))
            return valueOf(now());
        else
            return valueOf(ofInstant(dates[0].toInstant(), systemDefault()).toLocalDate());
    }

    /**
     * 同年
     */
    public static Boolean isSameYear(Date date1, Date date2) {
        return Objects.equals(date2LocalDateTime(date1).getYear(), date2LocalDateTime(date2).getYear());
    }

    /**
     * 同月
     */
    public static Boolean isSameMonth(Date date1, Date date2) {
        return Objects.equals(date2LocalDateTime(date1).getMonthValue(), date2LocalDateTime(date2).getMonthValue());
    }

    /**
     * 同月
     */
    public static Boolean isSameMonthFully(Date date1, Date date2) {
        return isSameYear(date1, date2) && Objects.equals(date2LocalDateTime(date1).getMonthValue(), date2LocalDateTime(date2).getMonthValue());
    }

    /**
     * 一年同天
     */
    public static Boolean isSameDayOfYear(Date date1, Date date2) {
        return Objects.equals(date2LocalDateTime(date1).getDayOfYear(), date2LocalDateTime(date2).getDayOfYear());
    }

    /**
     * 一年同天
     */
    public static Boolean isSameDayOfYearFully(Date date1, Date date2) {
        return isSameMonthFully(date1, date2) && Objects.equals(date2LocalDateTime(date1).getDayOfYear(), date2LocalDateTime(date2).getDayOfYear());
    }

    /**
     * 一月同天
     */
    public static Boolean isSameDayOfMonth(Date date1, Date date2) {
        return Objects.equals(date2LocalDateTime(date1).getDayOfMonth(), date2LocalDateTime(date2).getDayOfMonth());
    }

    /**
     * 一周同天
     */
    public static Boolean isSameDayOfWeek(Date date1, Date date2) {
        return Objects.equals(date2LocalDateTime(date1).getDayOfWeek().getValue(), date2LocalDateTime(date2).getDayOfWeek().getValue());
    }

    /**
     * 是否在前
     */
    public static Boolean isBefore(Date date1, Date date2) {
        return date2LocalDateTime(date1).isBefore(date2LocalDateTime(date2));
    }

    /**
     * 是否在后
     */
    public static Boolean isAfter(Date date1, Date date2) {
        return date2LocalDateTime(date1).isAfter(date2LocalDateTime(date2));
    }

    /**
     *
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        return ofInstant(date.toInstant(), systemDefault());
    }

    public static LocalDate date2LocalDate(Date date) {
        return ofInstant(date.toInstant(), systemDefault()).toLocalDate();
    }

    /**
     * 年间距
     */
    public static Integer yearGap(Date start, Date end) {
        return between(date2LocalDate(start), date2LocalDate(end)).getYears();
    }

    /**
     * 月间距
     */
    public static Integer monthGap(Date start, Date end) {
        return between(date2LocalDate(start), date2LocalDate(end)).getMonths();
    }

    /**
     * 日间距
     */
    public static Integer dayGap(Date start, Date end) {
        return between(date2LocalDate(start), date2LocalDate(end)).getDays();
    }

    /**
     * 减去年份
     */
    public static Date subtractYear(Date start, Integer years) {
        return addYears(start, -years);
    }

    /**
     * 减去月
     */
    public static Date subtractMonth(Date start, Integer months) {
        return addMonths(start, -months);
    }

    /**
     * 减去天
     */
    public static Date subtractDay(Date start, Integer days) {
        return addDays(start, -days);
    }

    public static Date getDate(int year, int month, int day, int hrs, int min, int sec) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, hrs, min, sec);
        return cal.getTime();
    }

    /**
     * 一个月有多少天
     */
    public static Integer dayOfMonthNum(Integer year, Integer month) {
        return LocalDate.now().withYear(year).withMonth(month).lengthOfMonth();
    }

    @Test
    public void testDateTruncate() {
        System.out.println(DateUtils.truncate(new Date(), YEAR));
        System.out.println(DateUtils.truncate(new Date(), MONTH));
        System.out.println(DateUtils.truncate(new Date(), DAY_OF_MONTH));
    }
}
