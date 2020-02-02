package com.gitee.qdbp.jdbc.plugins.impl;

import org.springframework.core.convert.ConversionService;
import com.alibaba.fastjson.util.TypeUtils;
import com.gitee.qdbp.able.jdbc.model.DbFieldValue;
import com.gitee.qdbp.jdbc.support.ConversionServiceAware;
import com.gitee.qdbp.tools.utils.DateTools;

/**
 * 使用spring的ConversionService实现类型转换<br>
 * 基础类型是指Boolean/Character/Date/Number/String<br>
 * 继承自ConfigableVarToDbValueConverter:<br>
 * -- 原有功能主要是将非基础类型转换为基础类型, 分为枚举类和其他对象两种:<br>
 * -- 枚举类, 哪些使用ordinal, 哪些使用name;<br>
 * -- 对象作为字段时(对应数据库表的列), 哪些使用json, 哪些使用toString(), 哪些不转换(由JDBC处理)<br>
 * 新增功能, 如果某个类单独配置了DbFieldValue转换类, 将会优先调用<br>
 * 先定义一个转换类: DataStateToDbValueConverter implements Converter&lt;DataState, DbFieldValue&gt;<br>
 * 或: DataStateToDbValueConverter implements ConditionalGenericConverter<br>
 * 再注入到ConversionService之中: <pre>
    &lt;bean class="org.springframework.format.support.FormattingConversionServiceFactoryBean"&gt;
        &lt;property name="converters"&gt;
            &lt;list&gt;
                &lt;bean class="com.xxx.DataStateToDbValueConverter" /&gt;
            &lt;/list&gt;
        &lt;/property&gt;
    &lt;/bean&gt;
 * </pre>
 *
 * @author zhaohuihua
 * @version 190705
 */
public class SpringVarToDbValueConverter extends ConfigableVarToDbValueConverter implements ConversionServiceAware {

    /** Spring的类型转换处理类 **/
    private ConversionService conversionService;

    @Override
    protected Object doEnumToDbValue(Enum<?> variable) {
        if (conversionService != null && conversionService.canConvert(variable.getClass(), DbFieldValue.class)) {
            DbFieldValue result = conversionService.convert(variable, DbFieldValue.class);
            return result.getFieldValue();
        } else {
            return convertEnumToDbValue(variable);
        }
    }

    @Override
    protected Object doObjectToDbValue(Object variable) {
        if (conversionService != null && conversionService.canConvert(variable.getClass(), DbFieldValue.class)) {
            DbFieldValue result = conversionService.convert(variable, DbFieldValue.class);
            return result.getFieldValue();
        } else {
            return convertObjectToDbValue(variable);
        }
    }

    @Override
    protected Object doVariableToString(Object variable) {
        if (conversionService != null && conversionService.canConvert(variable.getClass(), DbFieldValue.class)) {
            DbFieldValue result = conversionService.convert(variable, DbFieldValue.class);
            return TypeUtils.castToString(result.getFieldValue());
        } else {
            return convertObjectToString(variable);
        }
    }

    @Override
    protected Object doVariableToBoolean(Object variable) {
        if (conversionService != null && conversionService.canConvert(variable.getClass(), DbFieldValue.class)) {
            DbFieldValue result = conversionService.convert(variable, DbFieldValue.class);
            return TypeUtils.castToBoolean(result.getFieldValue());
        } else {
            return TypeUtils.castToBoolean(variable);
        }
    }

    @Override
    protected Object doVariableToBit(Object variable) {
        if (conversionService != null && conversionService.canConvert(variable.getClass(), DbFieldValue.class)) {
            DbFieldValue result = conversionService.convert(variable, DbFieldValue.class);
            return TypeUtils.castToByte(result.getFieldValue());
        } else {
            return TypeUtils.castToByte(variable);
        }
    }

    @Override
    protected Object doVariableToInteger(Object variable) {
        if (conversionService != null && conversionService.canConvert(variable.getClass(), DbFieldValue.class)) {
            DbFieldValue result = conversionService.convert(variable, DbFieldValue.class);
            return TypeUtils.castToInt(result.getFieldValue());
        } else {
            return TypeUtils.castToInt(variable);
        }
    }

    @Override
    protected Object doVariableToLong(Object variable) {
        if (conversionService != null && conversionService.canConvert(variable.getClass(), DbFieldValue.class)) {
            DbFieldValue result = conversionService.convert(variable, DbFieldValue.class);
            return TypeUtils.castToLong(result.getFieldValue());
        } else {
            return TypeUtils.castToLong(variable);
        }
    }

    @Override
    protected Object doVariableToDouble(Object variable) {
        if (conversionService != null && conversionService.canConvert(variable.getClass(), DbFieldValue.class)) {
            DbFieldValue result = conversionService.convert(variable, DbFieldValue.class);
            return TypeUtils.castToDouble(result.getFieldValue());
        } else {
            return TypeUtils.castToDouble(variable);
        }
    }

    @Override
    protected Object doVariableToDate(Object variable) {
        if (conversionService != null && conversionService.canConvert(variable.getClass(), DbFieldValue.class)) {
            DbFieldValue result = conversionService.convert(variable, DbFieldValue.class);
            return TypeUtils.castToDate(result.getFieldValue());
        } else {
            if (variable instanceof CharSequence) {
                return DateTools.parse(variable.toString());
            } else {
                return TypeUtils.castToDate(variable);
            }
        }
    }

    /** Spring的类型转换处理类 **/
    public ConversionService getConversionService() {
        return conversionService;
    }

    /** Spring的类型转换处理类 **/
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

}
