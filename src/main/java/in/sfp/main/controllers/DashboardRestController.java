package in.sfp.main.controllers;

import in.sfp.main.dto.DashboardDataDTO;
import in.sfp.main.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billing-app/api/dashboard")
public class DashboardRestController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private in.sfp.main.repository.SystemAnnouncementRepository announcementRepo;

    @GetMapping("/stats")
    public ResponseEntity<DashboardDataDTO> getDashboardStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .findFirst()
                .orElse("");

        if (role.contains("ADMIN")) {
            return ResponseEntity.ok(dashboardService.getAdminDashboardData());
        } else {
            return ResponseEntity.ok(dashboardService.getClientDashboardData(auth.getName()));
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/broadcast")
    public ResponseEntity<?> postBroadcast(
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> payload) {
        String message = payload.get("message");

        // Deactivate previous active announcements
        announcementRepo.findAll().forEach(a -> {
            if (a.isActive()) {
                a.setActive(false);
                announcementRepo.save(a);
            }
        });

        if (message != null && !message.trim().isEmpty()) {
            in.sfp.main.models.SystemAnnouncement announcement = in.sfp.main.models.SystemAnnouncement.builder()
                    .message(message)
                    .active(true)
                    .build();
            announcementRepo.save(announcement);
            return ResponseEntity.ok(java.util.Map.of("message", "Broadcast posted successfully!"));
        }

        return ResponseEntity.ok(java.util.Map.of("message", "Broadcast cleared!"));
    }
}
