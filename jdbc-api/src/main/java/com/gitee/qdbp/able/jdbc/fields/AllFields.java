package com.gitee.qdbp.able.jdbc.fields;

import java.io.Serializable;
import java.util.List;

/**
 * 全字段容器
 *
 * @author zhaohuihua
 * @version 180503
 */
public class AllFields implements Fields, Serializable {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    public AllFields() {
    }

    @Override
    public List<String> getItems() {
        return null;
    }
}
