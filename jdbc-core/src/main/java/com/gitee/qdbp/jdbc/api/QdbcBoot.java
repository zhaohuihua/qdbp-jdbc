package com.gitee.qdbp.jdbc.api;

import com.gitee.qdbp.able.jdbc.condition.TableJoin;

/**
 * 数据库操作对象的构造器
 *
 * @author 赵卉华
 * @version 190601
 */
public interface CoreJdbcBoot {

    /**
     * 构造单表增删改查对象<br>
     * 使用方法: <br>
     * <pre>
        &#64;Autowired
        private CoreJdbcBoot coreJdbcBoot;
        
        private void xxMethod() {
            EasyCrudDao&lt;Xxx&gt; xxxDao = coreJdbcBoot.buildCrudDao(Xxx.class);
            Xxx xxxBean = xxxDao.findById(id);
            EasyCrudDao&lt;Yyy&gt; yyyDao = coreJdbcBoot.buildCrudDao(Yyy.class);
            List&lt;Yyy&gt; xxxBeans = yyyDao.list(where, OrderPaging.NONE);
        }
     * </pre> 或 <pre>
        &#64;Autowired
        private CoreJdbcBoot coreJdbcBoot;
        private EasyCrudDao&lt;CommArchiveFileEntity&gt; dao;
    
        &#64;PostConstruct
        private void init() {
            dao = coreJdbcBoot.buildCrudDao(clazz);
        }
     * </pre>
     * 
     * @param <T> 单表对应的具体类型
     * @param clazz 单表对应的对象类型
     * @return 单表增删改查对象
     */
    <T> EasyCrudDao<T> buildCrudDao(Class<T> clazz);

    /**
     * 构造表关联查询对象<br>
     * 使用方法: <br>
     * <pre>
        &#64;Autowired
        private CoreJdbcBoot coreJdbcBoot;
        
        private void xxMethod() {
            EasyJoinQuery&lt;Xxx&gt; xxxJoinQuery = coreJdbcBoot.buildJoinQuery(Xxx.class);
            List&lt;Xxx&gt; xxxBeans = xxxJoinQuery.list(where, OrderPaging.NONE);
        }
     * </pre> 或 <pre>
        &#64;Autowired
        private CoreJdbcBoot coreJdbcBoot;
        private EasyJoinQuery&lt;Xxx&gt; xxxJoinQuery;
    
        &#64;PostConstruct
        private void init() {
            xxxJoinQuery = coreJdbcBoot.buildJoinQuery(Xxx.class);
        }
     * </pre>
     * 
     * @param <T> 查询结果的具体类型
     * @param tables 表关联对象
     * @param resultType 查询结果的对象类型
     * @return 表关联查询对象
     */
    <T> EasyJoinQuery<T> buildJoinQuery(TableJoin tables, Class<T> resultType);

    /**
     * 获取SqlBuffer数据库操作类
     * 
     * @return SqlBufferJdbcOperations
     */
    SqlBufferJdbcOperations getSqlBufferJdbcOperations();
}
