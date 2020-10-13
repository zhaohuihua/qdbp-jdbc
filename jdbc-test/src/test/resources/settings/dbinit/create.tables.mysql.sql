
DROP TABLE IF EXISTS TEST_SETTING;
DROP TABLE IF EXISTS TEST_LOGGER;
DROP TABLE IF EXISTS TEST_ROLE_CORE_INFO;
DROP TABLE IF EXISTS TEST_USER_CORE_INFO;
DROP TABLE IF EXISTS TEST_USER_ROLE_REF;
DROP TABLE IF EXISTS TEST_DEPARTMENT_CORE_INFO;

CREATE TABLE IF NOT EXISTS TEST_SETTING (
	ID VARCHAR(50) NOT NULL COMMENT '主键', 
	NAME VARCHAR(30) NOT NULL COMMENT '名称', 
	VALUE VARCHAR(50) NOT NULL COMMENT '文本', 
	VERSION INTEGER(8) NOT NULL DEFAULT 1 COMMENT '版本号', 
	REMARK VARCHAR(200) COMMENT '备注', 
	STATE TINYINT(1) NOT NULL COMMENT '状态', 
	CREATE_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', 
	UPDATE_TIME TIMESTAMP COMMENT '修改时间', 
	DATA_STATE INTEGER(10) NOT NULL DEFAULT 1 COMMENT '数据状态:1为正常|随机数为删除', 
	PRIMARY KEY (ID), 
	UNIQUE KEY TEST_SETTING_NAME(NAME, DATA_STATE)
) COMMENT='系统配置表';

CREATE TABLE IF NOT EXISTS TEST_LOGGER (
	ID VARCHAR(50) NOT NULL COMMENT '主键',
	NAME VARCHAR(30) COMMENT '名称',
	CONTENT TEXT NOT NULL COMMENT '内容',
	SORT_INDEX INTEGER(8) NOT NULL DEFAULT 1 COMMENT '排序号',
	CREATE_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	DATA_STATE INTEGER(10) NOT NULL DEFAULT 1 COMMENT '数据状态:1为正常|随机数为删除',
	PRIMARY KEY (ID)
) COMMENT='操作日志表';


CREATE TABLE IF NOT EXISTS TEST_ROLE_CORE_INFO (
   ID                   VARCHAR(50) NOT NULL COMMENT '角色ID',
   TENANT_CODE          VARCHAR(50) NOT NULL DEFAULT '0' COMMENT '租户编号',
   USER_TYPE            TINYINT(1) NOT NULL DEFAULT 1 COMMENT '用户类型:1.管理员|2.用户(1.admin|2.user)',
   ROLE_NAME            VARCHAR(50) NOT NULL COMMENT '角色名称(name)',
   ROLE_DESC            VARCHAR(200) COMMENT '描述',
   SORT_INDEX           INTEGER(8) COMMENT '排序号(越小越靠前)',
   CREATOR_ID           VARCHAR(50) COMMENT '创建人ID',
   CREATE_TIME          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   DEFAULTS             BIT(1) NOT NULL DEFAULT 0 COMMENT '默认角色(如果用户没有任何角色,默认会赋予该角色)',
   OPTIONS              TEXT COMMENT '选项(options)',
   DATA_STATE           INTEGER(10) NOT NULL DEFAULT 1 COMMENT '数据状态:1为正常|随机数为删除',
   PRIMARY KEY (ID),
   UNIQUE KEY UQ_ROLE_NAME (TENANT_CODE, ROLE_NAME, DATA_STATE)
) COMMENT='角色信息表(pkg:permission)';

