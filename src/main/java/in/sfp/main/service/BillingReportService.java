package in.sfp.main.service;

import in.sfp.main.models.TotalStockBillingInfo;
import java.util.List;

public interface BillingReportService {
    TotalStockBillingInfo getBillDetailById(Long id);

    TotalStockBillingInfo getBillDetailByInvoiceNumber(String invoiceNumber);

    List<TotalStockBillingInfo> getAllBills();
}
