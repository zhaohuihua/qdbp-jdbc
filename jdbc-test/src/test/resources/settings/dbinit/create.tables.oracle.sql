
DROP TABLE "TEST_SETTING" CASCADE CONSTRAINTS;
DROP TABLE "TEST_LOGGER" CASCADE CONSTRAINTS;
DROP TABLE "TEST_ROLE_CORE_INFO" CASCADE CONSTRAINTS;
DROP TABLE "TEST_USER_CORE_INFO" CASCADE CONSTRAINTS;
DROP TABLE "TEST_USER_ROLE_REF" CASCADE CONSTRAINTS;
DROP TABLE "TEST_DEPARTMENT_CORE_INFO" CASCADE CONSTRAINTS;


CREATE TABLE TEST_SETTING (
	ID VARCHAR2(50) NOT NULL, 
	NAME VARCHAR2(30) NOT NULL, 
	VALUE VARCHAR2(50) NOT NULL, 
	VERSION NUMBER(8) DEFAULT 1 NOT NULL, 
	REMARK VARCHAR2(200), 
	STATE NUMBER(3) NOT NULL, 
	CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	UPDATE_TIME TIMESTAMP, 
	DATA_STATE NUMBER(10) DEFAULT 1 NOT NULL, 
	CONSTRAINT TEST_SETTING_PK PRIMARY KEY (ID), 
	CONSTRAINT TEST_SETTING_NAME UNIQUE (NAME,DATA_STATE)
);
COMMENT ON TABLE TEST_SETTING IS '系统配置表';
COMMENT ON COLUMN TEST_SETTING.ID IS '主键';
COMMENT ON COLUMN TEST_SETTING.NAME IS '名称';
COMMENT ON COLUMN TEST_SETTING.VALUE IS '文本';
COMMENT ON COLUMN TEST_SETTING.VERSION IS '版本号';
COMMENT ON COLUMN TEST_SETTING.REMARK IS '备注';
COMMENT ON COLUMN TEST_SETTING.STATE IS '状态';
COMMENT ON COLUMN TEST_SETTING.CREATE_TIME IS '创建时间';
COMMENT ON COLUMN TEST_SETTING.UPDATE_TIME IS '修改时间';
COMMENT ON COLUMN TEST_SETTING.DATA_STATE IS '数据状态:1为正常|随机数为删除';


CREATE TABLE TEST_LOGGER (
	ID VARCHAR2(50) NOT NULL,
	NAME VARCHAR2(30) NOT NULL,
	CONTENT VARCHAR2(4000) NOT NULL,
	SORT_INDEX NUMBER(8) DEFAULT 1 NOT NULL,
	CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	DATA_STATE NUMBER(10) DEFAULT 1 NOT NULL,
	CONSTRAINT TEST_LOGGER_PK PRIMARY KEY (ID)
);
COMMENT ON TABLE TEST_LOGGER IS '日志测试表';
COMMENT ON COLUMN TEST_LOGGER.ID IS '主键';
COMMENT ON COLUMN TEST_LOGGER.NAME IS '名称';
COMMENT ON COLUMN TEST_LOGGER.CONTENT IS '内容';
COMMENT ON COLUMN TEST_LOGGER.SORT_INDEX IS '排序号';
COMMENT ON COLUMN TEST_LOGGER.CREATE_TIME IS '创建时间';
COMMENT ON COLUMN TEST_LOGGER.DATA_STATE IS '数据状态:1为正常|随机数为删除';


