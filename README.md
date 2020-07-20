qdbp-jdbc简称为qdbc，是一个数据库ORM框架，完善之后，希望可以成为MyBatis的替代品。

底层没有打算自己做，目前是基于Spring的JdbcTemplate的。

https://gitee.com/qdbp/qdbp-jdbc 求star~~

https://yuque.com/zhaohuihua/qdbc 文档中心


又造了一个轮子。

为什么会有这个项目，自然是现有的框架用得不够爽：
* Hibernate太重量级，学习成本高，用得不好容易出现性能问题；
* Spring的JdbcTemplate太基础，只做了预编译参数和结果映射；
* MyBatis要生成一大堆xml文件，难以维护，尤其表结构变更时，重新生成的xml就需要跟以前的比对，很是麻烦；
* 即使有MyBatisGenerator之类的工具来辅助生成代码，但由于代码是提前生成的，结构变更时依然麻烦；
* 还有一个问题，所有的框架输出预编译参数的SQL日志都是用问号代替参数，
如果参数很多，根据日志到数据库中重现问题的时候简直痛苦。
更多讨论见《[ORM框架的痛点讨论](https://yuque.com/zhaohuihua/qdbc/gebddu)》。

本项目的独创内容：
* 对于单表增删改查，多表关联的基本查询，以及所涉及到的大于/小于/不等于/like/in等基本条件，完全不要写sql或xml！《[基本用法简介](https://yuque.com/zhaohuihua/qdbc/vfkzgg)》
* 通过DbWhere与实体类结合，自动生成兼容多数据库的SQL，《实现原理介绍》(待补充)
* 日志中打印的SQL，复制到数据库就能执行：《[SQL日志问题排查的痛点](https://yuque.com/zhaohuihua/qdbc/cwk1uf)》
* SqlBuffer，一行内实现SQL文本与变量的统一：《[关于代码中SQL书写方式的思考](https://yuque.com/zhaohuihua/qdbc/bt2ryu)》
* 批量日志采样，批量操作日志只能在开启或关闭之间二选一？《[关于批量日志的思考](https://yuque.com/zhaohuihua/qdbc/kgo239)》

复杂的查询或统计，还是要写sql或xml：
* 一是因为太复杂无法封装，强行封装就会变成hibernate，学习成本骤升，得不偿失；
* 二是独立SQL方便DBA审查，而恰恰只有这部分复杂语句才是需要审查的。
这方面也存在优化点：
* MyBatis中大量的单表增删改查语句，对DBA审查来说只是一种干扰，并不会过多关注；
* 多种数据库就需要多套模板，但实际情况往往是多套模板大体相同，只有少量差异；
* SQL模板首先应该是SQL，然后其中有一些XML的判断条件和循环语句，而不应该是XML格式。
详见《[SQL模板](https://yuque.com/zhaohuihua/qdbc/bvk5gy)》(开发中)。
