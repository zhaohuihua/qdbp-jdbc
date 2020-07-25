package com.gitee.qdbp.jdbc.plugins;

import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;

/**
 * 实体的逻辑删除的数据状态填充策略<br>
 * 逻辑删除是指的使用一个字段来标记数据是否删除, 而并不物理删除记录的一种数据处理方式<br>
 * 实施数据状态填充策略的前提条件: 项目中所有表使用相同的字段名和字段值来标记数据状态<br>
 * <br>
 * 默认提供了SimpleEntityDataStatusFillStrategy和RandomNumberEntityDataStatusFillStrategy两种方式<br>
 * <b>SimpleEntityDataStatusFillStrategy</b>:<br>
 * 使用固定的数字或字母或枚举来标记数据状态, 如1表示有效2表示删除; 或E表示有效D表示删除<br>
 * 填充策略:<br>
 * 逻辑删除时填充数据状态=无效标记<br>
 * 创建时如果实体中没有指定数据状态, 则填充dataState=有效标记, 即默认插入有效记录<br>
 * 查询/更新时如果Where条件中没有数据状态, 也填充dataState=有效标记, 即默认只操作有效记录<br>
 * <b>RandomNumberEntityDataStatusFillStrategy</b>(使用随机数标记已删除记录):<br>
 * 上面的方式是常见的一种处理策略, 但存在一个问题, 就是无法多次删除含有唯一索引键的记录<br>
 * 例如deptCode是部门表的唯一索引键(deptCode+dataState), <br>
 * 新建A001的部门, 再删除, 再次新建A001的部门, 都能正常处理<br>
 * 但第二次新建的A001部门删除时就会报唯一索引冲突了, 此时已存在A001+无效标记的记录<br>
 * 可以使用随机数标记已删除记录的策略来解决这个问题<br>
 * 如1表示正常/其他大于9999的随机数表示已删除, 这样就能保证只有一条有效记录, 可以有多条无效记录<br>
 * 填充策略:<br>
 * 逻辑删除时填充数据状态=随机数<br>
 * 创建时如果实体中没有指定数据状态, 则填充dataState=有效标记, 即默认插入有效记录<br>
 * 创建时如果实体中数据状态=无效标记, 则填充数据状态=随机数<br>
 * 查询/更新时如果Where条件中没有数据状态, 也填充dataState=有效标记, 即默认只操作有效记录<br>
 * 查询/更新时如果Where条件中数据状态=无效标记, 则修改为dataState&gt;9999这样的条件<br>
 * <br>
 * 注意: 使用随机数标记已删除记录, 无效标记不能出现在IN条件中<br>
 * 即不能使用dataState IN (有效/作废/无效), 可以使用dataState IN (有效/作废)<br>
 * 注意: 使用随机数标记已删除记录, 那么数据状态字段必须是数字而不能是字母, Java中可以用数字或枚举<br>
 * 数据状态字段的随机数位数不能太小, 太小的话容易冲突, 建议设置为10位或更大的位数<br>
 * 注意: 如果使用随机数标记已删除记录, 那么如果除了正常和删除还有其他值时, 需要将已删除的码值设为最大<br>
 * 就是应该是1正常/2作废/3删除, 不要出现1正常/2删除/3作废的情况<br>
 * 因为随机数作为标记时, 在数据库中会大于任何其他码值<br>
 * 这样dataState=DataState.DELETED条件就能轻松转换为类似dataState&gt;9999的条件<br>
 * 也只有将删除码值设为最大, dataState&lt;DataState.DELETED条件才能符合逻辑<br>
 *
 * @author zhaohuihua
 * @version 20200725
 */
public interface EntityDataStatusFillStrategy<DS> {

    /** 逻辑删除字段名 **/
    String getLogicalDeleteField();

    /** 数据有效标记(正常状态) **/
    DS getDataEffectiveFlag();

    /** 数据无效标记(已删除状态) **/
    DS getDataIneffectiveFlag();

