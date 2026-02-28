package in.sfp.main.service.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.sfp.main.models.BusinessBillingInfo;
import in.sfp.main.repository.BusinessBillingInfoRepository;
import in.sfp.main.service.BusinessBillingService;

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
        BusinessBillingInfo info = businessRepo.findByBusinessEmail(email);
        if (info != null)
            validateOwnership(info);
        return info;
    }

    @Override
    public BusinessBillingInfo findByBusinessNumber(String businessNumber) {
        BusinessBillingInfo info = businessRepo.findByBusinessNumber(businessNumber);
        if (info != null)
            validateOwnership(info);
        return info;
    }

    @Override
    public BusinessBillingInfo updateBusinessBillingInfo(BusinessBillingInfo businessBillingInfo, Long id) {
        BusinessBillingInfo existing = businessRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Business profile not found"));

        validateOwnership(existing);

        existing.setBusinessLogo(businessBillingInfo.getBusinessLogo());
        existing.setBusinessName(businessBillingInfo.getBusinessName());
        existing.setBusinessStreetAddress(businessBillingInfo.getBusinessStreetAddress());
        existing.setBusinessNumber(businessBillingInfo.getBusinessNumber());
        existing.setBusinessEmail(businessBillingInfo.getBusinessEmail());
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
        existing.setUpdatedAt(java.time.LocalTime.now());
        existing.setUpdatedBy(getCurrentUser());

        return businessRepo.save(existing);
    }

    private String getCurrentUser() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "System";
    }

    private void validateOwnership(BusinessBillingInfo info) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || auth.getName().equals("anonymousUser"))
            return;

        String currentUser = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUser.equalsIgnoreCase(info.getCreatedBy())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Unauthorized access to this business profile.");
        }
    }

    @Override
    public List<BusinessBillingInfo> getAllBusinesses() {
        return businessRepo.findAll();
    }
}
