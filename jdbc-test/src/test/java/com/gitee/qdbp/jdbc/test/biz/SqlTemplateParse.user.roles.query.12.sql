SELECT * FROM TEST_USER_ROLE_REF ur
    INNER JOIN TEST_ROLE_CORE_INFO r
    ON ur.ROLE_ID=r.ID
    AND r.DATA_STATE=1
WHERE ur.DATA_STATE=1
    AND ur.USER_ID IN ( '10000001'/*$1*/,'10000002'/*$2*/,'10000008'/*$3*/ )