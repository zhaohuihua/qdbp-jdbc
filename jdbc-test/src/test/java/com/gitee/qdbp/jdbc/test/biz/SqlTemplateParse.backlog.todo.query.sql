SELECT * FROM (
   SELECT '1' AS TYPE,S.IS_START_NODE,C.TASK_NAME,S.BUSINESS_ID,S.BUS_CODE AS BUSCODE,
       S.PROJECT_CODE,S.PROJECT_NAME,S.PROD_TYPE_CODE,S.PROD_TYPE_NAME,S.PROC_STATE_CODE,
       T.ASSIGNEE_ AS ASSIGNEE,P.START_TIME_ AS CREATE_TIME,T.CREATE_TIME_ AS RECEIVE_TIME,S.BACKLOG_CONTENT,C.URL,
       S.KEYWORD_CODE1,S.KEYWORD_VALUE1,S.KEYWORD_CODE2,S.KEYWORD_VALUE2,S.KEYWORD_CODE3,S.KEYWORD_VALUE3,
       T.ID_ AS TASK_ID,T.PROC_INST_ID_ AS PROC_INST_ID,S.BUSINESS_PARAMS,
       S.USER_ID START_USER_ID,S.CURR_APPROVER_CODES HANDLER_USER_CODE,S.CURR_APPROVER_NAMES HANDLER_USER  
   FROM ACT_RU_TASK T
   INNER JOIN ACT_PROC_STATE S ON T.PROC_INST_ID_=S.PROC_INST_ID
   INNER JOIN ACT_HI_PROCINST P ON T.PROC_INST_ID_=P.PROC_INST_ID_
   LEFT JOIN ACT_BACKLOG_CONFIG C ON S.BUS_CODE=C.BUS_CODE
   WHERE (
       T.ASSIGNEE_='U0000001'/*$1*/
       OR (
           T.ASSIGNEE_ IN('R0000001'/*$2*/,'R0000006'/*$3*/)
           AND S.DEPT_ID IN('D0000001'/*$4*/,'D0000005'/*$5*/,'D0000012'/*$6*/)
       )
   )
   AND S.PROJECT_CODE='P0000001'/*$7*/
UNION ALL
   SELECT '2' AS TYPE,NULL IS_START_NODE,C.TASK_NAME,S.BUSINESS_ID,S.BUS_CODE AS BUSCODE,
       S.PROJECT_CODE,S.PROJECT_NAME,S.PROD_TYPE_CODE,S.PROD_TYPE_NAME,NULL PROC_STATE_CODE,
       S.ASSIGNEE,S.CREATE_TIME,S.CREATE_TIME AS RECEIVE_TIME,S.TASK_CONTENT AS BACKLOG_CONTENT,C.URL URL,
       S.KEYWORD_CODE1,S.KEYWORD_VALUE1,S.KEYWORD_CODE2,S.KEYWORD_VALUE2,S.KEYWORD_CODE3,S.KEYWORD_VALUE3,
       S.ID AS TASK_ID,S.ID AS PROC_INST_ID,S.BUSINESS_PARAMS,
       S.CREATE_USER START_USER_ID,NULL HANDLER_USER_CODE,NULL HANDLER_USER  
   FROM COMM_OPERATE_TASK S
   LEFT JOIN ACT_BACKLOG_CONFIG C ON S.BUS_CODE=C.BUS_CODE
   WHERE (
       S.ASSIGNEE='U0000001'/*$8*/
       OR (
           S.ASSIGNEE IN('R0000001'/*$9*/,'R0000006'/*$10*/)
           AND S.DEPT_ID IN('D0000001'/*$11*/,'D0000005'/*$12*/,'D0000012'/*$13*/)
       )
   )
   AND S.PROJECT_CODE='P0000001'/*$14*/
) R
ORDER BY R.RECEIVE_TIME DESC,R.CREATE_TIME DESC,R.BUSINESS_ID