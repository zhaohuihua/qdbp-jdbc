package com.gitee.qdbp.jdbc.plugins.impl;

import com.gitee.qdbp.jdbc.plugins.NameConverter;
import com.gitee.qdbp.jdbc.plugins.TableNameScans;

/**
 * 基础的表名扫描类
 *
 * @author zhaohuihua
 * @version 190727
 */
public abstract class BaseTableNameScans implements TableNameScans, NameConverter.Aware {

    /** 名称转换处理器 **/
    private NameConverter nameConverter;

    @Override
    public void setNameConverter(NameConverter nameConverter) {
        this.nameConverter = nameConverter;
    }

    public NameConverter getNameConverter() {
        return nameConverter;
    }
}
