package in.sfp.main.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "single_stock_billing_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "stockBillingInfo")
@ToString(exclude = "stockBillingInfo")
public class SingleStockBillingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_billing_info_id")
    private TotalStockBillingInfo stockBillingInfo;
}
