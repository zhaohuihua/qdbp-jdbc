INSERT INTO #saleSplitProduct (
		saleorderID, ProductCode, ProductNum, ProductPrice, productMaterialKind, periodTypeID,
		baseProduceProductTypeID
	)
	SELECT t.saleOrderID, sop.productCode, sop.productNum, sop.productPrice, NULL, periodTypeID,
		ISNULL ( t.baseProduceProductTypeID, 6 ) 
	FROM #tempSale t  
	INNER JOIN saleOrderDzProduct sop ON sop.saleOrderID = t.saleOrderID;

	
UPDATE psto SET productMaterialKind = sop.productMaterialKind
	FROM #tempSale t 
	JOIN dbo.produceSaleTaskOrderDetail AS pstod ON t.saleOrderID=pstod.saleOrderID
	JOIN dbo.produceSaleTaskOrder AS psto ON pstod.taskorderID = psto.taskOrderID
	JOIN dbo.saleOrder AS so ON t.saleOrderID = so.saleOrderID
	JOIN dbo.saleOrderDzProduct AS sop ON pstod.saleOrderID = sop.saleOrderID
