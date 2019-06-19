package com.gitee.qdbp.able.jdbc.fields;

import java.util.List;

/**
 * 导入型字段子集
 *
 * @author zhaohuihua
 * @version 180503
 */
public class IncludeFields extends BaseFields {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    public IncludeFields(String... fields) {
        super(fields);
    }

    public IncludeFields(List<String> fields) {
        super(fields);
    }
}
