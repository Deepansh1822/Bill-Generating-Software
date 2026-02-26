package in.sfp.main.models;

import java.time.LocalDateTime;

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

    private String unit; // e.g. Pcs, Box, Kg, Hr
    private boolean taxable; // true/false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategInfo stockCategories;

    private LocalDateTime createdAt; // for both product and service
    private String createdBy;
    private LocalDateTime updatedAt; // for both product and service
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
