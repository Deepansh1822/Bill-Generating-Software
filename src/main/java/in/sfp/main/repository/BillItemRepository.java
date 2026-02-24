package in.sfp.main.repository;

import in.sfp.main.models.SingleStockBillingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillItemRepository extends JpaRepository<SingleStockBillingInfo, Long> {
}
