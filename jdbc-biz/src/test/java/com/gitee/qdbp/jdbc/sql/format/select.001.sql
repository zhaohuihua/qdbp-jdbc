select * from employees 
where hire_date = ( select max ( hire_date ) from employees );

select goods_id, goods_name, price, dates from COMPUTER_PRICE
where dates = ( select max(dates) from computer_price group by goods_id );

select goods_id, goods_name, price, dates from computer_price 
where (goods_id, dates) = ( 
		select goods_id, max ( dates ) from computer_price group by goods_id
	);

select count(0) from (SELECT * FROM auth_tenant WHERE type = 'tenant' AND name LIKE ?"%") tmp_count;

