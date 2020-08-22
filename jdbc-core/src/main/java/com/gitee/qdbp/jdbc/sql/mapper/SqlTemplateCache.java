package com.gitee.qdbp.jdbc.sql.mapper;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.gitee.qdbp.able.exception.ResourceNotFoundException;
import com.gitee.qdbp.staticize.common.IMetaData;
import com.gitee.qdbp.staticize.common.IReader;
import com.gitee.qdbp.staticize.io.IReaderCreator;

class SqlTemplateCache {

    private static Map<String, IMetaData> SQL_DATA = new ConcurrentHashMap<>();
}
