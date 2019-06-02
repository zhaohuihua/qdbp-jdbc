package com.gitee.qdbp.jdbc.fields;

/**
 * 排除型字段子集
 *
 * @author zhaohuihua
 * @version 180503
 */
class ExcludeFields extends FilterFields {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    public ExcludeFields(AllFields all, String... fieldNames) {
        super(all, all.fields);
        if (fieldNames != null) {
            super.exclude(fieldNames);
        }
    }
}
