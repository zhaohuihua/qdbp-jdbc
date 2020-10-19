-- 标准递归语法都差不多
-- 前缀关键字有所不同: MySQL8,PostgreSQL,SQLite的是WITH RECURSIVE; DB2,SqlServer的是WITH
-- 实测发现DB2
-- 不支持_开头的字段名, 于是将_temp_parent_改为temp_parent
-- 不支持field AS alias, 于是去掉AS改成field alias
-- WITH AS中不支持INNER JOIN, 于是改成 A, B WHERE A.PARENT=B.temp_parent

-- <<recursive.find.children>> 递归查询所有子节点, 标准递归语法
-- <supports>mysql.8,mariadb.10.2.2,postgresql,db2,sqlserver,sqlite.3.8.3</supports>
${db.config.get('qdbc.recursive.keyword')} recursive_temp_table(temp_parent) AS (
    SELECT ${codeColumn} temp_parent FROM ${tableName} 
    	WHERE <sql:in column="${codeColumn}" value="${startCodes}" />
    UNION ALL
    SELECT ${codeColumn} FROM ${tableName} A, recursive_temp_table B
    	WHERE A.${parentColumn} = B.temp_parent
)
SELECT ${selectColumns}
	FROM ${tableName} 
WHERE ${codeColumn} IN ( SELECT temp_parent FROM recursive_temp_table )
	<append prefix="AND">#{whereCondition}</append>
<append prefix="ORDER BY">#{orderByCondition}</append>


-- <<recursive.find.children:oracle>> 递归查询所有子节点, oracle专用递归语法
SELECT ${selectColumns}
	FROM ${tableName}
START WITH <sql:in column="${codeColumn}" value="${startCodes}" />
	CONNECT BY PRIOR ${codeColumn} = ${parentColumn}
	<append prefix="AND">#{whereCondition}</append>
<append prefix="ORDER BY">#{orderByCondition}</append>


-- <<recursive.find.children>> 递归查询所有子节点, 使用存储过程实现的递归查询
CALL RECURSIVE_FIND_CHILDREN(
      /*startCodes=*/ ${startCodes},
      /*codeColumn=*/ ${codeColumn},
    /*parentColumn=*/ ${parentColumn},
       /*tableName=*/ ${tableName},
   /*selectColumns=*/ ${selectColumns},
  /*whereCondition=*/ ${whereCondition},
/*orderByCondition=*/ ${orderByCondition}
);

