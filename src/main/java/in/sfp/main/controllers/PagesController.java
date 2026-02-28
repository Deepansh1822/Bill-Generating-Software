package in.sfp.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/billing-app/api")
public class PagesController {

    @Autowired
    private in.sfp.main.service.UserAccessService userAccessService;

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Safe Defaults to prevent Thymeleaf rendering errors
        model.addAttribute("userRole", "CLIENT");
        model.addAttribute("displayUsername", "User");
        model.addAttribute("userFullName", "");
        model.addAttribute("userProfileImage", "");
        model.addAttribute("userCompanyName", "");
        model.addAttribute("userCompanyType", "");
        model.addAttribute("userEmail", "");

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            in.sfp.main.models.UserAccessInfo user = userAccessService.findByUsername(auth.getName());
            if (user == null) {
                user = userAccessService.findByEmail(auth.getName());
            }

            if (user != null) {
                String role = user.getRole() != null ? user.getRole().toUpperCase() : "CLIENT";
                model.addAttribute("userRole", role);
                model.addAttribute("displayUsername", user.getUsername() != null ? user.getUsername()
                        : (user.getEmail() != null ? user.getEmail() : auth.getName()));
                model.addAttribute("userFullName", user.getFullName() != null ? user.getFullName() : "");
                model.addAttribute("userProfileImage", ""); // Loaded async via /api/profile-image to avoid large Base64
                                                            // in HTML
                model.addAttribute("userCompanyName", user.getCompanyName() != null ? user.getCompanyName() : "");
                model.addAttribute("userCompanyType", user.getCompanyType() != null ? user.getCompanyType() : "");
                model.addAttribute("userEmail", user.getEmail() != null ? user.getEmail() : auth.getName());
                model.addAttribute("authStatus", "authenticated");
            } else {
                model.addAttribute("displayUsername", auth.getName());
                model.addAttribute("userEmail", auth.getName());
                // Fallback to role from Authorities in Token
                String role = auth.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .filter(a -> a.startsWith("ROLE_"))
                        .map(a -> a.substring(5))
                        .findFirst()
                        .orElse("CLIENT");
                model.addAttribute("userRole", role);
                model.addAttribute("authStatus", "authenticated");
            }
        } else {
            model.addAttribute("authStatus", "unauthenticated");
        }
    }

    @GetMapping("/Profile")
    public String getProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            in.sfp.main.models.UserAccessInfo user = userAccessService.findByUsername(auth.getName());
            if (user == null) {
                user = userAccessService.findByEmail(auth.getName());
            }
            if (user != null) {
                // Strip the clientImage to prevent a massive Base64 blob being embedded in
                // HTML.
                // The profile avatar image is loaded asynchronously via /api/profile-image
                // instead.
                user.setClientImage(null);
            }
            model.addAttribute("user", user);
        }
        return "Profile";
    }

    @GetMapping("/ManageClients")
    public String getManageClients() {
        return "ManageClients";
    }

    @GetMapping("/MainDashboard")
    public String getMainDashboard() {
        return "MainDashboard";
    }

    @GetMapping("/Login")
    public String getLogin() {
        return "Login";
    }

    @GetMapping("/RequestAccess")
    public String getRequestAccess() {
        return "RequestAccess";
    }

    @GetMapping("/Dashboard")
    public String getDashboard() {
        return "Dashboard";
    }

    @GetMapping("/ManageAccessRequests")
    public String getManageAccessRequests() {
        return "PendingAccessRequests";
    }

    @GetMapping("/setup-password")
    public String getSetupPassword() {
        return "SetupPassword";
    }

    @GetMapping("/Reports")
    public String getReports() {
        return "Reports";
    }

    @GetMapping("/Reports/BillDetail/{id}")
    public String getBillDetail(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        model.addAttribute("billId", id);
        return "BillDetail";
    }

    @GetMapping("/ManageCategories")
    public String getManageCategories() {
        return "ManageCategories";
    }

    @GetMapping("/ManageStocks")
    public String getManageStocks() {
        return "ManageStocks";
    }

    @GetMapping("/AddCategory")
    public String getAddCategory() {
        return "CategoryForm";
    }

    @GetMapping("/EditCategory/{id}")
    public String getEditCategory(@PathVariable Long id, Model model) {
        model.addAttribute("categoryId", id);
        return "CategoryForm";
    }

    @GetMapping("/AddStock")
    public String getAddStock() {
        return "StockForm";
    }

    @GetMapping("/EditStock/{id}")
    public String getEditStock(@PathVariable Long id, Model model) {
        model.addAttribute("stockId", id);
        return "StockForm";
    }

    @GetMapping("/StockDetails")
    public String getStockDetails() {
        return "StockDetails";
    }

    @GetMapping("/CategoryDetails")
    public String getCategoryDetails() {
        return "CategoryDetails";
    }

    @Autowired
    private in.sfp.main.service.UserBillingService userBillingService;

    @GetMapping("/GenerateProductBill")
    public String getGenerateProductBill(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check role and redirect if Admin
        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .findFirst()
                .orElse("");

        if (role.contains("ADMIN")) {
            return "redirect:/billing-app/api/Reports";
        }

        if (auth != null && auth.isAuthenticated()) {
            in.sfp.main.models.BusinessBillingInfo business = userBillingService.findByCreatedBy(auth.getName());
            model.addAttribute("businessInfo", business);
        }
        return "GenerateProductBill";
    }

    @GetMapping("/GenerateServiceBill")
    public String getGenerateServiceBill(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check role and redirect if Admin
        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .findFirst()
                .orElse("");

        if (role.contains("ADMIN")) {
            return "redirect:/billing-app/api/Reports";
        }

        if (auth != null && auth.isAuthenticated()) {
            in.sfp.main.models.BusinessBillingInfo business = userBillingService.findByCreatedBy(auth.getName());
            model.addAttribute("businessInfo", business);
        }
        return "GenerateServiceBill";
    }

    @GetMapping("/GenerateEWayBill")
    public String getGenerateEWayBill(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check role and redirect if Admin
        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .findFirst()
                .orElse("");

        if (role.contains("ADMIN")) {
            return "redirect:/billing-app/api/Reports";
        }

        if (auth != null && auth.isAuthenticated()) {
            in.sfp.main.models.BusinessBillingInfo business = userBillingService.findByCreatedBy(auth.getName());
            model.addAttribute("businessInfo", business);
        }
        return "EWayBill";
    }

    @GetMapping("/HelpSupport")
    public String getHelpSupport() {
        return "HelpSupport";
    }
}