CREATE TABLE "TEST_ROLE_CORE_INFO" (
   "ID"                 VARCHAR2(50)         NOT NULL,
   "TENANT_CODE"        VARCHAR2(50)         DEFAULT '0' NOT NULL,
   "USER_TYPE"          NUMBER(3)            DEFAULT 1 NOT NULL,
   "ROLE_NAME"          VARCHAR2(50)         NOT NULL,
   "ROLE_DESC"          VARCHAR2(200),
   "SORT_INDEX"         NUMBER(8),
   "CREATOR_ID"         VARCHAR2(50),
   "CREATE_TIME"        TIMESTAMP            DEFAULT CURRENT_TIMESTAMP NOT NULL,
   "DEFAULTS"           NUMBER(1)             DEFAULT 0 NOT NULL,
   "OPTIONS"            CLOB,
   "DATA_STATE"         NUMBER(10)            DEFAULT 1 NOT NULL,
   CONSTRAINT TEST_ROLE_CORE_PK PRIMARY KEY ("ID"),
   CONSTRAINT TEST_ROLE_CORE_NAME UNIQUE ("TENANT_CODE", "ROLE_NAME", "DATA_STATE")
);
COMMENT ON TABLE "TEST_ROLE_CORE_INFO" IS '角色信息表(pkg:permission)';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."ID" IS '角色ID';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."TENANT_CODE" IS '租户编号';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."USER_TYPE" IS '用户类型:1.管理员|2.用户(1.admin|2.user)';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."ROLE_NAME" IS '角色名称(name)';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."ROLE_DESC" is '描述';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."SORT_INDEX" IS '排序号(越小越靠前)';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."CREATOR_ID" IS '创建人ID';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."CREATE_TIME" IS '创建时间';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."DEFAULTS" IS '默认角色(如果用户没有任何角色,默认会赋予该角色)';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."OPTIONS" IS '选项(options)';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."DATA_STATE" IS '数据状态:1为正常|随机数为删除';


CREATE TABLE "TEST_USER_CORE_INFO" (
   "ID"                 VARCHAR2(50)         NOT NULL,
   "TENANT_CODE"        VARCHAR2(50)         DEFAULT '0' NOT NULL,
   "USER_TYPE"          NUMBER(3)            DEFAULT 1 NOT NULL,
   "DEPT_CODE"          VARCHAR2(50)         DEFAULT '0' NOT NULL,
   "USER_CODE"          VARCHAR2(50),
   "USER_NAME"          VARCHAR2(50),
   "NICK_NAME"          VARCHAR2(50),
   "REAL_NAME"          VARCHAR2(20),
   "PHONE"              VARCHAR2(20),
   "EMAIL"              VARCHAR2(50),
   "GENDER"             NUMBER(3)             NOT NULL,
   "PHOTO"              VARCHAR2(120),
   "CITY"               VARCHAR2(50),
   "IDENTITY"           VARCHAR2(20),
   "PASSWORD"           VARCHAR2(120),
   "CREATE_TIME"        TIMESTAMP            DEFAULT CURRENT_TIMESTAMP NOT NULL,
   "SUPERMAN"           NUMBER(1)             DEFAULT 0 NOT NULL,
   "USER_STATE"         NUMBER(3)             DEFAULT 0 NOT NULL,
   "USER_SOURCE"        NUMBER(3)             DEFAULT 0 NOT NULL,
   "OPTIONS"            CLOB,
   "DATA_STATE"         NUMBER(10)            DEFAULT 1 NOT NULL,
   CONSTRAINT TEST_USER_CORE_PK PRIMARY KEY ("ID")
);

COMMENT ON TABLE "TEST_USER_CORE_INFO" IS '用户基础信息表(pkg:personnel)';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."ID" IS '用户ID';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."TENANT_CODE" IS '租户编号';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."USER_TYPE" IS '用户类型:1.管理员|2.用户(1.admin|2.user)';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."DEPT_CODE" IS '部门编号';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."USER_CODE" IS '账号/工号';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."USER_NAME" IS '登录用户名';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."NICK_NAME" IS '昵称';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."REAL_NAME" IS '真实姓名';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."PHONE" IS '电话';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."EMAIL" IS '邮箱';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."GENDER" IS '性别:0.未知|1.男|2.女(gender:0.unknown|1.male|2.female)';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."PHOTO" IS '头像';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."CITY" IS '城市';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."IDENTITY" IS '身份证';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."PASSWORD" IS '密码';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."CREATE_TIME" IS '创建时间';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."SUPERMAN" IS '是否为超级用户';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."USER_STATE" IS '用户状态:0.正常|1.锁定|2.待激活|3.注销(UserState:0.Normal|1.Locked|2.Unactivated|3.Logoff)';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."USER_SOURCE" IS '用户来源:0.未知|1.录入(UserSource:0.Unknown|1.Input)';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."OPTIONS" IS '选项(options)';
COMMENT ON COLUMN "TEST_USER_CORE_INFO"."DATA_STATE" IS '数据状态:1为正常|随机数为删除';


