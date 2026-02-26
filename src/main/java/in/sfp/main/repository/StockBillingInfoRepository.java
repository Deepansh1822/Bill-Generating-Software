package in.sfp.main.repository;

import in.sfp.main.models.TotalStockBillingInfo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockBillingInfoRepository extends JpaRepository<TotalStockBillingInfo, Long> {

    @EntityGraph(attributePaths = { "billItems", "businessBillingInfo", "recipientBillingInfo" })
    Optional<TotalStockBillingInfo> findById(Long id);

    @EntityGraph(attributePaths = { "businessBillingInfo", "recipientBillingInfo" })
    Optional<TotalStockBillingInfo> findByInvoiceNumber(String invoiceNumber);

    @EntityGraph(attributePaths = { "businessBillingInfo", "recipientBillingInfo" })
    java.util.List<TotalStockBillingInfo> findByStockCreatedBy(String email);

    boolean existsByInvoiceNumber(String invoiceNumber);
}
