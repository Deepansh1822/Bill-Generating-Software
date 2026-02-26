package in.sfp.main.models;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    private String recipientBusinessName;
    private String recipientBusinessStreetAddress;
    private String recipientBusinessCity;
    private String recipientBusinessState;
    private String recipientBusinessCountry;
    private String recipientBusinessPinCode;
    private String recipientGstNumber;
    private String recipientPanNumber;
    private String recipientAdCode;
    private String recipientIecCode;
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
