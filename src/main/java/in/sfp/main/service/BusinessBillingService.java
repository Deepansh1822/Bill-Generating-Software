package in.sfp.main.service;

import in.sfp.main.models.BusinessBillingInfo;
import java.util.List;

public interface BusinessBillingService {
    BusinessBillingInfo saveBusinessBillingInfo(BusinessBillingInfo businessBillingInfo);

    BusinessBillingInfo findByEmail(String email);

    BusinessBillingInfo findByBusinessNumber(String businessNumber);

    BusinessBillingInfo updateBusinessBillingInfo(BusinessBillingInfo businessBillingInfo, Long id);

    List<BusinessBillingInfo> getAllBusinesses();
}
