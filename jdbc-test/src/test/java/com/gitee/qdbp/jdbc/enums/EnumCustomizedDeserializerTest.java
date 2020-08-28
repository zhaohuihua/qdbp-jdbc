package com.gitee.qdbp.jdbc.enums;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.testng.Assert;
import org.testng.Assert.ThrowingRunnable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;

// 这个用例目前通不过, fastjson的TypeUtils.castToEnum存在逻辑错误
/**
 * 针对特殊枚举定制的反序列化处理类测试
 *
 * @author zhaohuihua
 * @version 20200728
 */
@Test
public class EnumCustomizedDeserializerTest {

    @BeforeClass
    protected void init() {
        // 给UserState注册定制的反序列化处理类
        ParserConfig.getGlobalInstance().putDeserializer(UserState.class, new CodeEnumDeserializer());
    }

    @Test(priority = 101, enabled = false)
    public void testConvert() {
        castToEnumTest("NORMAL", UserState.NORMAL);
        castToEnumTest("1", UserState.NORMAL);
        castToEnumTest(1, UserState.NORMAL);
        castToEnumTest("LOCKED", UserState.LOCKED);
        castToEnumTest("2", UserState.LOCKED);
        castToEnumTest(2, UserState.LOCKED);
        castToEnumTest("UNACTIVATED", UserState.UNACTIVATED);
        castToEnumTest("3", UserState.UNACTIVATED);
        castToEnumTest(3, UserState.UNACTIVATED);
        castToEnumTest("LOGOFF", UserState.LOGOFF);
        castToEnumTest("9", UserState.LOGOFF);
        castToEnumTest(9, UserState.LOGOFF);
    }
    
    private void castToEnumTest(Object source, UserState target) {
        String msg = "cast " + source + " to UserState";
        Assert.assertEquals(TypeUtils.castToEnum(source, UserState.class, null), target, msg);
    }

    @Test(priority = 102, enabled = false)
    public void testException() {
        Assert.assertThrows(JSONException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                TypeUtils.castToEnum(0, UserState.class, null);
            }
        });

        Assert.assertThrows(JSONException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                TypeUtils.castToEnum("AAA", UserState.class, null);
            }
        });
    }

    /** 自定义编号的枚举类 **/
    protected static interface CodeEnum {
        int code();
    }

    /** 用户状态 **/
    protected static enum UserState implements CodeEnum {

        /** 1.正常 **/
        NORMAL(1),
        /** 2.锁定 **/
        LOCKED(2),
        /** 3.待激活 **/
        UNACTIVATED(3),
        /** 9.已注销 **/
        LOGOFF(9);

        private int code;

        UserState(int code) {
            this.code = code;
        }

        public int code() {
            return this.code;
        }
    }

    /** 返回自定义编号的专用反序列化处理类(数字根据code转换) **/
    protected static class CodeEnumDeserializer implements ObjectDeserializer {

        @SuppressWarnings("unchecked")
        public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            JSONLexer lexer = parser.lexer;
            if (lexer.token() == JSONToken.NULL) {
                lexer.nextToken(JSONToken.COMMA);
                return null;
            }

            Class<?> targetClass = (Class<?>) TypeUtils.getRawClass(type);
            // CodeEnumDeserializer只能注册给既是枚举又实现了CodeEnum接口的类
            // ParserConfig.getGlobalInstance().putDeserializer(UserState.class, new CodeEnumDeserializer());
            if (!CodeEnum.class.isAssignableFrom(targetClass)) {
                throw new JSONException("CodeEnumDeserializer do not support " + targetClass.getName());
            }
            if (!Enum.class.isAssignableFrom(targetClass)) {
                throw new JSONException("CodeEnumDeserializer do not support " + targetClass.getName());
            }
            Enum<?>[] ordinalEnums = (Enum[]) targetClass.getEnumConstants();
            if (lexer.token() == JSONToken.LITERAL_INT) {
                BigDecimal number = lexer.decimalValue();
                lexer.nextToken(JSONToken.COMMA);
                return (T) convertFromNumber(number, ordinalEnums, targetClass);
            } else if (lexer.token() == JSONToken.LITERAL_FLOAT) {
                BigDecimal number = lexer.decimalValue();
                lexer.nextToken(JSONToken.COMMA);
                return (T) convertFromNumber(number, ordinalEnums, targetClass);
            } else if (lexer.token() == JSONToken.LITERAL_STRING) {
                String string = lexer.stringVal();
                if (isDigitString(string)) {
                    BigDecimal number = new BigDecimal(string);
                    return (T) convertFromNumber(number, ordinalEnums, targetClass);
                } else {
                    return (T) convertFromString(string, ordinalEnums, targetClass);
                }
            }

            Object value = parser.parse();
            String msg = value + " can not cast to : " + targetClass.getName();
            throw new JSONException(msg);
        }

        private Enum<?> convertFromNumber(BigDecimal number, Enum<?>[] enums, Class<?> type) {
            // 整数部分
            BigInteger integer = number.toBigInteger();
            // 是不是整数
            boolean isIntegral = number.compareTo(new BigDecimal(integer)) == 0;
            if (!isIntegral) { // 不是整数
                String msg = "can not cast " + number.toPlainString() + " to : " + type.getName();
                throw new JSONException(msg);
            }
            BigInteger minInteger = BigInteger.valueOf(Integer.MIN_VALUE);
            BigInteger maxInteger = BigInteger.valueOf(Integer.MAX_VALUE);
            if (integer.compareTo(minInteger) < 0 || integer.compareTo(maxInteger) > 0) {
                // 不在Integer范围内
                throw new JSONException("can not cast " + integer + "to : " + type.getName());
            }
            // 根据code转换
            int targetValue = number.intValue();
            for (Enum<?> item : enums) {
                if (((CodeEnum) item).code() == targetValue) {
                    return item;
                }
            }
            throw new JSONException("can not cast " + integer + "to : " + type.getName());
        }

        private Enum<?> convertFromString(String string, Enum<?>[] enums, Class<?> type) {
            if (string.length() == 0) {
                return null;
            }
            // 根据name转换
            for (Enum<?> item : enums) {
                if (item.name().equals(string)) {
                    return item;
                }
            }
            throw new JSONException("can not cast " + string + "to : " + type.getName());
        }

        private boolean isDigitString(String string) {
            if (string == null || string.length() == 0) {
                return false;
            }
            for (int i = 0, z = string.length(); i < z; i++) {
                char c = string.charAt(i);
                if (c < '0' || c > '9') {
                    return false;
                }
            }
            return true;
        }

        public int getFastMatchToken() {
            return JSONToken.UNDEFINED; // name or ordinal
        }

    }
}
