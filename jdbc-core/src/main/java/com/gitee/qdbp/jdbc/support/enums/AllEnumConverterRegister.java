package com.gitee.qdbp.jdbc.support.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;

/**
 * 注册枚举转换处理类
 *
 * @author zhaohuihua
 * @version 20200807
 */
public class AllEnumConverterRegister {

    public static void registerEnumConverterFactory(ConverterRegistry registry) {
        // 删除系统默认的默认处理类
        registry.removeConvertible(String.class, Enum.class);
        registry.removeConvertible(Integer.class, Enum.class);
        registry.removeConvertible(Number.class, Enum.class);

        registry.addConverterFactory(new StringToEnumConverterFactory());
        registry.addConverterFactory(new NumberToEnumConverterFactory());
    }

    private static Class<?> getEnumType(Class<?> targetType) {
        Class<?> enumType = targetType;
        while (enumType != null && !enumType.isEnum()) {
            enumType = enumType.getSuperclass();
        }
        if (enumType == null) {
            String msg = "The target type " + targetType.getName() + " does not refer to an enum";
            throw new IllegalArgumentException(msg);
        }
        return enumType;
    }

    private static class StringToEnumConverterFactory extends ToEnumConverterFactory<String> {

        public StringToEnumConverterFactory() {
            super(String.class);
        }
    }

    private static class NumberToEnumConverterFactory extends ToEnumConverterFactory<Number> {

        public NumberToEnumConverterFactory() {
            super(Number.class);
        }
    }

    private static class ToEnumConverterFactory<S> implements ConverterFactory<S, Enum<?>> {

        private Class<S> sourceType;

        public ToEnumConverterFactory(Class<S> sourceType) {
            this.sourceType = sourceType;
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public <T extends Enum<?>> Converter<S, T> getConverter(Class<T> targetType) {
            Class targetClass = getEnumType(targetType);
            return (Converter<S, T>) new EnumConverter<>(sourceType, targetClass);
        }
    }

    private static class EnumConverter<S, T extends Enum<T>> implements Converter<S, T> {

        private ToEnumConverter<T> converter;

        public EnumConverter(Class<S> sourceType, Class<T> targetType) {
            this.converter = new ToEnumConverter<>(targetType);
        }

        @Override
        public T convert(S source) {
            return converter.convert(source);
        }

    }
}
