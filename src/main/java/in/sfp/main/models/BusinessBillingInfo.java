package in.sfp.main.models;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "business_billing_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "stockBillingInfos", "recipientBillingInfos" })
@ToString(exclude = { "stockBillingInfos", "recipientBillingInfos" })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class BusinessBillingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // business details
    @Lob
    private byte[] businessLogo;
    private String businessName;
    private String businessStreetAddress;
    private String businessNumber;
    private String businessEmail;
    private String businessCity;
    private String businessState;
    private String businessCountry;
    private String pinCode;
    private String termsAndCondition; // policy and conditions of company
    private String businessGstNumber;
    private String businessType;
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

    @JsonIgnore
    @OneToMany(mappedBy = "businessBillingInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference("business-bill")
    private List<TotalStockBillingInfo> stockBillingInfos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "businessBillingInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("business-recipient")
    private List<RecipientBillingInfo> recipientBillingInfos = new ArrayList<>();
}
