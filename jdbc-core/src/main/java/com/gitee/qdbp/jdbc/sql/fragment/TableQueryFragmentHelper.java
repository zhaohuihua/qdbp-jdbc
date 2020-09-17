package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.gitee.qdbp.able.exception.ServiceException;
import com.gitee.qdbp.able.jdbc.base.DbCondition;
import com.gitee.qdbp.able.jdbc.base.WhereCondition;
import com.gitee.qdbp.able.jdbc.condition.DbField;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.condition.SubWhere;
import com.gitee.qdbp.able.jdbc.fields.AllFields;
import com.gitee.qdbp.able.jdbc.fields.DistinctFields;
import com.gitee.qdbp.able.jdbc.fields.ExcludeFields;
import com.gitee.qdbp.able.jdbc.fields.Fields;
import com.gitee.qdbp.able.jdbc.fields.IncludeFields;
import com.gitee.qdbp.able.jdbc.model.DbFieldName;
import com.gitee.qdbp.able.jdbc.model.DbFieldValue;
import com.gitee.qdbp.able.jdbc.model.DbRawValue;
import com.gitee.qdbp.able.jdbc.ordering.OrderType;
import com.gitee.qdbp.able.jdbc.ordering.Ordering;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.able.result.ResultCode;
import com.gitee.qdbp.jdbc.exception.DbErrorCode;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldException;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.FieldColumns;
import com.gitee.qdbp.jdbc.model.FieldScene;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.operator.DbBaseOperator;
import com.gitee.qdbp.jdbc.operator.DbBinaryOperator;
import com.gitee.qdbp.jdbc.operator.DbMultivariateOperator;
import com.gitee.qdbp.jdbc.operator.DbTernaryOperator;
import com.gitee.qdbp.jdbc.operator.DbUnaryOperator;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.plugins.WhereSqlBuilder;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.sql.SqlTools;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 表查询SQL片段生成帮助类
 *
 * @author 赵卉华
 * @version 190601
 */
public abstract class TableQueryFragmentHelper implements QueryFragmentHelper {

    protected final AllFieldColumn<? extends SimpleFieldColumn> columns;
    protected final SqlDialect dialect;

    /** 构造函数 **/
    public TableQueryFragmentHelper(AllFieldColumn<? extends SimpleFieldColumn> columns, SqlDialect dialect) {
        VerifyTools.requireNotBlank(columns, "columns");
        this.columns = columns;
        this.dialect = dialect;
    }

