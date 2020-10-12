DROP TABLE TEST_SETTING;
DROP TABLE TEST_LOGGER;
DROP TABLE TEST_ROLE_CORE_INFO;
DROP TABLE TEST_USER_CORE_INFO;
DROP TABLE TEST_USER_ROLE_REF;



CREATE TABLE TEST_SETTING (
	ID VARCHAR(50) NOT NULL, 
	NAME VARCHAR(30) NOT NULL, 
	VALUE VARCHAR(50) NOT NULL, 
	VERSION INTEGER NOT NULL DEFAULT 1, 
	REMARK VARCHAR(200), 
	STATE SMALLINT NOT NULL, 
	CREATE_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	UPDATE_TIME TIMESTAMP, 
	DATA_STATE INTEGER NOT NULL DEFAULT 1, 
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
	ID VARCHAR(50) NOT NULL,
	NAME VARCHAR(30) NOT NULL,
	CONTENT VARCHAR(4000) NOT NULL,
	SORT_INDEX INTEGER NOT NULL DEFAULT 1,
	CREATE_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	DATA_STATE INTEGER NOT NULL DEFAULT 1,
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
   "ID"                 VARCHAR(50)            NOT NULL,
   "TENANT_CODE"        VARCHAR(50)            NOT NULL DEFAULT '0',
   "USER_TYPE"          SMALLINT               NOT NULL DEFAULT 1,
   "ROLE_NAME"          VARCHAR(50)            NOT NULL,
   "ROLE_DESC"          VARCHAR(200),
   "SORT_INDEX"         INTEGER,
   "CREATOR_ID"         VARCHAR(50),
   "CREATOR_NAME"       VARCHAR(50),
   "CREATE_TIME"        TIMESTAMP              NOT NULL DEFAULT CURRENT_TIMESTAMP,
   "DEFAULTS"           SMALLINT               NOT NULL DEFAULT 0,
   "OPTIONS"            VARCHAR(4000),
   "DATA_STATE"         INTEGER                NOT NULL DEFAULT 0,
   CONSTRAINT "TEST_ROLE_CORE_PK" PRIMARY KEY ("ID"),
   CONSTRAINT "TEST_ROLE_CORE_NAME" UNIQUE ("TENANT_CODE", "ROLE_NAME", "DATA_STATE")
);

COMMENT ON TABLE "TEST_ROLE_CORE_INFO" IS '角色信息表(pkg:permission)';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."ID" IS '角色ID';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."TENANT_CODE" IS '租户编号';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."USER_TYPE" IS '用户类型:1.管理员|2.用户(1.admin|2.user)';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."ROLE_NAME" IS '角色名称(name)';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."ROLE_DESC" is '描述';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."SORT_INDEX" IS '排序号(越小越靠前)';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."CREATOR_ID" IS '创建人ID';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."CREATOR_NAME" IS '创建人姓名';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."CREATE_TIME" IS '创建时间';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."DEFAULTS" IS '默认角色(如果用户没有任何角色,默认会赋予该角色)';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."OPTIONS" IS '选项(options)';
COMMENT ON COLUMN "TEST_ROLE_CORE_INFO"."DATA_STATE" IS '数据状态:1为正常|随机数为删除';


CREATE TABLE "TEST_USER_CORE_INFO" (
   "ID"                 VARCHAR(50)            NOT NULL,
   "TENANT_CODE"        VARCHAR(50)            NOT NULL,
   "USER_TYPE"          SMALLINT               NOT NULL DEFAULT 1,
   "DEPT_CODE"          VARCHAR(50)            NOT NULL DEFAULT '0',
   "USER_CODE"          VARCHAR(50),
   "USER_NAME"          VARCHAR(50),
   "NICK_NAME"          VARCHAR(50),
   "REAL_NAME"          VARCHAR(20),
   "PHONE"              VARCHAR(20),
   "EMAIL"              VARCHAR(50),
   "GENDER"             SMALLINT              NOT NULL,
   "PHOTO"              VARCHAR(120),
   "CITY"               VARCHAR(50),
   "IDENTITY"           VARCHAR(20),
   "PASSWORD"           VARCHAR(120),
   "CREATE_TIME"        TIMESTAMP              NOT NULL DEFAULT CURRENT_TIMESTAMP,
   "SUPERMAN"           SMALLINT               NOT NULL DEFAULT 0,
   "USER_STATE"         SMALLINT               NOT NULL DEFAULT 0,
   "USER_SOURCE"        SMALLINT               NOT NULL DEFAULT 0,
   "OPTIONS"            VARCHAR(4000),
   "DATA_STATE"         INTEGER                NOT NULL DEFAULT 0,
   CONSTRAINT "TEST_USER_CORE_PK" PRIMARY KEY ("ID")
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
   "ID"                 VARCHAR(50)            NOT NULL,
   "USER_ID"            VARCHAR(50)            NOT NULL,
   "ROLE_ID"            VARCHAR(50)            NOT NULL,
   "DATA_STATE"         INTEGER                NOT NULL DEFAULT 0,
   CONSTRAINT "TEST_USER_ROLE_PK" PRIMARY KEY ("ID"),
   CONSTRAINT "TEST_USER_ROLE_REF" UNIQUE ("USER_ID", "ROLE_ID", "DATA_STATE")
);

COMMENT ON TABLE "TEST_USER_ROLE_REF" IS '用户角色关系表(pkg:permission)';
COMMENT ON COLUMN "TEST_USER_ROLE_REF"."ID" IS '主键ID';
COMMENT ON COLUMN "TEST_USER_ROLE_REF"."USER_ID" IS '用户ID(list)';
COMMENT ON COLUMN "TEST_USER_ROLE_REF"."ROLE_ID" IS '角色ID(list)';
COMMENT ON COLUMN "TEST_USER_ROLE_REF"."DATA_STATE" IS '数据状态:1为正常|随机数为删除';

CREATE INDEX "TEST_USER_ROLE_USER_ID" ON "TEST_USER_ROLE_REF" ( "USER_ID" );
CREATE INDEX "TEST_USER_ROLE_ROLE_ID" ON "TEST_USER_ROLE_REF" ( "ROLE_ID" );
