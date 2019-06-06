SELECT
	IF (
		LOCATE ( familyKey, link, 1 ) = 0, NULL,
		SUBSTRING (
			link, LOCATE(familyKey, link, 1) + LENGTH(familyKey) + 1,
		    IF (
		    	LOCATE ( '&', link, LOCATE(familyKey, link, 1) ) = 0, LENGTH ( link ),
		        LOCATE ( '&', link, LOCATE(familyKey, link, 1) )
		        - ( LOCATE(familyKey, link, 1) + LENGTH(familyKey) + 1 )
		    )
	    )
	) familyKey
FROM illustrations;

SELECT 
	IF (
		strbegin=0, NULL, 
		SUBSTRING (
			link, strbegin + strlen + 1, 
			IF ( strend=0, LENGTH(link), strend - (strbegin + strlen + 1) )
		)
	) familyKey, 
	mainId
FROM (
	SELECT mainId, link, strbegin, LOCATE('&', link, strbegin) strend, LENGTH(familyKey) strlen
	FROM ( SELECT mainId, link, LOCATE(familyKey, link, 1) strbegin FROM illustrations ) tmp
) tmp;
