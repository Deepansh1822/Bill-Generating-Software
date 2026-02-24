package in.sfp.main.service.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.sfp.main.models.StockInfo;
import in.sfp.main.repository.StockInfoRepository;
import in.sfp.main.service.StockInfoService;

@Service
public class StockInfoServiceImpl implements StockInfoService {

    @Autowired
    private StockInfoRepository stockInfoRepository;

    @Override
    public StockInfo saveStock(StockInfo stock) {
        return stockInfoRepository.save(stock);
    }

    @Override
    public List<StockInfo> getAllStocks() {
        return stockInfoRepository.findAll();
    }

    @Override
    public StockInfo getStockById(Long id) {
        return stockInfoRepository.findById(id).orElse(null);
    }

    @Override
    public StockInfo getStockByName(String itemName) {
        return stockInfoRepository.findByItemName(itemName);
    }

    @Override
    public void deleteStock(Long id) {
        stockInfoRepository.deleteById(id);
    }

    @Override
    public StockInfo updateStock(Long id, StockInfo stock) {
        StockInfo existingStock = stockInfoRepository.findById(id).orElse(null);
        if (existingStock != null) {
            existingStock.setItemName(stock.getItemName());
            existingStock.setItemDescription(stock.getItemDescription());
            existingStock.setHsnCode(stock.getHsnCode());
            existingStock.setUnitPrice(stock.getUnitPrice());
            existingStock.setAvailableQuantity(stock.getAvailableQuantity());
            existingStock.setCgstRate(stock.getCgstRate());
            existingStock.setSgstRate(stock.getSgstRate());
            existingStock.setIgstRate(stock.getIgstRate());
            existingStock.setStockCategories(stock.getStockCategories());
            if (stock.getStockImage() != null) {
                existingStock.setStockImage(stock.getStockImage());
            }
            return stockInfoRepository.save(existingStock);
        }
        return null;
    }

}
