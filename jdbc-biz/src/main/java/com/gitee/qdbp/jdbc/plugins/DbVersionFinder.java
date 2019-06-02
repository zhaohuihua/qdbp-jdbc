package com.gitee.qdbp.jdbc.plugins;

import org.springframework.jdbc.core.JdbcOperations;
import com.gitee.qdbp.jdbc.model.DbVersion;

public interface DbVersionFinder {
    
    DbVersion findDbVersion(JdbcOperations jdbcOperations);

}
