select
product.brand_id,
product.lang_id,
product.product_id,
product.url_key,
product.model,
product.sku,
product.asin,
product.product_name,
product.short_name,
product.product_img,
product.status,
product.title,
product.keywords,
product.seo_desc,
product.description,
product.more_color,
product.master_color,
product.hot_flag,
product.new_flag,
product.deals_flag,
product.amz_desc,
product.amz_keywords,
product.amz_reviews,
product.amz_avg_star,
product.abtest,
product.sort,
product.old_product_id,
product.old_url_key,
categoryProduct.category_id
from catalog_product as product, catalog_category_product as categoryProduct
where product.brand_id = categoryProduct.brand_id and product.lang_id = categoryProduct.lang_id and
product.product_id = categoryProduct.product_id and product.brand_id = ? and product.lang_id = ? and
categoryProduct.category_id = ? and product.status = 1 and
product.more_color between (0 AND 1) and product.master_color = 1
order by product.sort asc
