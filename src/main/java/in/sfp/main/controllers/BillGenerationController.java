package in.sfp.main.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.sfp.main.dto.BillRequestDTO;
import in.sfp.main.models.BusinessBillingInfo;
import in.sfp.main.models.RecipientBillingInfo;
import in.sfp.main.models.SingleStockBillingInfo;
import in.sfp.main.models.TotalStockBillingInfo;
import in.sfp.main.repository.BusinessBillingInfoRepository;
import in.sfp.main.repository.RecipientBillingInfoRepository;
import in.sfp.main.repository.StockBillingInfoRepository;
import in.sfp.main.repository.StockInfoRepository;
import in.sfp.main.models.StockInfo;

@RestController
@RequestMapping("/billing-app/api")
public class BillGenerationController {

    @Autowired
    private BusinessBillingInfoRepository businessRepo;

    @Autowired
    private RecipientBillingInfoRepository recipientRepo;

    @Autowired
    private StockBillingInfoRepository stockBillingRepo;

    @Autowired
    private StockInfoRepository stockRepo;

    @Autowired
    private in.sfp.main.service.UserAccessService userAccessService;

    @PostMapping("/generateBill")
    @Transactional
    public ResponseEntity<?> generateBill(@RequestBody BillRequestDTO request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = auth.getName();

            // 0. Check for Existing Bill for Update logic
            TotalStockBillingInfo totalBilling = stockBillingRepo.findByInvoiceNumber(request.getInvoiceNumber())
                    .orElse(null);

            if (totalBilling != null) {
                // If it exists, check status
                if ("FINAL".equalsIgnoreCase(totalBilling.getStatus())) {
                    return ResponseEntity.badRequest().body("Invoice " + request.getInvoiceNumber()
                            + " is already FINAL and cannot be modified.");
                }
                // If it's DRAFT, we will clear existing items and recipient for fresh update
                // Or just update them. For simplicity in this logic, we'll reuse objects.
            } else {
                totalBilling = new TotalStockBillingInfo();
                totalBilling.setInvoiceNumber(request.getInvoiceNumber());
            }

            // 1. Handle Business Info (From)
            BusinessBillingInfo business = businessRepo.findByBusinessEmail(request.getBusinessEmail());
            if (business == null) {
                business = new BusinessBillingInfo();
                business.setBusinessName(request.getBusinessName());
                business.setBusinessStreetAddress(request.getBusinessStreetAddress());
                business.setBusinessCity(request.getBusinessCity());
                business.setBusinessState(request.getBusinessState());
                business.setBusinessCountry(request.getBusinessCountry());
                business.setPinCode(request.getBusinessPinCode());
                business.setBusinessGstNumber(request.getBusinessGstNumber());
                business.setBusinessNumber(request.getBusinessNumber());
                business.setBusinessEmail(request.getBusinessEmail());
                business.setTermsAndCondition(request.getTermsAndCondition());
                if (request.getPanNumber() != null)
                    business.setPanNumber(request.getPanNumber());
                if (request.getAdCode() != null)
                    business.setAdCode(request.getAdCode());
                if (request.getIecCode() != null)
                    business.setIecCode(request.getIecCode());
                if (request.getBusinessType() != null)
                    business.setBusinessType(request.getBusinessType());

                if (request.getBusinessLogoBase64() != null && !request.getBusinessLogoBase64().isBlank()) {
                    try {
                        byte[] img = java.util.Base64.getDecoder().decode(request.getBusinessLogoBase64());
                        business.setBusinessLogo(img);
                    } catch (IllegalArgumentException ex) {
                    }
                }
                business.setCreatedBy(currentUser);
                business = businessRepo.save(business);

                if (request.getBankDetails() != null && !request.getBankDetails().isEmpty()) {
                    List<String> banks = new java.util.ArrayList<>();
                    for (in.sfp.main.dto.BillRequestDTO.BankDetailDTO b : request.getBankDetails()) {
                        String entry = String.join("|",
                                b.getAccountHolderName() == null ? "" : b.getAccountHolderName(),
                                b.getBankName() == null ? "" : b.getBankName(),
                                b.getAccountNumber() == null ? "" : b.getAccountNumber(),
                                b.getIfscCode() == null ? "" : b.getIfscCode(),
                                b.getBranchName() == null ? "" : b.getBranchName());
                        banks.add(entry);
                    }
                    business.setBankDetails(banks);
                    business = businessRepo.save(business);
                }
            }

            // 2. Handle Recipient Info (To)
            RecipientBillingInfo recipient = totalBilling.getRecipientBillingInfo();
            if (recipient == null) {
                recipient = new RecipientBillingInfo();
            }
            recipient.setRecipientName(request.getRecipientName());
            recipient.setRecipientBusinessName(request.getRecipientBusinessName());
            recipient.setRecipientBusinessStreetAddress(request.getRecipientBusinessStreetAddress());
            recipient.setRecipientBusinessCity(request.getRecipientBusinessCity());
            recipient.setRecipientBusinessState(request.getRecipientBusinessState());
            recipient.setRecipientBusinessCountry(request.getRecipientBusinessCountry());
            recipient.setRecipientBusinessPinCode(request.getRecipientBusinessPinCode());
            recipient.setRecipientGstNumber(request.getRecipientGstNumber());
            recipient.setRecipientPanNumber(request.getRecipientPanNumber());
            recipient.setRecipientAdCode(request.getRecipientAdCode());
            recipient.setRecipientIecCode(request.getRecipientIecCode());
            recipient.setRecipientEmail(request.getRecipientEmail());
            recipient.setRecipientMobileNumber(request.getRecipientMobileNumber());
            recipient.setRecipientCreatedBy(currentUser);
            recipient.setBusinessBillingInfo(business);
            recipient = recipientRepo.save(recipient);

            // 3. Update/Create Stock Billing Info (Invoice)
            totalBilling.setAdvancedPayment(request.getAdvancedPayment());
            totalBilling.setAmountInWords(request.getAmountInWords());
            totalBilling.setBusinessBillingInfo(business);
            totalBilling.setRecipientBillingInfo(recipient);
            totalBilling.setStockCreatedBy(currentUser);
            totalBilling.setBillType(request.getBillType() != null ? request.getBillType().toUpperCase() : "PRODUCT");
            totalBilling.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "DRAFT");

