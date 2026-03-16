package in.sfp.main.controllers;

import in.sfp.main.models.UserAccessInfo;
import in.sfp.main.service.UserAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserDetailsRestController {

    @Autowired
    private UserAccessService userAccessService;

    @GetMapping("/context")
    public Map<String, Object> getUserContext() {
        Map<String, Object> context = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        context.put("userRole", "CLIENT");
        context.put("displayUsername", "User");
        context.put("userFullName", "");
        context.put("userCompanyName", "");
        context.put("userEmail", "");
        context.put("authStatus", "unauthenticated");

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            UserAccessInfo user = userAccessService.findByUsername(auth.getName());
            if (user == null) {
                user = userAccessService.findByEmail(auth.getName());
            }

            if (user != null) {
                context.put("userRole", user.getRole() != null ? user.getRole().toUpperCase() : "CLIENT");
                context.put("displayUsername", user.getUsername() != null ? user.getUsername() : user.getEmail());
                context.put("userFullName", user.getFullName() != null ? user.getFullName() : "");
                context.put("userCompanyName", user.getCompanyName() != null ? user.getCompanyName() : "");
                context.put("userEmail", user.getEmail() != null ? user.getEmail() : auth.getName());
                context.put("authStatus", "authenticated");
            } else {
                context.put("displayUsername", auth.getName());
                context.put("userEmail", auth.getName());
                context.put("authStatus", "authenticated");
            }
        }

        return context;
    }

    @GetMapping("/profile-details")
    public Map<String, Object> getProfileDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            UserAccessInfo user = userAccessService.findByUsername(auth.getName());
            if (user == null) {
                user = userAccessService.findByEmail(auth.getName());
            }
            if (user != null) {
                // Remove potential large blobs if necessary
                user.setClientImage(null);
                user.setProfileImage(null);
                Map<String, Object> response = new HashMap<>();
                response.put("user", user);
                return response;
            }
        }
        return null;
    }
}
