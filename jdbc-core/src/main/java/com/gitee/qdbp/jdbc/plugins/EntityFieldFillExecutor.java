package com.gitee.qdbp.jdbc.plugins;

import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.FieldScene;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 实体数据填充业务处理接口<br>
 * 每个项目的处理方式不一样, 调用EntityFieldFillStrategy接口由各项目提供实现类<br>
 * 命名说明:<br>
 * fillQueryXxx是查询时用到的, 兼容单表和表关联, 主要是给BaseQueryerImpl(JoinQueryerImpl/CrudDaoImpl的查询)调用<br>
 * fillEntityXxx是单表增删改查用到的, 只支持单表, 主要是给CrudDaoImpl调用<br>
 * 注意事项:<br>
 * fillXxxDataState和fillXxxParams都是成对出现的, 调用时也需要同时调用<br>
 * 为什么分开写, 是因为fillXxxDataState是无条件调用的, 而fillXxxParams受boolean fillParams的控制
 *
 * @author zhaohuihua
 * @version 190601
 */
public class EntityFieldFillExecutor {

    private AllFieldColumn<?> allFields;
    private EntityFieldFillStrategy fieldFillStrategy;
    private EntityDataStateFillStrategy<?> dataStateFillStrategy;

    public EntityFieldFillExecutor(AllFieldColumn<?> allFields, EntityFieldFillStrategy fieldFillStrategy, EntityDataStateFillStrategy<?> dataStateFillStrategy) {
        this.allFields = allFields;
        if (this.allFields.isEmpty()) {
            throw new IllegalArgumentException("fields is empty");
        }
        this.fieldFillStrategy = fieldFillStrategy;
        this.dataStateFillStrategy = dataStateFillStrategy;
    }

    /** 生成主键编号 **/
    public String generatePrimaryKeyCode(String tableName) {
        return fieldFillStrategy.generatePrimaryKeyCode(tableName);
    }

    /**
     * 单表是否支持逻辑删除
     * 
     * @return 是否支持
     */
    public boolean supportedTableLogicalDelete() {
        String logicalDeleteField = dataStateFillStrategy.getLogicalDeleteField();
        if (VerifyTools.isBlank(logicalDeleteField)) {
            return false;
        } else {
            return allFields.filter(FieldScene.UPDATE).containsByFieldName(logicalDeleteField);
        }
    }

    /**
     * 填充查询条件的数据状态标记
     * 
     * @param tableAlias 表别名
     * @param where 条件
     */
    public void fillQueryWhereDataState(DbWhere where, String tableAlias) {
        dataStateFillStrategy.fillQueryWhereDataState(where, tableAlias, allFields);
    }

    /**
     * 填充查询时的条件参数(如数据权限等)
     * 
     * @param tableAlias 表别名
     * @param where 查询条件
     */
    public void fillQueryWhereParams(DbWhere where, String tableAlias) {
        fieldFillStrategy.fillQueryWhereParams(where, tableAlias, allFields);
    }

    /**
     * 填充单表Where条件的数据状态标记
     * 
     * @param where 条件
     */
    public void fillUpdateWhereDataState(DbWhere where) {
        dataStateFillStrategy.fillUpdateWhereDataState(where, allFields);
    }

    /**
     * 填充更新时的查询条件参数(如数据权限等)
     * 
     * @param where 查询条件
     */
    public void fillUpdateWhereParams(DbWhere where) {
        fieldFillStrategy.fillUpdateWhereParams(where, allFields);
    }

    /**
     * 填充删除时的查询条件参数(如数据权限等)
     * 
     * @param where 查询条件
     */
    public void fillDeleteWhereParams(DbWhere where) {
        fieldFillStrategy.fillDeleteWhereParams(where, allFields);
    }

    /**
     * 填充单表Where条件的数据状态标记
     * 
     * @param where 条件
     */
    public void fillDeleteWhereDataState(DbWhere where) {
        dataStateFillStrategy.fillDeleteWhereDataState(where, allFields);
    }

    /**
     * 填充单表新增时的数据状态标记
     * 
     * @param condition 条件
     */
    public void fillEntityCreateDataState(Map<String, Object> condition) {
        dataStateFillStrategy.fillEntityCreateDataState(condition, allFields);
    }

    /**
     * 填充单表创建参数(如创建人创建时间等)
     * 
     * @param entity 实体对象
     */
    public void fillEntityCreateParams(Map<String, Object> entity) {
        fieldFillStrategy.fillEntityCreateParams(entity, allFields);
    }

    /**
     * 填充单表修改参数(如修改人修改时间等)
     * 
     * @param entity 实体对象
     */
    public void fillEntityUpdateParams(Map<String, Object> entity) {
        fieldFillStrategy.fillEntityUpdateParams(entity, allFields);
    }

    // 不需要自动填充修改时的数据状态标记, 没有这样的业务场景
    // /**
    //  * 填充单表修改时的数据状态标记
    //  * 
    //  * @param ud 更新对象
    //  */
    // public void fillEntityUpdateDataState(DbUpdate ud) {
    //     entityFieldFillStrategy.fillEntityUpdateDataState(ud, allFields);
    // }

    /**
     * 填充单表修改参数(如修改人修改时间等)
     * 
     * @param ud 更新对象
     */
    public void fillEntityUpdateParams(DbUpdate ud) {
        fieldFillStrategy.fillEntityUpdateParams(ud, allFields);
    }

    /**
     * 填充单表逻辑删除时的数据状态标记(应将数据状态设置为无效)
     * 
     * @param ud 更新对象
     */
    public void fillLogicalDeleteDataState(DbUpdate ud) {
        dataStateFillStrategy.fillLogicalDeleteDataState(ud, allFields);
    }

    /**
     * 填充逻辑删除时的参数(如修改人修改时间等)
     * 
     * @param entity 实体对象
     */
    public void fillLogicalDeleteParams(Map<String, Object> entity) {
        fieldFillStrategy.fillLogicalDeleteParams(entity, allFields);
    }

    /**
     * 填充逻辑删除时的参数(如修改人修改时间等)
     * 
     * @param ud 更新对象
     */
    public void fillLogicalDeleteParams(DbUpdate ud) {
        fieldFillStrategy.fillLogicalDeleteParams(ud, allFields);
    }

}
