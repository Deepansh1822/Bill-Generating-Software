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
    Optional<TotalStockBillingInfo> findByInvoiceNumberAndStockCreatedBy(String invoiceNumber, String email);

    @EntityGraph(attributePaths = { "businessBillingInfo", "recipientBillingInfo" })
    java.util.List<TotalStockBillingInfo> findByStockCreatedBy(String email);

    boolean existsByInvoiceNumber(String invoiceNumber);

    Optional<TotalStockBillingInfo> findFirstByStockCreatedByOrderByIdDesc(String email);

    // Optimized JPQL queries for Dashboard Performance
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(t) FROM TotalStockBillingInfo t WHERE t.status = :status")
    long countByStatus(String status);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(CAST(t.stockTotalAmount AS double)) FROM TotalStockBillingInfo t WHERE t.status = 'FINAL'")
    Double sumTotalAmountForFinalized();

    @org.springframework.data.jpa.repository.Query("SELECT t.stockCreatedBy, SUM(CAST(t.stockTotalAmount AS double)) FROM TotalStockBillingInfo t WHERE t.status = 'FINAL' GROUP BY t.stockCreatedBy ORDER BY SUM(CAST(t.stockTotalAmount AS double)) DESC")
    java.util.List<Object[]> findTopClientsByRevenue(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(t) FROM TotalStockBillingInfo t WHERE t.status = :status AND t.stockCreatedBy = :email")
    long countByStatusAndStockCreatedBy(String status, String email);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(CAST(t.stockTotalAmount AS double)) FROM TotalStockBillingInfo t WHERE t.status = 'FINAL' AND t.stockCreatedBy = :email")
    Double sumTotalAmountForFinalizedByUser(String email);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(CAST(t.balancePayment AS double)) FROM TotalStockBillingInfo t WHERE t.status = 'FINAL' AND t.stockCreatedBy = :email")
    Double sumBalanceDueByUser(String email);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TotalStockBillingInfo t WHERE t.stockCreatedBy = :email ORDER BY t.stockCreatedAt DESC")
    java.util.List<TotalStockBillingInfo> findRecentByCreatedBy(String email, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TotalStockBillingInfo t ORDER BY t.stockCreatedAt DESC")
    java.util.List<TotalStockBillingInfo> findRecentGlobal(org.springframework.data.domain.Pageable pageable);

    @EntityGraph(attributePaths = { "billItems" })
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TotalStockBillingInfo t ORDER BY t.stockCreatedAt DESC")
    java.util.List<TotalStockBillingInfo> findTrendBillsGlobal(org.springframework.data.domain.Pageable pageable);
}
