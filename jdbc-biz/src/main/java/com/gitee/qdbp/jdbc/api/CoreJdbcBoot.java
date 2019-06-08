package com.gitee.qdbp.jdbc.api;

import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;

/**
 * 数据库操作对象的构造器<br>
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
 * @author 赵卉华
 * @version 190601
 */
public interface CoreJdbcBoot {

    /** 查找当前数据源的数据库版本信息 **/
    DbVersion findDbVersion();

    /** 构造基础增删改查对象 **/
    <T> EasyCrudDao<T> buildCrudDao(Class<T> clazz);

    /** 构造SQL片段帮助类 **/
    CrudFragmentHelper buildSqlFragmentHelper(Class<?> clazz);
}