CREATE TABLE "TEST_USER_ROLE_REF" (
   "ID"                 VARCHAR2(50)         NOT NULL,
   "USER_ID"            VARCHAR2(50)         NOT NULL,
   "ROLE_ID"            VARCHAR2(50)         NOT NULL,
   "DATA_STATE"         NUMBER(10)           DEFAULT 1 NOT NULL,
   CONSTRAINT TEST_USER_ROLE_PK PRIMARY KEY ("ID"),
   CONSTRAINT TEST_USER_ROLE_REF UNIQUE ("USER_ID", "ROLE_ID", "DATA_STATE")
);

COMMENT ON TABLE "TEST_USER_ROLE_REF" IS '用户角色关系表(pkg:permission)';
COMMENT ON COLUMN "TEST_USER_ROLE_REF"."ID" IS '主键ID';
COMMENT ON COLUMN "TEST_USER_ROLE_REF"."USER_ID" IS '用户ID(list)';
COMMENT ON COLUMN "TEST_USER_ROLE_REF"."ROLE_ID" IS '角色ID(list)';
COMMENT ON COLUMN "TEST_USER_ROLE_REF"."DATA_STATE" IS '数据状态:1为正常|随机数为删除';

CREATE INDEX "TEST_USER_ROLE_USER_ID" ON "TEST_USER_ROLE_REF" ( "USER_ID" );
CREATE INDEX "TEST_USER_ROLE_ROLE_ID" ON "TEST_USER_ROLE_REF" ( "ROLE_ID" );


CREATE TABLE "TEST_DEPARTMENT_CORE_INFO" (
   "ID"                 VARCHAR2(50)            NOT NULL,
   "TENANT_CODE"        VARCHAR2(50)            NOT NULL DEFAULT '0',
   "USER_TYPE"          NUMBER(3)               NOT NULL DEFAULT 1,
   "DEPT_CODE"          VARCHAR2(50)            NOT NULL,
   "DEPT_NAME"          VARCHAR2(50)            NOT NULL,
   "PARENT_CODE"        VARCHAR2(50)            NOT NULL,
   "SORT_INDEX"         NUMBER(8),
   "CREATOR_ID"         VARCHAR2(50),
   "CREATE_TIME"        TIMESTAMP              NOT NULL DEFAULT CURRENT_TIMESTAMP,
   "DATA_STATE"         NUMBER(10)                NOT NULL DEFAULT 1,
   CONSTRAINT "TEST_DEPT_CORE_PK" PRIMARY KEY ("ID"),
   CONSTRAINT "TEST_DEPT_CORE_CODE" UNIQUE ("TENANT_CODE", "DEPT_CODE", "DATA_STATE"),
   CONSTRAINT "TEST_DEPT_CORE_NAME" UNIQUE ("TENANT_CODE", "PARENT_CODE", "DEPT_NAME", "DATA_STATE")
);
COMMENT ON TABLE "TEST_DEPARTMENT_CORE_INFO" IS '部门信息表(pkg:personnel)';
COMMENT ON COLUMN "TEST_DEPARTMENT_CORE_INFO"."ID" IS '角色ID';
COMMENT ON COLUMN "TEST_DEPARTMENT_CORE_INFO"."TENANT_CODE" IS '租户编号';
COMMENT ON COLUMN "TEST_DEPARTMENT_CORE_INFO"."DEPT_CODE" IS '部门编号';
COMMENT ON COLUMN "TEST_DEPARTMENT_CORE_INFO"."DEPT_NAME" IS '部门名称';
COMMENT ON COLUMN "TEST_DEPARTMENT_CORE_INFO"."PARENT_CODE" IS '上级部门编号';
COMMENT ON COLUMN "TEST_DEPARTMENT_CORE_INFO"."SORT_INDEX" IS '排序号(越小越靠前)';
COMMENT ON COLUMN "TEST_DEPARTMENT_CORE_INFO"."CREATOR_ID" IS '创建人ID';
COMMENT ON COLUMN "TEST_DEPARTMENT_CORE_INFO"."CREATE_TIME" IS '创建时间';
COMMENT ON COLUMN "TEST_DEPARTMENT_CORE_INFO"."DATA_STATE" IS '数据状态:1为正常|随机数为删除';
