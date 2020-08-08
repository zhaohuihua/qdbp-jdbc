package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.ConversionService;
import com.alibaba.fastjson.parser.ParserConfig;
import com.gitee.qdbp.jdbc.plugins.MapToBeanConverter;
import com.gitee.qdbp.jdbc.support.ConversionServiceAware;
import com.gitee.qdbp.jdbc.support.fastjson.UseSpringConversionDeserializer;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 利用fastjson进行Map到JavaBean的转换<br>
 * 由于TypeUtils.castToEnum()逻辑存在硬伤, 无法做到数字枚举值的自定义转换<br>
 * <pre>
 * // fastjson-1.2.72, TypeUtils.castToEnum(), BUG:
 * if (obj instanceof String) { // 只有字符串会从config中判断是否存在Deserializer
 *     ...
 *     if (deserializer instanceof EnumDeserializer) { // 只有EnumDeserializer会被调用
 *     }
 * }</pre>
 *
 * @author zhaohuihua
 * @version 20200201
 */
public class FastJsonMapToBeanConverter implements MapToBeanConverter, ConversionServiceAware {

    private ConversionService conversionService;

    @Override
    public <T> T convert(Map<String, ?> map, Class<T> clazz) {
        VerifyTools.requireNonNull(map, "map");
        VerifyTools.requireNonNull(clazz, "class");
        return FastJsonTools.mapToBean(map, clazz);
    }

    @Override
    public void fill(Map<String, ?> map, Object bean) {
        VerifyTools.requireNonNull(map, "map");
        VerifyTools.requireNonNull(bean, "bean");
        FastJsonTools.mapFillToBean(map, bean);
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /** 标明哪些类使用SpringConversion进行转换 **/
    public void setUseSpringConversionTypes(Class<?>... types) {
        this.setUseSpringConversionTypes(Arrays.asList(types));
    }

    /** 标明哪些类使用SpringConversion进行转换 **/
    public void setUseSpringConversionTypes(List<Class<?>> types) {
        if (conversionService == null) {
            throw new IllegalStateException("If use spring conversion, then conversionService is required.");
        }
        UseSpringConversionDeserializer deserializer = new UseSpringConversionDeserializer();
        deserializer.setConversionService(conversionService);
        // 将由spring进行转换的类注册到ParserConfig
        ParserConfig config = ParserConfig.getGlobalInstance();
        for (Class<?> type : types) {
            config.putDeserializer(type, deserializer);
        }
    }
}
