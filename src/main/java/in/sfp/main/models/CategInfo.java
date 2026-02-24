package in.sfp.main.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Lob;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.util.List;

import java.time.LocalTime;

@Entity
@Table(name = "stock_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private byte[] categoryImage;
    private String categoryName;
    private String categoryDescription;
    private LocalTime categoryCreatedAt;
    private String categoryCreatedBy;
    private LocalTime categoryUpdatedAt;
    private String categoryUpdatedBy;

    @PrePersist
    protected void onCreate() {
        categoryCreatedAt = LocalTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        categoryUpdatedAt = LocalTime.now();
    }

    @JsonIgnore
    @OneToMany(mappedBy = "stockCategories", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockInfo> stockInfo;
}
