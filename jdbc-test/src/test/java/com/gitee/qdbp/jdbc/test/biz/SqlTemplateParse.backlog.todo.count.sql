SELECT SUM(CNT) FROM (
   SELECT COUNT(*) AS CNT 
   FROM ACT_RU_TASK T
   INNER JOIN ACT_PROC_STATE S ON T.PROC_INST_ID_=S.PROC_INST_ID
   INNER JOIN ACT_HI_PROCINST P ON T.PROC_INST_ID_=P.PROC_INST_ID_
   LEFT JOIN ACT_BACKLOG_CONFIG C ON S.BUS_CODE=C.BUS_CODE
   WHERE (
       T.ASSIGNEE_='U0000001'/*$1*/
       OR (
           T.ASSIGNEE_ IN('R0000001'/*$2*/,'R0000006'/*$3*/)
           AND S.ORG_ID IN('D0000001'/*$4*/,'D0000005'/*$5*/,'D0000012'/*$6*/)
       )
   )
   AND S.PROJECT_CODE='P0000001'/*$7*/
UNION ALL
   SELECT COUNT(*) AS CNT
   FROM COMM_OPERATE_TASK S
   LEFT JOIN ACT_BACKLOG_CONFIG C ON S.BUS_CODE=C.BUS_CODE
   WHERE (
       S.ASSIGNEE='U0000001'/*$8*/
       OR (
           S.ASSIGNEE IN('R0000001'/*$9*/,'R0000006'/*$10*/)
           AND S.ORG_ID IN('D0000001'/*$11*/,'D0000005'/*$12*/,'D0000012'/*$13*/)
       )
   )
   AND S.PROJECT_CODE='P0000001'/*$14*/
) R