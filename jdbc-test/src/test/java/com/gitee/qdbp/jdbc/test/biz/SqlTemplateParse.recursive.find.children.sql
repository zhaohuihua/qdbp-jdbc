-- 实测发现DB2不支持_开头的字段名, 不支持field AS alias, WITH AS中不支持INNER JOIN
WITH RECURSIVE recursive_temp_table(temp_parent) AS (
    SELECT DEPT_CODE temp_parent FROM TEST_DEPARTMENT_CORE_INFO WHERE DEPT_CODE IN ( DEPT_CODE IN('D0000001'/*$1*/,'D0000002'/*$2*/) ) 
    UNION ALL
    SELECT DEPT_CODE FROM TEST_DEPARTMENT_CORE_INFO A, recursive_temp_table B ON A.PARENT_CODE = B.temp_parent
)
SELECT ID,TENANT_CODE,DEPT_CODE,DEPT_NAME,PARENT_CODE,SORT_INDEX,CREATOR_NAME,CREATOR_ID,CREATE_TIME,DATA_STATE FROM TEST_DEPARTMENT_CORE_INFO WHERE DEPT_CODE IN (
    SELECT temp_parent FROM recursive_temp_table
)
WHERE DATA_STATE='1'/*$3*/
ORDER BY PARENT_CODE ASC,SORT_INDEX ASC