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

    /** 枚举是否默认使用ordinal: true=ordinal, false=name **/
    private boolean enumUseOrdinal = true;
    /** 枚举例外列表, 使用与enumUseOrdinal相反的设置 **/
    private Map<String, Void> enumEspecialList = new HashMap<>();

    protected Object doEnumToDbValue(Enum<?> variable) {
        if (enumEspecialList.containsKey(variable.getClass().getName())) {
            return enumUseOrdinal ? variable.name() : variable.ordinal();
        } else {
            return enumUseOrdinal ? variable.ordinal() : variable.name();
        }
    }

    protected Object doObjectToDbValue(Object variable) {
        return variable;
    }

    protected String doVariableToString(Object variable) {
        return TypeUtils.castToString(variable);
    }

    protected Object doVariableToBlob(Object variable) {
        return doObjectToDbValue(variable);
    }

    protected Object doVariableToClob(Object variable) {
        return doVariableToString(variable);
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

}
