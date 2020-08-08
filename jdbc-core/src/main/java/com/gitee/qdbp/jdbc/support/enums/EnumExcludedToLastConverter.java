package com.gitee.qdbp.jdbc.support.enums;

/**
 * 枚举例外项转换为枚举最后一项的转换类
 *
 * @author zhaohuihua
 * @version 20200726
 */
public class EnumExcludedToLastConverter<E extends Enum<E>> extends EnumExcludedToDefaultConverter<E> {

    public EnumExcludedToLastConverter(Class<E> enumType) {
        super(enumType, getEnumLastItem(enumType), false);
    }

    private static <E> E getEnumLastItem(Class<E> enumType) {
        E[] enums = enumType.getEnumConstants();
        return enums[enums.length - 1];
    }
}
