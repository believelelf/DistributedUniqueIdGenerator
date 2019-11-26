package com.weiquding.id.uuid;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;

/**
 * 基于{@link UUID#randomUUID()} 生成一个base62编码的ID.
 * 原生成的ID的字符数为36，使用base62编码后可以缩小为最大22字符，减少38.89%空间。
 *
 * @author wuby
 * @version V1.0
 * @date 2019/11/26
 * @see java.util.UUID
 */
public class UUID62 {


    /**
     * 产生version 4的UUID,再进行base62
     *
     * @return base62 uuid
     */
    public static String randomUUID62() {
        return toUUID62(UUID.randomUUID());
    }


    /**
     * 从UUID转换为UUID62
     *
     * @param uuid UUID
     * @return UUID62 string
     */
    public static String toUUID62(UUID uuid) {
        return Base62.encode(UUIDConverter.toBigInteger(uuid));
    }

    /**
     * 将uuid62转UUId
     *
     * @param uuid62 UUID62 string
     * @return UUID
     */
    public static UUID toUUID(String uuid62) {
        return UUIDConverter.toUUID(Base62.decode(uuid62));
    }


    /**
     * UUID与BigInteger转换类
     */
    static class UUIDConverter {

        /**
         * 1 << 64 --> 2^64
         */
        private static final BigInteger HALF = BigInteger.ONE.shiftLeft(64);
        /**
         * long的最大值
         */
        private static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

        /**
         * signed --> unsigned
         */
        private static final Function<BigInteger, BigInteger> toUnsigned = value -> value.signum() < 0 ? value.add(HALF) : value;

        /**
         * unsigned --> signed
         */
        private static final Function<BigInteger, BigInteger> toSigned = value -> value.compareTo(MAX_LONG) > 0 ? value.subtract(HALF) : value;


        /**
         * 将UUID转换为BigInteger
         * 两个64位long转化为128位BigInteger
         *
         * @param uuid UUID
         * @return BigInteger
         */
        static BigInteger toBigInteger(UUID uuid) {
            BigInteger most = BigInteger.valueOf(uuid.getMostSignificantBits());
            BigInteger least = BigInteger.valueOf(uuid.getLeastSignificantBits());
            return (toUnsigned.apply(most).multiply(HALF)).add(toUnsigned.apply(least));
        }

        /**
         * 将BigInteger转换为UUID
         * 128位BigInteger转化为两个64位long
         *
         * @param value BigInteger
         * @return UUID
         */
        static UUID toUUID(BigInteger value) {
            BigInteger[] divideAndRemainder = value.divideAndRemainder(HALF);
            return new UUID(toSigned.apply(divideAndRemainder[0]).longValueExact(), toSigned.apply(divideAndRemainder[1]).longValueExact());
        }

    }

    /**
     * Base62编解码类
     */
    static class Base62 {

        private static final BigInteger BASE62 = BigInteger.valueOf(62);

        /**
         * Base62 编码字符表
         */
        private static final char[] TO_BASE62 = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
        };

        /**
         * Base62 解码字符映射表
         */
        private static final int[] FROM_BASE62 = new int[256];

        static {
            Arrays.fill(FROM_BASE62, -1);
            for (int i = 0; i < TO_BASE62.length; i++) {
                FROM_BASE62[TO_BASE62[i]] = i;
            }
        }

        /**
         * base62编码
         *
         * @param number 数值
         * @return 编码后字符串
         */
        static String encode(BigInteger number) {
            if (number == null
                    || number.compareTo(BigInteger.ZERO) < 0
            ) {
                throw new IllegalArgumentException("The number cannot be negative");
            }
            StringBuilder builder = new StringBuilder();
            while (number.compareTo(BigInteger.ZERO) > 0) {
                /*
                 * 除余运算
                 * an array of two BigIntegers: the quotient {@code (this / val)}
                 * is the initial element, and the remainder {@code (this % val)}
                 * is the final element.
                 */
                BigInteger[] divideAndRemainder = number.divideAndRemainder(BASE62);
                builder.insert(0, TO_BASE62[divideAndRemainder[1].intValue()]);
                number = divideAndRemainder[0];
            }
            return builder.toString();
        }

        /**
         * base62解码
         *
         * @param str 待解码字符串
         * @return 解码后的值
         */
        static BigInteger decode(String str) {
            if (str == null
                    || str.isEmpty()) {
                throw new IllegalArgumentException("str cannot be empty");
            }
            BigInteger result = BigInteger.valueOf(0);
            for (int i = 0, length = str.length(); i < length; i++) {
                int value = FROM_BASE62[str.charAt(length - i - 1)];
                result = result.add(BigInteger.valueOf(value).multiply(BASE62.pow(i)));
            }
            return result;
        }
    }


}
