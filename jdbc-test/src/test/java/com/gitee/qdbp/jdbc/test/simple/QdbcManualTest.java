package com.gitee.qdbp.jdbc.test.simple;

import com.alibaba.druid.pool.DruidDataSource;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.biz.QdbcBootImpl;
import com.gitee.qdbp.jdbc.support.AutoDruidDataSource;
import com.gitee.qdbp.jdbc.test.enums.SettingState;
import com.gitee.qdbp.jdbc.test.model.SysSettingEntity;
import com.gitee.qdbp.tools.utils.JsonTools;

/**
 * 纯手工连接数据库测试类<br>
 * 这里使用的是DbPluginContainer的默认插件, 未提供的特性包括:<br>
 * • TableInfoScans.commonFieldResolver: 不会将公共字段放在查询列表的最后<br>
 * • EntityFieldFillStrategy.entityFillBizResolver: 不会自动填充创建人/创建时间等业务参数<br>
 * • EntityDataStateFillStrategy: 不会自动填充数据状态, 不支持逻辑删除<br>
 * 需要通过DbPluginContainer.defaults().registerXxx针对具体项目进行定制配置。<br>
 *
 * @author zhaohuihua
 * @version 20200809
 */
public class QdbcManualTest {

    public static void main(String[] args) {
        // 切换数据库配置即可连接不同的数据库
        testDbOperate("mysql:develop:dev888pwd@127.0.0.1:3306/qdbpdev");
        testDbOperate("oracle:qdbpdev:dev888pwd@127.0.0.1:1521/orcl");
        testDbOperate("db2:db2admin:dev888pwd@127.0.0.1:50000/platform:qdbpdev");
    }

    private static void testDbOperate(String jdbcUrl) {
        // 1. 获取数据源对象
        try (DruidDataSource datasource = AutoDruidDataSource.buildWith(jdbcUrl);) {
            // 2. 获取数据库操作的构造器对象
            QdbcBoot boot = QdbcBootImpl.buildWith(datasource);
            // 3. 针对具体的实体, 生成增删改查DAO对象
            CrudDao<SysSettingEntity> dao = boot.buildCrudDao(SysSettingEntity.class);
            // 开始业务测试
            testSettingEntity(dao);
        }
    }

    private static void testSettingEntity(CrudDao<SysSettingEntity> dao) {
        { // 测试删除
            DbWhere where = new DbWhere();
            where.on("name", "starts", "HelloQdbc-");
            dao.physicalDelete(where);
        }
        String id;
        { // 测试新增
            SysSettingEntity entity = new SysSettingEntity();
            entity.setName("HelloQdbc-1");
            entity.setValue("测试新增 HelloQdbc-1");
            entity.setVersion(1);
            entity.setState(SettingState.ENABLED);
            id = dao.insert(entity);
            System.out.println("id = " + id);
        }
        { // 测试修改
            SysSettingEntity entity = new SysSettingEntity();
            entity.setId(id);
            entity.setValue("测试修改 HelloQdbc-1");
            entity.setVersion(2);
            dao.update(entity);
        }
        { // 测试查询
            SysSettingEntity entity = dao.findById(id);
            System.out.println(JsonTools.toLogString(entity));
        }
    }
}
