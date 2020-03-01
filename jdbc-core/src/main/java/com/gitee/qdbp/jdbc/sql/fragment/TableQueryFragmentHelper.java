package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.gitee.qdbp.able.jdbc.base.DbCondition;
import com.gitee.qdbp.able.jdbc.base.WhereCondition;
import com.gitee.qdbp.able.jdbc.condition.DbField;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.SubWhere;
import com.gitee.qdbp.able.jdbc.model.DbFieldName;
import com.gitee.qdbp.able.jdbc.model.DbFieldValue;
import com.gitee.qdbp.able.jdbc.ordering.OrderType;
import com.gitee.qdbp.able.jdbc.ordering.Ordering;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldExeption;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.operator.DbBaseOperator;
import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.operator.DbMultivariateOperator;
import com.gitee.qdbp.jdbc.operator.DbTernaryOperator;
import com.gitee.qdbp.jdbc.operator.DbUnaryOperator;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.plugins.WhereSqlBuilder;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlTools;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 表查询SQL片段生成帮助类
 *
 * @author 赵卉华
 * @version 190601
 */
public abstract class TableQueryFragmentHelper implements QueryFragmentHelper {

    protected final List<? extends SimpleFieldColumn> columns;
    protected final SqlDialect dialect;

    /** 构造函数 **/
    public TableQueryFragmentHelper(List<? extends SimpleFieldColumn> columns, SqlDialect dialect) {
        VerifyTools.requireNotBlank(columns, "columns");
        this.columns = columns;
        this.dialect = dialect;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildWhereSql(DbWhere where, boolean whole) throws UnsupportedFieldExeption {
        if (where == null || where.isEmpty()) {
            return null;
        }

        String logicType = "AND";
        if (where instanceof SubWhere) {
            logicType = ((SubWhere) where).getLogicType();
        }

        SqlBuffer buffer = new SqlBuffer();
        List<String> unsupported = new ArrayList<String>();
        boolean first = true;
        Iterator<DbCondition> iterator = where.iterator();
        while (iterator.hasNext()) {
            DbCondition condition = iterator.next();
            if (condition.isEmpty()) {
                continue;
            }

            if (first) {
                first = false;
            } else {
                buffer.append(' ', logicType, ' ');
            }

            try {
                if (condition instanceof WhereCondition) {
                    WhereCondition subCondition = (WhereCondition) condition;
                    SqlBuffer subSql = buildWhereSql(subCondition, false);
                    if (!subSql.isEmpty()) {
                        buffer.append("( ").append(subSql).append(" )");
                    }
                } else if (condition instanceof SubWhere) {
                    SubWhere subWhere = (SubWhere) condition;
                    SqlBuffer subSql = buildWhereSql(subWhere, false);
                    if (!subSql.isEmpty()) {
                        buffer.append(subSql);
                    }
                } else if (condition instanceof DbField) {
                    DbField item = (DbField) condition;
                    SqlBuffer fieldSql = buildWhereSql(item, false);
                    buffer.append(fieldSql);
                } else {
                    unsupported.add("UnsupportedCondition:" + condition.getClass().getSimpleName());
                }
            } catch (UnsupportedFieldExeption e) {
                unsupported.addAll(e.getFields());
            }
        }
        if (unsupported.isEmpty()) {
            if (!buffer.isEmpty()) {
                if (where instanceof SubWhere) {
                    // 子SQL要用括号括起来
                    buffer.prepend("( ").append(" )");
                    if (!((SubWhere) where).isPositive()) {
                        buffer.prepend("NOT", ' ');
                    }
                }
                if (whole) {
                    buffer.prepend("WHERE", ' ');
                }
            }
            return buffer;
        } else {
            // 此处必须报错, 否则将可能由于疏忽大意导致严重的问题
            // 由于前面的判断都是基于where.isEmpty(), 逻辑是只要where不是空就必定会生成where语句
            // 如果不报错, 那么有可能因为字段名写错导致where条件为空
            // -- 例如delete操作where.on("userId", "=", "xxx");的字段名userId写成userIdd
            // -- 期望的语句是DELETE FROM tableName WHERE USER_ID='xxx'
            // -- 但根据userIdd找不到对应的column信息, 实际上生成的语句会是DELETE FROM tableName
            // -- 如果不报错, 在这个场景下将会导致表记录被全部删除!
            throw ufe("build where sql unsupported fields", unsupported);
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildWhereSql(DbField condition, boolean whole) throws UnsupportedFieldExeption {
        if (condition == null || condition.isEmpty()) {
            return null;
        }

        String operateType = VerifyTools.nvl(condition.getOperateType(), "Equals");
        String fieldName = condition.getFieldName();
        Object fieldValue = condition.getFieldValue();
        if (VerifyTools.isBlank(fieldName)) {
            throw ufe("build where sql", "fieldName:MustNotBe" + (fieldName == null ? "Null" : "Empty"));
        }
        String columnName;
        try {
            columnName = getColumnName(fieldName);
        } catch (UnsupportedFieldExeption e) {
            e.setMessage("build where sql unsupported field");
            throw e;
        }
        // 查找Where运算符处理类
        DbBaseOperator operator = DbTools.getWhereOperator(operateType);
        if (operator == null) {
            throw ufe("build where sql", "UnsupportedOperator:(" + fieldName + ' ' + operateType + " ...)");
        }
        // 由运算符处理类生成子SQL
        String desc = "build where sql unsupported fields";
        SqlBuffer buffer = buildOperatorSql(fieldName, columnName, operator, fieldValue, desc);

        if (whole && !buffer.isEmpty()) {
            buffer.prepend("WHERE", ' ');
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public <T extends WhereCondition> SqlBuffer buildWhereSql(T condition, boolean whole)
            throws UnsupportedFieldExeption {
        if (condition == null || condition.isEmpty()) {
            return null;
        }

        Class<? extends WhereCondition> type = condition.getClass();
        // JDK8+不用强转
        @SuppressWarnings("unchecked")
        WhereSqlBuilder<T> builder = (WhereSqlBuilder<T>) DbPluginContainer.defaults().getWhereSqlBuilder(type);
        if (builder == null) {
            throw ufe("build where sql", "SqlBuilderNotFound:" + condition.getClass().getSimpleName());
        }
        SqlBuffer buffer = builder.buildSql(condition, this);
        if (whole && !buffer.isEmpty()) {
            buffer.prepend("WHERE", ' ');
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildInSql(String fieldName, Collection<?> fieldValues, boolean whole)
            throws UnsupportedFieldExeption {
        String columnName;
        try {
            columnName = getColumnName(fieldName);
        } catch (UnsupportedFieldExeption e) {
            e.setMessage("build where in sql unsupported field");
            throw e;
        }
        SqlBuffer buffer = SqlTools.buildInSql(fieldValues);
        prependWhereAndColumn(buffer, columnName, whole);
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildNotInSql(String fieldName, Collection<?> fieldValues, boolean whole)
            throws UnsupportedFieldExeption {
        String columnName;
        try {
            columnName = getColumnName(fieldName);
        } catch (UnsupportedFieldExeption e) {
            e.setMessage("build where not in sql unsupported field");
            throw e;
        }
        SqlBuffer buffer = SqlTools.buildNotInSql(fieldValues);
        prependWhereAndColumn(buffer, columnName, whole);
        return buffer;
    }

    protected SqlBuffer buildOperatorSql(String fieldName, String columnName, DbBaseOperator operator,
            Object fieldValue, String desc) {
        String operatorName = operator.getName();

        if (operator instanceof DbUnaryOperator) {
            if (VerifyTools.isBlank(fieldValue)) {
                return ((DbUnaryOperator) operator).buildSql(columnName, dialect);
            } else {
                throw ufe(desc, "TooManyArguments:(" + fieldName + ' ' + operatorName + ")");
            }
        } else if (operator instanceof DbBinaryOperator) {
            Object value = convertFieldValue(fieldValue);
            return ((DbBinaryOperator) operator).buildSql(columnName, value, dialect);
        } else if (operator instanceof DbTernaryOperator) {
            List<Object> values = convertFieldValues(parseListFieldValue(fieldValue));
            if (values.size() == 2) {
                Object first = convertFieldValue(values.get(0));
                Object second = convertFieldValue(values.get(1));
                return ((DbTernaryOperator) operator).buildSql(columnName, first, second, dialect);
            } else if (values.size() < 2) { // 参数不够
                throw ufe(desc, "MissArguments:(" + fieldName + ' ' + operatorName + " arg1 arg2)");
            } else { // 参数过多
                throw ufe(desc, "TooManyArguments:(" + fieldName + ' ' + operatorName + " arg1 arg2)");
            }
        } else if (operator instanceof DbMultivariateOperator) {
            List<Object> values = convertFieldValues(parseListFieldValue(fieldValue));
            if (!values.isEmpty()) {
                return ((DbMultivariateOperator) operator).buildSql(columnName, values, dialect);
            } else { // 参数不能为空
                throw ufe(desc, "MissArguments:(" + fieldName + ' ' + operatorName + " ...)");
            }
        } else {
            throw ufe(desc, "UnsupportedOperator:(" + fieldName + ' ' + operatorName + " ...)");
        }
    }

    protected Object convertFieldValue(Object fieldValue) {
        if (fieldValue instanceof DbFieldName) {
            // 已指定是字段名, 按字段名处理
            DbFieldName temp = (DbFieldName) fieldValue;
            String fieldName = temp.getFieldName();
            String columnName = getColumnName(fieldName);
            return new DbFieldName(columnName);
        }
        if (fieldValue instanceof DbFieldValue) {
            // 已指定是DbFieldValue, 按字段值处理
            return ((DbFieldValue) fieldValue).getFieldValue();
        }
        if (fieldValue instanceof String && ((String) fieldValue).indexOf('.') > 0) {
            // 字符值是字符串并且是表别名.字段名格式, 如t.updateTime, 尝试作为字段名处理
            String columnName = getColumnName((String) fieldValue, false);
            if (columnName != null) {
                return new DbFieldName(columnName);
            }
        }
        return fieldValue;
    }

    protected List<Object> convertFieldValues(List<Object> fieldValues) {
        List<Object> result = new ArrayList<>();
        if (fieldValues != null) {
            for (Object fieldValue : fieldValues) {
                result.add(convertFieldValue(fieldValue));
            }
        }
        return result;
    }

    protected List<Object> parseListFieldValue(Object fieldValue) {
        if (fieldValue == null) {
            return Arrays.asList(fieldValue);
        } else if (fieldValue.getClass().isArray()) {
            return Arrays.asList((Object[]) fieldValue);
        } else if (fieldValue instanceof Collection) {
            return new ArrayList<Object>((Collection<?>) fieldValue);
        } else if (fieldValue instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) fieldValue;
            return new ArrayList<Object>(map.values());
        } else if (fieldValue instanceof Iterable) {
            List<Object> values = new ArrayList<Object>();
            Iterable<?> iterable = (Iterable<?>) fieldValue;
            for (Object temp : iterable) {
                values.add(temp);
            }
            return values;
        } else {
            return Arrays.asList(fieldValue);
        }
    }

    private static void prependWhereAndColumn(SqlBuffer buffer, String columnName, boolean whole) {
        if (!buffer.isEmpty()) {
            buffer.prepend(columnName);
            if (whole) {
                buffer.prepend("WHERE", ' ');
            }
        }
    }

    private static String PINYIN_SUFFIX = "(PINYIN)";

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildOrderBySql(List<Ordering> orderings, boolean whole) throws UnsupportedFieldExeption {
        if (VerifyTools.isBlank(orderings)) {
            return null;
        }
        SqlBuffer buffer = new SqlBuffer();
        List<String> unsupported = new ArrayList<String>();
        boolean first = true;
        for (Ordering item : orderings) {
            String fieldName = item.getOrderBy();
            // 汉字按拼音排序: userName(PINYIN)
            boolean usePinyin = false;
            if (fieldName.toUpperCase().endsWith(PINYIN_SUFFIX)) {
                usePinyin = true;
                fieldName = fieldName.substring(0, fieldName.length() - PINYIN_SUFFIX.length()).trim();
            }
            String columnName;
            try {
                columnName = getColumnName(fieldName);
            } catch (UnsupportedFieldExeption e) {
                unsupported.addAll(e.getFields());
                continue;
            }
            if (usePinyin) { // 根据数据库类型转换为拼音排序表达式
                columnName = dialect.toPinyinOrderByExpression(columnName);
            }
            if (first) {
                first = false;
            } else {
                buffer.append(',');
            }
            buffer.append(' ', columnName);
            OrderType orderType = item.getOrderType();
            if (orderType == OrderType.ASC) {
                buffer.append(' ', "ASC");
            } else if (orderType == OrderType.DESC) {
                buffer.append(' ', "DESC");
            }
        }
        if (!unsupported.isEmpty()) {
            throw ufe("build order by sql unsupported fields", unsupported);
        }
        if (whole && !buffer.isEmpty()) {
            buffer.prepend("ORDER BY", ' ');
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildSelectFieldsSql(String... fields) throws UnsupportedFieldExeption {
        return doChooseBuildFieldsSql(fields, true);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildSelectFieldsSql(Collection<String> fields) throws UnsupportedFieldExeption {
        return doBuildSpecialFieldsSql(fields, true);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildInsertFieldsSql(String... fields) throws UnsupportedFieldExeption {
        return doChooseBuildFieldsSql(fields, false);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildInsertFieldsSql(Collection<String> fields) throws UnsupportedFieldExeption {
        return doBuildSpecialFieldsSql(fields, false);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildByFieldsSql(String... fields) throws UnsupportedFieldExeption {
        return doChooseBuildFieldsSql(fields, false);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildByFieldsSql(Collection<String> fields) throws UnsupportedFieldExeption {
        return doBuildSpecialFieldsSql(fields, false);
    }

    protected SqlBuffer doChooseBuildFieldsSql(String[] fields, boolean columnAlias) {
        if (fields == null || fields.length == 0) {
            return doBuildAllFieldsSql(true);
        } else {
            Set<String> fieldList = ConvertTools.toSet(fields);
            return doBuildSpecialFieldsSql(fieldList, true);
        }
    }

    protected SqlBuffer doBuildAllFieldsSql(boolean columnAlias) {
        SqlBuffer buffer = new SqlBuffer();
        for (SimpleFieldColumn item : columns) {
            if (!buffer.isEmpty()) {
                buffer.append(',');
            }
            buffer.append(columnAlias ? item.toFullColumnName() : item.toTableColumnName());
        }
        return buffer;
    }

    protected SqlBuffer doBuildSpecialFieldsSql(Collection<String> fields, boolean columnAlias)
            throws UnsupportedFieldExeption {
        VerifyTools.requireNotBlank(fields, "fields");

        // 字段名映射
        Map<String, Void> fieldMap = new HashMap<String, Void>();
        List<String> unsupported = new ArrayList<String>();
        for (String fieldName : fields) {
            if (containsField(fieldName)) {
                fieldMap.put(fieldName, null);
            } else {
                unsupported.add(fieldName);
            }
        }
        if (!unsupported.isEmpty()) {
            throw ufe("build field sql unsupported fields", unsupported);
        }

        // 根据列顺序生成SQL
        SqlBuffer buffer = new SqlBuffer();
        for (SimpleFieldColumn item : columns) {
            if (fieldMap.containsKey(item.getFieldName())) {
                if (!buffer.isEmpty()) {
                    buffer.append(',');
                }
                buffer.append(columnAlias ? item.toFullColumnName() : item.toTableColumnName());
            }
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    public SqlBuffer buildFromSql() {
        return buildFromSql(true);
    }

    /** {@inheritDoc} **/
    @Override
    public boolean containsField(String fieldName) {
        if (VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(columns)) {
            return false;
        }
        for (SimpleFieldColumn item : this.columns) {
            if (item.matchesByFieldName(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} **/
    @Override
    public List<String> getFieldNames() {
        List<String> list = new ArrayList<>();
        for (SimpleFieldColumn item : columns) {
            list.add(item.toTableFieldName());
        }
        return list;
    }

    /** {@inheritDoc} **/
    @Override
    public List<String> getColumnNames() {
        List<String> list = new ArrayList<>();
        for (SimpleFieldColumn item : columns) {
            list.add(item.toTableColumnName());
        }
        return list;
    }

    /** {@inheritDoc} **/
    @Override
    public String getColumnName(String fieldName) throws UnsupportedFieldExeption {
        return getColumnName(fieldName, true);
    }

    /** {@inheritDoc} **/
    @Override
    public String getColumnName(String fieldName, boolean throwOnUnsupportedField) throws UnsupportedFieldExeption {
        for (SimpleFieldColumn item : this.columns) {
            if (item.matchesByFieldName(fieldName)) {
                return item.toTableColumnName();
            }
        }
        if (throwOnUnsupportedField) {
            throw ufe("unsupported field", fieldName);
        } else {
            return null;
        }
    }

    protected abstract UnsupportedFieldExeption ufe(String message, String field);

    protected abstract UnsupportedFieldExeption ufe(String message, List<String> fields);
}
