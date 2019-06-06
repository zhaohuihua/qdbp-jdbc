SELECT cast('20岁以下' as char) as name, COUNT(DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) as value
FROM hm_applicant 
WHERE (DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) <= 20

UNION

SELECT cast('20 - 40岁' as char) as name, COUNT(DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) as value
FROM hm_applicant 
WHERE (DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) > 20 
	AND (DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) <= 40

UNION

SELECT cast('40 - 60岁' as char) as name, COUNT(DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) as value
FROM hm_applicant 
WHERE (DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) > 40 
	AND (DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) <= 60

UNION

SELECT cast('60 - 80岁' as char) as name, COUNT(DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) as value
FROM hm_applicant
WHERE (DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) > 60 
	AND (DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) <= 80

UNION

SELECT cast('80岁以上' as char) as name, COUNT(DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) as value 
FROM hm_applicant 
WHERE (DATE_FORMAT(NOW(),'%Y') - DATE_FORMAT(birthday,'%Y')) > 80;
