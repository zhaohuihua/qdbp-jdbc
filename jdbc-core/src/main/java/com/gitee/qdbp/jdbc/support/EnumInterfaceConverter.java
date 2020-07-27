package com.gitee.qdbp.jdbc.support;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import com.gitee.qdbp.able.enums.EnumInterface;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * EnumInterface转换类<br>
 * String/Integer转换为EnumInterface的子枚举类
 *
 * @author zhaohuihua
 * @version 20200202
 */
public class EnumInterfaceConverter<I extends EnumInterface, E extends Enum<E>> implements ConditionalGenericConverter {

    private Class<E> enumType;
    private List<E> enumItems;
    /** 默认值(解析失败时的返回值) **/
    private I defaultValue;
    /** 空值是否转换为默认值 **/
    private boolean nullToDefault;
    private Class<I> interfaceType;
    private TypeDescriptor targetDescriptor;
    private Set<ConvertiblePair> convertiblePairs;

    public EnumInterfaceConverter(Class<I> interfaceType, Class<E> enumType) {
        VerifyTools.requireNotBlank(interfaceType, "interfaceType");
        VerifyTools.requireNotBlank(enumType, "enumType");
        if (enumType.getEnumConstants() == null) {
            throw new IllegalArgumentException(enumType.getSimpleName() + " does not represent an enum type.");
        }
        if (!interfaceType.isAssignableFrom(enumType)) {
            String fmt = "EnumType[%s] is not an instance of InterfaceType[%s]";
            String msg = String.format(fmt, enumType.getSimpleName(), interfaceType.getSimpleName());
            throw new IllegalArgumentException(msg);
        }
        this.interfaceType = interfaceType;
        this.enumType = enumType;
        initConvertiblePairs();
        initEnumItems();
    }

    public EnumInterfaceConverter(Class<I> interfaceType, Class<E> enumType, String defaultValue) {
        this(interfaceType, enumType);
        this.defaultValue = convert(defaultValue);
        this.nullToDefault = true;
    }

    public EnumInterfaceConverter(Class<I> interfaceType, Class<E> enumType, I defaultValue) {
        this(interfaceType, enumType);
        this.defaultValue = defaultValue;
        this.nullToDefault = true;
    }

    public EnumInterfaceConverter(Class<I> interfaceType, Class<E> enumType, String defaultValue,
            boolean nullToDefault) {
        this(interfaceType, enumType);
        this.defaultValue = convert(defaultValue);
        this.nullToDefault = nullToDefault;
    }

    public EnumInterfaceConverter(Class<I> interfaceType, Class<E> enumType, I defaultValue, boolean nullToDefault) {
        this(interfaceType, enumType);
        this.defaultValue = defaultValue;
        this.nullToDefault = nullToDefault;
    }

    private void initConvertiblePairs() {
        this.targetDescriptor = TypeDescriptor.valueOf(interfaceType);
        Set<ConvertiblePair> convertiblePairs = new HashSet<ConvertiblePair>(2);
        convertiblePairs.add(new ConvertiblePair(String.class, interfaceType));
        convertiblePairs.add(new ConvertiblePair(Integer.class, interfaceType));
        convertiblePairs.add(new ConvertiblePair(Long.class, interfaceType));
        convertiblePairs.add(new ConvertiblePair(BigInteger.class, interfaceType));
        this.convertiblePairs = convertiblePairs;
    }

    private void initEnumItems() {
        E[] enums = enumType.getEnumConstants();
        this.enumItems = new ArrayList<>();
        for (E e : enums) {
            enumItems.add(e);
        }
    }

    public I convert(Object source) {
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

    @SuppressWarnings("unchecked")
    protected I convert(int source) {
        for (E i : enumItems) {
            if (i.ordinal() == source) {
                return (I) i;
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    protected I convert(String source) {
        if (source.trim().length() == 0) {
            return nullToDefault ? defaultValue : null;
        }

        for (E i : enumItems) {
            if (i.name().equals(source)) {
                return (I) i;
            }
        }
        // 如果没有完全匹配的, 不区分大小写再遍历一次
        for (E i : enumItems) {
            if (i.name().equalsIgnoreCase(source)) {
                return (I) i;
            }
        }
        return defaultValue;
    }

    /** 获取接口类型 **/
    public Class<I> getInterfaceType() {
        return interfaceType;
    }

    /** 获取枚举类型 **/
    public Class<E> getEnumType() {
        return enumType;
    }

    /** 获取默认值 **/
    public I getDefaultValue() {
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
    public I convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return convert(source);
    }

}
