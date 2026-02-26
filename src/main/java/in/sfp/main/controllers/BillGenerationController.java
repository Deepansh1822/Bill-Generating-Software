package in.sfp.main.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
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
    private in.sfp.main.service.UserAccessService userAccessService;

    @PostMapping("/generateBill")
    @Transactional
    public ResponseEntity<?> generateBill(@RequestBody BillRequestDTO request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = auth.getName();

            // 0. Check for Invoice Number Uniqueness
            if (stockBillingRepo.existsByInvoiceNumber(request.getInvoiceNumber())) {
                return ResponseEntity.badRequest().body("Invoice number " + request.getInvoiceNumber()
                        + " already exists. Please use a unique number.");
            }

            // 1. Handle Business Info (From)
            // For now, we search by email or create new if not exists
            BusinessBillingInfo business = businessRepo.findByBusinessEmail(request.getBusinessEmail());
            if (business == null) {
                business = new BusinessBillingInfo();
                business.setBusinessName(request.getBusinessName());
                business.setBusinessStreetAddress(request.getBusinessStreetAddress());
                business.setBusinessCity(request.getBusinessCity());
                business.setBusinessState(request.getBusinessState());
                business.setBusinessCountry(request.getBusinessCountry());
                business.setPinCode(request.getBusinessPinCode());
                // contact person details
                business.setContactPerson(request.getContactPerson());
                business.setContactPersonNumber(request.getContactPersonNumber());
                business.setContactPersonEmail(request.getContactPersonEmail());
                business.setBusinessGstNumber(request.getBusinessGstNumber());
                business.setBusinessNumber(request.getBusinessNumber());
                business.setBusinessEmail(request.getBusinessEmail());
                business.setTermsAndCondition(request.getTermsAndCondition());
                // Persist optional business identifiers when provided
                if (request.getPanNumber() != null)
                    business.setPanNumber(request.getPanNumber());
                if (request.getAdCode() != null)
                    business.setAdCode(request.getAdCode());
                if (request.getIecCode() != null)
                    business.setIecCode(request.getIecCode());
                if (request.getBusinessType() != null)
                    business.setBusinessType(request.getBusinessType());

                // If frontend sent a logo (base64) decode and persist
                if (request.getBusinessLogoBase64() != null && !request.getBusinessLogoBase64().isBlank()) {
                    try {
                        byte[] img = java.util.Base64.getDecoder().decode(request.getBusinessLogoBase64());
                        business.setBusinessLogo(img);
                    } catch (IllegalArgumentException ex) {
                        // ignore invalid base64
                    }
                }
                business.setCreatedBy(currentUser);
                business = businessRepo.save(business);
                // persist bank details if provided
                if (request.getBankDetails() != null && !request.getBankDetails().isEmpty()) {
                    List<String> banks = new java.util.ArrayList<>();
                    for (in.sfp.main.dto.BillRequestDTO.BankDetailDTO b : request.getBankDetails()) {
                        // store as simple pipe-separated string: holder|bank|account|ifsc|branch
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
            RecipientBillingInfo recipient = new RecipientBillingInfo();
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

            // 3. Create Stock Billing Info (Invoice)
            TotalStockBillingInfo totalBilling = new TotalStockBillingInfo();
            totalBilling.setInvoiceNumber(request.getInvoiceNumber());
            totalBilling.setAdvancedPayment(request.getAdvancedPayment());
            totalBilling.setAmountInWords(request.getAmountInWords());
            totalBilling.setBusinessBillingInfo(business);
            totalBilling.setRecipientBillingInfo(recipient);
            totalBilling.setStockCreatedBy(currentUser);

            // Snapshot company name/type from the saved business; do not store per-user
            // "bill by" details
            totalBilling.setBillByFullName(null);
            totalBilling.setBillByDesignation(null);
            totalBilling.setBillByMobileNumber(null);
            totalBilling.setBillByEmail(null);

            // Prefer the business record's name/type; if missing, fall back to
            // authenticated user's company fields
            String invoiceCompanyName = business.getBusinessName();
            String invoiceCompanyType = business.getBusinessType();
            try {
                in.sfp.main.models.UserAccessInfo current = userAccessService.findByUsername(currentUser);
                if (current == null)
                    current = userAccessService.findByEmail(currentUser);

                if ((invoiceCompanyName == null || invoiceCompanyName.isBlank()) && current != null
                        && current.getCompanyName() != null && !current.getCompanyName().isBlank()) {
                    invoiceCompanyName = current.getCompanyName();
                    // persist to business record for future invoices
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
                // ignore user lookup failures; invoice will use business values (may be null)
            }

            totalBilling.setCompanyName(invoiceCompanyName);
            totalBilling.setCompanyType(invoiceCompanyType);

            double totalCGST = 0;
            double totalSGST = 0;
            double totalIGST = 0;
            double grandTotal = 0;

            List<SingleStockBillingInfo> items = new ArrayList<>();
            for (BillRequestDTO.BillItemDTO itemDto : request.getItems()) {
                SingleStockBillingInfo item = new SingleStockBillingInfo();
                item.setItemName(itemDto.getItemName());
                item.setItemDescription(itemDto.getItemDescription());
                item.setHsnCode(itemDto.getHsnCode());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setCgstRate(itemDto.getCgstRate());
                item.setSgstRate(itemDto.getSgstRate());
                item.setIgstRate(itemDto.getIgstRate());
                item.setCgstAmount(itemDto.getCgstAmount());
                item.setSgstAmount(itemDto.getSgstAmount());
                item.setIgstAmount(itemDto.getIgstAmount());
                item.setTotalItemAmount(itemDto.getTotalItemAmount());
                item.setStockBillingInfo(totalBilling);

                items.add(item);

                totalCGST += itemDto.getCgstAmount();
                totalSGST += itemDto.getSgstAmount();
                totalIGST += itemDto.getIgstAmount();
                grandTotal += itemDto.getTotalItemAmount();
            }

            totalBilling.setBillItems(items);
            totalBilling.setTotalCGSTAmount(String.valueOf(totalCGST));
            totalBilling.setTotalSGSTAmount(String.valueOf(totalSGST));
            totalBilling.setTotalIGSTAmount(String.valueOf(totalIGST));
            totalBilling.setStockTotalAmount(String.valueOf(grandTotal));

            double balance = grandTotal - Double.parseDouble(request.getAdvancedPayment());
            totalBilling.setBalancePayment(String.valueOf(balance));

            stockBillingRepo.save(totalBilling);

            return ResponseEntity.ok("Bill generated successfully with Invoice: " + request.getInvoiceNumber());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating bill: " + e.getMessage());
        }
    }

    @GetMapping("/generateUniqueInvoiceNumber")
    public ResponseEntity<String> generateUniqueInvoiceNumber() {
        java.util.Random random = new java.util.Random();
        String invoiceNum;
        boolean exists;
        int attempts = 0;
        do {
            // Generate a random 6-digit number
            int num = 100000 + random.nextInt(900000);
            invoiceNum = "INV-" + num;
            exists = stockBillingRepo.existsByInvoiceNumber(invoiceNum);
            attempts++;
            // Safety break to prevent infinite loop
            if (attempts > 100) {
                return ResponseEntity.internalServerError().body("Could not generate unique invoice number");
            }
        } while (exists);

        return ResponseEntity.ok(invoiceNum);
    }
}
