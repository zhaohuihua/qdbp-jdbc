package com.gitee.qdbp.jdbc.plugins;

import java.util.Map;
import com.gitee.qdbp.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.fields.AllFields;

/**
 * 实体业务处理接口<br>
 * 每个项目的处理方式不一样, 抽象成接口由各项目提供实现类<br>
 * 命名说明:<br>
 * fillQueryXxx是查询时用到的, 兼容单表和表关联, 主要是给EasyTableQueryImpl和EasyJoinQueryImpl调用<br>
 * fillTableXxx是单表增删改查用到的, 只支持单表, 主要是给EasyCrudDaoImpl调用<br>
 *
 * @author zhaohuihua
 * @version 190601
 */
public interface ModelDataHandler {

    /** 生成主键编号 **/
    String generatePrimaryKeyCode(String tableName);

    /**
     * 单表是否支持逻辑删除
     * 
     * @param allFields 全字段
     * @return 是否支持
     */
    boolean supportedTableLogicalDelete(AllFields allFields);

    /**
     * 填充查询条件的数据状态标记
     * 
     * @param tableAlias 表别名
     * @param where 条件
     * @param allFields 全字段
     */
    void fillQueryWhereDataStatus(DbWhere where, String tableAlias, AllFields allFields);

    /**
     * 填充单表Where条件的数据状态标记
     * 
     * @param where 条件
     * @param allFields 全字段
     */
    void fillTableWhereDataStatus(DbWhere where, AllFields allFields);

    /**
     * 填充单表新增时的数据状态标记
     * 
     * @param condition 条件
     * @param allFields 全字段
     */
    void fillTableCreateDataStatus(Map<String, Object> condition, AllFields allFields);

    /**
     * 填充单表修改时的数据状态标记(此方法一般为空)
     * 
     * @param ud 更新对象
     * @param allFields 全字段
     */
    void fillTableUpdateDataStatus(DbUpdate ud, AllFields allFields);

    /**
     * 填充单表逻辑删除时的数据状态标记(应将数据状态设置为无效)
     * 
     * @param ud 更新对象
     * @param allFields 全字段
     */
    void fillTableLogicalDeleteDataStatus(DbUpdate ud, AllFields allFields);

    /**
     * 填充单表创建参数(如创建人创建时间等)
     * 
     * @param model 实体对象
     * @param allFields 全字段
     */
    void fillTableCreteParams(Map<String, Object> model, AllFields allFields);

    /**
     * 填充单表修改参数(如修改人修改时间等)
     * 
     * @param model 实体对象
     * @param allFields 全字段
     */
    void fillTableUpdateParams(Map<String, Object> model, AllFields allFields);

    /**
     * 填充单表修改参数(如修改人修改时间等)
     * 
     * @param ud 更新对象
     * @param allFields 全字段
     */
    void fillTableUpdateParams(DbUpdate ud, AllFields allFields);
}
