package in.sfp.main.service.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import in.sfp.main.models.BusinessBillingInfo;
import in.sfp.main.repository.UserBillingRepo;
import in.sfp.main.service.UserBillingService;

@Service
public class UserBillingServiceImpl implements UserBillingService {

    @Autowired
    private UserBillingRepo usersBillingRepo;

    public BusinessBillingInfo saveUsersBillingInfo(BusinessBillingInfo usersBilling) {
        return usersBillingRepo.save(usersBilling);
    }

    public BusinessBillingInfo findByBusinessEmail(String businessEmail) {
        return usersBillingRepo.findByBusinessEmail(businessEmail);
    }

    public BusinessBillingInfo findByBusinessNumber(String businessNumber) {
        return usersBillingRepo.findByBusinessNumber(businessNumber);
    }

    public BusinessBillingInfo findByCreatedBy(String createdBy) {
        return usersBillingRepo.findByCreatedBy(createdBy);
    }

    public BusinessBillingInfo updateUsersBillingInfo(BusinessBillingInfo usersBilling, Long id) {
        BusinessBillingInfo existing = usersBillingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Billing profile not found"));

        existing.setBusinessLogo(usersBilling.getBusinessLogo());
        existing.setBusinessOwnerName(usersBilling.getBusinessOwnerName());
        existing.setContactPerson(usersBilling.getContactPerson());
        existing.setBusinessStreetAddress(usersBilling.getBusinessStreetAddress());
        existing.setBusinessNumber(usersBilling.getBusinessNumber());
        existing.setContactPersonNumber(usersBilling.getContactPersonNumber());
        existing.setBusinessEmail(usersBilling.getBusinessEmail());
        existing.setContactPersonEmail(usersBilling.getContactPersonEmail());
        existing.setBusinessCity(usersBilling.getBusinessCity());
        existing.setBusinessState(usersBilling.getBusinessState());
        existing.setBusinessCountry(usersBilling.getBusinessCountry());
        existing.setPinCode(usersBilling.getPinCode());
        existing.setTermsAndCondition(usersBilling.getTermsAndCondition());
        existing.setBusinessGstNumber(usersBilling.getBusinessGstNumber());
        existing.setPanNumber(usersBilling.getPanNumber());
        existing.setAdCode(usersBilling.getAdCode());
        existing.setIecCode(usersBilling.getIecCode());
        existing.setBankDetails(usersBilling.getBankDetails());
        existing.setUpdatedAt(usersBilling.getUpdatedAt());
        existing.setUpdatedBy(usersBilling.getUpdatedBy());

        return usersBillingRepo.save(existing);
    }
}
