package in.sfp.main.controllers;

import in.sfp.main.models.UserAccessInfo;
import in.sfp.main.service.serviceimpl.UserAccessServiceImpl;
import in.sfp.main.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/billing-app/api")
public class LoginController {

    @Autowired
    private UserAccessServiceImpl userAccessService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload, HttpServletResponse response) {
        String email = payload.get("email");
        String password = payload.get("password");

        UserAccessInfo user = userAccessService.findByEmail(email);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // Check status
            if (!"APPROVED".equalsIgnoreCase(user.getStatus())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Your account is " + user.getStatus() + ". Please contact admin.");
                return ResponseEntity.status(403).body(errorResponse);
            }

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getFullName());

            // Set JWT in HttpOnly Cookie for Page Navigations
            Cookie jwtCookie = new Cookie("jwtToken", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // Set to true in production with HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(10 * 60 * 60); // 10 hours
            response.addCookie(jwtCookie);

            Map<String, String> body = new HashMap<>();
            body.put("token", token);
            body.put("role", user.getRole());
            body.put("name", user.getFullName());
            body.put("message", "Login successful!");
            return ResponseEntity.ok(body);
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid email or password");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
}
