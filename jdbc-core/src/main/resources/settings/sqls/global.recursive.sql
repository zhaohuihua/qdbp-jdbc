-- <<recursive.find.children>> 递归查询所有子节点
-- 实测发现DB2不支持_开头的字段名, 不支持field AS alias, WITH AS中不支持INNER JOIN
#{keyword} recursive_temp_table(temp_parent) AS (
    SELECT #{codeField} temp_parent FROM #{tableName} WHERE #{codeField} IN ( ${startCodeCondition} ) 
    UNION ALL
    SELECT #{codeField} FROM #{tableName} A, recursive_temp_table B ON A.#{parentField} = B.temp_parent
)
SELECT #{selectFields} FROM #{tableName} WHERE #{codeField} IN (
    SELECT temp_parent FROM recursive_temp_table
)
<append prefix="AND">${whereCondition}</append>
<append prefix="ORDER BY">#{orderByCondition}</append>

