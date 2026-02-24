package in.sfp.main.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_billing_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "billItems", "businessBillingInfo", "recipientBillingInfo" })
@ToString(exclude = { "billItems", "businessBillingInfo", "recipientBillingInfo" })
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

    private String advancedPayment;
    private String balancePayment;

    private LocalTime stockCreatedAt;

    @PrePersist
    protected void onCreate() {
        stockCreatedAt = LocalTime.now();
    }

    private String stockCreatedBy;
    private LocalTime stockUpdatedAt;

    @PreUpdate
    protected void onUpdate() {
        stockUpdatedAt = LocalTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_billing_id")
    private BusinessBillingInfo businessBillingInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_billing_id")
    private RecipientBillingInfo recipientBillingInfo;

    @OneToMany(mappedBy = "stockBillingInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SingleStockBillingInfo> billItems = new ArrayList<>();

}
