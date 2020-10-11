-- 递归查询子节点
DROP PROCEDURE IF EXISTS `RECURSIVE_FIND_CHILDREN`;
DELIMITER $$
CREATE PROCEDURE `RECURSIVE_FIND_CHILDREN`(
    TABLE_NAME VARCHAR(100),
    START_CODES TEXT, -- 逗号分隔的起始编号
    CODE_FIELD VARCHAR(100),
    PARENT_FIELD VARCHAR(100),
    SELECT_FIELDS TEXT,
    WHERE_SQL TEXT,
    ORDER_BY_SQL TEXT
)
READS SQL DATA
SQL SECURITY INVOKER
BEGIN
    -- zhaohuihua 2019-03-10
    -- CALL RECURSIVE_LIST_CHILDREN("sys_area_division", "340000,350000", "area_code", "parent_code", NULL, "scene_type='default'", "parent_code,sort_index");

    -- 创建临时表
    DROP TABLE IF EXISTS `temp_starts`;
    CREATE TEMPORARY TABLE `temp_starts` (
        `_CODE_` VARCHAR(100)
    );
    DROP TABLE IF EXISTS `temp_codes`;
    CREATE TEMPORARY TABLE `temp_codes` (
        `_IDX_` INT(8) NOT NULL AUTO_INCREMENT,
        `_CODE_` VARCHAR(100), 
        `_LEVEL_` INT(5),
        PRIMARY KEY (`_IDX_`)
    );
    DROP TABLE IF EXISTS `temp_buffer`;
    CREATE TEMPORARY TABLE `temp_buffer` (
        `_IDX_` INT(8) NOT NULL AUTO_INCREMENT,
        `_CODE_` VARCHAR(100), 
        `_LEVEL_` INT(5),
        PRIMARY KEY (`_IDX_`)
    );

    -- 按逗号拆分起始编号
    BEGIN
        DECLARE v_locate INT;
        DECLARE v_code VARCHAR(1000);
        DECLARE v_pending TEXT;

        SET v_pending = START_CODES;
        WHILE INSTR(v_pending, ',') > 0 DO
            SET v_locate = INSTR(v_pending, ',');
            SET v_code = SUBSTRING(v_pending, 1, v_locate - 1);
            SET v_pending = SUBSTRING(v_pending, v_locate + 1);
            INSERT INTO temp_starts VALUES(v_code);
        END WHILE;
        INSERT INTO temp_starts VALUES(v_pending);
    END;
    -- SELECT * FROM temp_starts;

    BEGIN
        DECLARE v_level INT;

        -- 查询起始编号
        SET v_level = 1;
        SET @sql_insert = CONCAT(
            'INSERT `temp_codes`(`_CODE_`, `_LEVEL_`) ',
            'SELECT X.`', CODE_FIELD, '`, ', v_level, ' ',
            'FROM `', TABLE_NAME, '` X ',
            'WHERE '
        );
        IF WHERE_SQL IS NOT NULL AND LENGTH(WHERE_SQL) > 0 THEN
            SET @sql_insert = CONCAT(@sql_insert, WHERE_SQL, ' AND ');
        END IF;
        SET @sql_insert = CONCAT(@sql_insert, 'X.`', CODE_FIELD, '` IN ( SELECT `_CODE_` FROM temp_starts ) ');
        IF ORDER_BY_SQL IS NOT NULL AND LENGTH(ORDER_BY_SQL) > 0 THEN
            SET @sql_insert = CONCAT(@sql_insert, 'ORDER BY ', ORDER_BY_SQL, ' ');
        END IF;
        -- SELECT @sql_insert FROM DUAL;
        -- 解析并执行SQL
        PREPARE stmt FROM @sql_insert;  
        EXECUTE stmt;

        -- 查询子级
        -- SELECT ROW_COUNT();
        WHILE ROW_COUNT() > 0 DO
            SET v_level = v_level + 1;
            -- 不能直接向temp_codes表中插入数据, 会报错: Can't reopen table
            -- 因为临时表在同一个语句中, 只能使用一次
            -- 改为先向temp_buffer表插入, 再转存到temp_codes
            SET @sql_insert = CONCAT(
                'INSERT `temp_buffer`(`_CODE_`, `_LEVEL_`) ',
                'SELECT DISTINCT X.`', CODE_FIELD, '`, ', v_level, ' ',
                'FROM `temp_codes` T ',
                'INNER JOIN `', TABLE_NAME, '` X ',
                'ON T.`_LEVEL_`=', v_level-1,' AND X.`', PARENT_FIELD, '` = T.`_CODE_` '
            );
            IF WHERE_SQL IS NOT NULL AND LENGTH(WHERE_SQL) > 0 THEN
                SET @sql_insert = CONCAT(@sql_insert, 'WHERE ', WHERE_SQL, ' ');
            END IF;
            IF ORDER_BY_SQL IS NOT NULL AND LENGTH(ORDER_BY_SQL) > 0 THEN
                SET @sql_insert = CONCAT(@sql_insert, 'ORDER BY ', ORDER_BY_SQL, ' ');
            END IF;
            -- SELECT @sql_insert FROM DUAL;
            PREPARE stmt FROM @sql_insert;  
            EXECUTE stmt;

            -- temp_buffer表记录转存到temp_codes
            IF ROW_COUNT() > 0 THEN
                INSERT `temp_codes`(`_CODE_`, `_LEVEL_`)
                    SELECT `_CODE_`, `_LEVEL_` FROM `temp_buffer`
                    ORDER BY `_IDX_`;
                DELETE FROM `temp_buffer`;
            END IF;
            
        END WHILE;
    END;

    -- SELECT * FROM temp_codes;
    -- SELECT count(*) FROM temp_codes;

    BEGIN
        -- 拼接查询语句
        SET @sql_query = 'SELECT ';
        IF SELECT_FIELDS IS NULL OR LENGTH(SELECT_FIELDS) = 0 THEN
            SET @sql_query = CONCAT(@sql_query, 'X.*');
        ELSE
            SET @sql_query = CONCAT(@sql_query, SELECT_FIELDS);
        END IF;
        SET @sql_query = CONCAT(@sql_query, ' ',
            'FROM `temp_codes` T INNER JOIN `', TABLE_NAME, '` X ',
            'ON T.`_CODE_`=X.`', CODE_FIELD, '` '
        );
        IF WHERE_SQL IS NOT NULL AND LENGTH(WHERE_SQL) > 0 THEN
            SET @sql_query = CONCAT(@sql_query, 'WHERE ', WHERE_SQL, ' ');
        END IF;
        SET @sql_query = CONCAT(@sql_query, 'ORDER BY T.`_IDX_` ');
        -- SELECT @sql_query FROM DUAL;

        PREPARE stmt FROM @sql_query;  
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END;
END $$

DELIMITER ;
