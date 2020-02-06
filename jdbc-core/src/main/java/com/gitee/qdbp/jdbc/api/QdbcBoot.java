package com.gitee.qdbp.jdbc.api;

import com.gitee.qdbp.able.jdbc.condition.TableJoin;

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
        // 查询单个对象
        CrudDao&lt;XxxBean&gt; xxxDao = qdbcBoot.buildCrudDao(XxxBean.class);
        XxxBean xxxBean = xxxDao.findById(id);
        // 查询列表(创建时间为今天的)
        DbWhere where = new DbWhere();
        where.on("createTime", ">=", today);
        CrudDao&lt;YyyBean&gt; yyyDao = qdbcBoot.buildCrudDao(YyyBean.class);
        List&lt;YyyBean&gt; yyyBeans = yyyDao.list(where, OrderPaging.NONE);

        // 删除昨天待处理和失败的记录
        Date yesterday = DateTools.addDay(today, -1);
        DbWhere where = new DbWhere();
        where.on("state", "in", XxxState.PENDING, XxxState.ERROR);
        where.on("handleTime", "between", yesterday, today);
        xxxDao.physicalDelete(where, false); // 物理删除

        // 表关联查询
        TableJoin tables = ...; // 详见TableJoin的注释
        DbWhere where = new DbWhere();
        where.on("createTime", ">=", today);
        OrderPaging odpg = OrderPaging.of(new Paging(1, 10), "u.createTime DESC");
        JoinQueryer&lt;ZzzBean&gt; zzzQuery = qdbcBoot.buildJoinQuery(tables, ZzzBean.class);
        List&lt;ZzzBean&gt; zzzBeans = zzzQuery.list(where, odpg);
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
     * 获取SqlBuffer数据库操作类
     * 
     * @return SqlBufferJdbcOperations
     */
    SqlBufferJdbcOperations getSqlBufferJdbcOperations();
}
