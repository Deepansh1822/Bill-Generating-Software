package in.sfp.main.controllers;

import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import in.sfp.main.models.EWayBillInfo;
import in.sfp.main.repository.EWayBillRepository;
import in.sfp.main.service.UserAccessService;

@RestController
@RequestMapping("/billing-app/api")
public class EWayBillRestController {

    @Autowired
    private EWayBillRepository ewayBillRepository;

    @Autowired
    private UserAccessService userAccessService;

    @Autowired
    private in.sfp.main.repository.StockBillingInfoRepository stockBillingRepo;

    @PostMapping("/EWayBill/save")
    public ResponseEntity<?> saveEWayBill(@RequestBody EWayBillInfo details) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        // --- VALIDATION LOOPHOLE ---
        if (details.getDocumentNumber() == null || details.getDocumentNumber().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invoice Number is mandatory");
        }

        var invoiceOpt = stockBillingRepo.findByInvoiceNumber(details.getDocumentNumber());
        if (invoiceOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Invoice #" + details.getDocumentNumber() + " not found in system.");
        }

        var invoice = invoiceOpt.get();
        // Check if invoice belongs to current user
        if (!invoice.getStockCreatedBy().equalsIgnoreCase(auth.getName())) {
            return ResponseEntity.status(403).body("You do not have permission to use this Invoice Number.");
        }

        details.setStockCreatedBy(auth.getName());
        details.setStockBillingInfo(invoice);
        details.setEwayBillNumber("EWB-" + System.currentTimeMillis());

        EWayBillInfo saved = ewayBillRepository.save(details);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/EWayBill/fetchInvoice/{invoiceNum}")
    public ResponseEntity<?> fetchInvoiceDetails(@PathVariable String invoiceNum) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        var invoiceOpt = stockBillingRepo.findByInvoiceNumber(invoiceNum);
        if (invoiceOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Invoice not found");
        }

        var invoice = invoiceOpt.get();
        if (!invoice.getStockCreatedBy().equalsIgnoreCase(auth.getName())) {
            return ResponseEntity.status(403).body("Access denied to this invoice");
        }

        // Return relevant details for pre-filling
        java.util.Map<String, Object> data = new java.util.HashMap<>();

        // Recipient (To)
        var recipient = invoice.getRecipientBillingInfo();
        data.put("toName", recipient.getRecipientName());
        data.put("toGSTIN", recipient.getRecipientGstNumber());
        data.put("toState", recipient.getRecipientBusinessState());
        String toFullAddress = (recipient.getRecipientBusinessStreetAddress() != null
                ? recipient.getRecipientBusinessStreetAddress() + ", "
                : "") +
                (recipient.getRecipientBusinessCity() != null ? recipient.getRecipientBusinessCity() : "");
        data.put("toAddress", toFullAddress);
        data.put("toPincode", recipient.getRecipientBusinessPinCode());

        // Business (From)
        var business = invoice.getBusinessBillingInfo();
        data.put("fromName", business.getBusinessName());
        data.put("fromGSTIN", business.getBusinessGstNumber());
        data.put("fromState", business.getBusinessState());
        String fromFullAddress = (business.getBusinessStreetAddress() != null
                ? business.getBusinessStreetAddress() + ", "
                : "") +
                (business.getBusinessCity() != null ? business.getBusinessCity() : "");
        data.put("fromAddress", fromFullAddress);
        data.put("fromPincode", business.getPinCode());

        data.put("consignmentValue", invoice.getStockTotalAmount());
        data.put("docDate", invoice.getInvoiceDate());

        return ResponseEntity.ok(data);
    }

    @GetMapping("/EWayBill/logs")
    public ResponseEntity<List<EWayBillInfo>> getEWayBillLogs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(new ArrayList<>());
        }

        in.sfp.main.models.UserAccessInfo user = userAccessService.findByUsername(auth.getName());
        if (user == null)
            user = userAccessService.findByEmail(auth.getName());

        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.ok(ewayBillRepository.findAll());
        } else {
            return ResponseEntity.ok(ewayBillRepository.findByStockCreatedBy(auth.getName()));
        }
    }

    @GetMapping("/EWayBill/{id}")
    public ResponseEntity<?> getEWayBillById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        var ewayOpt = ewayBillRepository.findById(id);
        if (ewayOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var eway = ewayOpt.get();
        in.sfp.main.models.UserAccessInfo user = userAccessService.findByUsername(auth.getName());
        if (user == null)
            user = userAccessService.findByEmail(auth.getName());

        boolean isAdmin = user != null && "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isAdmin && !eway.getStockCreatedBy().equalsIgnoreCase(auth.getName())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        return ResponseEntity.ok(eway);
    }
}
