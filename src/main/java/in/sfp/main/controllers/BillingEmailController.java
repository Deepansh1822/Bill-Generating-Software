package in.sfp.main.controllers;

import in.sfp.main.models.SingleStockBillingInfo;
import in.sfp.main.models.TotalStockBillingInfo;
import in.sfp.main.service.BillingReportService;
import in.sfp.main.service.PdfService;
import in.sfp.main.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/billing-app/api/email")
public class BillingEmailController {

    @Autowired
    private BillingReportService reportService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/sendInvoice/{billId}")
    public ResponseEntity<?> sendInvoiceEmail(@PathVariable Long billId, Authentication auth) {
        try {
            TotalStockBillingInfo bill = reportService.getBillDetailById(billId);

            // Prepare data for PDF
            Map<String, Object> model = new HashMap<>();
            model.put("bill", bill);

            // Calculate subtotal and tax for PDF (since it's not stored directly as a
            // single sum)
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
            model.put("grandTotal", Double.parseDouble(bill.getStockTotalAmount()));

            // Parse Bank Details (String joined by |)
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

            // 2. Prepare Email
            String businessName = bill.getBusinessBillingInfo() != null
                    ? bill.getBusinessBillingInfo().getBusinessName()
                    : "SFP Billing";
            String recipientName = bill.getRecipientBillingInfo() != null
                    ? bill.getRecipientBillingInfo().getRecipientName()
                    : "Valued Customer";
            String recipientEmail = (bill.getRecipientBillingInfo() != null
                    && bill.getRecipientBillingInfo().getRecipientEmail() != null)
                            ? bill.getRecipientBillingInfo().getRecipientEmail()
                            : bill.getBillByEmail();

            if (recipientEmail == null || recipientEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Recipient email not found for this bill.");
            }

            String subject = "Official Invoice #" + bill.getInvoiceNumber() + " from " + businessName;
            String body = "Dear " + recipientName + ",\n\n" +
                    "Please find attached the official PDF of your invoice #" + bill.getInvoiceNumber() + ".\n\n" +
                    "Summary:\n" +
                    "Invoice No: #" + bill.getInvoiceNumber() + "\n" +
                    "Total Amount: ₹" + bill.getStockTotalAmount() + "\n" +
                    "Date: " + bill.getInvoiceDate() + "\n\n" +
                    "Thank you for your business!\n\n" +
                    "Best Regards,\n" +
                    businessName;

            // 3. Send Email
            String fileName = "Invoice_" + bill.getInvoiceNumber() + ".pdf";
            emailService.sendEmailWithAttachment(recipientEmail, subject, body, pdfBytes, fileName);

            return ResponseEntity.ok(
                    Map.of("message", "Professional Email sent with PDF attachment successfully to " + recipientEmail));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to send email: " + e.getMessage());
        }
    }
}
