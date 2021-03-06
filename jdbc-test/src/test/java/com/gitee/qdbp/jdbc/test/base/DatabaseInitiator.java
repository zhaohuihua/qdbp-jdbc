package com.gitee.qdbp.jdbc.test.base;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.model.DbType;

@Service
public class DatabaseInitiator extends AbstractTestNGSpringContextTests {

    @Value("#{setting['database.init.enabled']}")
    private boolean enabled;
    @Autowired
    private QdbcBoot qdbcBoot;

    @PostConstruct
    public void init() {
        if (!enabled) {
            return;
        }

        DbType dbType = qdbcBoot.getSqlDialect().getDbVersion().getDbType();
        String path = "settings/dbinit/create.tables." + dbType.name().toLowerCase() + ".sql";
        qdbcBoot.getSqlBufferJdbcOperations().executeSqlScript(path);
    }
}
