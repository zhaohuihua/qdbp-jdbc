

-- << user.roles.query >> ��ѯ�û�����Щ��ɫ
SELECT * FROM TEST_USER_ROLE_REF ur
    INNER JOIN TEST_ROLE_CORE_INFO r
    ON ur.ROLE_ID=r.ID
    AND r.DATA_STATE=1
WHERE ur.DATA_STATE=1
    AND ur.USER_ID IN ( #{userIds} )
    <append prefix="AND">#{whereCondition}</append>
<append prefix="ORDER BY">${orderByCondition}</append>


-- << role.users.query >> ��ѯ��ɫ������Щ�û�
SELECT * FROM TEST_USER_ROLE_REF ur
    INNER JOIN TEST_USER_CORE_INFO u
    ON ur.USER_ID=u.ID
    AND u.DATA_STATE=1
WHERE ur.DATA_STATE=1
    AND ur.ROLE_ID IN ( #{roleIds} )
    <append prefix="AND">#{whereCondition}</append>
<append prefix="ORDER BY">${orderByCondition}</append>

