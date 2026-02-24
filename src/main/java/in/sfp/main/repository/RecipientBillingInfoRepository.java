package in.sfp.main.repository;

import in.sfp.main.models.RecipientBillingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipientBillingInfoRepository extends JpaRepository<RecipientBillingInfo, Long> {
}
