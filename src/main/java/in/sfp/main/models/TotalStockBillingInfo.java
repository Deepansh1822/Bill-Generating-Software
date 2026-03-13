package in.sfp.main.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "stock_billing_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "billItems", "businessBillingInfo", "recipientBillingInfo" })
@ToString(exclude = { "billItems", "businessBillingInfo", "recipientBillingInfo" })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TotalStockBillingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String invoiceNumber; // Unique Invoice ID

    private String totalCGSTAmount;
    private String totalSGSTAmount;
    private String totalIGSTAmount;
    private String stockTotalAmount; // Grand Total
    private String amountInWords;

    private String advancedPayment;
    private String balancePayment;

    // Snapshot of who generated the bill (bill-by)
    private String billByFullName;
    private String billByDesignation;
    private String billByMobileNumber;
    private String billByEmail;

    // Snapshot of company info at time of billing
    private String companyName;
    private String companyType;
    private String billType; // PRODUCT or SERVICE
    private String invoiceType; // dropdown choice, e.g. "Tax Invoice"

    private String status; // DRAFT or FINAL
    private LocalDate invoiceDate;
    private LocalDate dueDate;

    @Column(length = 2000)
    private String notes;

    private LocalDateTime stockCreatedAt;

    @PrePersist
    protected void onCreate() {
        stockCreatedAt = LocalDateTime.now();
        if (status == null)
            status = "DRAFT";
    }

    private String stockCreatedBy;
    private LocalDateTime stockUpdatedAt;

    @PreUpdate
    protected void onUpdate() {
        stockUpdatedAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_billing_id")
    @JsonManagedReference("business-bill")
    private BusinessBillingInfo businessBillingInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_billing_id")
    @JsonManagedReference("recipient-bill")
    private RecipientBillingInfo recipientBillingInfo;

    @OneToMany(mappedBy = "stockBillingInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("bill-items")
    private List<SingleStockBillingInfo> billItems = new ArrayList<>();

}
