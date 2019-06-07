package com.gitee.qdbp.jdbc.api;

import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.fragment.CrudFragmentHelper;

/**
 * 基础增删改查对象的构造器<br>
 * 使用方法: <br>
 * <pre>
    &#64;Autowired
    private BaseCrudBuilder baseCrudBuilder;
    
    private void xxMethod() {
        BaseCrudDao&lt;Xxx&gt; xxxDao = baseCrudBuilder.buildDao(Xxx.class);
        Xxx xxxBean = xxxDao.findById(id);
        BaseCrudDao&lt;Yyy&gt; yyyDao = baseCrudBuilder.buildDao(Yyy.class);
        List&lt;Yyy&gt; xxxBeans = yyyDao.list(where, OrderPaging.NONE);
    }
 * </pre> 或 <pre>
    &#64;Autowired
    private BaseCrudBuilder baseCrudBuilder;
    private BaseCrudDao&lt;CommArchiveFileEntity&gt; dao;

    &#64;PostConstruct
    private void init() {
        dao = baseCrudBuilder.buildDao(clazz);
    }
 * </pre>
 *
 * @author 赵卉华
 * @version 190601
 */
public interface BaseCrudBuilder {

    /** 使用默认的数据库类型构造基础增删改查对象 **/
    <T> BaseCrudDao<T> buildDao(Class<T> clazz);

    /** 使用默认的数据库类型构造数据库言处理类 **/
    SqlDialect buildDialect();

    /** 构造SQL片段帮助类 **/
    CrudFragmentHelper buildSqlFragmentHelper(Class<?> clazz);

    /** 根据数据源查找数据库信息 **/
    DbVersion findDbVersion();
}
