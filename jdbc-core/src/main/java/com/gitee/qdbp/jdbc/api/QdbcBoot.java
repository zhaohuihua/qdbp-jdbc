package com.gitee.qdbp.jdbc.api;

import com.gitee.qdbp.able.jdbc.condition.TableJoin;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.build.CrudSqlBuilder;
import com.gitee.qdbp.jdbc.sql.build.QuerySqlBuilder;

/**
 * 数据库操作对象的构造器<br>
 * qdbp-jdbc简称为qdbc<br>
 * 使用方法示例: <br>
 * <pre>
    &#64;Autowired
    private QdbcBoot qdbcBoot;

    private void xxMethod() {
        // 单表增删改查
        Date today = DateTools.toStartTime(new Date());
        Date yesterday = DateTools.addDay(today, -1);
        { // 查询单个对象
            CrudDao&lt;XxxBean&gt; xxxDao = qdbcBoot.buildCrudDao(XxxBean.class);
            XxxBean xxxBean = xxxDao.findById(id);
        }
        { // 查询列表(创建时间为今天的)
            CrudDao&lt;YyyBean&gt; yyyDao = qdbcBoot.buildCrudDao(YyyBean.class);
            DbWhere where = new DbWhere();
            where.on("createTime", ">=", today);
            Orderings orderings = Orderings.of("createTime DESC");
            List&lt;YyyBean&gt; yyyBeans = yyyDao.list(where, orderings);
        }
        { // 分页查询(创建时间为昨天的第1页的10条记录)
            CrudDao&lt;YyyBean&gt; yyyDao = qdbcBoot.buildCrudDao(YyyBean.class);
            DbWhere where = new DbWhere();
            where.on("createTime", ">=", yesterday);
            where.on("createTime", "<", today);
            OrderPaging odpg = OrderPaging.of(new Paging(1, 10), "createTime ASC");
            PageList&lt;YyyBean&gt; yyyBeans = yyyDao.list(where, odpg);
        }
        { // 删除昨天待处理和失败的记录
            CrudDao&lt;ZzzBean&gt; zzzDao = qdbcBoot.buildCrudDao(ZzzBean.class);
            DbWhere where = new DbWhere();
            where.on("state", "in", ZzzState.PENDING, ZzzState.ERROR);
            where.on("handleTime", "between", yesterday, today);
            zzzDao.physicalDelete(where); // 物理删除
        }

        { // 表关联查询
            TableJoin tables = ...; // 详见TableJoin的注释
            JoinQueryer&lt;ZzzBean&gt; zzzQuery = qdbcBoot.buildJoinQuery(tables, ZzzBean.class);
            DbWhere where = new DbWhere();
            where.on("u.createTime", ">=", today);
            OrderPaging odpg = OrderPaging.of(new Paging(1, 10), "u.createTime DESC");
            PageList&lt;ZzzBean&gt; zzzBeans = zzzQuery.list(where, odpg);
        }

        { // SQL模板查询
            Paging paging = new Paging(1, 10);

            DbWhere where = new DbWhere();
            where.on("ur.roleName", "like", keyword);

            Orderings orderings = Orderings.of("ur.roleName ASC");

            TableJoin tables = TableJoin.of(SysUserRoleEntity.class, "ur", SysRoleEntity.class, "r");
            QueryFragmentHelper sqlHelper = qdbcBoot.buildSqlBuilder(tables).helper();

            Map&lt;String, Object&gt; params = new HashMap&lt;&gt;();
            params.put("userIds", Arrays.asList(userId));
            if (VerifyTools.isNotBlank(where)) {
                params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
            }
            if (VerifyTools.isNotBlank(orderings)) {
                params.put("orderByCondition", sqlHelper.buildOrderBySql(orderings, false));
            }

            String sqlId = "user.roles.query";
            SqlDao sqlDao = qdbcBoot.getSqlDao();
            PageList&lt;SysRoleEntity&gt; list = sqlDao.pageForObjects(sqlId, params, paging, SysRoleEntity.class);
        }
    }
 * </pre>
 *
 * @author 赵卉华
 * @version 190601
 * @see TableJoin
 */
public interface QdbcBoot {

    /**
     * 构造单表增删改查对象
     * 
     * @param <T> 单表对应的具体类型
     * @param clazz 单表对应的对象类型
     * @return 单表增删改查对象
     */
    <T> CrudDao<T> buildCrudDao(Class<T> clazz);

    /**
     * 构造表关联查询对象
     * 
     * @param <T> 查询结果的具体类型
     * @param tables 表关联对象
     * @param resultType 查询结果的对象类型
     * @return 表关联查询对象
     */
    <T> JoinQueryer<T> buildJoinQuery(TableJoin tables, Class<T> resultType);

    /**
     * 构造单表增删改查SQL生成工具
     * 
     * @param clazz 单表对应的对象类型
     * @return SQL生成工具
     */
    CrudSqlBuilder buildSqlBuilder(Class<?> clazz);

    /**
     * 构造表关联对象SQL生成工具
     * 
     * @param clazz 表关联对象
     * @return SQL生成工具
     */
    QuerySqlBuilder buildSqlBuilder(TableJoin tables);

    /**
     * 获取SQL执行接口
     * 
     * @return SqlDao
     */
    SqlDao getSqlDao();

    /**
     * 获取SQL方言处理类
     * 
     * @return SQL方言处理类
     */
    SqlDialect getSqlDialect();

    /**
     * 获取SqlBuffer数据库操作类
     * 
     * @return SqlBufferJdbcOperations
     */
    SqlBufferJdbcOperations getSqlBufferJdbcOperations();
}