            if (request.getInvoiceDate() != null)
                totalBilling.setInvoiceDate(LocalDate.parse(request.getInvoiceDate()));
            if (request.getDueDate() != null)
                totalBilling.setDueDate(LocalDate.parse(request.getDueDate()));

            // Handle Business logic update
            String invoiceCompanyName = business.getBusinessName();
            String invoiceCompanyType = business.getBusinessType();
            try {
                in.sfp.main.models.UserAccessInfo current = userAccessService.findByUsername(currentUser);
                if (current == null)
                    current = userAccessService.findByEmail(currentUser);

                if ((invoiceCompanyName == null || invoiceCompanyName.isBlank()) && current != null
                        && current.getCompanyName() != null && !current.getCompanyName().isBlank()) {
                    invoiceCompanyName = current.getCompanyName();
                    business.setBusinessName(invoiceCompanyName);
                    business = businessRepo.save(business);
                }
                if ((invoiceCompanyType == null || invoiceCompanyType.isBlank()) && current != null
                        && current.getCompanyType() != null && !current.getCompanyType().isBlank()) {
                    invoiceCompanyType = current.getCompanyType();
                    business.setBusinessType(invoiceCompanyType);
                    business = businessRepo.save(business);
                }
            } catch (Exception e) {
            }

            totalBilling.setCompanyName(invoiceCompanyName);
            totalBilling.setCompanyType(invoiceCompanyType);

            double totalCGST = 0;
            double totalSGST = 0;
            double totalIGST = 0;
            double grandTotal = 0;

            // 3.5 Stock Sync Logic (Smart Inventory Management)
            // A. Restore Stock: If we are updating an existing draft, "return" its items to
            // inventory first
            for (SingleStockBillingInfo oldItem : totalBilling.getBillItems()) {
                StockInfo stock = stockRepo.findByItemNameAndCreatedBy(oldItem.getItemName(), currentUser);
                if (stock != null && "PRODUCT".equalsIgnoreCase(stock.getStockType())) {
                    stock.setAvailableQuantity(stock.getAvailableQuantity() + oldItem.getQuantity());
                    stockRepo.save(stock);
                }
            }

