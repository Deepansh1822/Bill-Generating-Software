package in.sfp.main.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/billing-app/api")
public class PagesController {

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

    @GetMapping("/ManageCategories")
    public String getManageCategories() {
        return "ManageCategories";
    }

    @GetMapping("/ManageStocks")
    public String getManageStocks() {
        return "ManageStocks";
    }

    @GetMapping("/StockDetails")
    public String getStockDetails() {
        return "StockDetails";
    }

    @GetMapping("/CategoryDetails")
    public String getCategoryDetails() {
        return "CategoryDetails";
    }
}
