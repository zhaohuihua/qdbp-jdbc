package com.gitee.qdbp.jdbc.support;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;

/**
 * 调用Spring的转换服务体系进行反序列化的处理类(不支持数组)
 *
 * @author zhaohuihua
 * @version 20200726
 */
public class UseSpringConversionDeserializer implements ObjectDeserializer, ConversionServiceAware {

    private ConversionService conversionService;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == JSONToken.NULL) {
            lexer.nextToken(JSONToken.COMMA);
            return null;
        }

        Class<T> targetClass = (Class<T>) TypeUtils.getRawClass(type);
        if (lexer.token() == JSONToken.LITERAL_INT) {
            BigDecimal number = lexer.decimalValue();
            lexer.nextToken(JSONToken.COMMA);
            return convertNumber(number, targetClass);
        } else if (lexer.token() == JSONToken.LITERAL_FLOAT) {
            BigDecimal number = lexer.decimalValue();
            lexer.nextToken(JSONToken.COMMA);
            return convertNumber(number, targetClass);
        } else if (lexer.token() == JSONToken.TRUE) {
            lexer.nextToken(JSONToken.COMMA);
            return convertBoolean(Boolean.TRUE, targetClass);
        } else if (lexer.token() == JSONToken.FALSE) {
            lexer.nextToken(JSONToken.COMMA);
            return convertBoolean(Boolean.FALSE, targetClass);
        } else if (lexer.token() == JSONToken.LITERAL_STRING) {
            String string = lexer.stringVal();
            return convertString(string, targetClass);
        }

        Object value = parser.parse();
        if (value == null) {
            return null;
        }

        if (value instanceof JSONObject) {
            JSONObject json = (JSONObject) value;
            return TypeUtils.castToJavaBean(json, targetClass, ParserConfig.getGlobalInstance());
        }

        TypeDescriptor sourceType = TypeDescriptor.forObject(value);
        TypeDescriptor targetType = TypeDescriptor.valueOf(targetClass);
        if (conversionService.canConvert(sourceType, targetType)) {
            return conversionService.convert(value, targetClass);
        } else {
            throw new ConversionFailedException(sourceType, targetType, value, null);
        }
    }

    private <T> T convertNumber(BigDecimal number, Class<T> targetClass) {
        // 整数部分
        BigInteger integer = number.toBigInteger();
        // 是不是整数
        boolean isIntegral = number.compareTo(new BigDecimal(integer)) == 0;

        // 依次判断spring能转换哪些数字或字符串
        if (conversionService.canConvert(BigDecimal.class, targetClass)) {
            return conversionService.convert(number, targetClass);
        } else if (isIntegral && conversionService.canConvert(BigInteger.class, targetClass)) {
            return conversionService.convert(integer, targetClass);
        } else if (conversionService.canConvert(Double.class, targetClass)) {
            return conversionService.convert(new Double(number.doubleValue()), targetClass);
        } else if (conversionService.canConvert(Float.class, targetClass)) {
            return conversionService.convert(new Float(number.doubleValue()), targetClass);
        } else if (isIntegral && conversionService.canConvert(Long.class, targetClass)) {
            return conversionService.convert(new Long(TypeUtils.longValue(number)), targetClass);
        } else if (isIntegral && conversionService.canConvert(Integer.class, targetClass)) {
            return conversionService.convert(new Integer(TypeUtils.intValue(number)), targetClass);
        } else if (isIntegral && conversionService.canConvert(Short.class, targetClass)) {
            return conversionService.convert(new Short(TypeUtils.shortValue(number)), targetClass);
        } else if (conversionService.canConvert(String.class, targetClass)) {
            String string = isIntegral ? integer.toString() : number.toPlainString();
            return conversionService.convert(string, targetClass);
        } else {
            Class<?> sourceClass = isIntegral ? getBigIntegerRealType(integer) : getBigDecimalRealType(number);
            String sourceValue = isIntegral ? integer.toString() : number.toPlainString();
            TypeDescriptor sourceType = TypeDescriptor.valueOf(sourceClass);
            TypeDescriptor targetType = TypeDescriptor.valueOf(targetClass);
            throw new ConversionFailedException(sourceType, targetType, sourceValue, null);
        }
    }

    private <T> T convertBoolean(Boolean value, Class<T> targetClass) {
        if (conversionService.canConvert(Boolean.class, targetClass)) {
            return conversionService.convert(value, targetClass);
        } else if (conversionService.canConvert(String.class, targetClass)) {
            return conversionService.convert(value.toString(), targetClass);
        } else {
            TypeDescriptor sourceType = TypeDescriptor.valueOf(Boolean.class);
            TypeDescriptor targetType = TypeDescriptor.valueOf(targetClass);
            throw new ConversionFailedException(sourceType, targetType, value, null);
        }
    }

    private <T> T convertString(String string, Class<T> targetClass) {
        if (conversionService.canConvert(String.class, targetClass)) {
            return conversionService.convert(string, targetClass);
        } else {
            TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
            TypeDescriptor targetType = TypeDescriptor.valueOf(targetClass);
            throw new ConversionFailedException(sourceType, targetType, string, null);
        }
    }

    private Class<?> getBigDecimalRealType(BigDecimal number) {
        BigDecimal minDouble = new BigDecimal(Double.MIN_VALUE);
        BigDecimal maxDouble = new BigDecimal(Double.MAX_VALUE);
        if (number.compareTo(minDouble) < 0 || number.compareTo(maxDouble) > 0) {
            return BigDecimal.class; // 不在Double范围内
        }
        return Double.class;
    }

    private Class<?> getBigIntegerRealType(BigInteger number) {
        BigInteger minLong = BigInteger.valueOf(Long.MIN_VALUE);
        BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
        if (number.compareTo(minLong) < 0 || number.compareTo(maxLong) > 0) {
            return BigInteger.class; // 不在Long范围内
        }
        BigInteger minInteger = BigInteger.valueOf(Integer.MIN_VALUE);
        BigInteger maxInteger = BigInteger.valueOf(Integer.MAX_VALUE);
        if (number.compareTo(minInteger) < 0 || number.compareTo(maxInteger) > 0) {
            return Long.class; // 不在Integer范围内
        }
        return Integer.class;
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.UNDEFINED;
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

}
