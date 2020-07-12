package com.gitee.qdbp.jdbc.test.biz;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.test.model.SysLoggerEntity;
import com.gitee.qdbp.tools.utils.StringTools;

@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class BatchInsertUpdateTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private QdbcBoot qdbcBoot;
    private CrudDao<SysLoggerEntity> dao;
    private int total = 855;
    private int defaultIndex;
    private int updateTotal = 25;

    @PostConstruct
    public void init() {
        this.dao = qdbcBoot.buildCrudDao(SysLoggerEntity.class);

        DbWhere where = new DbWhere();
        where.on("name", "starts", "BatchTest");
        dao.physicalDelete(where, false);
    }

    @Test(priority = 101)
    public void testBatchInsert() {
        // 构造实体数据
        List<SysLoggerEntity> entities = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            SysLoggerEntity entity = new SysLoggerEntity();
            String index = StringTools.pad(i, 4);
            entity.setName("BatchTest-Insert-" + index);
            entity.setContent("BatchTest-Content-" + index);
            if (i % 5 == 0) {
                defaultIndex++;
                // 测试默认值
                // 由于数据库不允许为空, 所以必须使用@ColumnDefault注解在sortIndex字段上设置默认值
                // 否则会报Column 'SORT_INDEX' cannot be null
                entity.setSortIndex(null);
            } else {
                entity.setSortIndex(i * 10);
            }
            entities.add(entity);
        }
        // 执行批量新增
        dao.inserts(entities, true);
    }

    @Test(priority = 102, dependsOnMethods = "testBatchInsert")
    public void testAllRecordTotal() {
        // 检查记录总数
        DbWhere where = new DbWhere();
        where.on("name", "starts", "BatchTest-Insert");
        int count = dao.count(where);
        Assert.assertEquals(count, total, "TotalRecord");
    }

    @Test(priority = 103, dependsOnMethods = "testBatchInsert")
    public void testDefaultIndexTotal() {
        // 检查序号为默认值的记录数
        DbWhere where = new DbWhere();
        where.on("name", "starts", "BatchTest-Insert");
        where.on("sortIndex", "=", 1);
        int count = dao.count(where);
        Assert.assertEquals(count, defaultIndex, "DefaultIndexRecord");
    }

    @Test(priority = 201, dependsOnMethods = "testBatchInsert")
    public void testBatchUpdate() {
        // 查询一些数据的ID, 用来执行批量更新
        DbWhere where = new DbWhere();
        where.on("name", "starts", "BatchTest-Insert");
        where.on("sortIndex", ">", 1);
        OrderPaging odpg = OrderPaging.of(new Paging(3, updateTotal), "sortIndex");
        PageList<String> ids = dao.listFieldValues("id", false, where, odpg, String.class);
        // 构造批量更新的实体数据
        List<SysLoggerEntity> changed = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            SysLoggerEntity entity = new SysLoggerEntity();
            entity.setId(id);
            String index = StringTools.pad(i + 1, 4);
            entity.setName("BatchTest-Update-" + index);
            entity.setContent("BatchTest-Content-" + index);
            changed.add(entity);
        }
        // 执行批量更新
        dao.updates(changed, true);
    }

    @Test(priority = 202, dependsOnMethods = { "testBatchInsert", "testBatchUpdate" })
    public void testUpdateTotal() {
        // 检查更新成功的记录数
        DbWhere where = new DbWhere();
        where.on("name", "starts", "BatchTest-Update");
        int count = dao.count(where);
        Assert.assertEquals(count, updateTotal, "UpdateRecord");
    }
}
