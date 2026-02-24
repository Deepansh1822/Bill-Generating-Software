package in.sfp.main.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "business_billing_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "stockBillingInfos", "recipientBillingInfos" })
@ToString(exclude = { "stockBillingInfos", "recipientBillingInfos" })
public class BusinessBillingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // business details
    @Lob
    private byte[] businessLogo;
    private String businessOwnerName;
    private String contactPerson;
    private String businessStreetAddress;
    private String businessNumber;
    private String contactPersonNumber;
    private String businessEmail;
    private String contactPersonEmail;
    private String businessCity;
    private String businessState;
    private String businessCountry;
    private String pinCode;
    private String termsAndCondition; // policy and conditions of company
    private String businessGstNumber;
    private String panNumber;
    private String adCode;
    private String iecCode;

    @ElementCollection
    @CollectionTable(name = "business_bank_details", joinColumns = @JoinColumn(name = "business_id"))
    @Column(name = "bank_detail")
    private List<String> bankDetails = new ArrayList<>(); // Company Name, Bank Name, Account Number, IFSC Code, Branch
                                                          // Name

    private LocalTime createdAt;
    private String createdBy;
    private LocalTime updatedAt;
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalTime.now();
    }

    @OneToMany(mappedBy = "businessBillingInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TotalStockBillingInfo> stockBillingInfos = new ArrayList<>();

    @OneToMany(mappedBy = "businessBillingInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipientBillingInfo> recipientBillingInfos = new ArrayList<>();
}
