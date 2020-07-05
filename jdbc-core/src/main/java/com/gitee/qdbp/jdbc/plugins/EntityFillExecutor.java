package com.gitee.qdbp.jdbc.plugins;

import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;

/**
 * 实体数据填充业务处理接口<br>
 * 每个项目的处理方式不一样, 调用EntityFillHandler接口由各项目提供实现类<br>
 * 命名说明:<br>
 * fillQueryXxx是查询时用到的, 兼容单表和表关联, 主要是给EasyBaseQueryImpl(JoinQueryerImpl/EasyCrudDaoImpl的查询)调用<br>
 * fillEntityXxx是单表增删改查用到的, 只支持单表, 主要是给EasyCrudDaoImpl调用<br>
 *
 * @author zhaohuihua
 * @version 190601
 */
public class EntityFillExecutor {

    private AllFieldColumn<?> allFields;
    private EntityFillHandler entityFillHandler;

    public EntityFillExecutor(AllFieldColumn<?> allFields, EntityFillHandler entityFillHandler) {
        this.allFields = allFields;
        if (this.allFields.isEmpty()) {
            throw new IllegalArgumentException("fields is empty");
        }
        this.entityFillHandler = entityFillHandler;
    }

    /** 生成主键编号 **/
    public String generatePrimaryKeyCode(String tableName) {
        return entityFillHandler.generatePrimaryKeyCode(tableName);
    }

    /**
     * 单表是否支持逻辑删除
     * 
     * @return 是否支持
     */
    public boolean supportedTableLogicalDelete() {
        return entityFillHandler.supportedTableLogicalDelete(allFields);
    }

    /**
     * 填充查询时的条件参数(如数据权限等)
     * 
     * @param tableAlias 表别名
     * @param where 查询条件
     */
    public void fillQueryWhereParams(DbWhere where, String tableAlias) {
        entityFillHandler.fillQueryWhereParams(where, tableAlias, allFields);
    }

    /**
     * 填充更新时的查询条件参数(如数据权限等)
     * 
     * @param where 查询条件
     */
    public void fillUpdateWhereParams(DbWhere where) {
        entityFillHandler.fillUpdateWhereParams(where, allFields);
    }

    /**
     * 填充删除时的查询条件参数(如数据权限等)
     * 
     * @param where 查询条件
     */
    public void fillDeleteWhereParams(DbWhere where) {
        entityFillHandler.fillDeleteWhereParams(where, allFields);
    }

    /**
     * 填充查询条件的数据状态标记
     * 
     * @param tableAlias 表别名
     * @param where 条件
     */
    public void fillQueryWhereDataStatus(DbWhere where, String tableAlias) {
        entityFillHandler.fillQueryWhereDataStatus(where, tableAlias, allFields);
    }

    /**
     * 填充单表Where条件的数据状态标记
     * 
     * @param where 条件
     */
    public void fillUpdateWhereDataStatus(DbWhere where) {
        entityFillHandler.fillUpdateWhereDataStatus(where, allFields);
    }

    /**
     * 填充单表Where条件的数据状态标记
     * 
     * @param where 条件
     */
    public void fillDeleteWhereDataStatus(DbWhere where) {
        entityFillHandler.fillDeleteWhereDataStatus(where, allFields);
    }

    /**
     * 填充单表新增时的数据状态标记
     * 
     * @param condition 条件
     */
    public void fillEntityCreateDataStatus(Map<String, Object> condition) {
        entityFillHandler.fillEntityCreateDataStatus(condition, allFields);
    }

    /**
     * 填充单表创建参数(如创建人创建时间等)
     * 
     * @param model 实体对象
     */
    public void fillEntityCreateParams(Map<String, Object> model) {
        entityFillHandler.fillEntityCreateParams(model, allFields);
    }

    /**
     * 填充单表修改参数(如修改人修改时间等)
     * 
     * @param model 实体对象
     */
    public void fillEntityUpdateParams(Map<String, Object> model) {
        entityFillHandler.fillEntityUpdateParams(model, allFields);
    }

    /**
     * 填充单表修改参数(如修改人修改时间等)
     * 
     * @param ud 更新对象
     */
    public void fillEntityUpdateParams(DbUpdate ud) {
        entityFillHandler.fillEntityUpdateParams(ud, allFields);
    }

    // 不需要自动填充修改时的数据状态标记, 没有这样的业务场景
    // /**
    //  * 填充单表修改时的数据状态标记
    //  * 
    //  * @param ud 更新对象
    //  */
    // public void fillEntityUpdateDataStatus(DbUpdate ud) {
    //     entityFillHandler.fillEntityUpdateDataStatus(ud, allFields);
    // }

    /**
     * 填充逻辑删除时的参数(如修改人修改时间等)
     * 
     * @param model 实体对象
     */
    public void fillLogicalDeleteParams(Map<String, Object> model) {
        entityFillHandler.fillLogicalDeleteParams(model, allFields);
    }

    /**
     * 填充逻辑删除时的参数(如修改人修改时间等)
     * 
     * @param ud 更新对象
     */
    public void fillLogicalDeleteParams(DbUpdate ud) {
        entityFillHandler.fillLogicalDeleteParams(ud, allFields);
    }

    /**
     * 填充单表逻辑删除时的数据状态标记(应将数据状态设置为无效)
     * 
     * @param ud 更新对象
     */
    public void fillLogicalDeleteDataStatus(DbUpdate ud) {
        entityFillHandler.fillLogicalDeleteDataStatus(ud, allFields);
    }

}
