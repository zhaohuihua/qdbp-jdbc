package com.gitee.qdbp.jdbc.utils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.springframework.jdbc.core.SqlParameterValue;
import com.gitee.qdbp.able.jdbc.base.UpdateCondition;
import com.gitee.qdbp.able.jdbc.base.WhereCondition;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.JoinItem;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.TableItem;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.model.TablesFieldColumn;
import com.gitee.qdbp.jdbc.model.TypedDbVariable;
import com.gitee.qdbp.jdbc.operator.DbBaseOperator;
import com.gitee.qdbp.jdbc.plugins.BatchInsertExecutor;
import com.gitee.qdbp.jdbc.plugins.BatchUpdateExecutor;
import com.gitee.qdbp.jdbc.plugins.DbConditionConverter;
import com.gitee.qdbp.jdbc.plugins.DbOperatorContainer;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.DbVersionFinder;
import com.gitee.qdbp.jdbc.plugins.EntityDataStateFillStrategy;
import com.gitee.qdbp.jdbc.plugins.EntityFieldFillStrategy;
import com.gitee.qdbp.jdbc.plugins.MapToBeanConverter;
import com.gitee.qdbp.jdbc.plugins.RawValueConverter;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.plugins.SqlFormatter;
import com.gitee.qdbp.jdbc.plugins.TableInfoScans;
import com.gitee.qdbp.jdbc.plugins.UpdateSqlBuilder;
import com.gitee.qdbp.jdbc.plugins.VariableToDbValueConverter;
import com.gitee.qdbp.jdbc.plugins.WhereSqlBuilder;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleSqlDialect;
import com.gitee.qdbp.jdbc.sql.mapper.SqlParser;
import com.gitee.qdbp.tools.utils.Config;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 数据库公共工具类
 *
 * @author 赵卉华
 * @version 190601
 */
public abstract class DbTools {

    /**
     * 将变量转换为数据库字段值<br>
     * 基本对象不做转换, 直接返回, 其他对象转换为基本对象后返回<br>
     * 基本对象是指Boolean/Character/Date/Number/String
     * 
     * @param variable 变量
     * @return 转换后的字段值对象
     */
    public static Object variableToDbValue(Object variable, SqlDialect dialect) {
        VariableToDbValueConverter helper = DbPluginContainer.defaults().getToDbValueConverter();
        Object result = helper.convert(variable);
        if (result instanceof TypedDbVariable) {
            TypedDbVariable temp = (TypedDbVariable) result;
            return new SqlParameterValue(temp.getSqlType(), temp.getValue());
        } else if (result instanceof Character) {
            // Character类型不能自动识别
            // @see org.springframework.jdbc.core.StatementCreatorUtils.setValue
            // 调用的是PreparedStatement.setObject(int index, Object x)
            // 而不是PreparedStatement.setObject(int index, Object x, int Types.xxx)
            return new SqlParameterValue(Types.VARCHAR, variable);
        } else {
            return result;
        }
    }

    /**
     * 将变量转换为字符串, 用于拼接SQL<br>
     * 如字符串会返回单引号括起来的'stringValue', Boolean会返回1或0<br>
     * 日期: MYSQL: '2019-06-01 12:34:56.789'<br>
     * ORACLE: TO_TIMESTAMP('2019-06-01 12:34:56.789', 'YYYY-MM-DD HH24:MI:SS.FF')
     * 
     * @param variable 变量
     * @param dialect 数据库方言
     * @return 转换后的字符串
     */
    public static String variableToString(Object variable, SqlDialect dialect) {
        VariableToDbValueConverter converter = DbPluginContainer.defaults().getToDbValueConverter();
        Object result = converter.convert(variable);
        if (result instanceof TypedDbVariable) {
            TypedDbVariable temp = (TypedDbVariable) result;
            result = temp.getValue();
        } else if (result instanceof SqlParameterValue) {
            SqlParameterValue temp = (SqlParameterValue) result;
            result = temp.getValue();
        }
        if (result == null) {
            return "NULL";
        } else if (result instanceof Number) {
            return result.toString();
        } else if (result instanceof CharSequence) {
            return dialect.variableToString(result.toString());
        } else if (result instanceof Boolean) {
            return dialect.variableToString((Boolean) result);
        } else if (result instanceof Date) {
            return dialect.variableToString((Date) result);
        } else if (result instanceof Character) {
            return dialect.variableToString(result.toString());
        } else {
            return dialect.variableToString(result.toString());
        }
    }

