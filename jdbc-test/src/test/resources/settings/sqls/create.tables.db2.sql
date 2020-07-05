
DROP TABLE TEST_LOGGER;

CREATE TABLE TEST_LOGGER (
	ID VARCHAR(50) NOT NULL,
	NAME VARCHAR(30) NOT NULL,
	CONTENT VARCHAR(4000) NOT NULL,
	SORT_INDEX INTEGER NOT NULL DEFAULT 1,
	CREATE_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	DATA_STATE INTEGER NOT NULL DEFAULT 0,
	CONSTRAINT TEST_LOGGER_PK PRIMARY KEY (ID)
);

COMMENT ON TABLE TEST_LOGGER IS '日志测试表';
COMMENT ON COLUMN TEST_LOGGER.ID IS '主键';
COMMENT ON COLUMN TEST_LOGGER.NAME IS '名称';
COMMENT ON COLUMN TEST_LOGGER.CONTENT IS '内容';
COMMENT ON COLUMN TEST_LOGGER.SORT_INDEX IS '排序号';
COMMENT ON COLUMN TEST_LOGGER.CREATE_TIME IS '创建时间';
COMMENT ON COLUMN TEST_LOGGER.DATA_STATE IS '数据状态:0为正常|其他为删除';
