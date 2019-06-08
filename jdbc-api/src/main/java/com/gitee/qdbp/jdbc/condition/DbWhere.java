package com.gitee.qdbp.jdbc.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.model.db.DbCondition;
import com.gitee.qdbp.able.model.db.WhereCondition;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 数据库Where条件容器<br>
 * <pre>
    DbWhere where = new DbWhere();
    // [SQL] AND ID = :$1$Id
    where.on("id", "=", entity.getId());
    // [SQL] AND ID != :$1$Id
    where.on("id", "!=", entity.getId());
    // [SQL] AND CREATE_TIME > :$1$CreateTime
    where.on("createTime", ">", new Date());
    // [SQL] AND CREATE_TIME >= :$1$CreateTime
    where.on("createTime", ">=", new Date());
    // [SQL] AND CREATE_TIME < :$1$CreateTime
    where.on("createTime", "<", new Date());
    // [SQL] AND CREATE_TIME <= :$1$CreateTime
    where.on("createTime", "<=", new Date());
    // [SQL] AND USER_STATE IS NULL
    where.on("userState", "is null");
    // [SQL] AND USER_STATE IS NOT NULL
    where.on("userState", "is not null");
    // [ORACLE/DB2] AND USER_NAME LIKE '%'||:$1$UserName||'%'
    // [MYSQL] AND USER_NAME LIKE CONCAT('%',:$1$UserName,'%')
    where.on("userName", "like", entity.getUserName());
    // [ORACLE/DB2] AND USER_NAME NOT LIKE '%'||:$1$UserName||'%'
    // [MYSQL] AND USER_NAME NOT LIKE CONCAT('%',:$1$UserName,'%')
    where.on("userName", "not like", entity.getUserName());
    // [ORACLE/DB2] AND PHONE LIKE :$1$Phone||'%'
    // [MYSQL] AND PHONE LIKE CONCAT(:$1$Phone,'%')
    where.on("phone", "starts", "139");
    // [ORACLE/DB2] AND PHONE LIKE '%'||:$1$Phone
    // [MYSQL] AND PHONE LIKE CONCAT('%',:$1$Phone)
    where.on("phone", "ends", "8888");
    // [SQL] AND EFTFLAG IN (:$1$Eftflag, :$2$Eftflag, ...)
    where.on("eftflag", "in", 'E', 'N', ...);
    // [SQL] AND EFTFLAG NOT IN (:$1$Eftflag, :$2$Eftflag, ...)
    where.on("eftflag", "not in", 'E', 'N', ...);
    // [SQL] AND CREATE_TIME BETWEEN :$1$CreateTime AND :$2$CreateTime
    where.on("createTime", "between", entity.getStartTime(), entity.getEndTime());
    // [SQL] AND ( USER_NAME LIKE '%'||:$1$UserName||'%' OR REAL_NAME LIKE '%'||:$2$realName||'%' OR ... )
    where.sub("or") // 子条件
        .on("userName", "like", entity.getKeyword())
        .on("realName", "like", entity.getKeyword())
        .on(...);
 * </pre>
 *
 * @author zhaohuihua
 * @version 181221
 */
public class DbWhere extends DbItems {

    /** SerialVersionUID **/
    private static final long serialVersionUID = 1L;

    /** 没有查询条件的空Where **/
    public static final DbWhere NONE = new EmptyDbWhere();

