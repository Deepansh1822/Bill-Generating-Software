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
        return stockRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public TotalStockBillingInfo getBillDetailByInvoiceNumber(String invoiceNumber) {
        return stockRepo.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Bill not found with Invoice: " + invoiceNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TotalStockBillingInfo> getAllBills() {
        return stockRepo.findAll();
    }
}
