package in.sfp.main.service;

import in.sfp.main.models.TotalStockBillingInfo;
import java.util.List;

public interface BillingReportService {
    TotalStockBillingInfo getBillDetailById(Long id);

    TotalStockBillingInfo getBillDetailByInvoiceNumber(String invoiceNumber);

    List<TotalStockBillingInfo> getAllBills();

    List<TotalStockBillingInfo> getAllBillsByUser(String email, String role);

    String getNextInvoiceNumber(String email);
}
