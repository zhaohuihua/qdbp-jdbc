-- https://www.cnblogs.com/binye-typing/p/5934202.html


CREATE TABLE maingroup_id_parentId AS
SELECT n1.id AS parentId,n2.id AS id
FROM nav n1, nav n2
WHERE n1.meta->'$.model' = n2.parent_meta->'$.model'
AND n1.meta->'$.spec' = n2.parent_meta->'$.spec'
AND n1.meta->'$.lang' = n2.parent_meta->'$.lang'
AND n1.meta->'$.startup' = n2.parent_meta->'$.startup'
AND n1.meta->'$.localMarketOnly' = n2.parent_meta->'$.localMarketOnly'
AND n2.nav_table = 'nav-mainGroup-table'
AND n1.nav_table <> 'nav-mainGroup-table';

-- pnc字段关键字提取
ALTER TABLE pnc ADD pnc_number VARCHAR(20) generated always AS (json_extract(meta,'$.pnc')) virtual;
ALTER TABLE pnc ADD drawingVar VARCHAR(8) generated always AS (json_extract(meta,'$.drawingVar')) virtual;
ALTER TABLE pnc ADD drawing VARCHAR(8) generated always AS (meta->"$.drawing") virtual;
ALTER TABLE pnc ADD subGroup VARCHAR(8) generated always AS (json_extract(meta,'$.subGroup')) virtual;
ALTER TABLE pnc ADD mainGroup VARCHAR(8) generated always AS (json_extract(meta,'$.mainGroup')) virtual;
ALTER TABLE pnc ADD spec VARCHAR(150) generated always AS (json_extract(meta,'$.spec')) virtual;
ALTER TABLE pnc ADD model VARCHAR(50) generated always AS (json_extract(meta,'$.model')) virtual;
-- pnc建索引
ALTER TABLE pnc ADD INDEX link(pnc_number,drawingVar,drawing,subGroup,mainGroup,spec,model);

-- 修改bom表
ALTER TABLE bom ADD pnc_number VARCHAR(20) generated always AS (json_extract(parent_meta,'$.pnc')) virtual;
ALTER TABLE bom ADD drawingVar VARCHAR(8) generated always AS (json_extract(parent_meta,'$.drawingVar')) virtual;
ALTER TABLE bom ADD drawing VARCHAR(8) generated always AS (parent_meta->"$.drawing") virtual;
ALTER TABLE bom ADD subGroup VARCHAR(8) generated always AS (json_extract(parent_meta,'$.subGroup')) virtual;
ALTER TABLE bom ADD mainGroup VARCHAR(8) generated always AS (json_extract(parent_meta,'$.mainGroup')) virtual;
ALTER TABLE bom ADD spec VARCHAR(150) generated always AS (json_extract(parent_meta,'$.spec')) virtual;
ALTER TABLE bom ADD model VARCHAR(50) generated always AS (json_extract(parent_meta,'$.model')) virtual;
-- bom建索引
ALTER TABLE bom ADD INDEX link(pnc_number,drawingVar,drawing,subGroup,mainGroup,spec,model); 

-- 造表间关系
DROP TABLE IF EXISTS pnc_bom_id;

CREATE TABLE pnc_bom_id
	SELECT bom.id AS bomId,pnc.id AS pncId
	FROM nissan_bom bom,nissan_pnc pnc
	WHERE bom.pnc_number=pnc.pnc_number
		AND bom.drawingVar=pnc.drawingVar
		AND bom.drawing=pnc.drawing
		AND bom.mainGroup=pnc.mainGroup
		AND bom.subGroup=pnc.subGroup
		AND bom.spec=pnc.spec
		AND bom.model=pnc.model;