    /**
     * 获取SQL方言处理类
     * 
     * @return SQL方言处理类
     */
    @Override
    public SqlDialect getSqlDialect() {
        return this.dialect;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildWhereSql(DbWhere where, boolean whole) throws UnsupportedFieldException {
        if (where == null || where.isEmpty()) {
            return null;
        }

        String logicType = "AND";
        if (where instanceof SubWhere) {
            logicType = ((SubWhere) where).getLogicType();
        }

        SqlBuilder buffer = new SqlBuilder();
        List<String> unsupported = new ArrayList<String>();
        boolean first = true;
        // 逐一遍历条件项, 生成where语句
        Iterator<DbCondition> iterator = where.iterator();
        while (iterator.hasNext()) {
            DbCondition condition = iterator.next();
            if (condition.isEmpty()) {
                continue;
            }

            if (first) {
                first = false;
            } else {
                buffer.ad(logicType);
            }

            try {
                if (condition instanceof WhereCondition) { // 自定义条件
                    WhereCondition subCondition = (WhereCondition) condition;
                    SqlBuffer subSql = buildWhereSql(subCondition, false);
                    if (!subSql.isEmpty()) {
                        buffer.ad("( ").ad(subSql).ad(" )");
                    }
                } else if (condition instanceof SubWhere) { // 子条件
                    SubWhere subWhere = (SubWhere) condition;
                    SqlBuffer subSql = buildWhereSql(subWhere, false);
                    if (!subSql.isEmpty()) {
                        buffer.ad(subSql);
                    }
                } else if (condition instanceof DbField) { // 字段条件
                    DbField item = (DbField) condition;
                    SqlBuffer fieldSql = buildWhereSql(item, false);
                    buffer.ad(fieldSql);
                } else {
                    unsupported.add("UnsupportedCondition:" + condition.getClass().getSimpleName());
                }
            } catch (UnsupportedFieldException e) {
                unsupported.addAll(e.getFields());
            }
        }

        if (!unsupported.isEmpty()) { // 存在异常字段
            // 此处必须报错, 否则将可能由于疏忽大意导致严重的问题
            // 由于前面的判断都是基于where.isEmpty(), 逻辑是只要where不是空就必定会生成where语句
            // 如果不报错, 那么有可能因为字段名写错导致where条件为空
            // -- 例如delete操作where.on("userId", "=", "xxx");的字段名userId写成userIdd
            // -- 期望的语句是DELETE FROM tableName WHERE USER_ID='xxx'
            // -- 但根据userIdd找不到对应的column信息, 实际上生成的语句会是DELETE FROM tableName
            // -- 如果不报错, 在这个场景下将会导致表记录被全部删除!
            throw ufe("build where sql unsupported fields", unsupported);
        }

        if (!buffer.isEmpty()) {
            // 后期处理, 如果不是肯定条件则前面加上NOT, 如果whole=true则在前面加上WHERE
            if (where instanceof SubWhere) {
                // 子SQL要用括号括起来
                buffer.pd("( ").ad(" )");
                if (!((SubWhere) where).isPositive()) {
                    buffer.pd("NOT");
                }
            }
            if (whole) {
                buffer.pd("WHERE");
            }
        }
        return buffer.out();
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildWhereSql(DbField condition, boolean whole) throws UnsupportedFieldException {
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
            columnName = getColumnName(FieldScene.CONDITION, fieldName);
        } catch (UnsupportedFieldException e) {
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
        SqlBuffer buffer = buildOperatorSql(fieldName, columnName, operateType, operator, fieldValue, desc);

        if (whole && !buffer.isEmpty()) {
            buffer.shortcut().pd("WHERE");
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public <T extends WhereCondition> SqlBuffer buildWhereSql(T condition, boolean whole)
            throws UnsupportedFieldException {
        if (condition == null || condition.isEmpty()) {
            return null;
        }

        WhereSqlBuilder<T> builder = DbTools.getWhereSqlBuilder(condition);
        if (builder == null) {
            throw ufe("build where sql", "SqlBuilderNotFound:" + condition.getClass().getSimpleName());
        }
        SqlBuffer buffer = builder.buildSql(condition, this);
        if (whole && !buffer.isEmpty()) {
            buffer.shortcut().pd("WHERE");
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildInSql(String fieldName, Collection<?> fieldValues, boolean whole)
            throws UnsupportedFieldException {
        String columnName;
        try {
            columnName = getColumnName(FieldScene.CONDITION, fieldName);
        } catch (UnsupportedFieldException e) {
            e.setMessage("build where in sql unsupported field");
            throw e;
        }
        SqlBuffer buffer = SqlTools.buildInSql(columnName, fieldValues, dialect);
        if (whole && !buffer.isEmpty()) {
            buffer.shortcut().pd("WHERE");
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildNotInSql(String fieldName, Collection<?> fieldValues, boolean whole)
            throws UnsupportedFieldException {
        String columnName;
        try {
            columnName = getColumnName(FieldScene.CONDITION, fieldName);
        } catch (UnsupportedFieldException e) {
            e.setMessage("build where not in sql unsupported field");
            throw e;
        }
        SqlBuffer buffer = SqlTools.buildNotInSql(columnName, fieldValues, dialect);
        if (whole && !buffer.isEmpty()) {
            buffer.shortcut().pd("WHERE");
        }
        return buffer;
    }

    protected SqlBuffer buildOperatorSql(String fieldName, String columnName, String operateType, Object fieldValue,
            String desc) {
        // 查找Where运算符处理类
        DbBaseOperator operator = DbTools.getWhereOperator(operateType);
        if (operator == null) {
            throw ufe("build where sql", "UnsupportedOperator:(" + fieldName + ' ' + operateType + " ...)");
        }
        return buildOperatorSql(fieldName, columnName, operateType, operator, fieldValue, desc);
    }

    protected SqlBuffer buildOperatorSql(String fieldName, String columnName, String operateType,
            DbBaseOperator operator, Object fieldValue, String desc) {
        if (operator instanceof DbUnaryOperator) {
            if (VerifyTools.isBlank(fieldValue)) {
                return ((DbUnaryOperator) operator).buildSql(columnName, dialect);
            } else {
                throw ufe(desc, "TooManyArguments:(" + fieldName + ' ' + operateType + ")");
            }
        } else if (operator instanceof DbBinaryOperator) {
            Object value = convertSpecialFieldValue(fieldValue);
            return ((DbBinaryOperator) operator).buildSql(columnName, value, dialect);
        } else if (operator instanceof DbTernaryOperator) {
            List<Object> values = convertSpecialFieldValues(ConvertTools.parseListIfNullToEmpty(fieldValue));
            if (values.size() == 2) {
                Object first = convertSpecialFieldValue(values.get(0));
                Object second = convertSpecialFieldValue(values.get(1));
                return ((DbTernaryOperator) operator).buildSql(columnName, first, second, dialect);
            } else if (values.size() < 2) { // 参数不够
                throw ufe(desc, "MissArguments:(" + fieldName + ' ' + operateType + " arg1 arg2)");
            } else { // 参数过多
                throw ufe(desc, "TooManyArguments:(" + fieldName + ' ' + operateType + " arg1 arg2)");
            }
        } else if (operator instanceof DbMultivariateOperator) {
            List<Object> values = convertSpecialFieldValues(ConvertTools.parseListIfNullToEmpty(fieldValue));
            if (!values.isEmpty()) {
                return ((DbMultivariateOperator) operator).buildSql(columnName, values, dialect);
            } else { // 参数不能为空
                throw ufe(desc, "MissArguments:(" + fieldName + ' ' + operateType + " ...)");
            }
        } else {
            throw ufe(desc, "UnsupportedOperator:(" + fieldName + ' ' + operateType + " ...)");
        }
    }

    /** {@inheritDoc} **/
    @Override
    public Object convertSpecialFieldValue(Object fieldValue) {
        // 这里是生成aColumnName=bColumnName表达式中bColumnName的值
        // 因此应该使用FieldScene.CONDITION, 只要有@Column注解, 就是字段名
        // 即使bColumnName的updatable=false, 只要aColumnName的updatable=true即可

        if (fieldValue instanceof DbFieldName) {
            // 已指定是字段名, 按字段名处理, 根据字段名转换为列名
            DbFieldName temp = (DbFieldName) fieldValue;
            String fieldName = temp.getFieldName();
            String columnName = getColumnName(FieldScene.CONDITION, fieldName);
            return new DbRawValue(columnName);
        }
        if (fieldValue instanceof DbFieldValue) {
            // 已指定是DbFieldValue, 按字段值处理; 防止作为下面的表别名.字段名解析
            return ((DbFieldValue) fieldValue).getFieldValue();
        }
        if (fieldValue instanceof String && ((String) fieldValue).indexOf('.') > 0) {
            // 字符值是字符串并且是表别名.字段名格式, 如t.updateTime, 尝试作为字段名处理
            String columnName = getColumnName(FieldScene.CONDITION, (String) fieldValue, false);
            if (columnName != null) {
                return new DbRawValue(columnName);
            }
        }
        return fieldValue;
    }

    /** {@inheritDoc} **/
    @Override
    public List<Object> convertSpecialFieldValues(List<Object> fieldValues) {
        List<Object> result = new ArrayList<>();
        if (fieldValues != null) {
            for (Object fieldValue : fieldValues) {
                result.add(convertSpecialFieldValue(fieldValue));
            }
        }
        return result;
    }

    private static String PINYIN_SUFFIX = "(PINYIN)";

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildOrderBySql(Orderings orderings, boolean whole) throws UnsupportedFieldException {
        if (VerifyTools.isBlank(orderings)) {
            return null;
        }
        SqlBuilder buffer = new SqlBuilder();
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
                columnName = getColumnName(FieldScene.CONDITION, fieldName);
            } catch (UnsupportedFieldException e) {
                unsupported.addAll(e.getFields());
                continue;
            }
            if (usePinyin) { // 根据数据库类型转换为拼音排序表达式
                columnName = dialect.toPinyinOrderByExpression(columnName);
            }
            if (first) {
                first = false;
            } else {
                buffer.ad(',');
            }
            buffer.ad(columnName);
            OrderType orderType = item.getOrderType();
            if (orderType == OrderType.ASC) {
                buffer.ad("ASC");
            } else if (orderType == OrderType.DESC) {
                buffer.ad("DESC");
            }
        }
        if (!unsupported.isEmpty()) {
            throw ufe("build order by sql unsupported fields", unsupported);
        }
        if (whole && !buffer.isEmpty()) {
            buffer.pd("ORDER BY");
        }
        return buffer.out();
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildSelectFieldsSql(String... fields) throws UnsupportedFieldException {
        // fields可以为空, 走doChooseBuildFieldsSql判断
        return doChooseBuildFieldsSql(FieldScene.CONDITION, fields, true);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildSelectFieldsSql(Collection<String> fields) throws UnsupportedFieldException {
        // fields不能为空, 直接调doBuildSpecialFieldsSql
        return doBuildSpecialFieldsSql(FieldScene.CONDITION, fields, true, true);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildSelectFieldsSql(Fields fields) throws UnsupportedFieldException {
        return doChooseBuildFieldsSql(FieldScene.INSERT, fields, true);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildInsertFieldsSql(String... fields) throws UnsupportedFieldException {
        return doChooseBuildFieldsSql(FieldScene.INSERT, fields, false);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildInsertFieldsSql(Collection<String> fields) throws UnsupportedFieldException {
        return doBuildSpecialFieldsSql(FieldScene.INSERT, fields, true, false);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildByFieldsSql(String... fields) throws UnsupportedFieldException {
        return doChooseBuildFieldsSql(FieldScene.CONDITION, fields, false);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildByFieldsSql(Collection<String> fields) throws UnsupportedFieldException {
        return doBuildSpecialFieldsSql(FieldScene.CONDITION, fields, true, false);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildByFieldsSql(Fields fields) throws UnsupportedFieldException {
        return doChooseBuildFieldsSql(FieldScene.CONDITION, fields, false);
    }

    protected SqlBuffer doChooseBuildFieldsSql(FieldScene scene, String[] fields, boolean columnAlias) {
        if (fields == null || fields.length == 0) {
            return doBuildAllFieldsSql(scene, columnAlias);
        } else {
            Set<String> fieldList = ConvertTools.toSet(fields);
            return doBuildSpecialFieldsSql(scene, fieldList, true, columnAlias);
        }
    }

    protected SqlBuffer doChooseBuildFieldsSql(FieldScene scene, Fields fields, boolean columnAlias) {
        if (fields == null || fields instanceof AllFields) {
            // 全部字段
            return doBuildDefFieldsSql(scene, columnAlias);
        } else if (fields instanceof ExcludeFields) {
            // 排除型字段列表
            List<String> items = fields.getItems();
            if (items == null || items.isEmpty()) {
                // 如果未排除任何字段, 就是全部字段
                return doBuildAllFieldsSql(scene, columnAlias);
            } else {
                // isWhitelist=false, 只生成不在列表中的字段
                return doBuildSpecialFieldsSql(scene, items, false, columnAlias);
            }
        } else if (fields instanceof IncludeFields) {
            // 导入型字段列表
            List<String> items = fields.getItems();
            if (items == null || items.isEmpty()) {
                // 未指定任何字段, 报错
                throw new ServiceException(DbErrorCode.DB_INCLUDE_FIELDS_IS_EMPTY);
            } else {
                // 以白名单方式生成在列表中指定的字段
                SqlBuffer buffer = doBuildSpecialFieldsSql(scene, items, true, columnAlias);
                // 如果是DistinctFields, SQL前面加上DISTINCT
                if (fields instanceof DistinctFields) {
                    buffer.shortcut().pd("DISTINCT");
                }
                return buffer;
            }
        } else {
            // 不支持的字段类型
            String msg = "UnsupportedFieldsType: " + fields.getClass().getSimpleName();
            throw new ServiceException(ResultCode.UNSUPPORTED_OPERATION, msg);
        }
    }

    /**
     * 生成默认字段列表<br>
     * 单表时与doBuildAllFieldsSql相同<br>
     * 表关联时, 默认只生成指定了resultField的表字段<br>
     * 例如此处查询角色下的用户, 只有SysUser指定了resultField<br>
     * Fields.ALL只会查询SysUser的字段, 而不会查询SysUserRole的字段<br>
     * <pre>
     * TableJoin tables = new TableJoin(SysUserRole.class, "ur")
     *     .innerJoin(SysUser.class, "u", "this")
     *     .on("u.id", "=", "ur.userId")
     *     .end();</pre>
     * 
     * @param scene 字段使用场景
     * @param columnAlias 是否使用表别名: SELECT后面的字段列表需要使用表别名, 其他情况不需要
     * @return 字段列表SQL片段
     */
    protected SqlBuffer doBuildDefFieldsSql(FieldScene scene, boolean columnAlias) {
        return doBuildAllFieldsSql(scene, columnAlias);
    }

    /**
     * 生成全部字段列表
     * 
     * @param scene 字段使用场景
     * @param columnAlias 是否使用表别名: SELECT后面的字段列表需要使用表别名, 其他情况不需要
     * @return 字段列表SQL片段
     */
    protected SqlBuffer doBuildAllFieldsSql(FieldScene scene, boolean columnAlias) {
        SqlBuilder buffer = new SqlBuilder();
        FieldColumns<? extends SimpleFieldColumn> fieldColumns = this.columns.filter(scene);
        for (SimpleFieldColumn item : fieldColumns) {
            if (!buffer.isEmpty()) {
                buffer.ad(',');
            }
            buffer.ad(columnAlias ? item.toFullColumnName() : item.toTableColumnName());
        }
        return buffer.out();
    }

    /**
     * 生成指定字段列表
     * 
     * @param scene 字段使用场景
     * @param fields 指定的字段名
     * @param isWhitelist 是白名单方式还是黑名单方式
     * @param columnAlias 是否使用表别名: SELECT后面的字段列表需要使用表别名, 其他情况不需要
     * @return 字段列表SQL片段
     * @throws UnsupportedFieldException 存在不支持的字段
     */
    protected SqlBuffer doBuildSpecialFieldsSql(FieldScene scene, Collection<String> fields, boolean isWhitelist,
            boolean columnAlias) throws UnsupportedFieldException {
        VerifyTools.requireNotBlank(fields, "fields");

        // 检查字段名
        checkSupportedFields(scene, fields, "build field sql");
        // 字段名映射表
        Map<String, ?> fieldMap = ConvertTools.toMap(fields);

        // 根据列顺序生成SQL
        SqlBuilder buffer = new SqlBuilder();
        FieldColumns<? extends SimpleFieldColumn> fieldColumns = this.columns.filter(scene);
        for (SimpleFieldColumn item : fieldColumns) {
            if (fieldMap.containsKey(item.getFieldName()) == isWhitelist) {
                if (!buffer.isEmpty()) {
                    buffer.ad(',');
                }
                buffer.ad(columnAlias ? item.toFullColumnName() : item.toTableColumnName());
            }
        }
        return buffer.out();
    }

    /** {@inheritDoc} **/
    public SqlBuffer buildFromSql() {
        return buildFromSql(true);
    }

    /** {@inheritDoc} **/
    @Override
    public void checkSupportedFields(FieldScene scene, Collection<String> fields, String desc) {
        // 不支持的字段名列表
        List<String> unsupported = new ArrayList<String>();
        FieldColumns<? extends SimpleFieldColumn> fieldColumns = this.columns.filter(scene);
        FieldColumns<? extends SimpleFieldColumn> conditionColumns = this.columns.filter(FieldScene.CONDITION);
        for (String fieldName : fields) {
            if (fieldColumns.containsByFieldName(fieldName)) {
                continue;
            }
            if (scene == FieldScene.INSERT) {
                if (conditionColumns.containsByFieldName(fieldName)) {
                    unsupported.add(fieldName + '#' + "NonInsertable");
                } else {
                    unsupported.add(fieldName);
                }
            } else if (scene == FieldScene.UPDATE) {
                if (conditionColumns.containsByFieldName(fieldName)) {
                    unsupported.add(fieldName + '#' + "NonUpdatable");
                } else {
                    unsupported.add(fieldName);
                }
            } else {
                unsupported.add(fieldName);
            }
        }
        if (!unsupported.isEmpty()) {
            String message = StringTools.concat(' ', desc, "unsupported fields");
            throw ufe(message, unsupported);
        }
    }

    /** {@inheritDoc} **/
    @Override
    public AllFieldColumn<? extends SimpleFieldColumn> getAllFieldColumns() {
        return this.columns;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean containsField(FieldScene scene, String fieldName) {
        if (VerifyTools.isBlank(fieldName)) {
            return false;
        }
        return this.columns.filter(scene).containsByFieldName(fieldName);
    }

    /** {@inheritDoc} **/
    @Override
    public List<String> getFieldNames(FieldScene scene) {
        return this.columns.filter(scene).getFieldNames();
    }

    /** {@inheritDoc} **/
    @Override
    public List<String> getColumnNames(FieldScene scene) {
        List<String> list = new ArrayList<>();
        FieldColumns<? extends SimpleFieldColumn> fieldColumns = this.columns.filter(scene);
        for (SimpleFieldColumn item : fieldColumns) {
            list.add(item.toTableColumnName());
        }
        return list;
    }

    /** {@inheritDoc} **/
    @Override
    public String getColumnName(FieldScene scene, String fieldName) throws UnsupportedFieldException {
        return getColumnName(scene, fieldName, true);
    }

    /** {@inheritDoc} **/
    @Override
    public String getColumnName(FieldScene scene, String fieldName, boolean throwOnUnsupportedField)
            throws UnsupportedFieldException {
        FieldColumns<? extends SimpleFieldColumn> fieldColumns = this.columns.filter(scene);
        for (SimpleFieldColumn item : fieldColumns) {
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

    /** 关于当前SQL片段帮助类的详情描述(用于异常信息问题定位) **/
    protected abstract String getOwnerDescString();

    protected final UnsupportedFieldException ufe(String message, String field) {
        return new UnsupportedFieldException(getOwnerDescString(), message, Arrays.asList(field));
    }

    protected final UnsupportedFieldException ufe(String message, List<String> fields) {
        return new UnsupportedFieldException(getOwnerDescString(), message, fields);
    }
}
