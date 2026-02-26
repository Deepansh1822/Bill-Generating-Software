package in.sfp.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDataDTO {
    // Basic Stats for Client
    private double totalRevenue;
    private long pendingQuotations;
    private double balanceDue;
    private long totalInvoices;

    // Inventory stats
    private long totalProducts;
    private long totalServices;

    // Admin specific
    private long totalClientsCount;
    private long pendingRequestsCount;
    private double globalRevenue;
    private long globalFinalizedInvoices;
    private long globalDraftInvoices;
    private List<PendingRequestDTO> latestPendingRequests;
    private List<TopCategoryDTO> topCategories;

    // Client Insights (Point 1-5)
    private List<LowStockDTO> lowStockAlerts;
    private List<TopProductDTO> topProducts;
    private double currentMonthRevenue;
    private double monthlyTarget;
    private List<RecentBillDTO> overdueBills;
    private List<RecentBillDTO> draftShortcuts;

    // System Announcement
    private String broadcastMessage;

    // Charts Data
    private List<MonthlyRevenueDTO> monthlyRevenue;
    private List<RecentBillDTO> recentInvoices;
    private List<EliteClientDTO> eliteClients;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenueDTO {
        private String month;
        private double revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentBillDTO {
        private Long id;
        private String invoiceNumber;
        private String recipientName;
        private String businessName;
        private String date;
        private double amount;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EliteClientDTO {
        private String companyName;
        private String clientName;
        private double totalRevenue;
        private String image;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingRequestDTO {
        private Long id;
        private String fullName;
        private String companyName;
        private String requestDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCategoryDTO {
        private String categoryName;
        private double value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockDTO {
        private String itemName;
        private int currentStock;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductDTO {
        private String itemName;
        private double revenue;
        private String category;
    }
}