    /**
     * 转换原始关键字, 如sysdate/CURRENT_TIMESTAMP之前的互转
     * 
     * @param value 原生值, 如sysdate
     * @param dialect 数据库方言
     * @return 转换后的值, 如mysql的CURRENT_TIMESTAMP, SqlServer的GETDATE()
     * @see RawValueConverter
     */
    public static String resolveRawValue(String rawValue, SqlDialect dialect) {
        RawValueConverter converter = DbPluginContainer.defaults().getRawValueConverter();
        return converter.convert(rawValue, dialect);
    }

    /** 获取Map到JavaBean的转换处理类 **/
    public static MapToBeanConverter getMapToBeanConverter() {
        return DbPluginContainer.defaults().getMapToBeanConverter();
    }

    /** 获取JavaBean到数据库条件的转换处理类 **/
    public static DbConditionConverter getDbConditionConverter() {
        return DbPluginContainer.defaults().getDbConditionConverter();
    }

    /** 实体类逻辑删除数据状态填充策略 **/
    public static EntityFieldFillStrategy getEntityFieldFillStrategy() {
        return DbPluginContainer.defaults().getEntityFieldFillStrategy();
    }

    /** 实体类逻辑删除数据状态填充策略 **/
    public static EntityDataStateFillStrategy<?> getEntityDataStateFillStrategy() {
        return DbPluginContainer.defaults().getEntityDataStateFillStrategy();
    }

    /** 根据DbType生成SqlDialect **/
    public static SqlDialect buildSqlDialect(DbType dbType) {
        return buildSqlDialect(new DbVersion(dbType));
    }

    /** 根据DbVersion生成SqlDialect **/
    public static SqlDialect buildSqlDialect(DbVersion version) {
        return new SimpleSqlDialect(version);
    }

    /** 根据SqlDialect生成SqlParser **/
    public static SqlParser buildSqlParser(SqlDialect dialect) {
        return new SqlParser(dialect);
    }

    /** 获取自定义WhereSqlBuilder **/
    public static <T extends WhereCondition, B extends WhereSqlBuilder<T>> B getWhereSqlBuilder(Class<T> type) {
        return DbPluginContainer.defaults().getWhereSqlBuilder(type);
    }