CREATE TABLE IF NOT EXISTS TEST_USER_CORE_INFO (
   ID                   VARCHAR(50) NOT NULL COMMENT '用户ID',
   TENANT_CODE          VARCHAR(50) NOT NULL DEFAULT '0' COMMENT '租户编号',
   USER_TYPE            TINYINT(1) NOT NULL DEFAULT 1 COMMENT '用户类型:1.管理员|2.用户(1.admin|2.user)',
   DEPT_CODE            VARCHAR(50) NOT NULL DEFAULT '0' COMMENT '部门编号',
   USER_CODE            VARCHAR(50) COMMENT '账号/工号',
   USER_NAME            VARCHAR(50) COMMENT '登录用户名',
   NICK_NAME            VARCHAR(50) COMMENT '昵称',
   REAL_NAME            VARCHAR(20) COMMENT '真实姓名',
   PHONE                VARCHAR(20) COMMENT '电话',
   EMAIL                VARCHAR(50) COMMENT '邮箱',
   GENDER               TINYINT(1) NOT NULL COMMENT '性别:0.未知|1.男|2.女(Gender:0.unknown|1.male|2.female)',
   PHOTO                VARCHAR(120) COMMENT '头像',
   CITY                 VARCHAR(50) COMMENT '城市',
   IDENTITY             VARCHAR(20) COMMENT '身份证',
   PASSWORD             VARCHAR(120) COMMENT '密码',
   CREATE_TIME          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   SUPERMAN             BIT(1) NOT NULL DEFAULT 0 COMMENT '是否为超级用户',
   USER_STATE           TINYINT(1) NOT NULL DEFAULT 0 COMMENT '用户状态:0.正常|1.锁定|2.待激活|3.注销(UserState:0.normal|1.locked|2.unactivated|3.logoff)',
   USER_SOURCE          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '用户来源:0.未知|1.录入(UserSource:0.unknown|1.input)',
   OPTIONS              TEXT COMMENT '选项(options)',
   DATA_STATE           INTEGER(10) NOT NULL DEFAULT 1 COMMENT '数据状态:1为正常|随机数为删除',
   PRIMARY KEY (ID),
   UNIQUE KEY UQ_USER_CODE (TENANT_CODE, USER_TYPE, USER_CODE, DATA_STATE),
   UNIQUE KEY UQ_USER_NAME (TENANT_CODE, USER_TYPE, USER_NAME, DATA_STATE),
   UNIQUE KEY UQ_USER_PHONE (TENANT_CODE, USER_TYPE, PHONE, DATA_STATE),
   UNIQUE KEY UQ_USER_EMAIL (TENANT_CODE, USER_TYPE, EMAIL, DATA_STATE)
) COMMENT='用户基础信息表(pkg:personnel)';


CREATE TABLE IF NOT EXISTS TEST_USER_ROLE_REF (
   ID                   VARCHAR(50) NOT NULL COMMENT '主键ID',
   USER_ID              VARCHAR(50) NOT NULL COMMENT '用户ID(list)',
   ROLE_ID              VARCHAR(50) NOT NULL COMMENT '角色ID(list)',
   DATA_STATE           INTEGER(10) NOT NULL DEFAULT 1 COMMENT '数据状态:1为正常|随机数为删除',
   PRIMARY KEY (ID),
   UNIQUE KEY UQ_USER_ROLE_REF (USER_ID, ROLE_ID, DATA_STATE)
) COMMENT='用户角色关系表(pkg:permission)';

CREATE INDEX IDX_USER_ID ON TEST_USER_ROLE_REF ( USER_ID );
CREATE INDEX IDX_ROLE_ID ON TEST_USER_ROLE_REF ( ROLE_ID );


CREATE TABLE TEST_DEPARTMENT_CORE_INFO (
  ID VARCHAR(50) NOT NULL COMMENT '部门ID',
  TENANT_CODE VARCHAR(50) NOT NULL DEFAULT '0' COMMENT '租户编号',
  DEPT_CODE VARCHAR(50) NOT NULL COMMENT '部门编号',
  DEPT_NAME VARCHAR(50) NOT NULL COMMENT '部门名称',
  PARENT_CODE VARCHAR(50) NOT NULL COMMENT '上级部门编号',
  SORT_INDEX INTEGER(8) DEFAULT NULL COMMENT '排序号(越小越靠前)',
  CREATOR_ID VARCHAR(50) DEFAULT NULL COMMENT '创建人ID',
  CREATE_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  DATA_STATE INTEGER(10) NOT NULL DEFAULT 1 COMMENT '数据状态:1为正常|其他为删除',
  PRIMARY KEY (ID),
  UNIQUE KEY AK_UQ_DEPT_CODE (TENANT_CODE,DEPT_CODE,DATA_STATE),
  UNIQUE KEY AK_UQ_DEPT_NAME (TENANT_CODE,PARENT_CODE,DEPT_NAME,DATA_STATE)
) COMMENT='部门信息表(pkg:personnel)';
