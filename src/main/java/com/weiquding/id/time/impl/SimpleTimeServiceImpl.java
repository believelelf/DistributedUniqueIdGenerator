package com.weiquding.id.time.impl;

import com.weiquding.id.time.TimeService;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 基于本机时间的简单时间服务
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/6
 */
public class SimpleTimeServiceImpl implements TimeService {

    /**
     * 远程时间
     */
    private long remoteTimestamp;
    /**
     * 本机时间
     */
    private long localTimestamp;
    /**
     * 同步周期
     */
    private int sychronizeInterval = 60000;
    /**
     * 一周开始
     */
    private int firstDayOfWeek = Calendar.MONDAY;

    /**
     * the time zone
     */
    private TimeZone timeZone;
    /**
     * the locale for the week data
     */
    private Locale locale;

    public void setSychronizeInterval(int sychronizeInterval) {
        this.sychronizeInterval = sychronizeInterval;
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * 获取本地时间
     *
     * @return 本地时间
     */
    protected long localTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取远程时间
     *
     * @return 远程时间
     */
    protected long remoteTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 重置时间
     */
    public final void reset() {
        this.localTimestamp = localTimestamp();
        this.remoteTimestamp = remoteTimestamp();
    }

    /**
     * 距上次时间同步周期已过去多少时间
     *
     * @return 时间周期
     */
    private long eclapse() {
        return System.currentTimeMillis() - this.localTimestamp;
    }

    /**
     * 根据Locale及Timezone获取Calendar
     *
     * @return Calendar
     */
    private Calendar getCalendar() {
        Calendar calendar = null;
        if (timeZone != null && locale != null) {
            /*
             * Gets a calendar with the specified time zone and locale.
             * The <code>Calendar</code> returned is based on the current time
             * in the given time zone with the given locale.
             */
            calendar = Calendar.getInstance(timeZone, locale);
        } else if (timeZone != null) {
            calendar = Calendar.getInstance(timeZone);
        } else if (locale != null) {
            calendar = Calendar.getInstance(locale);
        } else {
            calendar = Calendar.getInstance();
        }
        calendar.setFirstDayOfWeek(firstDayOfWeek);
        return calendar;
    }

    /**
     * 根据timestamp获取field的值
     *
     * @param field     the given calendar field.
     * @param timestamp 时间
     * @return value
     */
    private int getUint(int field, long timestamp) {
        Calendar calendar = getCalendar();
        calendar.setTimeInMillis(timestamp);
        return calendar.get(field);
    }


    @Override
    public boolean isCutoff(int cutoffType, long nowTimestamp, long lastTimestamp) {
        if (!supportCutoffType(cutoffType)) {
            throw new IllegalArgumentException("Unsupported cutoffType: " + cutoffType);
        }
        // 先判断年
        if (getUint(YEAR, nowTimestamp) - getUint(YEAR, lastTimestamp) > 0) {
            return true;
        }
        // 同一年再判断当前cutoffType[YEAR,MONTH,WEEK_OF_YEAR,DAY_OF_MONTH]
        if (getUint(YEAR, nowTimestamp) - getUint(YEAR, lastTimestamp) == 0) {
            if (cutoffType == YEAR) {
                return false;
            }
            // 判断月[MONTH]
            if (getUint(MONTH, nowTimestamp) - getUint(MONTH, localTimestamp) > 0) {
                return true;
            }
            // 同一月再判断当前cutoffType[WEEK_OF_YEAR,DAY_OF_MONTH]
            if (getUint(MONTH, nowTimestamp) - getUint(MONTH, localTimestamp) == 0) {
                return getUint(cutoffType, nowTimestamp) - getUint(cutoffType, localTimestamp) > 0;
            }
        }
        return false;
    }

    @Override
    public boolean isCutoff(int cutoffType, long lastTimestamp) {
        return isCutoff(cutoffType, System.currentTimeMillis(), lastTimestamp);
    }

    @Override
    public boolean supportCutoffType(int cutoffType) {
        return cutoffType == DAY
                || cutoffType == MONTH
                || cutoffType == WEEK
                || cutoffType == YEAR;
    }

    @Override
    public synchronized long currentTimeMillis() {
        long interval = eclapse();
        if (interval > sychronizeInterval) {
            reset();
        }
        return remoteTimestamp + eclapse();
    }
}
