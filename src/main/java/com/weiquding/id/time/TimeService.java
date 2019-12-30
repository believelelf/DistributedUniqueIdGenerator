package com.weiquding.id.time;

import java.util.Calendar;

/**
 * 时间服务
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/6
 */
public interface TimeService {

    int FIRST_DAY_OF_WEEK = Calendar.MONDAY;
    /**
     * 支持的切换类型
     */
    int DAY = Calendar.DAY_OF_MONTH;
    int WEEK = Calendar.WEEK_OF_YEAR;
    int MONTH = Calendar.MONTH;
    int YEAR = Calendar.YEAR;

    /**
     * 是否进行切换
     * @param cutoffType 切换类型
     * @param nowTimestamp 当前时间
     * @param lastTimestamp 上次取ID的时间
     * @return 是否需要进行切换
     */
    boolean isCutoff(int cutoffType, long nowTimestamp, long lastTimestamp);

    /**
     *  是否进行切换
     *
     * @param cutoffType 切换类型
     * @param lastTimestamp 上次取ID的时间
     * @return 是否需要进行切换
     */
    boolean isCutoff(int cutoffType, long lastTimestamp);

    /**
     * 是否支持当前切换
     * @param cutoffType 切换类型
     * @return 是否支持
     */
    boolean supportCutoffType(int cutoffType);

    /**
     * 获取当前时间
     * @return 当前时间
     */
    long currentTimeMillis();

}