    /**
     * 填充查询时WHERE条件的数据状态标记<br>
     * SELECT * FROM table WHERE ... {在这里限制数据状态为有效}<br>
     * 如果查询条件中的数据状态为已删除, 则应根据逻辑删除策略将条件修改为已删除的过滤条件<br>
     * 如使用随机数作为标记时, 应设置过滤条件为 dataState >= logicalDeleteRandoms的最小值
     * 
     * @param tableAlias 表别名
     * @param where 条件
     * @param allFields 全字段
     */
    void fillQueryWhereDataStatus(DbWhere where, String tableAlias, AllFieldColumn<?> allFields);

    /**
     * 填充更新时WHERE条件的数据状态标记<br>
     * UPDATE table SET ... WHERE ... {在这里限制数据状态为有效}<br>
     * 如果查询条件中的数据状态为已删除, 则应根据逻辑删除策略将条件修改为已删除的过滤条件<br>
     * 如使用随机数作为标记时, 应设置过滤条件为 dataState >= logicalDeleteRandoms的最小值
     * 
     * @param where 条件
     * @param allFields 全字段
     */
    void fillUpdateWhereDataStatus(DbWhere where, AllFieldColumn<?> allFields);

    /**
     * 填充删除时WHERE条件的数据状态标记<br>
     * DELETE FOM table WHERE ... {在这里限制数据状态为有效}<br>
     * 如果查询条件中的数据状态为已删除, 则应根据逻辑删除策略将条件修改为已删除的过滤条件<br>
     * 如使用随机数作为标记时, 应设置过滤条件为 dataState >= logicalDeleteRandoms的最小值
     * 
     * @param where 条件
     * @param allFields 全字段
     */
    void fillDeleteWhereDataStatus(DbWhere where, AllFieldColumn<?> allFields);

    /**
     * 填充新增时的数据状态标记<br>
     * 增加INSERT时的数据状态字段的默认值设置为有效<br>
     * 如果实体中的数据状态为已删除, 则应根据逻辑删除策略将字段修改为已删除状态<br>
     * 如使用随机数作为标记时, 应将字段修改为 dataState = 根据logicalDeleteRandoms生成的随机数
     * 
     * @param entity 实体对象
     * @param allFields 全字段
     */
    void fillEntityCreateDataStatus(Map<String, Object> entity, AllFieldColumn<?> allFields);

    // 不需要自动填充修改时的数据状态标记, 修改时不涉及数据状态的变更, 如需修改应使用logicalDelete
    // /**
    //  * 填充修改时的数据状态标记
    //  * 
    //  * @param entity 实体对象
    //  * @param allFields 全字段
    //  */
    // void fillEntityUpdateDataStatus(Map<String, Object> entity, AllFieldColumn<?> allFields);

    // 不需要自动填充修改时的数据状态标记, 修改时不涉及数据状态的变更, 如需修改应使用logicalDelete
    // /**
    //  * 填充修改时的数据状态标记
    //  * 
    //  * @param ud 更新对象
    //  * @param allFields 全字段
    //  */
    // void fillEntityUpdateDataStatus(DbUpdate ud, AllFieldColumn<?> allFields);

    /**
     * 填充逻辑删除时的数据状态标记<br>
     * UPDATE table SET ..., dataStatus={在这里将数据状态设置为无效} WHERE ...<br>
     * 需要根据逻辑删除策略将字段修改为已删除状态<br>
     * 如使用随机数作为标记时, 应将字段修改为 dataState = 根据logicalDeleteRandoms生成的随机数
     * 
     * @param entity 实体对象
     * @param allFields 全字段
     */
    void fillLogicalDeleteDataStatus(Map<String, Object> entity, AllFieldColumn<?> allFields);

    /**
     * 填充逻辑删除时的数据状态标记<br>
     * UPDATE table SET ..., dataStatus={在这里将数据状态设置为无效} WHERE ...<br>
     * 需要根据逻辑删除策略将字段修改为已删除状态<br>
     * 如使用随机数作为标记时, 应将字段修改为 dataState = 根据logicalDeleteRandoms生成的随机数
     * 
     * @param ud 更新对象
     * @param allFields 全字段
     */
    void fillLogicalDeleteDataStatus(DbUpdate ud, AllFieldColumn<?> allFields);
}
