package in.sfp.main.controllers;

import in.sfp.main.models.TotalStockBillingInfo;
import in.sfp.main.service.serviceimpl.BillingReportServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/billing-app/api/reports")
public class ReportsController {

    @Autowired
    private BillingReportServiceImpl reportService;

    @GetMapping("/bill/{id}")
    public ResponseEntity<TotalStockBillingInfo> getBillDetail(@PathVariable Long id) {
        TotalStockBillingInfo bill = reportService.getBillDetailById(id);
        if (bill == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/bill/invoice/{invoiceNumber}")
    public ResponseEntity<TotalStockBillingInfo> getBillByInvoice(@PathVariable String invoiceNumber) {
        TotalStockBillingInfo bill = reportService.getBillDetailByInvoiceNumber(invoiceNumber);
        if (bill == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/bills/all")
    public ResponseEntity<List<TotalStockBillingInfo>> getAllBills() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String email = auth.getName();
        String role = auth.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("CLIENT");

        return ResponseEntity.ok(reportService.getAllBillsByUser(email, role));
    }

    @GetMapping("/next-invoice-number")
    public ResponseEntity<java.util.Map<String, String>> getNextInvoiceNumber() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String email = auth.getName();
        String nextNum = reportService.getNextInvoiceNumber(email);
        return ResponseEntity.ok(java.util.Map.of("nextInvoiceNumber", nextNum));
    }
}
