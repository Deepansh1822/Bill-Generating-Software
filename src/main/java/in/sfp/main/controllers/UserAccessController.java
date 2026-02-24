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
    public ResponseEntity<UserAccessInfo> requestAccess(@RequestBody UserAccessInfo usersAccess) {
        UserAccessInfo saved = usersAccessRepoService.saveUsersAccessInfo(usersAccess);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
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

    // 7. Internal Admin Creation (Postman only)
    @PostMapping("/internal/create-admin")
    public ResponseEntity<UserAccessInfo> createAdmin(@RequestBody UserAccessInfo adminInfo) {
        UserAccessInfo created = usersAccessRepoService.createAdminAccount(adminInfo);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

}