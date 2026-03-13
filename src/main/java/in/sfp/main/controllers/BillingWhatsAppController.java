package in.sfp.main.controllers;

import in.sfp.main.models.SingleStockBillingInfo;
import in.sfp.main.models.TotalStockBillingInfo;
import in.sfp.main.service.BillingReportService;
import in.sfp.main.service.PdfService;
import in.sfp.main.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/billing-app/api/whatsapp")
public class BillingWhatsAppController {

    @Autowired
    private BillingReportService reportService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private WhatsAppService whatsappService;

    @PostMapping("/sendInvoice/{billId}")
    @Transactional
    public ResponseEntity<?> sendInvoiceWhatsApp(@PathVariable Long billId, Authentication auth) {
        try {
            TotalStockBillingInfo bill = reportService.getBillDetailById(billId);

            // Helper to parse numbers safely from formatted strings
            java.util.function.Function<String, Double> safeParse = (s) -> {
                if (s == null || s.isBlank()) return 0.0;
                try {
                    return Double.parseDouble(s.replaceAll("[^\\d.]", ""));
                } catch (Exception e) { return 0.0; }
            };

            // Prepare data for PDF (same logic as Email)
            Map<String, Object> model = new HashMap<>();
            model.put("bill", bill);

            double subtotal = 0;
            double totalTax = 0;
            for (SingleStockBillingInfo item : bill.getBillItems()) {
                double qty = item.getQuantity();
                double price = item.getUnitPrice();
                double base = qty * price;
                subtotal += base;
                totalTax += (item.getTotalItemAmount() - base);
            }
            model.put("subtotal", subtotal);
            model.put("totalTax", totalTax);
            model.put("grandTotal", safeParse.apply(bill.getStockTotalAmount()));
            model.put("advancePaid", safeParse.apply(bill.getAdvancedPayment()));
            model.put("balanceDue", safeParse.apply(bill.getBalancePayment()));

            List<Map<String, String>> parsedBanks = new ArrayList<>();
            if (bill.getBusinessBillingInfo() != null && bill.getBusinessBillingInfo().getBankDetails() != null) {
                for (String bankStr : bill.getBusinessBillingInfo().getBankDetails()) {
                    String[] parts = bankStr.split("\\|");
                    Map<String, String> bMap = new HashMap<>();
                    bMap.put("holder", parts.length > 0 ? parts[0] : "");
                    bMap.put("bank", parts.length > 1 ? parts[1] : "");
                    bMap.put("account", parts.length > 2 ? parts[2] : "");
                    bMap.put("ifsc", parts.length > 3 ? parts[3] : "");
                    bMap.put("branch", parts.length > 4 ? parts[4] : "");
                    parsedBanks.add(bMap);
                }
            }
            model.put("parsedBanks", parsedBanks);

            // 1. Generate PDF
            byte[] pdfBytes = pdfService.generatePdf("pdf-invoice", model);

            // 2. Resolve Phone Number
            String recipientPhone = (bill.getRecipientBillingInfo() != null
                    && bill.getRecipientBillingInfo().getRecipientMobileNumber() != null)
                            ? bill.getRecipientBillingInfo().getRecipientMobileNumber()
                            : bill.getBillByMobileNumber();

            if (recipientPhone == null || recipientPhone.isBlank()) {
                return ResponseEntity.badRequest().body("Recipient phone number not found for this bill.");
            }

            // Clean phone number (Meta API requires country code and no prefix like +)
            // Example: 919876543210
            recipientPhone = recipientPhone.replaceAll("[^0-9]", "");
            if (recipientPhone.length() == 10) {
                recipientPhone = "91" + recipientPhone; // Default to India if only 10 digits
            }

            // 3. Send via WhatsApp
            String fileName = "Invoice_" + bill.getInvoiceNumber() + ".pdf";
            whatsappService.sendInvoicePdf(recipientPhone, pdfBytes, fileName);

            return ResponseEntity
                    .ok(Map.of("message", "Professional WhatsApp PDF sent successfully to " + recipientPhone));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "WhatsApp delivery failed: " + e.getMessage()));
        }
    }

    @PostMapping("/sendInvoiceWithFile/{billId}")
    @Transactional
    public ResponseEntity<?> sendInvoiceWhatsAppWithFile(@PathVariable Long billId, 
                                                       @RequestParam("file") MultipartFile file,
                                                       Authentication auth) {
        try {
            TotalStockBillingInfo bill = reportService.getBillDetailById(billId);
            
            String recipientPhone = (bill.getRecipientBillingInfo() != null
                    && bill.getRecipientBillingInfo().getRecipientMobileNumber() != null)
                            ? bill.getRecipientBillingInfo().getRecipientMobileNumber()
                            : bill.getBillByMobileNumber();

            if (recipientPhone == null || recipientPhone.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Recipient phone not found."));
            }

            recipientPhone = recipientPhone.replaceAll("[^0-9]", "");
            if (recipientPhone.length() == 10) recipientPhone = "91" + recipientPhone;

            whatsappService.sendInvoicePdf(recipientPhone, file.getBytes(), file.getOriginalFilename());

            return ResponseEntity.ok(Map.of("message", "WhatsApp PDF sent successfully."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed: " + e.getMessage()));
        }
    }
}
