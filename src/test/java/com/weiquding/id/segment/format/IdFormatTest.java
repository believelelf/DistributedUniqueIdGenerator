package com.weiquding.id.segment.format;

import com.weiquding.id.segment.IdGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * IdFormat testing
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/6
 */
public class IdFormatTest {

    @Test
    public void testFormat() throws ParseException {
        //xxx{$code}{$using}yyy{yyyyMMddHHmmss}zzz{##############}
        Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse("20180807060504");
        Assert.assertEquals("xxxcodetrueyyy20180807060504zzz00000000000211", IdFormat.getInstance("xxx{$code}{$using}yyy{yyyyMMddHHmmss}zzz{##############}").format(new TestIdGenerator(), 211, date));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidPattern() throws ParseException {
        Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse("20180807060504");
        Assert.assertEquals("xxxcodetrueyyy20180807060504zzz00000000000211", IdFormat.getInstance("xxx{{$code}{$using}yyy{yyyyMMddHHmmss}zzz{##############}").format(new TestIdGenerator(), 211, date));    }

    /**
     * 测试IdGenerator
     */
    class TestIdGenerator implements IdGenerator<String> {

        private String code = "code";
        private boolean using = true;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public boolean isUsing() {
            return using;
        }

        public void setUsing(boolean using) {
            this.using = using;
        }

        @Override
        public String generate() {
            return null;
        }

        @Override
        public List<String> generate(int size) {
            return null;
        }
    }
}
