package in.sfp.main.models;

import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stock_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "stockCategories")
@ToString(exclude = "stockCategories")
public class StockInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private byte[] stockImage; // for both product and service

    private String itemName; // for both product and service
    private String itemDescription; // for both product and service
    private String hsnCode; // for product only
    private String sacCode; // for service only
    private double unitPrice; // for both product and service
    private int availableQuantity; // for product only

    private String stockType; // service based or product based

    private double cgstRate; // for both product and service
    private double sgstRate; // for both product and service
    private double igstRate; // for both product and service

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategInfo stockCategories;

    private LocalTime createdAt; // for both product and service
    private LocalTime updatedAt; // for both product and service

    @PrePersist
    protected void onCreate() {
        createdAt = LocalTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalTime.now();
    }

}
