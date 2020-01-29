package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.ConversionService;
import com.alibaba.fastjson.util.TypeUtils;
import com.gitee.qdbp.able.jdbc.model.DbFieldValue;
import com.gitee.qdbp.jdbc.utils.InnerTools;
import com.gitee.qdbp.tools.utils.DateTools;

/**
 * DataConvertHandler配置实现类<br>
 * 基础类型是指Boolean/Character/Date/Number/String<br>
 * 这个类主要作用是将非基础类型转换为基础类型, 分为枚举类和其他对象两种:<br>
 * 枚举类, 哪些使用ordinal, 哪些使用name;<br>
 * 对象作为字段时(对应数据库表的列), 哪些使用json, 哪些使用toString(), 哪些不转换(由JDBC处理)<br>
 * 另外, 如果某个类单独配置了ToDbValue转换类, 将会优先调用<br>
 * 先定义一个转换类: DataStateToDbValueConverter implements Converter&lt;DataState, DbFieldValue&gt;<br>
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
public class ConfigableDataConvertHandler extends SimpleDataConvertHandler {

    /** Spring的类型转换处理类 **/
    private ConversionService conversionService;
    /** 枚举转换是否默认使用ordinal: true=ordinal, false=name **/
    private boolean enumConvertUseOrdinal = true;
    /** 枚举转换例外列表, 使用与enumConvertUseOrdinal相反的设置 **/
    private Map<String, Void> enumConvertEspecialList = new HashMap<>();
    /** 未知类型对象转换是否启用: true=执行convertToString()转换, false=不转换直接返回对象(由JDBC处理) **/
    private boolean untypedObjectConvertEnabled = false;
    /** 未知类型对象转换的例外列表, 使用与untypedObjectConvertEnabled相反的设置 **/
    private Map<String, Void> untypedObjectConvertEspecialList = new HashMap<>();
    /** 对象(基本对象除外)转字符串convertToString()是否默认使用JSON格式: true=使用JSON格式, false=object.toString() **/
    private boolean objectToStringUseJson = true;
    /** 对象转字符串的例外列表, 使用与objectToStringUseJson相反的设置 **/
    private Map<String, Void> objectToStringEspecialList = new HashMap<>();

    protected Object convertEnumToDbValue(Enum<?> variable) {
        if (enumConvertEspecialList.containsKey(variable.getClass().getName())) {
            return enumConvertUseOrdinal ? variable.name() : variable.ordinal();
        } else {
            return enumConvertUseOrdinal ? variable.ordinal() : variable.name();
        }
    }

    protected Object convertObjectToDbValue(Object variable) {
        if (untypedObjectConvertEspecialList.containsKey(variable.getClass().getName())) {
            return untypedObjectConvertEnabled ? variable : convertObjectToString(variable);
        } else {
            return untypedObjectConvertEnabled ? convertObjectToString(variable) : variable;
        }
    }

    protected String convertObjectToString(Object variable) {
        if (objectToStringEspecialList.containsKey(variable.getClass().getName())) {
            return objectToStringUseJson ? variable.toString() : TypeUtils.castToString(variable);
        } else {
            return objectToStringUseJson ? TypeUtils.castToString(variable) : variable.toString();
        }
    }

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

    @Override
    protected Object doVariableToBinary(Object variable) {
        return convertObjectToDbValue(variable);
    }

    @Override
    protected Object doVariableToBlob(Object variable) {
        return convertObjectToDbValue(variable);
    }

    @Override
    protected Object doVariableToClob(Object variable) {
        return doVariableToString(variable);
    }

    @Override
    protected Object doVariableToOtherTypedValue(int sqlType, Object variable) {
        return convertObjectToDbValue(variable);
    }

    /** 判断枚举转换是否默认使用ordinal: true=ordinal, false=name **/
    public boolean isEnumConvertUseOrdinal() {
        return enumConvertUseOrdinal;
    }

    /** 设置枚举转换是否默认使用ordinal: true=ordinal, false=name **/
    public void setEnumConvertUseOrdinal(boolean enumConvertUseOrdinal) {
        this.enumConvertUseOrdinal = enumConvertUseOrdinal;
    }

    /** 获取枚举转换例外列表 **/
    public List<String> getEnumConvertEspecialList() {
        return new ArrayList<>(enumConvertEspecialList.keySet());
    }

    /** 设置枚举转换例外列表(以文本方式设置, 分隔符为[,; \t\n]) **/
    public void setEnumConvertEspecialText(String text) {
        List<String> list = InnerTools.tokenizeToStringList(text);
        this.setEnumConvertEspecialList(list);
    }

    /** 设置枚举转换例外列表 **/
    public void setEnumConvertEspecialList(List<String> enumEspecialList) {
        this.enumConvertEspecialList.clear();
        this.addEnumConvertEspecialList(enumEspecialList);
    }

    /** 增加枚举转换例外列表 **/
    public void addEnumConvertEspecialList(Class<?>... enumEspecialList) {
        if (enumEspecialList != null) {
            for (Class<?> item : enumEspecialList) {
                this.enumConvertEspecialList.put(item.getName(), null);
            }
        }
    }

    /** 增加枚举转换例外列表 **/
    public void addEnumConvertEspecialList(String... enumEspecialList) {
        if (enumEspecialList != null) {
            for (String item : enumEspecialList) {
                this.enumConvertEspecialList.put(item, null);
            }
        }
    }

    /** 增加枚举转换例外列表 **/
    public void addEnumConvertEspecialList(List<String> enumEspecialList) {
        if (enumEspecialList != null) {
            for (String item : enumEspecialList) {
                this.enumConvertEspecialList.put(item, null);
            }
        }
    }

    /** 判断未知类型对象转换是否启用: true=执行convertToString()转换, false=不转换直接返回对象(由JDBC处理) **/
    public boolean isUntypedObjectConvertEnabled() {
        return untypedObjectConvertEnabled;
    }

    /** 设置未知类型对象转换是否启用: true=执行convertToString()转换, false=不转换直接返回对象(由JDBC处理) **/
    public void setUntypedObjectConvertEnabled(boolean untypedObjectConvertEnabled) {
        this.untypedObjectConvertEnabled = untypedObjectConvertEnabled;
    }

    /** 获取类型对象转换的例外列表 **/
    public List<String> getUntypedObjectConvertEspecialList() {
        return new ArrayList<>(untypedObjectConvertEspecialList.keySet());
    }

    /** 设置类型对象转换的例外列表(以文本方式设置, 分隔符为[,; \t\n]) **/
    public void setUntypedObjectConvertEspecialText(String text) {
        List<String> list = InnerTools.tokenizeToStringList(text);
        this.setUntypedObjectConvertEspecialList(list);
    }

    /** 设置类型对象转换的例外列表 **/
    public void setUntypedObjectConvertEspecialList(List<String> untypedObjectConvertEspecialList) {
        this.untypedObjectConvertEspecialList.clear();
        this.addUntypedObjectConvertEspecialList(untypedObjectConvertEspecialList);
    }

    /** 增加类型对象转换的例外列表 **/
    public void addUntypedObjectConvertEspecialList(Class<?>... untypedObjectConvertEspecialList) {
        if (untypedObjectConvertEspecialList != null) {
            for (Class<?> item : untypedObjectConvertEspecialList) {
                this.untypedObjectConvertEspecialList.put(item.getName(), null);
            }
        }
    }

    /** 增加类型对象转换的例外列表 **/
    public void addUntypedObjectConvertEspecialList(String... untypedObjectConvertEspecialList) {
        if (untypedObjectConvertEspecialList != null) {
            for (String item : untypedObjectConvertEspecialList) {
                this.untypedObjectConvertEspecialList.put(item, null);
            }
        }
    }

    /** 增加类型对象转换的例外列表 **/
    public void addUntypedObjectConvertEspecialList(List<String> untypedObjectConvertEspecialList) {
        if (untypedObjectConvertEspecialList != null) {
            for (String item : untypedObjectConvertEspecialList) {
                this.untypedObjectConvertEspecialList.put(item, null);
            }
        }
    }

    /** 判断对象(基本对象除外)转字符串是否默认使用JSON格式: true=使用JSON格式, false=object.toString() **/
    public boolean isObjectToStringUseJson() {
        return objectToStringUseJson;
    }

    /** 设置对象(基本对象除外)转字符串是否默认使用JSON格式: true=使用JSON格式, false=object.toString() **/
    public void setObjectToStringUseJson(boolean objectToStringUseJson) {
        this.objectToStringUseJson = objectToStringUseJson;
    }

    /** 获取对象转字符串例外列表 **/
    public List<String> getObjectToStringEspecialList() {
        return new ArrayList<>(objectToStringEspecialList.keySet());
    }

    /** 设置对象转字符串例外列表(以文本方式设置, 分隔符为[,; \t\n]) **/
    public void setObjectToStringEspecialText(String text) {
        List<String> list = InnerTools.tokenizeToStringList(text);
        this.setObjectToStringEspecialList(list);
    }

    /** 设置对象转字符串例外列表 **/
    public void setObjectToStringEspecialList(List<String> objectToStringEspecialList) {
        this.objectToStringEspecialList.clear();
        this.addObjectToStringEspecialList(objectToStringEspecialList);
    }

    /** 增加对象转字符串例外列表 **/
    public void addObjectToStringEspecialList(Class<?>... objectToStringEspecialList) {
        if (objectToStringEspecialList != null) {
            for (Class<?> item : objectToStringEspecialList) {
                this.objectToStringEspecialList.put(item.getName(), null);
            }
        }
    }

    /** 增加对象转字符串例外列表 **/
    public void addObjectToStringEspecialList(String... objectToStringEspecialList) {
        if (objectToStringEspecialList != null) {
            for (String item : objectToStringEspecialList) {
                this.objectToStringEspecialList.put(item, null);
            }
        }
    }

    /** 增加对象转字符串例外列表 **/
    public void addObjectToStringEspecialList(List<String> objectToStringEspecialList) {
        if (objectToStringEspecialList != null) {
            for (String item : objectToStringEspecialList) {
                this.objectToStringEspecialList.put(item, null);
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
