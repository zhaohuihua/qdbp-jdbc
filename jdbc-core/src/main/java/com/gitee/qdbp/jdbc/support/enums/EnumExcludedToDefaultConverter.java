package com.gitee.qdbp.jdbc.support.enums;

/**
 * 枚举例外项转换为默认值的转换类
 *
 * @author zhaohuihua
 * @version 20200726
 */
public class EnumExcludedToDefaultConverter<E extends Enum<E>> extends ToEnumConverter<E> {

    /** 默认值(解析失败时的返回值) **/
    private E defaultValue;
    /** 空值是否转换为默认值 **/
    private boolean nullToDefault;

    public EnumExcludedToDefaultConverter(Class<E> enumType) {
        super(enumType);
    }

    public EnumExcludedToDefaultConverter(Class<E> enumType, E defaultValue) {
        this(enumType);
        this.defaultValue = defaultValue;
        this.nullToDefault = true;
    }

    public EnumExcludedToDefaultConverter(Class<E> enumType, String defaultValue) {
        this(enumType);
        this.defaultValue = convertStringValue(defaultValue);
        this.nullToDefault = true;
    }

    public EnumExcludedToDefaultConverter(Class<E> enumType, E defaultValue, boolean nullToDefault) {
        this(enumType);
        this.defaultValue = defaultValue;
        this.nullToDefault = nullToDefault;
    }

    public EnumExcludedToDefaultConverter(Class<E> enumType, String defaultValue, boolean nullToDefault) {
        this(enumType);
        this.defaultValue = convertStringValue(defaultValue);
        this.nullToDefault = nullToDefault;
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
