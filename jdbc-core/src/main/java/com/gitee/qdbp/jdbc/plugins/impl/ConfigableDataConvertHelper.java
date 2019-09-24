package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.util.TypeUtils;

/**
 * DataConvertHelper配置实现类
 *
 * @author zhaohuihua
 * @version 190705
 */
public class ConfigableDataConvertHelper extends SimpleDataConvertHelper {

    //    /** Spring的类型转换处理类 **/
    //    private ConversionService conversionService;
    /** 枚举是否默认使用ordinal: true=ordinal, false=name **/
    private boolean enumUseOrdinal = true;
    /** 枚举例外列表, 使用与enumUseOrdinal相反的设置 **/
    private Map<String, Void> enumEspecialList = new HashMap<>();
    /** 对象(基本对象除外)转字符串convertToString()是否默认使用JSON格式: true=使用JSON格式, false=object.toString() **/
    private boolean objectToStringUseJson = true;
    /** 对象转字符串的例外列表, 使用与objectToStringUseJson相反的设置 **/
    private Map<String, Void> objectToStringEspecialList = new HashMap<>();
    /** 未知类型对象转换是否启用: true=执行convertToString()转换, false=不转换直接返回对象(由JDBC处理) **/
    private boolean untypedObjectConvertEnabled = false;
    /** 未知类型对象转换的例外列表, 使用与untypedObjectConvertEnabled相反的设置 **/
    private Map<String, Void> untypedObjectConvertEspecialList = new HashMap<>();

    protected Object convertEnumToDbValue(Enum<?> variable) {
        if (enumEspecialList.containsKey(variable.getClass().getName())) {
            return enumUseOrdinal ? variable.name() : variable.ordinal();
        } else {
            return enumUseOrdinal ? variable.ordinal() : variable.name();
        }
    }

    protected String convertObjectToString(Object variable) {
        if (objectToStringEspecialList.containsKey(variable.getClass().getName())) {
            return objectToStringUseJson ? variable.toString() : TypeUtils.castToString(variable);
        } else {
            return objectToStringUseJson ?  TypeUtils.castToString(variable) : variable.toString();
        }
    }

    protected Object convertObjectToDbValue(Object variable) {
        if (untypedObjectConvertEspecialList.containsKey(variable.getClass().getName())) {
            return untypedObjectConvertEnabled ? variable : convertObjectToString(variable);
        } else {
            return untypedObjectConvertEnabled ?  convertObjectToString(variable) : variable;
        }
    }

    protected Object doEnumToDbValue(Enum<?> variable) {
        return convertEnumToDbValue(variable);
    }

    protected Object doObjectToDbValue(Object variable) {
        return convertObjectToDbValue(variable);
    }

    protected Object doVariableToString(Object variable) {
        return convertObjectToString(variable);
    }

    protected Object doVariableToBinary(Object variable) {
        return convertObjectToDbValue(variable);
    }

    protected Object doVariableToBlob(Object variable) {
        return convertObjectToDbValue(variable);
    }

    protected Object doVariableToClob(Object variable) {
        return doVariableToString(variable);
    }

    protected Object doVariableToOtherTypedValue(int sqlType, Object variable) {
        return convertObjectToDbValue(variable);
    }

    /** 判断枚举是否默认使用ordinal: true=ordinal, false=name **/
    public boolean isEnumUseOrdinal() {
        return enumUseOrdinal;
    }

    /** 设置枚举是否默认使用ordinal: true=ordinal, false=name **/
    public void setEnumUseOrdinal(boolean enumUseOrdinal) {
        this.enumUseOrdinal = enumUseOrdinal;
    }

    /** 获取枚举例外列表 **/
    public List<String> getEnumEspecialList() {
        return new ArrayList<>(enumEspecialList.keySet());
    }

    /** 设置枚举例外列表 **/
    public void setEnumEspecialList(List<String> enumEspecialList) {
        this.enumEspecialList.clear();
        this.addEnumEspecialList(enumEspecialList);
    }

    /** 增加枚举例外列表 **/
    public void addEnumEspecialList(Enum<?>... enumEspecialList) {
        if (enumEspecialList != null) {
            for (Enum<?> item : enumEspecialList) {
                this.enumEspecialList.put(item.getClass().getName(), null);
            }
        }
    }

    /** 增加枚举例外列表 **/
    public void addEnumEspecialList(String... enumEspecialList) {
        if (enumEspecialList != null) {
            for (String item : enumEspecialList) {
                this.enumEspecialList.put(item, null);
            }
        }
    }

    /** 增加枚举例外列表 **/
    public void addEnumEspecialList(List<String> enumEspecialList) {
        if (enumEspecialList != null) {
            for (String item : enumEspecialList) {
                this.enumEspecialList.put(item, null);
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

}
