package in.sfp.main.dto;

import java.util.List;

import lombok.Data;

@Data
public class BillRequestDTO {
    private String invoiceNumber;

    // Business (From) details
    private String businessName;
    private String businessStreetAddress;
    private String businessCity;
    private String businessState;
    private String businessCountry;
    private String businessPinCode;
    private String businessGstNumber;
    private String businessNumber;
    private String businessEmail;
    private String termsAndCondition;

    // Recipient (To) details
    private String recipientName;
    private String recipientBusinessName;
    private String recipientBusinessStreetAddress;
    private String recipientBusinessCity;
    private String recipientBusinessState;
    private String recipientBusinessCountry;
    private String recipientBusinessPinCode;
    private String recipientGstNumber;
    private String recipientMobileNumber;
    private String recipientEmail;
    private String recipientPanNumber;
    private String recipientAdCode;
    private String recipientIecCode;

    // Billing details
    private String advancedPayment;
    private String amountInWords;
    private String panNumber;
    private String adCode;
    private String iecCode;
    private String businessType;
    private String businessLogoBase64;
    private String invoiceDate;
    private String dueDate;
    private String billType;
    private String status;
    private List<BillItemDTO> items;

    @Data
    public static class BankDetailDTO {
        private String accountHolderName;
        private String bankName;
        private String accountNumber;
        private String ifscCode;
        private String branchName;
    }

    private List<BankDetailDTO> bankDetails;

    @Data
    public static class BillItemDTO {
        private String itemName;
        private String itemDescription;
        private String hsnCode;
        private int quantity;
        private String unit;
        private double unitPrice;
        private double cgstRate;
        private double sgstRate;
        private double igstRate;
        private double cgstAmount;
        private double sgstAmount;
        private double igstAmount;
        private double totalItemAmount;
    }
}
