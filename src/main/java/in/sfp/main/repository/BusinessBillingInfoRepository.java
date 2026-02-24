package in.sfp.main.repository;

import in.sfp.main.models.BusinessBillingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessBillingInfoRepository extends JpaRepository<BusinessBillingInfo, Long> {
    BusinessBillingInfo findByBusinessEmail(String businessEmail);

    BusinessBillingInfo findByBusinessNumber(String businessNumber);
    // Add other queries as needed
}
