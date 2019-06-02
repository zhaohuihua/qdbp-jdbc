package com.gitee.qdbp.jdbc.plugins;

import java.util.Map;
import com.gitee.qdbp.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.condition.DbWhere;

/**
 * 实体业务处理接口<br>
 * 每个项目的处理方式不一样, 抽象成接口由各项目提供实现类
 *
 * @author zhaohuihua
 * @version 190601
 */
public interface ModelDataHandler {

    /** 生成主键编号 **/
    String generatePrimaryKeyCode(String tableName);

    /**
     * 是否存在逻辑删除字段
     * 
     * @param 
     * @return 否存在
     */
    boolean containsLogicalDeleteFlag(Map<String, String> fieldColumnMap);

    /**
     * 填充数据有效标记
     * 
     * @param condition 条件
     */
    void fillDataEffectiveFlag(Map<String, Object> condition, Map<String, String> fieldColumnMap);

    /**
     * 填充数据有效标记
     * 
     * @param where 条件
     */
    void fillDataEffectiveFlag(DbWhere where, Map<String, String> fieldColumnMap);

    /**
     * 填充数据有效标记
     * 
     * @param ud 更新对象
     */
    void fillDataEffectiveFlag(DbUpdate ud, Map<String, String> fieldColumnMap);

    /**
     * 填充数据无效标记
     * 
     * @param condition 条件
     */
    void fillDataIneffectiveFlag(Map<String, Object> condition, Map<String, String> fieldColumnMap);

    /**
     * 填充数据无效标记
     * 
     * @param where 条件
     */
    void fillDataIneffectiveFlag(DbWhere where, Map<String, String> fieldColumnMap);

    /**
     * 填充数据无效标记
     * 
     * @param ud 更新对象
     */
    void fillDataIneffectiveFlag(DbUpdate ud, Map<String, String> fieldColumnMap);

    /**
     * 填充创建参数
     * 
     * @param model 实体对象
     */
    void fillCreteParams(Map<String, Object> model, Map<String, String> fieldColumnMap);

    /**
     * 填充更新参数
     * 
     * @param model 实体对象
     */
    void fillUpdateParams(Map<String, Object> model, Map<String, String> fieldColumnMap);

    /**
     * 填充更新参数
     * 
     * @param ud 更新对象
     */
    void fillUpdateParams(DbUpdate ud, Map<String, String> fieldColumnMap);

    /**
     * 填充数据有效性标记
     * 
     * @param model 实体对象
     */
    void fillEffectiveFlag(Object model);

    /**
     * 填充创建参数
     * 
     * @param model 实体对象
     */
    void fillCreteParams(Object model);

    /**
     * 填充更新参数
     * 
     * @param model 实体对象
     */
    void fillUpdateParams(Object model);
}
