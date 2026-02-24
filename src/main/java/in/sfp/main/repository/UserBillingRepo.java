package in.sfp.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.sfp.main.models.BusinessBillingInfo;

@Repository
public interface UserBillingRepo extends JpaRepository<BusinessBillingInfo, Long> {

    BusinessBillingInfo findByBusinessEmail(String businessEmail);

    BusinessBillingInfo findByBusinessNumber(String businessNumber);

    BusinessBillingInfo findByCreatedBy(String createdBy);

}
