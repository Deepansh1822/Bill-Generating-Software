package in.sfp.main.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipient_billing_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "stockBillingInfos", "businessBillingInfo" })
@ToString(exclude = { "stockBillingInfos", "businessBillingInfo" })
public class RecipientBillingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipientName;
    private String recipientBusinessAddress;
    private String recipientGstNumber;
    private String recipientPanNumber;
    private String recipientMobileNumber;
    private String recipientEmail;

    private LocalTime recipientCreatedAt;
    private String recipientCreatedBy;
    private LocalTime recipientUpdatedAt;
    private String recipientUpdatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_billing_id")
    private BusinessBillingInfo businessBillingInfo;

    @OneToMany(mappedBy = "recipientBillingInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TotalStockBillingInfo> stockBillingInfos = new ArrayList<>();

}
