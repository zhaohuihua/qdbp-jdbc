package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.gitee.qdbp.able.jdbc.base.DbCondition;
import com.gitee.qdbp.able.jdbc.base.WhereCondition;
import com.gitee.qdbp.able.jdbc.condition.DbField;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.SubWhere;
import com.gitee.qdbp.able.jdbc.ordering.OrderType;
import com.gitee.qdbp.able.jdbc.ordering.Ordering;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldExeption;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.plugins.WhereSqlBuilder;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlTools;
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
        List<DbCondition> items = where.items();
        boolean first = true;
        for (DbCondition condition : items) {
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
                    unsupported.add(condition.getClass().getSimpleName() + "#UnsupportedCondition");
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
            // -- 例如delete操作where.on("userId", "=", "xxx");的字段名写成userIdd
            // -- 期望的语句是DELETE FROM tableName WHERE USER_ID='xxx'
            // -- 但根据userIdd找不到对应的column信息, 实际上生成的语句会是DELETE FROM tableName
            // -- 如果不报错, 在这个场景下将会导致表记录被全部删除!
            throw ufe("where sql", unsupported);
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
            throw ufe("where sql", "fieldName$" + operateType + "#IsBlank");
        }
        String columnName = getColumnName(fieldName);
        if (VerifyTools.isBlank(columnName)) {
            throw ufe("where sql", fieldName);
        }

        SqlBuffer buffer = new SqlBuffer();
        if ("In".equals(operateType) || "NotIn".equals(operateType) || "Between".equals(operateType)) {
            if (VerifyTools.isBlank(fieldValue)) {
                throw ufe("where sql", fieldName + '$' + operateType + '(' + fieldValue + "#IsBlank" + ')');
            }
            List<Object> values;
            if (fieldValue.getClass().isArray()) {
                values = Arrays.asList((Object[]) fieldValue);
            } else if (fieldValue instanceof Collection) {
                values = new ArrayList<Object>((Collection<?>) fieldValue);
            } else if (fieldValue instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) fieldValue;
                values = new ArrayList<Object>(map.values());
            } else if (fieldValue instanceof Iterable) {
                values = new ArrayList<Object>();
                Iterable<?> iterable = (Iterable<?>) fieldValue;
                for (Object temp : iterable) {
                    values.add(temp);
                }
            } else {
                values = Arrays.asList(fieldValue);
            }
            if ("Between".equals(operateType)) {
                if (values.size() < 2) {
                    throw ufe("where sql", fieldName + '$' + operateType + '(' + "#MissVars" + ')');
                }
                buffer.append(columnName);
                buffer.append(' ', "BETWEEN", ' ');
                buffer.addVariable(fieldName, values.get(0));
                buffer.append(' ', "AND", ' ');
                buffer.addVariable(fieldName, values.get(1));
            } else {
                if ("NotIn".equals(operateType)) {
                    buffer.append(buildNotInSql(fieldName, values, false));
                } else {
                    buffer.append(buildInSql(fieldName, values, false));
                }
            }
        } else if ("IsNull".equals(operateType)) {
            buffer.append(columnName).append(' ', "IS NULL");
        } else if ("IsNotNull".equals(operateType)) {
            buffer.append(columnName).append(' ', "IS NOT NULL");
        } else {
            if ("GreaterThen".equals(operateType)) {
                buffer.append(columnName).append(">").addVariable(fieldName, fieldValue);
            } else if ("GreaterEqualsThen".equals(operateType)) {
                buffer.append(columnName).append(">=").addVariable(fieldName, fieldValue);
            } else if ("LessThen".equals(operateType)) {
                buffer.append(columnName).append('<').addVariable(fieldName, fieldValue);
            } else if ("LessEqualsThen".equals(operateType)) {
                buffer.append(columnName).append("<=").addVariable(fieldName, fieldValue);
            } else if ("Starts".equals(operateType)) {
                buffer.append(columnName, ' ').append(dialect.buildStartsWithSql(fieldName, fieldValue));
            } else if ("Ends".equals(operateType)) {
                buffer.append(columnName, ' ').append(dialect.buildEndsWithSql(fieldName, fieldValue));
            } else if ("Like".equals(operateType)) {
                buffer.append(columnName, ' ').append(dialect.buildLikeSql(fieldName, fieldValue));
            } else if ("NotLike".equals(operateType)) {
                buffer.append(columnName, ' ').append("NOT", ' ').append(dialect.buildLikeSql(fieldName, fieldValue));
            } else if ("NotEquals".equals(operateType)) {
                buffer.append(columnName).append("!=").addVariable(fieldName, fieldValue);
            } else if ("Equals".equals(operateType)) {
                buffer.append(columnName).append('=').addVariable(fieldName, fieldValue);
            } else {
                throw ufe("where sql", fieldName + '$' + operateType + '(' + "#UnsupportedOperate" + ')');
            }
        }
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
        WhereSqlBuilder<T> builder = (WhereSqlBuilder<T>) DbPluginContainer.global.getWhereSqlBuilder(type);
        if (builder == null) {
            throw ufe("where sql", condition.getClass().getSimpleName() + "#SqlBuilderNotFound");
        }
        SqlBuffer buffer = builder.buildSql(condition, this);
        if (whole && !buffer.isEmpty()) {
            buffer.prepend("WHERE", ' ');
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildInSql(String fieldName, List<?> fieldValues, boolean whole) throws UnsupportedFieldExeption {
        String columnName = getColumnName(fieldName);
        if (VerifyTools.isBlank(columnName)) {
            throw ufe("where sql", fieldName);
        }
        SqlBuffer buffer = SqlTools.buildInSql(fieldName, fieldValues);
        prependWhereAndColumn(buffer, columnName, whole);
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildNotInSql(String fieldName, List<?> fieldValues, boolean whole)
            throws UnsupportedFieldExeption {
        String columnName = getColumnName(fieldName);
        if (VerifyTools.isBlank(columnName)) {
            throw ufe("where sql", fieldName);
        }
        SqlBuffer buffer = SqlTools.buildNotInSql(fieldName, fieldValues);
        prependWhereAndColumn(buffer, columnName, whole);
        return buffer;
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
            String columnName = getColumnName(fieldName);
            if (VerifyTools.isBlank(columnName)) {
                unsupported.add(fieldName);
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
            throw ufe("order by sql", unsupported);
        }
        if (whole && !buffer.isEmpty()) {
            buffer.prepend("ORDER BY", ' ');
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildFieldsSql() {
        SqlBuffer buffer = new SqlBuffer();
        for (SimpleFieldColumn item : columns) {
            if (!buffer.isEmpty()) {
                buffer.append(',');
            }
            buffer.append(toFullColumnName(item));
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildFieldsSql(String... fields) throws UnsupportedFieldExeption {
        if (fields == null || fields.length == 0) {
            return buildFieldsSql();
        } else {
            Set<String> fieldList = ConvertTools.toSet(fields);
            return buildFieldsSql(fieldList);
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildFieldsSql(Collection<String> fields) throws UnsupportedFieldExeption {
        if (VerifyTools.isBlank(fields)) {
            return buildFieldsSql();
        }

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
            throw ufe("field sql", unsupported);
        }

        // 根据列顺序生成SQL
        SqlBuffer buffer = new SqlBuffer();
        for (SimpleFieldColumn item : columns) {
            if (!fieldMap.containsKey(item.getFieldName())) {
                continue;
            }
            if (!buffer.isEmpty()) {
                buffer.append(',');
            }
            buffer.append(toFullColumnName(item));
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

    /** 返回带表别名的字段名 **/
    protected String toTableFieldName(SimpleFieldColumn field) {
        return field.toTableFieldName();
    }

    /** 返回带表别名的列名 **/
    protected String toTableColumnName(SimpleFieldColumn field) {
        return field.toTableColumnName();
    }

    /** 返回带表别名和列别名的完整列名 **/
    protected String toFullColumnName(SimpleFieldColumn field) {
        return field.toFullColumnName();
    }

    /** {@inheritDoc} **/
    @Override
    public List<String> getFieldNames() {
        List<String> list = new ArrayList<>();
        for (SimpleFieldColumn item : columns) {
            list.add(toTableFieldName(item));
        }
        return list;
    }

    /** {@inheritDoc} **/
    @Override
    public List<String> getColumnNames() {
        List<String> list = new ArrayList<>();
        for (SimpleFieldColumn item : columns) {
            list.add(toTableColumnName(item));
        }
        return list;
    }

    /** {@inheritDoc} **/
    @Override
    public String getColumnName(String fieldName) {
        return getColumnName(fieldName, false);
    }

    /** {@inheritDoc} **/
    @Override
    public String getColumnName(String fieldName, boolean throwOnNotFound) throws UnsupportedFieldExeption {
        for (SimpleFieldColumn item : this.columns) {
            if (item.matchesByFieldName(fieldName)) {
                return toTableColumnName(item);
            }
        }
        if (throwOnNotFound) {
            throw ufe("-", fieldName);
        } else {
            return null;
        }
    }

    protected abstract UnsupportedFieldExeption ufe(String subject, String field);

    protected abstract UnsupportedFieldExeption ufe(String subject, List<String> fields);
}
