package in.sfp.main.controllers;

import in.sfp.main.models.TotalStockBillingInfo;
import in.sfp.main.service.serviceimpl.BillingReportServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private in.sfp.main.service.PdfService pdfService;

    @GetMapping("/bill/download/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
        try {
            TotalStockBillingInfo bill = reportService.getBillDetailById(id);
            if (bill == null)
                return ResponseEntity.notFound().build();

            // Prepare model for PDF
            java.util.Map<String, Object> model = new java.util.HashMap<>();
            model.put("bill", bill);

            // Helper to parse numbers safely from formatted strings
            java.util.function.Function<String, Double> safeParse = (s) -> {
                if (s == null || s.isBlank()) return 0.0;
                try {
                    return Double.parseDouble(s.replaceAll("[^\\d.]", ""));
                } catch (Exception e) { return 0.0; }
            };

            // Calculate subtotal and tax
            double subtotal = 0;
            double totalTax = 0;
            for (in.sfp.main.models.SingleStockBillingInfo item : bill.getBillItems()) {
                double base = item.getQuantity() * item.getUnitPrice();
                subtotal += base;
                totalTax += (item.getTotalItemAmount() - base);
            }
            model.put("subtotal", subtotal);
            model.put("totalTax", totalTax);
            model.put("grandTotal", safeParse.apply(bill.getStockTotalAmount()));
            model.put("advancePaid", safeParse.apply(bill.getAdvancedPayment()));
            model.put("balanceDue", safeParse.apply(bill.getBalancePayment()));

            // Parse Bank Details
            java.util.List<java.util.Map<String, String>> parsedBanks = new java.util.ArrayList<>();
            if (bill.getBusinessBillingInfo() != null && bill.getBusinessBillingInfo().getBankDetails() != null) {
                for (String bankStr : bill.getBusinessBillingInfo().getBankDetails()) {
                    String[] parts = bankStr.split("\\|");
                    java.util.Map<String, String> bMap = new java.util.HashMap<>();
                    bMap.put("holder", parts.length > 0 ? parts[0] : "");
                    bMap.put("bank", parts.length > 1 ? parts[1] : "");
                    bMap.put("account", parts.length > 2 ? parts[2] : "");
                    bMap.put("ifsc", (parts.length > 3 && parts[3] != null) ? parts[3] : "");
                    bMap.put("branch", (parts.length > 4 && parts[4] != null) ? parts[4] : "");
                    parsedBanks.add(bMap);
                }
            }
            model.put("parsedBanks", parsedBanks);

            byte[] pdfBytes = pdfService.generatePdf("pdf-invoice", model);

            String fileName = "Invoice_" + bill.getInvoiceNumber() + ".pdf";
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + fileName)
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }
}