    /**
     * Where条件<br>
     * 字段名可以带表别名, 如where.on("u.id", "=", entity.getId());<br>
     * 
     * @param fieldName 字段名称
     * @param operate 目前支持如下操作:<br>
     *            =, !=, &lt;, &lt;=, &gt;, &gt;=, <br>
     *            Equals(equals), NotEquals(not equals), <br>
     *            LessThen(less then), LessEqualsThen(less equals then), <br>
     *            GreaterThen(greater then), GreaterEqualsThen(greater equals then), <br>
     *            IsNull(is null), IsNotNull(is not null), <br>
     *            Like(like), NotLike(not like), Starts(starts), Ends(ends), <br>
     *            In(in), NotIn(not in), Between(between)
     * @param fieldValues 字段值
     * @return 返回容器自身, 用于链式操作
     */
    public DbWhere on(String fieldName, String operate, Object... fieldValues) {
        if (isMatches(operate, "IsNull", "is null", "IsNotNull", "is not null")) {
            Object first = fieldValues == null || fieldValues.length == 0 ? null : fieldValues[0];
            if (isMatches(operate, "IsNull", "is null")) {
                if (isPositive(first)) {
                    this.onIsNull(fieldName);
                } else {
                    this.onIsNotNull(fieldName);
                }
            } else if (isMatches(operate, "IsNotNull", "is not null")) {
                if (isPositive(first)) {
                    this.onIsNotNull(fieldName);
                } else {
                    this.onIsNull(fieldName);
                }
            }
        } else if ("Between".equalsIgnoreCase(operate)) {
            List<Object> arrayValues = parseArrayValue(fieldValues);
            // 必须有两个参数
            if (VerifyTools.isBlank(arrayValues)) {
                String msg = "FieldValues is required, fieldName is %s, operate is %s";
                throw new IllegalArgumentException(String.format(msg, fieldName, operate));
            }
            if (arrayValues.size() < 2) {
                String msg = "FieldValues.size() can't be less then 2, fieldName is %s, operate is %s";
                throw new IllegalArgumentException(String.format(msg, fieldName, operate));
            }
            this.onBetween(fieldName, arrayValues);
        } else if (isMatches(operate, "In", "NotIn", "not in", "Between")) {
            // 至少要有一个参数
            if (VerifyTools.isBlank(fieldValues)) {
                String msg = "FieldValues is required, fieldName is %s, operate is %s";
                throw new IllegalArgumentException(String.format(msg, fieldName, operate));
            }
            List<Object> arrayValues = parseArrayValue(fieldValues);
            if ("In".equalsIgnoreCase(operate)) {
                this.onIn(fieldName, arrayValues);
            } else if (isMatches(operate, "NotIn", "not in")) {
                this.onNotIn(fieldName, arrayValues);
            }
        } else {
            // 必须要有一个参数
            Object first = fieldValues == null || fieldValues.length == 0 ? null : fieldValues[0];
            if (VerifyTools.isBlank(first)) {
                String msg = "FieldValue is required, fieldName is %s, operate is %s";
                throw new IllegalArgumentException(String.format(msg, fieldName, operate));
            }
            if (isMatches(operate, ">", "GreaterThen", "greater then")) {
                this.onGreaterThen(fieldName, first);
            } else if (isMatches(operate, "<", "LessThen", "less then")) {
                this.onLessThen(fieldName, first);
            } else if (isMatches(operate, ">=", "GreaterEqualsThen", "greater equals then")) {
                this.onGreaterEqualsThen(fieldName, first);
            } else if (isMatches(operate, "<=", "LessEqualsThen", "less equals then")) {
                this.onLessEqualsThen(fieldName, first);
            } else if (isMatches(operate, "=", "Equals")) {
                this.onEquals(fieldName, first);
            } else if (isMatches(operate, "!=", "<>", "NotEquals", "not equals")) {
                this.onNotEquals(fieldName, first);
            } else if ("Like".equalsIgnoreCase(operate)) {
                this.onLike(fieldName, first);
            } else if (isMatches(operate, "NotLike", "not like")) {
                this.onNotLike(fieldName, first);
            } else if ("Starts".equalsIgnoreCase(operate)) {
                this.onStarts(fieldName, first);
            } else if ("Ends".equalsIgnoreCase(operate)) {
                this.onEnds(fieldName, first);
            } else {
                String msg = "Unsupported operate, fieldName is %s, operate is %s, fieldValue is %s";
                throw new IllegalArgumentException(String.format(msg, fieldName, operate, first));
            }
        }
        return this;
    }

    /** 增加自定义条件 **/
    public DbWhere on(WhereCondition condition) {
        super.put(condition);
        return this;
    }

