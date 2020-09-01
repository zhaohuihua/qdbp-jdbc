package com.gitee.qdbp.jdbc.api;

import java.util.List;

public interface SqlDao {

    <T> T find(String sqlId, Class<T> resultType);

    <T> T find(String sqlId, Object params, Class<T> resultType);

    <T> List<T> list(String sqlId, Class<T> resultType);

    <T> List<T> list(String sqlId, Object params, Class<T> resultType);

    int insert(String sqlId);

    int insert(String sqlId, Object params);

    int update(String sqlId);

    int update(String sqlId, Object params);

    int delete(String sqlId);

    int delete(String sqlId, Object params);
}