    /** 获取自定义WhereSqlBuilder **/
    public static <T extends WhereCondition, B extends WhereSqlBuilder<T>> B getWhereSqlBuilder(T condition) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) condition.getClass();
        return DbPluginContainer.defaults().getWhereSqlBuilder(type);
    }

    /** 获取自定义UpdateSqlBuilder **/
    public static <T extends UpdateCondition, B extends UpdateSqlBuilder<T>> B getUpdateSqlBuilder(Class<T> type) {
        return DbPluginContainer.defaults().getUpdateSqlBuilder(type);
    }

    /** 获取自定义UpdateSqlBuilder **/
    public static <T extends UpdateCondition, B extends UpdateSqlBuilder<T>> B getUpdateSqlBuilder(T condition) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) condition.getClass();
        return DbPluginContainer.defaults().getUpdateSqlBuilder(type);
    }

    /**
     * 根据运算符获取WhereOperator处理类
     * 
     * @param operatorType 运算符
     * @return WhereOperator处理类
     */
    public static DbBaseOperator getWhereOperator(String operatorType) {
        DbOperatorContainer container = DbPluginContainer.defaults().getOperatorContainer();
        return container == null ? null : container.getWhereOperator(operatorType);
    }

    /**
     * 根据运算符获取UpdateOperator处理类
     * 
     * @param operatorType 运算符
     * @return UpdateOperator处理类
     */
    public static DbBaseOperator getUpdateOperator(String operatorType) {
        DbOperatorContainer container = DbPluginContainer.defaults().getOperatorContainer();
        return container == null ? null : container.getUpdateOperator(operatorType);
    }

    /**
     * 格式化SQL语句
     * 
     * @param sql 待格式化的SQL语句
     * @param indent 缩进层数
     * @return 已格式化的SQL语句
     */
    public static String formatSql(String sql, int indent) {
        SqlFormatter formatter = DbPluginContainer.defaults().getSqlFormatter();
        return formatter.format(sql, indent);
    }

    /**
     * 查找数据库版本信息
     * 
     * @param datasource 数据源
     * @return 数据库版本信息
     */
    public static DbVersion findDbVersion(DataSource datasource) {
        DbVersionFinder finder = DbPluginContainer.defaults().getDbVersionFinder();
        return finder.findDbVersion(datasource);
    }
    
    /** 获取数据库配置选项 **/
    public static Config getDbConfig() {
        return DbPluginContainer.defaults().getDbConfig(true);
    }

    /** 根据数据库类型获取批量新增处理类 **/
    public static BatchInsertExecutor getBatchInsertExecutor(DbVersion version) {
        DbPluginContainer plugins = DbPluginContainer.defaults();
        List<BatchInsertExecutor> batchOperateExecutors = plugins.getBatchInsertExecutors();
        if (batchOperateExecutors != null && !batchOperateExecutors.isEmpty()) {
            for (BatchInsertExecutor item : batchOperateExecutors) {
                if (item.supports(version)) {
                    return item;
                }
            }
        }
        return plugins.getDefaultBatchInsertExecutor();
    }

    /** 根据数据库类型获取批量更新处理类 **/
    public static BatchUpdateExecutor getBatchUpdateExecutor(DbVersion version) {
        DbPluginContainer plugins = DbPluginContainer.defaults();
        List<BatchUpdateExecutor> batchOperateExecutors = plugins.getBatchUpdateExecutors();
        if (batchOperateExecutors != null && !batchOperateExecutors.isEmpty()) {
            for (BatchUpdateExecutor item : batchOperateExecutors) {
                if (item.supports(version)) {
                    return item;
                }
            }
        }
        return plugins.getDefaultBatchUpdateExecutor();
    }

    /** Entity的表名缓存 **/
    private static Map<Class<?>, String> entityTableNameCache = new ConcurrentHashMap<>();

    /**
     * 扫描表名信息(有缓存)
     * 
     * @param clazz 类名
     * @return 表名
     */
    public static String parseTableName(Class<?> clazz) {
        VerifyTools.requireNonNull(clazz, "class");
        if (entityTableNameCache.containsKey(clazz)) {
            return entityTableNameCache.get(clazz);
        }

        TableInfoScans scans = DbPluginContainer.defaults().getTableInfoScans();
        String tableName = scans.scanTableName(clazz);
        entityTableNameCache.put(clazz, tableName);
        return tableName;
    }

    /** Entity的主键缓存 **/
    private static Map<Class<?>, PrimaryKeyFieldColumn> entityPrimaryKeyCache = new ConcurrentHashMap<>();

    /**
     * 扫描获取主键(有缓存)
     * 
     * @param clazz 类名
     * @return 主键
     */
    public static PrimaryKeyFieldColumn parsePrimaryKey(Class<?> clazz) {
        VerifyTools.requireNonNull(clazz, "class");
        if (entityPrimaryKeyCache.containsKey(clazz)) {
            return entityPrimaryKeyCache.get(clazz);
        }

        TableInfoScans scans = DbPluginContainer.defaults().getTableInfoScans();
        PrimaryKeyFieldColumn pk = scans.scanPrimaryKey(clazz);
        entityPrimaryKeyCache.put(clazz, pk);
        return pk;
    }

    /** TableJoin的列名缓存 **/
    private static Map<String, List<TablesFieldColumn>> joinColumnsCache = new ConcurrentHashMap<>();

    private static List<TablesFieldColumn> scanColumnList(TableItem table) {
        TableInfoScans scans = DbPluginContainer.defaults().getTableInfoScans();
        List<SimpleFieldColumn> fields = scans.scanColumnList(table.getTableType());
        String tableAlias = table.getTableAlias();
        String resultField = table.getResultField();
        List<TablesFieldColumn> result = new ArrayList<>(fields.size());
        for (SimpleFieldColumn item : fields) {
            TablesFieldColumn copied = item.to(TablesFieldColumn.class);
            copied.setTableAlias(tableAlias);
            copied.setResultField(resultField);
            result.add(copied);
        }
        return result;
    }

    /**
     * 扫描获取字段名和数据库列名的映射表(有缓存)
     * 
     * @param tables 表关联对象
     * @return AllFields: fieldName - columnName
     */
    public static List<TablesFieldColumn> parseFieldColumns(TableJoin tables) {
        VerifyTools.requireNonNull(tables, "tables");
        String cacheKey = TableJoin.buildCacheKey(tables, false);
        if (joinColumnsCache.containsKey(cacheKey)) {
            return joinColumnsCache.get(cacheKey);
        }
        TableItem major = tables.getMajor();
        List<JoinItem> joins = tables.getJoins();
        List<TablesFieldColumn> all = new ArrayList<>();
        { // 添加主表的字段
            List<TablesFieldColumn> fields = scanColumnList(major);
            all.addAll(fields);
        }
        if (VerifyTools.isNotBlank(joins)) {
            // 添加关联表的字段
            for (JoinItem item : joins) {
                List<TablesFieldColumn> fields = scanColumnList(item);
                all.addAll(fields);
            }
        }
        // 处理重名字段: 设置columnAlias
        // 1.先统计字段出现次数
        Map<String, Integer> countMaps = new HashMap<>();
        for (SimpleFieldColumn field : all) {
            String fieldName = field.getFieldName();
            if (countMaps.containsKey(fieldName)) {
                countMaps.put(fieldName, countMaps.get(fieldName) + 1);
            } else {
                countMaps.put(fieldName, 1);
            }
        }
        // 2.如果出现多次则设置columnAlias=tableAlias_columnName
        for (TablesFieldColumn field : all) {
            String fieldName = field.getFieldName();
            if (countMaps.get(fieldName) > 1) {
                String columnAlias = StringTools.concat('_', field.getTableAlias(), field.getColumnName());
                field.setColumnAlias(columnAlias);
                field.setAmbiguous(true);
            }
        }
        joinColumnsCache.put(cacheKey, all);
        return all;
    }

    /**
     * 扫描获取字段名和数据库列名的映射表(有缓存)
     * 
     * @param tables 表关联对象
     * @return AllFieldColumn: fieldName - columnName
     */
    public static AllFieldColumn<TablesFieldColumn> parseToAllFieldColumn(TableJoin tables) {
        List<TablesFieldColumn> fields = parseFieldColumns(tables);
        return new AllFieldColumn<>(fields);
    }

    /** Entity的列名缓存 **/
    private static Map<Class<?>, List<SimpleFieldColumn>> entityColumnsCache = new ConcurrentHashMap<>();

    /**
     * 扫描获取字段名和数据库列名的映射表(有缓存)
     * 
     * @param clazz 类型
     * @return AllFields: fieldName - columnName
     */
    public static List<SimpleFieldColumn> parseFieldColumns(Class<?> clazz) {
        VerifyTools.requireNonNull(clazz, "class");
        List<SimpleFieldColumn> all;
        if (entityColumnsCache.containsKey(clazz)) {
            all = entityColumnsCache.get(clazz);
        } else {
            TableInfoScans scans = DbPluginContainer.defaults().getTableInfoScans();
            all = scans.scanColumnList(clazz);
            entityColumnsCache.put(clazz, all);
        }
        if (all.isEmpty()) {
            String m = "fields not found, please check config of TableInfoScans, class=" + clazz.getName();
            throw new IllegalArgumentException(m);
        }
        return all;
    }

    /**
     * 扫描获取字段名和数据库列名的映射表(有缓存)
     * 
     * @param clazz 表关联对象
     * @return AllFieldColumn: fieldName - columnName
     */
    public static AllFieldColumn<SimpleFieldColumn> parseToAllFieldColumn(Class<?> clazz) {
        List<SimpleFieldColumn> fields = parseFieldColumns(clazz);
        return new AllFieldColumn<>(fields);
    }

}
