package com.gitee.qdbp.jdbc.plugins;

import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;

/**
 * 实体公共字段填充策略(主要作用是自动填充创建人/创建时间/修改人/修改时间等全局通用字段)<br>
 * 每个项目的处理方式不一样, 抽象成接口由各项目提供实现类<br>
 * 这个类的实现类是提供给EntityFieldFillExecutor调用的
 *
 * @author zhaohuihua
 * @version 190601
 */
public interface EntityFieldFillStrategy {

    /** 获取当前登录账号, 一般是UserId **/
    String getLoginAccount();

    /** 生成主键编号 **/
    String generatePrimaryKeyCode(String tableName);

    /**
     * 填充查询时WHERE条件参数(如数据权限)<br>
     * SELECT * FROM table WHERE ... {在这里增加限制数条件}
     * 
     * @param tableAlias 表别名
     * @param where 条件
     * @param allFields 全字段
     */
    void fillQueryWhereParams(DbWhere where, String tableAlias, AllFieldColumn<?> allFields);

    /**
     * 填充更新时WHERE条件参数<br>
     * UPDATE table SET ... WHERE ... {在这里增加限制数条件}
     * 
     * @param where 条件
     * @param allFields 全字段
     */
    void fillUpdateWhereParams(DbWhere where, AllFieldColumn<?> allFields);

    /**
     * 填充删除时WHERE条件参数<br>
     * DELETE FOM table WHERE ... {在这里增加限制数条件}
     * 
     * @param where 条件
     * @param allFields 全字段
     */
    void fillDeleteWhereParams(DbWhere where, AllFieldColumn<?> allFields);

    /**
     * 填充创建参数(如创建人创建时间等)
     * 
     * @param entity 实体对象
     * @param allFields 全字段
     */
    void fillEntityCreateParams(Map<String, Object> entity, AllFieldColumn<?> allFields);

    /**
     * 填充修改参数(如修改人修改时间等)<br>
     * UPDATE table SET ..., {在这里填充修改时的默认值} WHERE ...
     * 
     * @param entity 实体对象
     * @param allFields 全字段
     */
    void fillEntityUpdateParams(Map<String, Object> entity, AllFieldColumn<?> allFields);

    /**
     * 填充修改参数(如修改人修改时间等)<br>
     * UPDATE table SET ..., {在这里填充修改时的默认值} WHERE ...
     * 
     * @param ud 更新对象
     * @param allFields 全字段
     */
    void fillEntityUpdateParams(DbUpdate ud, AllFieldColumn<?> allFields);

    /**
     * 填充逻辑删除时的参数(如修改人修改时间等)<br>
     * UPDATE table SET ..., {在这里填充修改时的默认值} WHERE ...
     * 
     * @param entity 实体对象
     * @param allFields 全字段
     */
    void fillLogicalDeleteParams(Map<String, Object> entity, AllFieldColumn<?> allFields);

    /**
     * 填充逻辑删除时的参数(如修改人修改时间等)<br>
     * UPDATE table SET ..., {在这里填充修改时的默认值} WHERE ...
     * 
     * @param ud 更新对象
     * @param allFields 全字段
     */
    void fillLogicalDeleteParams(DbUpdate ud, AllFieldColumn<?> allFields);
}
