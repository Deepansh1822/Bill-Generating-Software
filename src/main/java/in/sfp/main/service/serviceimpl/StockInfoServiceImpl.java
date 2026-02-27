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
        if (stock.getCreatedBy() == null) {
            stock.setCreatedBy(getCurrentUser());
        }
        return stockInfoRepository.save(stock);
    }

    private String getCurrentUser() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return "System";
    }

    @Override
    public List<StockInfo> getAllStocks() {
        return stockInfoRepository.findAll();
    }

    @Override
    public List<StockInfo> getAllStocksByUser(String email, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return stockInfoRepository.findAll();
        } else {
            return stockInfoRepository.findByCreatedBy(email);
        }
    }

    @Override
    public StockInfo getStockById(Long id) {
        StockInfo stock = stockInfoRepository.findById(id).orElse(null);
        if (stock != null)
            validateOwnership(stock);
        return stock;
    }

    @Override
    public StockInfo getStockByName(String itemName) {
        StockInfo stock = stockInfoRepository.findByItemName(itemName);
        if (stock != null)
            validateOwnership(stock);
        return stock;
    }

    @Override
    public void deleteStock(Long id) {
        StockInfo stock = stockInfoRepository.findById(id).orElse(null);
        if (stock != null) {
            validateOwnership(stock);
            stockInfoRepository.deleteById(id);
        }
    }

    @Override
    public StockInfo updateStock(Long id, StockInfo stock) {
        StockInfo existingStock = stockInfoRepository.findById(id).orElse(null);
        if (existingStock != null) {
            validateOwnership(existingStock);
            existingStock.setItemName(stock.getItemName());
            existingStock.setItemDescription(stock.getItemDescription());
            existingStock.setHsnCode(stock.getHsnCode());
            existingStock.setUnitPrice(stock.getUnitPrice());
            existingStock.setAvailableQuantity(stock.getAvailableQuantity());
            existingStock.setCgstRate(stock.getCgstRate());
            existingStock.setSgstRate(stock.getSgstRate());
            existingStock.setIgstRate(stock.getIgstRate());
            existingStock.setStockCategories(stock.getStockCategories());
            existingStock.setSacCode(stock.getSacCode());
            existingStock.setStockType(stock.getStockType());
            existingStock.setUnit(stock.getUnit());
            existingStock.setTaxable(stock.isTaxable());
            existingStock.setCreatedBy(stock.getCreatedBy());
            existingStock.setUpdatedBy(getCurrentUser());

            if (stock.getStockImage() != null) {
                existingStock.setStockImage(stock.getStockImage());
            }
            return stockInfoRepository.save(existingStock);
        }
        return null;
    }

    private void validateOwnership(StockInfo stock) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || auth.getName().equals("anonymousUser"))
            return;

        String currentUser = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUser.equalsIgnoreCase(stock.getCreatedBy())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Unauthorized access to this stock item.");
        }
    }

}