            // B. Strict Mode Check: If finalizing, ensure we have enough physical stock
            if ("FINAL".equalsIgnoreCase(request.getStatus())) {
                for (BillRequestDTO.BillItemDTO newItem : request.getItems()) {
                    StockInfo stock = stockRepo.findByItemNameAndCreatedBy(newItem.getItemName(), currentUser);
                    if (stock != null && "PRODUCT".equalsIgnoreCase(stock.getStockType())) {
                        if (stock.getAvailableQuantity() < newItem.getQuantity()) {
                            return ResponseEntity.badRequest().body("Insufficient stock for: " + newItem.getItemName()
                                    + " (Available: " + stock.getAvailableQuantity() + ")");
                        }
                    }
                }
            }

            // Clear old items for update
            totalBilling.getBillItems().clear();

            for (BillRequestDTO.BillItemDTO itemDto : request.getItems()) {
                // C. Deduct Stock: Apply the new quantities to inventory
                StockInfo stock = stockRepo.findByItemNameAndCreatedBy(itemDto.getItemName(), currentUser);
                if (stock != null && "PRODUCT".equalsIgnoreCase(stock.getStockType())) {
                    stock.setAvailableQuantity(stock.getAvailableQuantity() - itemDto.getQuantity());
                    stockRepo.save(stock);
                }

                SingleStockBillingInfo item = new SingleStockBillingInfo();
                item.setItemName(itemDto.getItemName());
                item.setItemDescription(itemDto.getItemDescription());
                item.setHsnCode(itemDto.getHsnCode());
                item.setQuantity(itemDto.getQuantity());
                item.setUnit(itemDto.getUnit());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setCgstRate(itemDto.getCgstRate());
                item.setSgstRate(itemDto.getSgstRate());
                item.setIgstRate(itemDto.getIgstRate());
                item.setCgstAmount(itemDto.getCgstAmount());
                item.setSgstAmount(itemDto.getSgstAmount());
                item.setIgstAmount(itemDto.getIgstAmount());
                item.setTotalItemAmount(itemDto.getTotalItemAmount());
                item.setStockBillingInfo(totalBilling);

                totalBilling.getBillItems().add(item);

                totalCGST += itemDto.getCgstAmount();
                totalSGST += itemDto.getSgstAmount();
                totalIGST += itemDto.getIgstAmount();
                grandTotal += itemDto.getTotalItemAmount();
            }

            totalBilling.setTotalCGSTAmount(String.valueOf(totalCGST));
            totalBilling.setTotalSGSTAmount(String.valueOf(totalSGST));
            totalBilling.setTotalIGSTAmount(String.valueOf(totalIGST));
            totalBilling.setStockTotalAmount(String.valueOf(grandTotal));

            double balance = grandTotal - Double.parseDouble(request.getAdvancedPayment());
            totalBilling.setBalancePayment(String.valueOf(balance));

            totalBilling = stockBillingRepo.save(totalBilling);

            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", totalBilling.getId());
            response.put("invoiceNumber", totalBilling.getInvoiceNumber());
            response.put("status", totalBilling.getStatus());
            response.put("message", ("FINAL".equalsIgnoreCase(totalBilling.getStatus()) ? "Invoice finalized"
                    : "Quotation saved as Draft") + " successfully!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating bill: " + e.getMessage());
        }
    }

    @Autowired
    private in.sfp.main.service.BillingReportService reportService;

    @GetMapping("/getBill/{invoiceNumber}")
    public ResponseEntity<?> getBillByInvoiceNumber(@PathVariable String invoiceNumber) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String email = auth.getName();
        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("CLIENT");

        return stockBillingRepo.findByInvoiceNumber(invoiceNumber)
                .map(bill -> {
                    // SEC-CHECK: Only ADMIN or the creator can view
                    if (!"ADMIN".equals(role) && !email.equalsIgnoreCase(bill.getStockCreatedBy())) {
                        return (ResponseEntity) ResponseEntity.status(403).body("Access Denied");
                    }
                    return ResponseEntity.ok(bill);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/generateUniqueInvoiceNumber")
    public ResponseEntity<String> generateUniqueInvoiceNumber() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String email = auth.getName();
        String nextNum = reportService.getNextInvoiceNumber(email);
        return ResponseEntity.ok(nextNum);
    }
}
