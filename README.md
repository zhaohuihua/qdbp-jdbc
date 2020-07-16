qdbp-jdbc简称为qdbc，是一个数据库orm框架，完善之后，希望可以成为MyBatis的替代品。

底层没有打算自己做，目前是基于Spring的JdbcTemplate的。

https://gitee.com/qdbp/qdbp-jdbc 求star~~

https://yuque.com/zhaohuihua/qdbc 文档中心


又造了一个轮子。

为什么会有这个项目，自然是现有的框架用得不爽啦！
* Hibernate太重量级，学习成本高，用得不好容易出现性能问题；
* Spring的JdbcTemplate太基础，只做了预编译参数和结果映射；
* MyBatis要生成一大堆xml文件，难以维护，尤其表结构变更时，重新生成的xml就需要跟以前的比对，很是麻烦；
* 即使有MyBatisGenerator之类的工具来辅助生成代码，但由于代码是提前生成的，结构变更时依然麻烦；
* 还有一个问题，所有的框架输出预编译参数的SQL日志都是用问号代替参数，
如果参数很多，根据日志到数据库中重现问题的时候简直痛苦。

综上所述，我的目标就是：
1. 对于单表增删改查，多表关联的基本查询，以及所涉及到的大于/小于/不等于/like/in等基本条件，不要写sql或xml；
2. 日志中打印的SQL，复制到数据库就能执行；
3. 
```sql
-- 面对这么多问号要一个个替换, 想必大家也都一头问号
>>DEBUG 20:06:07.358 | main | Executing prepared SQL statement [
SELECT * FROM sys_user_core_info
WHERE USER_TYPE=? AND USER_STATE  IN (?,?) AND CREATE_TIME>=? AND DATA_STATE=?] | o.s.j.c.JdbcTemplate.execute

-- 下面这种才是我们程序员想要的: 根据数据库类型生成不同的SQL(例如日期函数), 复制到数据库中就能执行
>>DEBUG 20:06:07.354 | main | Executing SQL query:
  SELECT * FROM sys_user_core_info
  WHERE USER_TYPE=1/*$1*/ AND USER_STATE  IN (0/*$2*/,1/*$3*/) 
    AND CREATE_TIME>='2017-01-01 00:00:00.000'/*$4*/ AND DATA_STATE=0/*$5*/ | c.g.q.j.b.SqlBufferJdbcOperationsImpl
```

复杂的查询或统计，还是要写sql或xml：
* 一是因为太复杂无法封装，强行封装就会变成hibernate，学习成本骤升，得不偿失；
* 二是独立SQL方便DBA审查，而恰恰只有这部分才是需要审查的；
* MyBatis中很多单表增删改查语句，对DBA审查来说只是干扰，并不会过多关注。
