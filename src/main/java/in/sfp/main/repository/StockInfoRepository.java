package in.sfp.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.sfp.main.models.StockInfo;

@Repository
public interface StockInfoRepository extends JpaRepository<StockInfo, Long> {

    StockInfo findByItemNameAndCreatedBy(String itemName, String createdBy);

    java.util.List<StockInfo> findByCreatedBy(String createdBy);

    java.util.List<StockInfo> findByStockCategoriesIdAndCreatedBy(Long categoryId, String createdBy);

    StockInfo findByItemName(String itemName);
}
