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
 * fillTableXxx是单表增删改查用到的, 只支持单表, 主要是给EasyCrudDaoImpl调用<br>
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
    public void fillTableWhereDataStatus(DbWhere where) {
        entityFillHandler.fillTableWhereDataStatus(where, allFields);
    }

    /**
     * 填充单表新增时的数据状态标记
     * 
     * @param condition 条件
     */
    public void fillTableCreateDataStatus(Map<String, Object> condition) {
        entityFillHandler.fillTableCreateDataStatus(condition, allFields);
    }

    // 不需要自动填充修改时的数据状态标记, 没有这样的业务场景
    // /**
    //  * 填充单表修改时的数据状态标记
    //  * 
    //  * @param ud 更新对象
    //  */
    // public void fillTableUpdateDataStatus(DbUpdate ud) {
    //     entityFillHandler.fillTableUpdateDataStatus(ud, allFields);
    // }

    /**
     * 填充单表逻辑删除时的数据状态标记(应将数据状态设置为无效)
     * 
     * @param ud 更新对象
     */
    public void fillTableLogicalDeleteDataStatus(DbUpdate ud) {
        entityFillHandler.fillTableLogicalDeleteDataStatus(ud, allFields);
    }

    /**
     * 填充单表创建参数(如创建人创建时间等)
     * 
     * @param model 实体对象
     */
    public void fillTableCreateParams(Map<String, Object> model) {
        entityFillHandler.fillTableCreateParams(model, allFields);
    }

    /**
     * 填充单表修改参数(如修改人修改时间等)
     * 
     * @param model 实体对象
     */
    public void fillTableUpdateParams(Map<String, Object> model) {
        entityFillHandler.fillTableUpdateParams(model, allFields);
    }

    /**
     * 填充单表修改参数(如修改人修改时间等)
     * 
     * @param ud 更新对象
     */
    public void fillTableUpdateParams(DbUpdate ud) {
        entityFillHandler.fillTableUpdateParams(ud, allFields);
    }

}
