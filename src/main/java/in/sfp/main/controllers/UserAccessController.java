package in.sfp.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import in.sfp.main.models.UserAccessInfo;
import in.sfp.main.service.serviceimpl.UserAccessServiceImpl;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/billing-app/api")
public class UserAccessController {

    @Autowired
    private UserAccessServiceImpl usersAccessRepoService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // 1. Client Submits Request
    @PostMapping("/saveAccessRequests")
    public ResponseEntity<?> requestAccess(@RequestBody UserAccessInfo usersAccess) {
        try {
            // Check if email already exists
            if (usersAccessRepoService.findByEmail(usersAccess.getEmail()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(java.util.Map.of("message", "Email is already registered."));
            }

            // Check if mobile number already exists
            if (usersAccessRepoService.findByMobileNumber(usersAccess.getMobileNumber()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(java.util.Map.of("message", "Mobile number is already registered."));
            }

            UserAccessInfo saved = usersAccessRepoService.saveUsersAccessInfo(usersAccess);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // 2. Admin: Get all Pending Requests
    @GetMapping("/admin/pending-requests")
    public ResponseEntity<List<UserAccessInfo>> getPendingRequests() {
        return ResponseEntity.ok(usersAccessRepoService.getPendingRequests());
    }

    // 3. Admin: Accept Request
    @PostMapping("/admin/approve/{id}")
    public ResponseEntity<String> approveRequest(@PathVariable Long id) {
        usersAccessRepoService.approveRequest(id);
        return ResponseEntity.ok("User approved and email sent!");
    }

    // 4. Admin: Reject Request
    @PostMapping("/admin/reject/{id}")
    public ResponseEntity<String> rejectRequest(@PathVariable Long id) {
        usersAccessRepoService.rejectRequest(id);
        return ResponseEntity.ok("User request rejected.");
    }

    // 5. Client: Setup Password via Token
    @PostMapping("/setup-password")
    public ResponseEntity<String> setupPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String password = payload.get("password");

        UserAccessInfo user = usersAccessRepoService.findByToken(token);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(password)); // Encode the password
            user.setSetupToken(null); // Clear token after use
            usersAccessRepoService.updateUsersAccessInfo(user);
            return ResponseEntity.ok("Password set successfully! You can now login.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
    }

    // 6. Forgot Password Reset
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String secretKey = payload.get("secretKey");
        String newPassword = payload.get("newPassword");

        try {
            usersAccessRepoService.resetPassword(email, secretKey, newPassword);
            return ResponseEntity.ok("Password reset successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @org.springframework.beans.factory.annotation.Value("${admin.creation.secret}")
    private String adminCreationSecret;

    // 7. Internal Admin Creation (Secure)
    @PostMapping("/internal/create-admin")
    public ResponseEntity<?> createAdmin(
            @RequestBody UserAccessInfo adminInfo,
            @RequestHeader(value = "X-Admin-Secret", required = false) String secret) {

        if (secret == null || !secret.equals(adminCreationSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("message", "Invalid or missing Admin Secret Key. Access Denied."));
        }

        try {
            UserAccessInfo created = usersAccessRepoService.createAdminAccount(adminInfo);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/getAllClients")
    public ResponseEntity<List<UserAccessInfo>> getAllClients() {
        return ResponseEntity.ok(usersAccessRepoService.getAllClients());
    }

    // 8. Update Profile Details
    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        UserAccessInfo user = usersAccessRepoService.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        if (payload.containsKey("fullName"))
            user.setFullName(payload.get("fullName"));
        if (payload.containsKey("designation"))
            user.setDesignation(payload.get("designation"));
        if (payload.containsKey("companyName"))
            user.setCompanyName(payload.get("companyName"));
        if (payload.containsKey("companyType"))
            user.setCompanyType(payload.get("companyType"));
        if (payload.containsKey("mobileNumber"))
            user.setMobileNumber(payload.get("mobileNumber"));
        if (payload.containsKey("clientImage"))
            user.setClientImage(payload.get("clientImage"));

        UserAccessInfo updated = usersAccessRepoService.updateUsersAccessInfo(user);
        return ResponseEntity.ok(updated);
    }

    // 9. Verify Password to reveal sensitive data
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        UserAccessInfo user = usersAccessRepoService.findByEmail(email);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity
                    .ok(Map.of("secretKey", user.getSecretKey() != null ? user.getSecretKey() : "NOT_SET"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Incorrect password"));
    }
}