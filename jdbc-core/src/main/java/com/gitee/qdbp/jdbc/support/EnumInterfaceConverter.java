package com.gitee.qdbp.jdbc.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import com.gitee.qdbp.able.enums.EnumInterface;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * EnumInterface转换类<br>
 * String/Integer转换为EnumInterface的子枚举类
 *
 * @author zhaohuihua
 * @version 200202
 */
public class EnumInterfaceConverter<I extends EnumInterface, E extends Enum<E>> implements ConditionalGenericConverter {

    private Class<E> enumType;
    private List<E> enumItems;
    /** 默认值(解析失败时的返回值) **/
    private I defaultValue;
    private Class<I> interfaceType;
    private TypeDescriptor targetDescriptor;
    private Set<ConvertiblePair> convertiblePairs;
    private static final TypeDescriptor STRING_TYPE = TypeDescriptor.valueOf(String.class);
    private static final TypeDescriptor NUMBER_TYPE = TypeDescriptor.valueOf(Number.class);

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
    }

    public EnumInterfaceConverter(Class<I> interfaceType, Class<E> enumType, I defaultValue) {
        this(interfaceType, enumType);
        this.defaultValue = defaultValue;
    }

    private void initConvertiblePairs() {
        this.targetDescriptor = TypeDescriptor.valueOf(interfaceType);
        Set<ConvertiblePair> convertiblePairs = new HashSet<ConvertiblePair>(2);
        convertiblePairs.add(new ConvertiblePair(String.class, interfaceType));
        convertiblePairs.add(new ConvertiblePair(Number.class, interfaceType));
        this.convertiblePairs = convertiblePairs;
    }

    private void initEnumItems() {
        E[] enums = enumType.getEnumConstants();
        this.enumItems = new ArrayList<>();
        for (E e : enums) {
            enumItems.add(e);
        }
    }

    @SuppressWarnings("unchecked")
    public I convert(Object type) {
        if (type instanceof String || type instanceof Number) {
            String target = type.toString();
            for (E i : enumItems) {
                if (i.name().equals(target) || String.valueOf(i.ordinal()).equals(target)) {
                    return (I) i;
                }
            }
        }
        return (I) defaultValue;
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

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return convertiblePairs;
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return targetType.isAssignableTo(targetDescriptor)
                && (sourceType.isAssignableTo(STRING_TYPE) || sourceType.isAssignableTo(NUMBER_TYPE));
    }

    @Override
    public I convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return convert(source);
    }

}
