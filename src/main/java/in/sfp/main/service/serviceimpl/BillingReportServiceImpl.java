package in.sfp.main.service.serviceimpl;

import in.sfp.main.models.TotalStockBillingInfo;
import in.sfp.main.repository.StockBillingInfoRepository;
import in.sfp.main.service.BillingReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BillingReportServiceImpl implements BillingReportService {

    @Autowired
    private StockBillingInfoRepository stockRepo;

    @Override
    @Transactional(readOnly = true)
    public TotalStockBillingInfo getBillDetailById(Long id) {
        TotalStockBillingInfo bill = stockRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + id));

        validateOwnership(bill);
        return bill;
    }

    @Override
    @Transactional(readOnly = true)
    public TotalStockBillingInfo getBillDetailByInvoiceNumber(String invoiceNumber) {
        TotalStockBillingInfo bill = stockRepo.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Bill not found with Invoice: " + invoiceNumber));

        validateOwnership(bill);
        return bill;
    }

    private void validateOwnership(TotalStockBillingInfo bill) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null)
            return;

        String currentUser = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUser.equalsIgnoreCase(bill.getStockCreatedBy())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized access to this bill.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TotalStockBillingInfo> getAllBills() {
        return stockRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TotalStockBillingInfo> getAllBillsByUser(String email, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return stockRepo.findAll();
        }
        return stockRepo.findByStockCreatedBy(email);
    }

    @Override
    public String getNextInvoiceNumber(String email) {
        return stockRepo.findFirstByStockCreatedByOrderByIdDesc(email)
                .map(lastBill -> {
                    String lastNum = lastBill.getInvoiceNumber();
                    try {
                        // Attempt to find the numeric part at the end
                        String numericPart = lastNum.replaceAll("[^0-9]", "");
                        if (numericPart.isEmpty())
                            return "INV-1001";
                        long nextVal = Long.parseLong(numericPart) + 1;
                        String prefix = lastNum.substring(0, lastNum.lastIndexOf(numericPart));
                        return prefix + nextVal;
                    } catch (Exception e) {
                        return "INV-1001";
                    }
                })
                .orElse("INV-1001");
    }
}
