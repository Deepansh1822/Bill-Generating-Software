package in.sfp.main.service.serviceimpl;

import in.sfp.main.models.BusinessBillingInfo;
import in.sfp.main.repository.BusinessBillingInfoRepository;
import in.sfp.main.service.BusinessBillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BusinessBillingServiceImpl implements BusinessBillingService {

    @Autowired
    private BusinessBillingInfoRepository businessRepo;

    @Override
    public BusinessBillingInfo saveBusinessBillingInfo(BusinessBillingInfo businessBillingInfo) {
        return businessRepo.save(businessBillingInfo);
    }

    @Override
    public BusinessBillingInfo findByEmail(String email) {
        return businessRepo.findByBusinessEmail(email);
    }

    @Override
    public BusinessBillingInfo findByBusinessNumber(String businessNumber) {
        return businessRepo.findByBusinessNumber(businessNumber);
    }

    @Override
    public BusinessBillingInfo updateBusinessBillingInfo(BusinessBillingInfo businessBillingInfo, Long id) {
        BusinessBillingInfo existing = businessRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Business profile not found"));

        existing.setBusinessLogo(businessBillingInfo.getBusinessLogo());
        existing.setBusinessOwnerName(businessBillingInfo.getBusinessOwnerName());
        existing.setContactPerson(businessBillingInfo.getContactPerson());
        existing.setBusinessStreetAddress(businessBillingInfo.getBusinessStreetAddress());
        existing.setBusinessNumber(businessBillingInfo.getBusinessNumber());
        existing.setContactPersonNumber(businessBillingInfo.getContactPersonNumber());
        existing.setBusinessEmail(businessBillingInfo.getBusinessEmail());
        existing.setContactPersonEmail(businessBillingInfo.getContactPersonEmail());
        existing.setBusinessCity(businessBillingInfo.getBusinessCity());
        existing.setBusinessState(businessBillingInfo.getBusinessState());
        existing.setBusinessCountry(businessBillingInfo.getBusinessCountry());
        existing.setPinCode(businessBillingInfo.getPinCode());
        existing.setTermsAndCondition(businessBillingInfo.getTermsAndCondition());
        existing.setBusinessGstNumber(businessBillingInfo.getBusinessGstNumber());
        existing.setPanNumber(businessBillingInfo.getPanNumber());
        existing.setAdCode(businessBillingInfo.getAdCode());
        existing.setIecCode(businessBillingInfo.getIecCode());
        existing.setBankDetails(businessBillingInfo.getBankDetails());
        existing.setUpdatedAt(businessBillingInfo.getUpdatedAt());
        existing.setUpdatedBy(businessBillingInfo.getUpdatedBy());

        return businessRepo.save(existing);
    }

    @Override
    public List<BusinessBillingInfo> getAllBusinesses() {
        return businessRepo.findAll();
    }
}
