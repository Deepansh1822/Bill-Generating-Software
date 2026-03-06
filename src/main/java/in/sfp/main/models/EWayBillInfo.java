package in.sfp.main.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "eway_bill_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "stockBillingInfo")
public class EWayBillInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String ewayBillNumber; // Number allocated after generation

    private String supplyType; // OUTWARD, INWARD
    private String subType; // SUPPLY, IMPORT, EXPORT, etc.
    private String docType; // INVOICE
    private String documentNumber; // The Tax Invoice Number being linked
    private LocalDate docDate;

    private String fromName;
    private String fromGSTIN;
    private String fromState;
    private String fromAddress;
    private String fromPincode;

    private String toName;
    private String toGSTIN;
    private String toState;
    private String toAddress;
    private String toPincode;

    // Transport Details
    private String transporterName;
    private String transporterId;
    private String vehicleNumber;
    private String distance;
    private String transportMode; // ROAD, RAIL, AIR, SHIP
    private String transactionType; // REGULAR, BILL_TO_SHIP_TO, etc.
    private String validity; // e.g., 24 hours
    private String transDocNo;
    private LocalDate transDocDate;
    private String cewbNo; // Consolidate E-Way Bill Number
    private String multiVehicleInfo; // Details if multiple vehicles used

    // Need to add these details Also
    // mode - road
    // approx. distance - 4km
    // transactionType - Regular
    // validity - 24 hours

    // From and to Addresses
    // -- From
    // GSTIN, CompanyName, CompanyAddress, Dispatch From (Pin code)
    // -- To
    // GSTIN, CompanyName, CompanyAddress, Ship To (Pin code)

    // Transport Doc. No & docDate
    // CEWB no.
    // Multi-Vehicle info

    private String stockCreatedBy; // Username of the person who created this
    private String consignmentValue;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Link to the original Tax Invoice
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_billing_info_id")
    private TotalStockBillingInfo stockBillingInfo;
}
