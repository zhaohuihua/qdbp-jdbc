/**
 * 用于构造查询SQL时指定查询字段<br>
 * <pre>
 * AllFields allFields = DbTools.parseToAllFields(XxxBean.class);
 * AllFields allFields = DbTools.parseToAllFields(tableJoin);
 * Fields fields = allFields.include(fieldNames);
 * Fields fields = allFields.exclude(fieldNames);
 * </pre>
 *
 * @author zhaohuihua
 * @version 180503
 */
package com.gitee.qdbp.jdbc.fields;
