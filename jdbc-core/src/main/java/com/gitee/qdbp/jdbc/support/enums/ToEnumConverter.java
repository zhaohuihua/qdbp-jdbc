package com.gitee.qdbp.jdbc.support.enums;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 枚举转换处理类
 *
 * @author zhaohuihua
 * @version 20200807
 */
public class ToEnumConverter<E extends Enum<E>> implements ConditionalGenericConverter {

    private Class<E> enumType;
    private List<E> enumItems;
    private TypeDescriptor targetDescriptor;
    private Set<ConvertiblePair> convertiblePairs;

    public ToEnumConverter(Class<E> enumType) {
        VerifyTools.requireNotBlank(enumType, "enumType");
        if (enumType.getEnumConstants() == null) {
            throw new IllegalArgumentException(enumType.getSimpleName() + " does not represent an enum type.");
        }
        this.enumType = enumType;
        initConvertiblePairs();
        initEnumItems();
    }

    protected void initConvertiblePairs() {
        this.setTargetDescriptor(TypeDescriptor.valueOf(enumType));
        Set<ConvertiblePair> convertiblePairs = new HashSet<>();
        convertiblePairs.add(new ConvertiblePair(String.class, enumType));
        convertiblePairs.add(new ConvertiblePair(Number.class, enumType));
        this.setConvertibleTypes(convertiblePairs);
    }

    protected void initEnumItems() {
        E[] enums = enumType.getEnumConstants();
        this.enumItems = new ArrayList<>();
        for (E e : enums) {
            enumItems.add(e);
        }
    }

    public E convert(Object source) {
        if (source == null) {
            return convertNullValue();
        }

        if (source instanceof Integer || source instanceof Long || source instanceof Short || source instanceof Byte) {
            return convertLongValue(((Number) source).longValue());
        } else if (source instanceof Double || source instanceof Float) {
            return convertDoubleValue(((Number) source).doubleValue());
        } else if (source instanceof String) {
            String string = (String) source;
            if (StringTools.isDigit(string)) {
                return convertLongValue(ConvertTools.toLong(string));
            } else {
                return convertStringValue(string);
            }
        } else if (source instanceof BigInteger) {
            return convertBigIntegerValue((BigInteger) source);
        } else if (source instanceof BigDecimal) {
            return convertBigDecimalValue((BigDecimal) source);
        } else {
            TypeDescriptor sourceType = TypeDescriptor.forObject(source);
            TypeDescriptor targetType = TypeDescriptor.valueOf(enumType);
            throw new ConversionFailedException(sourceType, targetType, source, null);
        }
    }

    protected E convertNullValue() {
        return null;
    }

    protected E convertExcludedValue(Object source) {
        return null;
    }

    protected E convertLongValue(long source) {
        for (E i : enumItems) {
            if (i.ordinal() == source) {
                return i;
            }
        }
        return convertExcludedValue(source);
    }

    protected E convertDoubleValue(double source) {
        long longValue = (long) source;
        // 是不是整数
        boolean isIntegral = source == longValue;
        if (isIntegral) {
            return convertLongValue(longValue);
        } else {
            TypeDescriptor sourceType = TypeDescriptor.forObject(source);
            TypeDescriptor targetType = TypeDescriptor.valueOf(enumType);
            throw new ConversionFailedException(sourceType, targetType, source, null);
        }
    }

    protected E convertBigDecimalValue(BigDecimal source) {
        // 整数部分
        BigInteger integer = source.toBigInteger();
        // 是不是整数
        boolean isIntegral = source.compareTo(new BigDecimal(integer)) == 0;
        if (isIntegral) {
            return convertBigIntegerValue(integer);
        } else {
            TypeDescriptor sourceType = TypeDescriptor.forObject(source);
            TypeDescriptor targetType = TypeDescriptor.valueOf(enumType);
            throw new ConversionFailedException(sourceType, targetType, source.toPlainString(), null);
        }
    }

    protected E convertBigIntegerValue(BigInteger source) {
        BigInteger minLong = BigInteger.valueOf(Long.MIN_VALUE);
        BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
        if (source.compareTo(minLong) >= 0 && source.compareTo(maxLong) <= 0) {
            // 在Long范围内
            return convertLongValue(source.longValue());
        } else {
            TypeDescriptor sourceType = TypeDescriptor.forObject(source);
            TypeDescriptor targetType = TypeDescriptor.valueOf(enumType);
            throw new ConversionFailedException(sourceType, targetType, source, null);
        }
    }

    protected E convertStringValue(String source) {
        if (source.trim().length() == 0) {
            return convertNullValue();
        }

        for (E i : enumItems) {
            if (i.name().equals(source)) {
                return i;
            }
        }
        // 如果没有完全匹配的, 不区分大小写再遍历一次
        for (E i : enumItems) {
            if (i.name().equalsIgnoreCase(source)) {
                return i;
            }
        }
        return convertExcludedValue(source);
    }

    /** 获取枚举类型 **/
    public Class<E> getEnumType() {
        return enumType;
    }

    protected List<E> getEnumItems() {
        return enumItems;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return convertiblePairs;
    }

    protected void setConvertibleTypes(Set<ConvertiblePair> convertiblePairs) {
        this.convertiblePairs = convertiblePairs;
    }

    public TypeDescriptor getTargetDescriptor() {
        return targetDescriptor;
    }

    protected void setTargetDescriptor(TypeDescriptor targetDescriptor) {
        this.targetDescriptor = targetDescriptor;
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType.isAssignableTo(targetDescriptor)) {
            for (ConvertiblePair pair : convertiblePairs) {
                if (pair.getSourceType().isAssignableFrom(sourceType.getObjectType())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public E convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return convert(source);
    }

}
