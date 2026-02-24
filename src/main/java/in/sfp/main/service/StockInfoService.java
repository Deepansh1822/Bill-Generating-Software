package in.sfp.main.service;

import java.util.List;

import in.sfp.main.models.StockInfo;

public interface StockInfoService {

    StockInfo saveStock(StockInfo stock);

    List<StockInfo> getAllStocks();

    StockInfo getStockById(Long id);

    StockInfo getStockByName(String itemName);

    void deleteStock(Long id);

    StockInfo updateStock(Long id, StockInfo stock);

}
