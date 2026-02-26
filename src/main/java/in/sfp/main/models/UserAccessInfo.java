package in.sfp.main.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users_access")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String designation;

    @Column(unique = true)
    private String username; // Generated after approval

    private String password; // Set by user via link

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String mobileNumber;

    private String companyName;

    private String companyType;

    private String role; // From RequestAccess form

    // Default status for new requests
    private String status = "PENDING";

    private String setupToken; // UUID for password setup link

    private String secretKey; // For password recovery

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String clientImage;

    private Double monthlyTarget = 100000.0;

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

}
