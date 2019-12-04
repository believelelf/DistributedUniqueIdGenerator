package com.weiquding.id.segment.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ID格式化类
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/4
 */
public class IdFormat {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdFormat.class);
    private String pattern;
    private String datePattern;


    public static IdFormat getInstance(String pattern) {
        return new IdFormat(pattern);
    }

    private IdFormat(String pattern){
        this.pattern = pattern;
    }


}
