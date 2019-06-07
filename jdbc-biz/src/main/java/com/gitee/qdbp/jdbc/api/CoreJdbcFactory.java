package com.gitee.qdbp.jdbc.api;

import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;

/**
 * 数据库操作对象的构造器<br>
 * 使用方法: <br>
 * <pre>
    &#64;Autowired
    private CoreJdbcFactory coreJdbcFactory;
    
    private void xxMethod() {
        BaseCrudDao&lt;Xxx&gt; xxxDao = coreJdbcFactory.buildCrudDao(Xxx.class);
        Xxx xxxBean = xxxDao.findById(id);
        BaseCrudDao&lt;Yyy&gt; yyyDao = coreJdbcFactory.buildCrudDao(Yyy.class);
        List&lt;Yyy&gt; xxxBeans = yyyDao.list(where, OrderPaging.NONE);
    }
 * </pre> 或 <pre>
    &#64;Autowired
    private CoreJdbcFactory coreJdbcFactory;
    private BaseCrudDao&lt;CommArchiveFileEntity&gt; dao;

    &#64;PostConstruct
    private void init() {
        dao = coreJdbcFactory.buildCrudDao(clazz);
    }
 * </pre>
 *
 * @author 赵卉华
 * @version 190601
 */
public interface CoreJdbcFactory {

    /** 查找当前数据源的数据库版本信息 **/
    DbVersion findDbVersion();

    /** 构造数据库方言处理类 **/
    SqlDialect buildDialect();

    /** 构造基础增删改查对象 **/
    <T> CrudDao<T> buildCrudDao(Class<T> clazz);

    /** 构造SQL片段帮助类 **/
    CrudFragmentHelper buildSqlFragmentHelper(Class<?> clazz);
}
