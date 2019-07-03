package com.gitee.qdbp.jdbc.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.jdbc.core.JdbcOperations;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.JoinItem;
import com.gitee.qdbp.able.jdbc.condition.TableJoin.TableItem;
import com.gitee.qdbp.able.jdbc.utils.FieldTools;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.model.TablesFieldColumn;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.DbVersionFinder;
import com.gitee.qdbp.jdbc.plugins.ModelDataExecutor;
import com.gitee.qdbp.jdbc.plugins.ModelDataHandler;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.plugins.SqlFormatter;
import com.gitee.qdbp.jdbc.plugins.TableInfoScans;
import com.gitee.qdbp.jdbc.plugins.VariableConverter;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.build.QuerySqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;
import com.gitee.qdbp.jdbc.sql.fragment.TableCrudFragmentHelper;
import com.gitee.qdbp.jdbc.sql.fragment.TableJoinFragmentHelper;
import com.gitee.qdbp.tools.utils.ConvertTools;
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
     * 将Java对象转换为Map, 只保留有列信息的字段
     * 
     * @param object Java对象
     * @return Map对象
     */
    public static Map<String, Object> beanToMap(Object object) {
        if (object == null) {
            return null;
        }
        // 删除qdbp-tools依赖
        // Map<String, Object> map = JsonTools.beanToMap(object, true);
        Map<String, Object> map = doBeanToMap(object, true);
        if (VerifyTools.isBlank(map)) {
            return map;
        }

        // 从bean.getClass()扫描获取列名与字段名的对应关系
        AllFieldColumn<?> allFields = parseToAllFieldColumn(object.getClass());
        if (allFields == null || allFields.isEmpty()) {
            return null;
        }
        List<String> fieldNames = allFields.getFieldNames();

        // 只保留有列信息的字段
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (FieldTools.contains(fieldNames, entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static Map<String, Object> doBeanToMap(Object object, boolean clearBlankValue) {
        if (object == null) {
            return null;
        }
        Map<String, Object> map = (JSONObject) JSON.toJSON(object);
        return clearBlankValue ? ConvertTools.clearBlankValue(map, false) : map;
    }

    /**
     * 将变量转换为字符串, 用于拼接SQL
     * 
     * @param variable 变量
     * @return 转换后的字符串
     */
    public static String variableToString(Object variable) {
        return variableToString(variable, true);
    }

    private static String variableToString(Object variable, boolean recursive) {
        if (variable == null) {
            return "NULL";
        } else if (variable instanceof SqlBuffer) {
            return ((SqlBuffer) variable).getNormalSqlString();
        } else if (variable instanceof Number) {
            return variable.toString();
        } else if (variable instanceof CharSequence) {
            return getSqlDialect().variableToString(variable.toString());
        } else if (variable instanceof Character) {
            return new StringBuilder().append("'").append(variable).append("'").toString();
        } else if (variable instanceof Boolean) {
            return getSqlDialect().variableToString((Boolean) variable);
        } else if (variable instanceof Date) {
            return getSqlDialect().variableToString((Date) variable);
        } else {
            if (!recursive) {
                return getSqlDialect().variableToString(variable.toString());
            } else {
                VariableConverter converter = DbPluginContainer.global.getVariableConverter();
                if (variable instanceof Enum) {
                    Object value = converter.variableToDbValue(((Enum<?>) variable));
                    return variableToString(value, false);
                } else {
                    Object value = converter.variableToDbValue(variable);
                    return variableToString(value, false);
                }
            }
        }
    }

    /**
     * 获取实体业务执行接口
     * 
     * @param clazz
     * @return 实体业务执行类
     */
    public static ModelDataExecutor getModelDataExecutor(Class<?> clazz) {
        AllFieldColumn<?> allFields = parseToAllFieldColumn(clazz);
        if (allFields.isEmpty()) {
            throw new IllegalArgumentException("fields is empty");
        }
        ModelDataHandler handler = DbPluginContainer.global.getModelDataHandler();
        return new ModelDataExecutor(allFields, handler);
    }

    /**
     * 获取实体业务执行接口
     * 
     * @return 实体业务执行类
     */
    public static ModelDataExecutor getModelDataExecutor(TableJoin tables) {
        AllFieldColumn<?> allFields = parseToAllFieldColumn(tables);
        if (allFields.isEmpty()) {
            throw new IllegalArgumentException("fields is empty");
        }
        ModelDataHandler handler = DbPluginContainer.global.getModelDataHandler();
        return new ModelDataExecutor(allFields, handler);
    }

    public static CrudSqlBuilder getCrudSqlBuilder(Class<?> clazz) {
        CrudFragmentHelper sqlHelper = new TableCrudFragmentHelper(clazz);
        return new CrudSqlBuilder(sqlHelper);
    }

    public static QuerySqlBuilder getCrudSqlBuilder(TableJoin tables) {
        TableJoinFragmentHelper sqlHelper = new TableJoinFragmentHelper(tables);
        return new QuerySqlBuilder(sqlHelper);
    }

    /**
     * 获取数据库方言处理类
     * 
     * @return 方言处理类
     */
    public static SqlDialect getSqlDialect() {
        return DbPluginContainer.global.getSqlDialect();
    }

    /**
     * 格式化SQL语句
     * 
     * @param sql 待格式化的SQL语句
     * @param indent 缩进层数
     * @return 已格式化的SQL语句
     */
    public static String formatSql(String sql, int indent) {
        SqlFormatter formatter = DbPluginContainer.global.getSqlFormatter();
        return formatter.format(sql, indent);
    }

    /**
     * 格式化SQL语句
     * 
     * @param sql 待格式化的SQL语句
     * @param indent 缩进层数
     * @return 已格式化的SQL语句
     */
    public static String formatSql(SqlBuffer sql, int indent) {
        return formatSql(sql.toString(), indent);
    }

    /**
     * 查找数据库版本信息
     * 
     * @param jdbcOperations JDBC操作类
     * @return 数据库版本信息
     */
    public static DbVersion findDbVersion(JdbcOperations jdbcOperations) {
        DbVersionFinder finder = DbPluginContainer.global.getDbVersionFinder();
        return finder.findDbVersion(jdbcOperations);
    }

    /** Entity的表名缓存 **/
    private static Map<Class<?>, String> entityTableNameCache = new ConcurrentHashMap<>();

    /**
     * 扫描表名信息
     * 
     * @param clazz 类名
     * @return 表名
     */
    public static String parseTableName(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }
        if (entityTableNameCache.containsKey(clazz)) {
            return entityTableNameCache.get(clazz);
        }

        TableInfoScans scans = DbPluginContainer.global.getTableInfoScans();
        String tableName = scans.scanTableName(clazz);
        entityTableNameCache.put(clazz, tableName);
        return tableName;
    }

    /** Entity的主键缓存 **/
    private static Map<Class<?>, PrimaryKeyFieldColumn> entityPrimaryKeyCache = new ConcurrentHashMap<>();

    /**
     * 扫描获取主键
     * 
     * @param clazz 类名
     * @return 主键
     */
    public static PrimaryKeyFieldColumn parsePrimaryKey(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }
        if (entityPrimaryKeyCache.containsKey(clazz)) {
            return entityPrimaryKeyCache.get(clazz);
        }

        TableInfoScans scans = DbPluginContainer.global.getTableInfoScans();
        PrimaryKeyFieldColumn pk = scans.scanPrimaryKey(clazz);
        entityPrimaryKeyCache.put(clazz, pk);
        return pk;
    }

    /** TableJoin的列名缓存 **/
    private static Map<String, List<TablesFieldColumn>> joinColumnsCache = new ConcurrentHashMap<>();

    public static String buildCacheKey(TableJoin tables) {
        StringBuilder buffer = new StringBuilder();
        TableItem major = tables.getMajor();
        buffer.append(parseTableName(major.getTableType()));
        if (VerifyTools.isNotBlank(major.getTableAlias())) {
            buffer.append(' ').append(major.getTableAlias().toUpperCase());
        }
        List<JoinItem> joins = tables.getJoins();
        if (VerifyTools.isNotBlank(joins)) {
            for (JoinItem item : joins) {
                buffer.append('-').append(parseTableName(item.getTableType()));
                if (VerifyTools.isNotBlank(item.getTableAlias())) {
                    buffer.append(' ').append(item.getTableAlias().toUpperCase());
                }
            }
        }
        return buffer.toString();
    }

    private static List<TablesFieldColumn> scanColumnList(TableItem table) {
        TableInfoScans scans = DbPluginContainer.global.getTableInfoScans();
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
     * 扫描获取字段名和数据库列名的映射表
     * 
     * @param tables 表关联对象
     * @return AllFields: fieldName - columnName
     */
    public static List<TablesFieldColumn> parseFieldColumns(TableJoin tables) {
        if (tables == null) {
            throw new IllegalArgumentException("tables is null");
        }
        String cacheKey = buildCacheKey(tables);
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
            }
        }
        joinColumnsCache.put(cacheKey, all);
        return all;
    }

    /**
     * 扫描获取字段名和数据库列名的映射表
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
     * 扫描获取字段名和数据库列名的映射表
     * 
     * @param clazz 类型
     * @return AllFields: fieldName - columnName
     */
    public static List<SimpleFieldColumn> parseFieldColumns(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }
        if (entityColumnsCache.containsKey(clazz)) {
            return entityColumnsCache.get(clazz);
        }

        TableInfoScans scans = DbPluginContainer.global.getTableInfoScans();
        List<SimpleFieldColumn> all = scans.scanColumnList(clazz);
        entityColumnsCache.put(clazz, all);
        return all;
    }

    /**
     * 扫描获取字段名和数据库列名的映射表
     * 
     * @param tables 表关联对象
     * @return AllFieldColumn: fieldName - columnName
     */
    public static AllFieldColumn<SimpleFieldColumn> parseToAllFieldColumn(Class<?> clazz) {
        List<SimpleFieldColumn> fields = parseFieldColumns(clazz);
        return new AllFieldColumn<>(fields);
    }

}
