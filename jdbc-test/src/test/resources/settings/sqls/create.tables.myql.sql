CREATE TABLE IF NOT EXISTS TEST_SETTING (
	ID VARCHAR(50) NOT NULL COMMENT '主键', 
	NAME VARCHAR(30) NOT NULL COMMENT '名称', 
	VALUE VARCHAR(50) NOT NULL COMMENT '文本', 
	VERSION INT(8) NOT NULL DEFAULT 1 COMMENT '版本号', 
	REMARK VARCHAR(200) COMMENT '备注', 
	STATE TINYINT(1) NOT NULL COMMENT '状态', 
	CREATE_TIME DATETIME NOT NULL COMMENT '创建时间', 
	UPDATE_TIME DATETIME COMMENT '修改时间', 
	DATA_STATE INT(10) NOT NULL COMMENT '数据状态:0为正常|其他为删除', 
	PRIMARY KEY (ID), 
	UNIQUE KEY TEST_SETTING_NAME(NAME, DATA_STATE)
) COMMENT='系统配置表';

CREATE TABLE IF NOT EXISTS TEST_LOGGER (
	ID VARCHAR(50) NOT NULL COMMENT '主键',
	NAME VARCHAR(30) COMMENT '名称',
	CONTENT TEXT NOT NULL COMMENT '内容',
	SORT_INDEX INT(8) NOT NULL DEFAULT 1 COMMENT '排序号',
	CREATE_TIME DATETIME NOT NULL COMMENT '创建时间',
	DATA_STATE INT(10) NOT NULL COMMENT '数据状态:0为正常|其他为删除',
	PRIMARY KEY (ID)
) COMMENT='操作日志表';