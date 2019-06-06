SELECT 
    (
        SELECT id 
        FROM orc_scheme d 
        WHERE d.business = 208 
          AND d.type IN (
              29, 30, 31, 321, 33, 34, 3542, 361, 327,  38, 39, 40, 41, 42, 431, 4422, 415, 4546, 
              47, 48, 'a', 29, 30, 31, 321, 33, 34, 3542,  361, 327, 38, 39, 40, 41, 42, 431, 4422, 
              415, 4546, 47, 48, 'a'
          ) 
          AND t_create >= DATE_FORMAT((DATE_SUB(NOW(), INTERVAL 1 DAY)), '%Y-%c-%d')
    ), 
    DATE_FORMAT(b.t_create, '%Y-%c-%d') dateID, b.title memo 
FROM  
    (
        SELECT id 
        FROM orc_scheme_detail d 
        WHERE d.business = 208 
          AND d.type IN (
              29, 30, 31, 321, 33, 34, 3542, 361, 327,  38, 39, 40, 41, 42, 431, 4422, 415, 4546, 
              47, 48, 'a', 29, 30, 31, 321, 33, 34, 3542,  361, 327, 38, 39, 40, 41, 42, 431, 4422, 
              415, 4546, 47, 48, 'a'
          ) 
          AND d.title IS NOT NULL 
          AND t_create >= DATE_FORMAT((DATE_SUB(NOW(), INTERVAL 1 DAY)), '%Y-%c-%d')
          AND t_create < DATE_FORMAT(NOW(), '%Y-%c-%d') 
    ) a, 
    orc_scheme_detail b 
WHERE a.id = b.id
ORDER BY d.id 
LIMIT 2, 10