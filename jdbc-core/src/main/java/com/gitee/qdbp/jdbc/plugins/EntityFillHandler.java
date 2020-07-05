package com.gitee.qdbp.jdbc.plugins;

import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;

/**
 * 实体数据填充的业务处理接口(主要作用是自动填充逻辑删除/创建人/创建时间/修改人/修改时间等全局通用字段)<br>
 * 每个项目的处理方式不一样, 抽象成接口由各项目提供实现类<br>
 * 这个类的实现类是提供给EntityFillExecutor调用的
 *
 * @author zhaohuihua
 * @version 190601
 */
public interface EntityFillHandler {

    /** 获取当前登录账号, 一般是UserId **/
    String getLoginAccount();

    /** 生成主键编号 **/
    String generatePrimaryKeyCode(String tableName);

    /**
     * 单表是否支持逻辑删除
     * 
     * @param allFields 全字段
     * @return 是否支持
     */
    boolean supportedTableLogicalDelete(AllFieldColumn<?> allFields);

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
     * 填充查询时WHERE条件的数据状态标记<br>
     * SELECT * FROM table WHERE ... {在这里限制数据状态为有效}
     * 
     * @param tableAlias 表别名
     * @param where 条件
     * @param allFields 全字段
     */
    void fillQueryWhereDataStatus(DbWhere where, String tableAlias, AllFieldColumn<?> allFields);

    /**
     * 填充更新时WHERE条件的数据状态标记<br>
     * UPDATE table SET ... WHERE ... {在这里限制数据状态为有效}
     * 
     * @param where 条件
     * @param allFields 全字段
     */
    void fillUpdateWhereDataStatus(DbWhere where, AllFieldColumn<?> allFields);

    /**
     * 填充删除时WHERE条件的数据状态标记<br>
     * DELETE FOM table WHERE ... {在这里限制数据状态为有效}
     * 
     * @param where 条件
     * @param allFields 全字段
     */
    void fillDeleteWhereDataStatus(DbWhere where, AllFieldColumn<?> allFields);

    /**
     * 填充新增时的数据状态标记<br>
     * 增加INSERT时的数据状态字段的默认值设置为有效
     * 
     * @param condition 条件
     * @param allFields 全字段
     */
    void fillEntityCreateDataStatus(Map<String, Object> condition, AllFieldColumn<?> allFields);

    /**
     * 填充创建参数(如创建人创建时间等)
     * 
     * @param model 实体对象
     * @param allFields 全字段
     */
    void fillEntityCreateParams(Map<String, Object> model, AllFieldColumn<?> allFields);

    /**
     * 填充修改参数(如修改人修改时间等)<br>
     * UPDATE table SET ..., {在这里填充修改时的默认值} WHERE ...
     * 
     * @param model 实体对象
     * @param allFields 全字段
     */
    void fillEntityUpdateParams(Map<String, Object> model, AllFieldColumn<?> allFields);

    /**
     * 填充修改参数(如修改人修改时间等)<br>
     * UPDATE table SET ..., {在这里填充修改时的默认值} WHERE ...
     * 
     * @param ud 更新对象
     * @param allFields 全字段
     */
    void fillEntityUpdateParams(DbUpdate ud, AllFieldColumn<?> allFields);

    // 不需要自动填充修改时的数据状态标记, 没有这样的业务场景
    // /**
    //  * 填充修改时的数据状态标记
    //  * 
    //  * @param ud 更新对象
    //  * @param allFields 全字段
    //  */
    // void fillEntityUpdateDataStatus(DbUpdate ud, AllFieldColumn<?> allFields);

    /**
     * 填充逻辑删除时的参数(如修改人修改时间等)<br>
     * UPDATE table SET ..., {在这里填充修改时的默认值} WHERE ...
     * 
     * @param model 实体对象
     * @param allFields 全字段
     */
    void fillLogicalDeleteParams(Map<String, Object> model, AllFieldColumn<?> allFields);

    /**
     * 填充逻辑删除时的参数(如修改人修改时间等)<br>
     * UPDATE table SET ..., {在这里填充修改时的默认值} WHERE ...
     * 
     * @param ud 更新对象
     * @param allFields 全字段
     */
    void fillLogicalDeleteParams(DbUpdate ud, AllFieldColumn<?> allFields);

    /**
     * 填充逻辑删除时的数据状态标记<br>
     * UPDATE table SET ..., dataStatus={在这里将数据状态设置为无效} WHERE ...
     * 
     * @param model 实体对象
     * @param allFields 全字段
     */
    void fillLogicalDeleteDataStatus(Map<String, Object> model, AllFieldColumn<?> allFields);

    /**
     * 填充逻辑删除时的数据状态标记<br>
     * UPDATE table SET ..., dataStatus={在这里将数据状态设置为无效} WHERE ...
     * 
     * @param ud 更新对象
     * @param allFields 全字段
     */
    void fillLogicalDeleteDataStatus(DbUpdate ud, AllFieldColumn<?> allFields);
}
