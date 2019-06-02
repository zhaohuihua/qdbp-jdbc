package com.gitee.qdbp.jdbc.plugins;

import java.util.List;
import java.util.Map;
import com.gitee.qdbp.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.FieldColumn;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 实体业务处理接口<br>
 * 每个项目的处理方式不一样, 抽象成接口由各项目提供实现类
 *
 * @author zhaohuihua
 * @version 190601
 */
public class ModelDataExecutor {

    private Map<String, String> fieldColumnMap;
    private ModelDataHandler modelDataHandler;

    public ModelDataExecutor(Class<?> clazz) {
        List<FieldColumn> columns = DbTools.parseFieldColumns(clazz);
        if (VerifyTools.isBlank(columns)) {
            throw new IllegalArgumentException("columns is empty");
        }
        this.fieldColumnMap = DbTools.toFieldColumnMap(columns);
        this.modelDataHandler = DbPluginContainer.global.getModelDataHandler();
    }

    public ModelDataExecutor(List<FieldColumn> columns, ModelDataHandler modelDataHandler) {
        this.fieldColumnMap = DbTools.toFieldColumnMap(columns);
        this.modelDataHandler = modelDataHandler;
    }
    
    /** 生成主键编号 **/
    public String generatePrimaryKeyCode(String tableName) {
        return modelDataHandler.generatePrimaryKeyCode(tableName);
    }

    /**
     * 是否存在逻辑删除字段
     * 
     * @return 否存在
     */
    public boolean containsLogicalDeleteFlag() {
        return modelDataHandler.containsLogicalDeleteFlag(fieldColumnMap);
    }

    /**
     * 填充数据有效标记
     * 
     * @param condition 条件
     */
    public void fillDataEffectiveFlag(Map<String, Object> condition) {
        modelDataHandler.fillDataEffectiveFlag(condition, fieldColumnMap);
    }

    /**
     * 填充数据有效标记
     * 
     * @param where 条件
     */
    public void fillDataEffectiveFlag(DbWhere where) {
        modelDataHandler.fillDataEffectiveFlag(where, fieldColumnMap);
    }

    /**
     * 填充数据有效标记
     * 
     * @param ud 更新对象
     */
    public void fillDataEffectiveFlag(DbUpdate ud) {
        modelDataHandler.fillDataEffectiveFlag(ud, fieldColumnMap);
    }

    /**
     * 填充数据无效标记
     * 
     * @param condition 条件
     */
    public void fillDataIneffectiveFlag(Map<String, Object> condition) {
        modelDataHandler.fillDataIneffectiveFlag(condition, fieldColumnMap);
    }

    /**
     * 填充数据无效标记
     * 
     * @param where 条件
     */
    public void fillDataIneffectiveFlag(DbWhere where) {
        modelDataHandler.fillDataIneffectiveFlag(where, fieldColumnMap);
    }

    /**
     * 填充数据无效标记
     * 
     * @param ud 更新对象
     */
    public void fillDataIneffectiveFlag(DbUpdate ud) {
        modelDataHandler.fillDataIneffectiveFlag(ud, fieldColumnMap);
    }

    /**
     * 填充创建参数
     * 
     * @param model 实体对象
     */
    public void fillCreteParams(Map<String, Object> model) {
        modelDataHandler.fillCreteParams(model);
    }

    /**
     * 填充更新参数
     * 
     * @param model 实体对象
     */
    public void fillUpdateParams(Map<String, Object> model) {
        modelDataHandler.fillUpdateParams(model);
    }

    /**
     * 填充更新参数
     * 
     * @param ud 更新对象
     */
    public void fillUpdateParams(DbUpdate ud) {
        modelDataHandler.fillUpdateParams(ud);
    }

    /**
     * 填充数据有效性标记
     * 
     * @param model 实体对象
     */
    public void fillEffectiveFlag(Object model) {
        modelDataHandler.fillEffectiveFlag(model);
    }

    /**
     * 填充创建参数
     * 
     * @param model 实体对象
     */
    public void fillCreteParams(Object model) {
        modelDataHandler.fillCreteParams(model);
    }

    /**
     * 填充更新参数
     * 
     * @param model 实体对象
     */
    public void fillUpdateParams(Object model) {
        modelDataHandler.fillUpdateParams(model);
    }
}
