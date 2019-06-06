-- https://q.cnblogs.com/q/103199/
select 
	CASE WHEN (
		SELECT COUNT(1) FROM (
			SELECT '0' channel_name UNION ALL SELECT '-1' channel_name
		) t {{WHERE channel}}
	) = 1
	THEN '合计'
	ELSE t.channel_name
	END channel_name,
	CAST (
		CASE WHEN (
			SELECT COUNT(1) FROM (
				SELECT '0' prod_price UNION ALL SELECT '-1' prod_price
			) t {{WHERE product}}
		) = 1
		THEN '合计'
		ELSE t.prod_price
		END AS CHAR
	) prod_price,
	CASE WHEN (
		SELECT COUNT(1) FROM (
			SELECT '0' province_name UNION ALL SELECT '-1' province_name
		) t {{WHERE province}}
	) = 1
	THEN '合计'
	ELSE t.province_name
	END province_name,
	CAST (
		CASE WHEN (
			SELECT COUNT(1) FROM (
				SELECT '0' star_level UNION ALL SELECT '-1' star_level
			) t {{WHERE star_level}}
		) = 1
		THEN '合计'
		ELSE t.star_level
		END AS CHAR
	) star_level,
	sum (t.cover_user_cnt) cover_user_cnt,
	sum (t.arrive_user_cnt) arrive_user_cnt,
	sum (t.activation_user_cnt) activation_user_cnt,
	sum (t.renew_user_cnt) renew_user_cnt
