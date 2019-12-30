package com.weiquding.id.segment.format;

import com.weiquding.id.segment.IdGenerator;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ID格式化类
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/4
 */
public class IdFormat {

    protected String pattern;
    protected String datePattern;
    protected List<Format> formats;


    /**
     * 获取IdFormat实例
     *
     * @param pattern 模式
     * @return IdFormat
     */
    public static IdFormat getInstance(String pattern) {
        return new IdFormat(pattern);
    }

    /**
     * 私有构造
     *
     * @param pattern 模式
     */
    private IdFormat(String pattern) {
        this.pattern = pattern;
        compile();
    }

    /**
     * 格式化ID
     *
     * @param idGenerator ID生成器
     * @param id          所生成的ID
     * @param date        生成时间
     * @return 格式化ID
     */
    public String format(IdGenerator<?> idGenerator, long id, Date date) {
        StringBuilder builder = new StringBuilder(32);
        for (Format format : formats) {
            if (format instanceof FieldFormat) {
                format.format(idGenerator, builder);
            } else if (format instanceof NumberFormat) {
                format.format(id, builder);
            } else if (format instanceof DateTimeFormat) {
                format.format(date, builder);
            } else if (format instanceof StringFormat) {
                format.format(null, builder);
            }
        }
        return builder.toString();
    }

    /**
     * 编译pattern
     * 示例xxx{$code}yyy{yyyyMMddHHmmss}zzz{##############}
     */
    protected void compile() {
        Deque<Character> stack = new ArrayDeque<>(1);
        StringBuilder builder = new StringBuilder();
        List<Format> formatList = new ArrayList<>(3);
        for (int i = 0, length = pattern.length(); i < length; i++) {
            char c = pattern.charAt(i);
            if ('{' == c) {
                if (!stack.isEmpty()) {
                    throw new IllegalArgumentException("invalid pattern: " + pattern);
                }
                stack.push(c);
                if (builder.length() > 0) {
                    formatList.add(new StringFormat(builder.toString()));
                    builder.setLength(0);
                }
            } else if ('}' == c) {
                if (stack.size() != 1 || stack.pop() != '{' ) {
                    throw new IllegalArgumentException("invalid pattern: " + pattern);
                }
                if (builder.length() > 0) {
                    formatList.add(parse(builder.toString()));
                    builder.setLength(0);
                }
            } else {
                builder.append(c);
            }
        }

        if (!stack.isEmpty()) {
            throw new IllegalArgumentException("invalid pattern: " + pattern);
        }
        if (builder.length() > 0) {
            formatList.add(new StringFormat(builder.toString()));
        }
        this.formats = formatList;
    }

    /**
     * 转化Format
     *
     * @param str 字符串
     * @return Format
     */
    protected Format parse(String str) {
        char c = str.charAt(0);
        Format format;
        switch (c) {
            case '#':
                format = new NumberFormat(str.length());
                break;
            case '$':
                format = new FieldFormat(str.substring(1));
                break;
            default:
                this.datePattern = str;
                format = new DateTimeFormat(str);
        }
        return format;
    }

    public String getPattern() {
        return this.pattern;
    }

    public String getDatePattern() {
        return this.datePattern;
    }


    /**
     * 格式化接口
     */
    interface Format {

        /**
         * 格式化data
         *
         * @param data    参数
         * @param builder 格式化后字符串容器
         */
        void format(Object data, StringBuilder builder);
    }


    /**
     * 字符串格式化
     */
    class StringFormat implements Format {

        private final String value;

        StringFormat(String value) {
            this.value = value;
        }

        @Override
        public void format(Object data, StringBuilder builder) {
            if (value == null) {
                throw new IllegalArgumentException("Parameter value must not be null.");
            }
            builder.append(value);
        }
    }

    /**
     * 数字格式化
     */
    class NumberFormat implements Format {

        private final int length;
        private final long seed;

        NumberFormat(int length) {
            this.length = length;
            this.seed = (long) Math.pow(10.0D, length);

        }

        @Override
        public void format(Object data, StringBuilder builder) {
            if (!(data instanceof Number)) {
                throw new IllegalArgumentException("data must be a number: " + data);
            }
            String str = Long.toString(this.seed + ((Number) data).longValue());
            builder.append(str.substring(str.length() - this.length));
        }
    }


    /**
     * 时间格式化
     */
    class DateTimeFormat implements Format {

        private final ThreadLocal<DateFormat> dateFormat;

        DateTimeFormat(final String format) {
            this.dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat(format));
        }

        @Override
        public void format(Object data, StringBuilder builder) {
            if (!(data instanceof Date)) {
                throw new IllegalArgumentException("data must be a date: " + data);
            }
            builder.append(dateFormat.get().format(data));
        }
    }

    /**
     * 字段格式化
     */
    class FieldFormat implements Format {

        private final String fieldName;

        FieldFormat(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public void format(Object data, StringBuilder builder) {
            if (!(data instanceof IdGenerator)) {
                throw new IllegalArgumentException("data must be a instance of IdGenerator: " + data);
            }
            try {
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(fieldName, data.getClass());
                Method readMethod = propertyDescriptor.getReadMethod();
                Object result = readMethod.invoke(data);
                if (result == null) {
                    throw new IllegalArgumentException("field '" + this.fieldName +
                            "'s value is null: " + data);
                }
                builder.append(result);
            } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("cannot get field '" + this.fieldName +
                        "'s value: " + data);
            }
        }
    }


}
