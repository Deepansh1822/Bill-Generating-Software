package in.sfp.main.config;

import in.sfp.main.models.UserAccessInfo;
import in.sfp.main.repository.UserAccessRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the admin account into the database on every application startup.
 * If an account with the admin email already exists, nothing happens.
 */
@Component
public class AdminDataSeeder implements ApplicationRunner {

    @Autowired
    private UserAccessRepo userAccessRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@sfp.com}")
    private String adminEmail;

    @Value("${admin.password:Admin@SFP2026}")
    private String adminPassword;

    @Value("${admin.username:SFP_ADMIN}")
    private String adminUsername;

    @Value("${admin.fullName:SFP Administrator}")
    private String adminFullName;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Check if admin already exists
        UserAccessInfo existingAdmin = userAccessRepo.findByEmail(adminEmail);
        if (existingAdmin != null) {
            // Admin exists — ensure role is uppercase ADMIN and status is APPROVED
            boolean needsUpdate = false;
            if (!"ADMIN".equals(existingAdmin.getRole())) {
                existingAdmin.setRole("ADMIN");
                needsUpdate = true;
            }
            if (!"APPROVED".equals(existingAdmin.getStatus())) {
                existingAdmin.setStatus("APPROVED");
                needsUpdate = true;
            }
            if (needsUpdate) {
                userAccessRepo.save(existingAdmin);
                System.out.println("✅ Admin account updated: " + adminEmail);
            } else {
                System.out.println("✅ Admin account already present: " + adminEmail);
            }
            return;
        }

        // Admin doesn't exist — create it
        UserAccessInfo admin = new UserAccessInfo();
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFullName(adminFullName);
        admin.setUsername(adminUsername);
        admin.setRole("ADMIN");
        admin.setStatus("APPROVED");
        admin.setDesignation("System Administrator");
        admin.setCompanyName("SFP Billing");

        userAccessRepo.save(admin);
        System.out.println("✅ Admin account created: " + adminEmail + " / password: " + adminPassword);
    }
}
