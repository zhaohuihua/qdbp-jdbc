package com.gitee.qdbp.jdbc.support;

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
 * 枚举例外项转换为默认值的转换类
 *
 * @author zhaohuihua
 * @version 20200726
 */
public class EnumExcludedToDefaultConverter<E extends Enum<E>> implements ConditionalGenericConverter {

    private Class<E> enumType;
    private List<E> enumItems;
    /** 默认值(解析失败时的返回值) **/
    private E defaultValue;
    /** 空值是否转换为默认值 **/
    private boolean nullToDefault;
    private TypeDescriptor targetDescriptor;
    private Set<ConvertiblePair> convertiblePairs;

    public EnumExcludedToDefaultConverter(Class<E> enumType) {
        VerifyTools.requireNotBlank(enumType, "enumType");
        if (enumType.getEnumConstants() == null) {
            throw new IllegalArgumentException(enumType.getSimpleName() + " does not represent an enum type.");
        }
        this.enumType = enumType;
        initConvertiblePairs();
        initEnumItems();
    }

    public EnumExcludedToDefaultConverter(Class<E> enumType, E defaultValue) {
        this(enumType);
        this.defaultValue = defaultValue;
        this.nullToDefault = true;
    }

    public EnumExcludedToDefaultConverter(Class<E> enumType, String defaultValue) {
        this(enumType);
        this.defaultValue = convert(defaultValue);
        this.nullToDefault = true;
    }

    public EnumExcludedToDefaultConverter(Class<E> enumType, E defaultValue, boolean nullToDefault) {
        this(enumType);
        this.defaultValue = defaultValue;
        this.nullToDefault = nullToDefault;
    }

    public EnumExcludedToDefaultConverter(Class<E> enumType, String defaultValue, boolean nullToDefault) {
        this(enumType);
        this.defaultValue = convert(defaultValue);
        this.nullToDefault = nullToDefault;
    }

    private void initConvertiblePairs() {
        this.targetDescriptor = TypeDescriptor.valueOf(enumType);
        Set<ConvertiblePair> convertiblePairs = new HashSet<ConvertiblePair>(2);
        convertiblePairs.add(new ConvertiblePair(String.class, enumType));
        convertiblePairs.add(new ConvertiblePair(Integer.class, enumType));
        convertiblePairs.add(new ConvertiblePair(Long.class, enumType));
        convertiblePairs.add(new ConvertiblePair(BigInteger.class, enumType));
        this.convertiblePairs = convertiblePairs;
    }

    private void initEnumItems() {
        E[] enums = enumType.getEnumConstants();
        this.enumItems = new ArrayList<>();
        for (E e : enums) {
            enumItems.add(e);
        }
    }

    public E convert(Object source) {
        if (source == null) {
            return nullToDefault ? defaultValue : null;
        }

        if (source instanceof Integer || source instanceof Long || source instanceof BigInteger) {
            return convert(((Number) source).intValue());
        } else if (source instanceof String) {
            String string = (String) source;
            if (StringTools.isDigit(string)) {
                return convert(ConvertTools.toInteger(string));
            } else {
                return convert(string);
            }
        } else {
            TypeDescriptor sourceType = TypeDescriptor.forObject(source);
            TypeDescriptor targetType = TypeDescriptor.valueOf(enumType);
            throw new ConversionFailedException(sourceType, targetType, source, null);
        }
    }

    protected E convert(int source) {
        for (E i : enumItems) {
            if (i.ordinal() == source) {
                return i;
            }
        }
        return defaultValue;
    }

    protected E convert(String source) {
        if (source.trim().length() == 0) {
            return nullToDefault ? defaultValue : null;
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
        return defaultValue;
    }

    /** 获取枚举类型 **/
    public Class<E> getEnumType() {
        return enumType;
    }

    /** 获取默认值 **/
    public E getDefaultValue() {
        return defaultValue;
    }
    
    /** 空值是否转换为默认值 **/
    public boolean isNullToDefault() {
        return nullToDefault;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return convertiblePairs;
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType.isAssignableTo(targetDescriptor)) {
            for (ConvertiblePair pair : convertiblePairs) {
                if (sourceType.getObjectType().isAssignableFrom(pair.getSourceType())) {
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
