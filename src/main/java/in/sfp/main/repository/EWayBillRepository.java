package in.sfp.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.sfp.main.models.EWayBillInfo;
import in.sfp.main.models.TotalStockBillingInfo;

@Repository
public interface EWayBillRepository extends JpaRepository<EWayBillInfo, Long> {

    // Find all E-Way Bills for a specific Invoice (usually it's 1:1, but could be
    // multiple if vehicle changes)
    List<EWayBillInfo> findByStockBillingInfo(TotalStockBillingInfo stockBillingInfo);

    // New: Find by username for Client view
    List<EWayBillInfo> findByStockCreatedBy(String username);

    // Find by E-Way Bill Number (useful for later search)
    Optional<EWayBillInfo> findByEwayBillNumber(String ewayBillNumber);
}