    private static boolean isMatches(String string, String... expectStrings) {
        for (String expect : expectStrings) {
            if (expect.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPositive(Object value) {
        if (value == null) {
            return true;
        } else if (value.getClass() == boolean.class || value.getClass() == Boolean.class) {
            return !Boolean.FALSE.equals(value);
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        } else if (value instanceof String) {
            return StringTools.isPositive((String) value, true);
        } else {
            String msg = "FieldValue format error, can't convert to boolean, value=%s";
            throw new IllegalArgumentException(String.format(msg, value));
        }
    }

    private static List<Object> parseArrayValue(Object... values) {
        if (values == null) {
            return null;
        }
        if (values.length == 0) {
            return new ArrayList<Object>();
        }
        Object value = values.length == 1 ? values[0] : values;
        List<Object> list;
        if (value.getClass().isArray()) {
            list = Arrays.asList((Object[]) value);
        } else if (value instanceof Collection) {
            list = new ArrayList<Object>((Collection<?>) value);
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            list = new ArrayList<Object>(map.values());
        } else if (value instanceof Iterable) {
            list = new ArrayList<Object>();
            Iterable<?> iterable = (Iterable<?>) value;
            for (Object temp : iterable) {
                list.add(temp);
            }
        } else {
            list = Arrays.asList(value);
        }
        return list;
    }

    /** WHERE :fieldName = :fieldValue **/
    protected DbWhere onEquals(String fieldName, Object fieldValue) {
        this.put("Equals", fieldName, fieldValue);
        return this;
    }

    /** WHERE :fieldName != :fieldValue **/
    protected DbWhere onNotEquals(String fieldName, Object fieldValue) {
        this.put("NotEquals", fieldName, fieldValue);
        return this;
    }

    /** WHERE :fieldName &gt; :fieldValue **/
    protected DbWhere onGreaterThen(String fieldName, Object fieldValue) {
        this.put("GreaterThen", fieldName, fieldValue);
        return this;
    }

    /** WHERE :fieldName &lt; :fieldValue **/
    protected DbWhere onLessThen(String fieldName, Object fieldValue) {
        this.put("LessThen", fieldName, fieldValue);
        return this;
    }

    /** WHERE :fieldName &gt;= :fieldValue **/
    protected DbWhere onGreaterEqualsThen(String fieldName, Object fieldValue) {
        this.put("GreaterEqualsThen", fieldName, fieldValue);
        return this;
    }

    /** WHERE :fieldName &lt;= :fieldValue **/
    protected DbWhere onLessEqualsThen(String fieldName, Object fieldValue) {
        this.put("LessEqualsThen", fieldName, fieldValue);
        return this;
    }

    /** WHERE :fieldName IS NULL **/
    protected DbWhere onIsNull(String fieldName) {
        this.put("IsNull", fieldName, null);
        return this;
    }

    /** WHERE :fieldName IS NOT NULL **/
    protected DbWhere onIsNotNull(String fieldName) {
        this.put("IsNotNull", fieldName, null);
        return this;
    }

    /** WHERE :fieldName LIKE CONCAT('%', :fieldValue, '%') **/
    protected DbWhere onLike(String fieldName, Object fieldValue) {
        this.put("Like", fieldName, fieldValue);
        return this;
    }

    /** WHERE :fieldName NOT LIKE CONCAT('%', :fieldValue, '%') **/
    protected DbWhere onNotLike(String fieldName, Object fieldValue) {
        this.put("NotLike", fieldName, fieldValue);
        return this;
    }

    /** WHERE :fieldName LIKE CONCAT(:fieldValue, '%') **/
    protected DbWhere onStarts(String fieldName, Object fieldValue) {
        this.put("Starts", fieldName, fieldValue);
        return this;
    }

    /** WHERE :fieldName LIKE CONCAT('%', :fieldValue) **/
    protected DbWhere onEnds(String fieldName, Object fieldValue) {
        this.put("Ends", fieldName, fieldValue);
        return this;
    }

    // /** WHERE :fieldName NOT IN (:fieldValue1, :fieldValue2, ...) **/
    // protected DbWhere onNotIn(String fieldName, Object... fieldValues) {
    //     this.put("NotIn", fieldName, fieldValues);
    //     return this;
    // }

    /** WHERE :fieldName IN (:fieldValue1, :fieldValue2, ...) **/
    protected DbWhere onNotIn(String fieldName, List<?> fieldValues) {
        this.put("NotIn", fieldName, fieldValues);
        return this;
    }

    // /** WHERE :fieldName IN (:fieldValue1, :fieldValue2, ...) **/
    // protected DbWhere onIn(String fieldName, Object... fieldValues) {
    //     this.put("In", fieldName, fieldValues);
    //     return this;
    // }

    /** WHERE :fieldName IN (:fieldValue1, :fieldValue2, ...) **/
    protected DbWhere onIn(String fieldName, List<?> fieldValues) {
        this.put("In", fieldName, fieldValues);
        return this;
    }

    // /** WHERE :fieldName BETWEEN :minValue AND :maxValue **/
    // protected DbWhere onBetween(String fieldName, Object... fieldValues) {
    //     this.put("Between", fieldName, fieldValues);
    //     return this;
    // }

    /** WHERE :fieldName BETWEEN :minValue AND :maxValue **/
    protected DbWhere onBetween(String fieldName, List<Object> fieldValues) {
        this.put("Between", fieldName, fieldValues);
        return this;
    }

    // /** WHERE :fieldName BETWEEN :minValue AND :maxValue **/
    // protected DbWhere onBetween(String fieldName, Object minValue, Object maxValue) {
    //     this.put("Between", fieldName, new Object[] { minValue, maxValue });
    //     return this;
    // }
    //
    // /** WHERE :fieldName BETWEEN :minValue AND :maxValue **/
    // protected DbWhere onBetween(String fieldName, Date minValue, Date maxValue) {
    //     this.put("Between", fieldName, new Date[] { minValue, maxValue });
    //     return this;
    // }

    /** 创建子查询条件 **/
    public SubWhere sub(String operateType) {
        return this.sub(operateType, true);
    }

    /** 创建子查询条件 **/
    public SubWhere sub(String logicType, boolean positive) {
        SubWhere sub = new SubWhere(this, logicType, positive);
        this.put(sub);
        return sub;
    }

    /**
     * 从map中获取参数构建对象
     * 
     * @param map Map参数
     * @return 对象实例
     */
    public static DbWhere from(Map<String, Object> map) {
        return from(map, DbWhere.class);
    }

    /**
     * 空的查询条件
     *
     * @author zhaohuihua
     * @version 190310
     */
    public static class EmptyDbWhere extends DbWhere {

        /** serialVersionUID **/
        private static final long serialVersionUID = 1L;

        private EmptyDbWhere() {
        }

        protected void put(String fieldName, Object fieldValue) {
            throw new UnsupportedOperationException("EmptyDbWhere");
        }

        protected void put(String operateType, String fieldName, Object fieldValue) {
            throw new UnsupportedOperationException("EmptyDbWhere");
        }

        protected void put(DbFields fields) {
            throw new UnsupportedOperationException("EmptyDbWhere");
        }

        protected void put(DbCondition condition) {
            throw new UnsupportedOperationException("EmptyDbWhere");
        }

    }
}
