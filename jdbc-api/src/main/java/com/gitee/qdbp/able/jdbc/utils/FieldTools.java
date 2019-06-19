package com.gitee.qdbp.able.jdbc.utils;

import java.util.Iterator;
import java.util.List;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 字段工具类<br>
 * tableAlias可为空; fieldName不可带表别名; tableFieldName可带表别名, 如u.userName
 *
 * @author zhaohuihua
 * @version 190609
 */
public abstract class FieldTools {

    public static String toTableFieldName(String fieldName, String tableAlias) {
        StringBuilder buffer = new StringBuilder();
        // 表别名
        if (VerifyTools.isNotBlank(tableAlias)) {
            buffer.append(tableAlias.toLowerCase()).append('.');
        }
        // 字段名
        buffer.append(fieldName);
        return buffer.toString();
    }

    /**
     * 当前字段名与目标字段名是否匹配<br>
     * 如果当前字段名或目标字段名没有表别名, 只要字段名匹配即为匹配<br>
     * 如果当前字段名和目标字段名都有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param thisTableFieldName 当前字段名
     * @param thatTableFieldName 目标字段名
     * @return 是否匹配
     */
    public static boolean matches(String thisTableFieldName, String thatTableFieldName) {
        return FieldItem.parse(thisTableFieldName).matches(FieldItem.parse(thatTableFieldName));
    }

    /**
     * 判断字段名是否存在<br>
     * 判断是否匹配:<br>
     * 如果当前字段名或目标字段名没有表别名, 只要字段名匹配即为匹配<br>
     * 如果当前字段名和目标字段名都有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fields 字段列表
     * @param tableFieldName 目标字段名, 可带表别名, 如u.userName
     * @return 是否存在
     */
    public static boolean contains(List<String> fields, String tableFieldName) {
        VerifyTools.requireNotBlank(tableFieldName, "fieldName");
        // 遍历查找匹配项
        Iterator<String> itr = fields.iterator();
        while (itr.hasNext()) {
            String item = itr.next();
            if (matches(item, tableFieldName)) {
                return true;
            }
        }
        return false;
    }

    public static class FieldItem {

        private final String tableAlias;
        private final String fieldName;

        public FieldItem(String fieldName, String tableAlias) {
            this.tableAlias = tableAlias;
            this.fieldName = fieldName;
        }

        public String getTableAlias() {
            return tableAlias;
        }

        public String getFieldName() {
            return fieldName;
        }

        public boolean matches(FieldItem another) {
            return this.matches(another.getFieldName(), another.getTableAlias());
        }

        public boolean matches(String fieldName, String tableAlias) {
            if (VerifyTools.isBlank(this.tableAlias) || VerifyTools.isBlank(tableAlias)) {
                return this.fieldName.equals(fieldName);
            } else {
                return this.tableAlias.equalsIgnoreCase(tableAlias) && this.fieldName.equals(fieldName);
            }
        }

        public static FieldItem parse(String tableFieldName) {
            String tableAlias = null;
            String fieldName = tableFieldName;
            int dotIndex = tableFieldName.indexOf('.');
            if (dotIndex > 0) {
                tableAlias = tableFieldName.substring(0, dotIndex);
                fieldName = tableFieldName.substring(dotIndex + 1);
            } else if (dotIndex == 0) {
                fieldName = tableFieldName.substring(dotIndex + 1);
            }
            return new FieldItem(fieldName, tableAlias);
        }
    }
}
