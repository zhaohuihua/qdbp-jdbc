package com.gitee.qdbp.jdbc.support.enums;

import java.util.HashSet;
import java.util.Set;
import org.springframework.core.convert.TypeDescriptor;
import com.gitee.qdbp.able.enums.EnumInterface;

/**
 * EnumInterface转换类<br>
 * String/Integer转换为EnumInterface的子枚举类
 *
 * @author zhaohuihua
 * @version 20200202
 */
public class EnumInterfaceConverter<I extends EnumInterface, E extends Enum<E>> extends ToEnumConverter<E> {

    /** 默认值(解析失败时的返回值) **/
    private E defaultValue;
    /** 空值是否转换为默认值 **/
    private boolean nullToDefault;
    private Class<I> interfaceType;

    public EnumInterfaceConverter(Class<I> interfaceType, Class<E> enumType) {
        super(enumType);
        if (!interfaceType.isAssignableFrom(enumType)) {
            String fmt = "EnumType[%s] is not an instance of InterfaceType[%s]";
            String msg = String.format(fmt, enumType.getSimpleName(), interfaceType.getSimpleName());
            throw new IllegalArgumentException(msg);
        }

        // 初始化目标类型
        this.initTargetType(interfaceType);
    }

    public EnumInterfaceConverter(Class<I> interfaceType, Class<E> enumType, String defaultValue) {
        this(interfaceType, enumType);
        this.defaultValue = convert(defaultValue);
        this.nullToDefault = true;
    }

    public EnumInterfaceConverter(Class<I> interfaceType, Class<E> enumType, E defaultValue) {
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

    public EnumInterfaceConverter(Class<I> interfaceType, Class<E> enumType, E defaultValue, boolean nullToDefault) {
        this(interfaceType, enumType);
        this.defaultValue = defaultValue;
        this.nullToDefault = nullToDefault;
    }

    private void initTargetType(Class<I> interfaceType) {
        this.interfaceType = interfaceType;
        // 修改目标类型
        this.setTargetDescriptor(TypeDescriptor.valueOf(interfaceType));
        // 替换ConvertibleTypes中的目标类型
        Set<ConvertiblePair> oldPairs = this.getConvertibleTypes();
        Set<ConvertiblePair> newPairs = new HashSet<>();
        for (ConvertiblePair item : oldPairs) {
            newPairs.add(new ConvertiblePair(item.getSourceType(), interfaceType));
        }
        this.setConvertibleTypes(newPairs);
    }

    /** 获取接口类型 **/
    public Class<I> getInterfaceType() {
        return interfaceType;
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
    protected E convertNullValue() {
        return this.nullToDefault ? this.defaultValue : null;
    }

    @Override
    protected E convertExcludedValue(Object source) {
        return this.defaultValue;
    }

}