from (
	SELECT '短信' channel_name, sxsp.SMS_PRODUCT_PRICE prod_price, sxtd.PRO_NAME province_name, 
		sxrsq.RIGHTS_STAR star_level, COUNT(*) cover_user_cnt,
		SUM ( CASE WHEN sxsp.SMS_SEND_RESULT IN (0, 3) THEN 1 ELSE 0 END ) arrive_user_cnt,
		SUM ( CASE WHEN sxoi.STATUS_CODE IN (200, 998, 999) THEN 1 ELSE 0 END ) activation_user_cnt,
		SUM ( CASE WHEN sxoi1.STATUS_CODE IN (200, 998, 999) THEN 1 ELSE 0 END ) renew_user_cnt
	FROM SOURCE_XINYUAN_SMS_PRESEND sxsp
	LEFT JOIN SOURCE_XINYUAN_RIGHTS_STAR_QUERY sxrsq 
		ON sxsp.SMS_QUERY_ID = sxrsq.RIGHTS_ID AND sxrsq.IS_DELETED = 0
	LEFT JOIN SOURCE_XINYUAN_TERM_DATA sxtd 
		ON sxtd.TERM_ID = sxrsq.RIGHTS_TERM_ID and sxtd.IS_DELETED = 0
	LEFT JOIN SOURCE_XINYUAN_ORDER_INFO sxoi 
		ON sxoi.ORD_ID = sxsp.SMS_ORDER_ID AND sxoi.IS_DELETED = 0
	LEFT JOIN SOURCE_XINYUAN_ORDER_INFO sxoi1 
		ON sxoi1.PHONE_NUMBER = sxoi.PHONE_NUMBER AND sxoi1.BUSS_TYPE = 2 AND sxoi1.IS_DELETED = 0
	WHERE sxsp.SMS_SEND_TIME >= {{lval order_date}}
		AND sxsp.SMS_SEND_TIME <= {{rval order_date}} AND sxsp.IS_DELETED = 0 AND sxsp.SMS_TYPE = 1
	GROUP BY sxsp.SMS_PRODUCT_PRICE, sxtd.PRO_NAME, sxrsq.RIGHTS_STAR
	
	union all
	
	SELECT '外呼', sxocat.TARGET_PRODUCT_PRICE prod_price, mapp.SOURCE_PROVINCE_NAME province_name,
		sxocat.TARGET_STARS star_level, COUNT(*),
		SUM ( CASE WHEN sxoil.LOG_CALLOUT_RESULT IN (1, 2) THEN 1 ELSE 0 END ),
		SUM ( CASE WHEN sxoi.STATUS_CODE IN (200, 998, 999) THEN 1 ELSE 0 END ) activation_user_cnt,
		SUM ( CASE WHEN sxoi1.STATUS_CODE IN (200, 998, 999) THEN 1 ELSE 0 END ) renew_user_cnt
	FROM SOURCE_XINYUAN_OUTCALL_IMPORT_LOG sxoil
	LEFT JOIN SOURCE_XINYUAN_OUTCALL_ACTIVITY_TARGET sxocat 
		ON sxocat.TARGET_ID = sxoil.LOG_TARGET_ID AND sxocat.IS_DELETED = 0
	LEFT JOIN SOURCE_XINYUAN_OUTCALL_ACTIVITY sxoa 
		ON sxoa.ACTIVITY_ID = sxocat.ACTIVITY_ID AND sxoa.IS_DELETED = 0
	LEFT JOIN MAPPING_PROVINCE mapp 
		ON mapp.ID = sxocat.MAPPING_TARGET_PROVINCE_ID
	left join SOURCE_XINYUAN_OUTCALL_RECORD sxor 
		on sxor.TARGET_ID=sxoil.LOG_TARGET_ID and sxor.IS_DELETED=0
	left join SOURCE_XINYUAN_APPLY_INFO sxai 
		on sxai.APPLY_ID=sxor.APPLY_ID and sxai.IS_DELETED=0
	left join SOURCE_XINYUAN_ORDER_INFO sxoi 
		on sxoi.ORD_ID=sxai.ORD_ID and sxoi.IS_DELETED=0
	LEFT JOIN SOURCE_XINYUAN_ORDER_INFO sxoi1 
		ON sxoi1.PHONE_NUMBER = sxoi.PHONE_NUMBER AND sxoi1.BUSS_TYPE = 2 AND sxoi1.IS_DELETED = 0
	WHERE sxoil.LOG_OUTCALL_TIME >= {{lval order_date}}
		AND sxoil.LOG_OUTCALL_TIME <= {{rval order_date}}
		AND sxoa.ACTIVITY_SOURCE = 101
		AND sxoil.IS_DELETED = 0
		AND sxoil.LOG_OUTCALL_TIME = (
			SELECT MAX(sxoil1.LOG_OUTCALL_TIME)
			FROM SOURCE_XINYUAN_OUTCALL_IMPORT_LOG sxoil1
			WHERE sxoil1.LOG_TARGET_ID = sxoil.LOG_TARGET_ID
			AND sxoil1.IS_DELETED = 0
		)
	GROUP BY sxocat.TARGET_PRODUCT_PRICE, mapp.SOURCE_PROVINCE_NAME, sxocat.TARGET_STARS
) t
where 1=1 {{channel}} {{product}} {{province}} {{star_level}}
group by 
	CASE WHEN (
		SELECT COUNT(1) FROM (
			SELECT '0' channel_name UNION ALL SELECT '-1' channel_name
		) t {{WHERE channel}}
	) = 1
	THEN '合计' 
	ELSE t.channel_name 
	END,
	CAST (
		CASE WHEN (
			SELECT COUNT(1) FROM (
				SELECT '0' prod_price UNION ALL SELECT '-1' prod_price
			) t {{WHERE product}}
		) = 1
		THEN '合计'
		ELSE t.prod_price
		END AS CHAR
	),
	CASE WHEN (
		SELECT COUNT(1) FROM (
			SELECT '0' province_name UNION ALL SELECT '-1' province_name
		) t {{WHERE province}}
	) = 1
	THEN '合计'
	ELSE t.province_name
	END,
	CASE WHEN (
		SELECT COUNT(1) FROM (
			SELECT '0' star_level UNION ALL SELECT '-1' star_level
		) t {{WHERE star_level}}
	) = 1
	THEN '合计'
	ELSE t.star_level
	END
