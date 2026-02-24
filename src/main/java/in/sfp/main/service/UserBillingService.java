package in.sfp.main.service;

import in.sfp.main.models.BusinessBillingInfo;

public interface UserBillingService {

    public BusinessBillingInfo saveUsersBillingInfo(BusinessBillingInfo usersBilling);

    public BusinessBillingInfo findByBusinessEmail(String businessEmail);

    public BusinessBillingInfo findByBusinessNumber(String businessNumber);

    public BusinessBillingInfo findByCreatedBy(String createdBy);

    public BusinessBillingInfo updateUsersBillingInfo(BusinessBillingInfo usersBilling, Long id);
}
