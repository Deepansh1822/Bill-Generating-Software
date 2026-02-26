package in.sfp.main.service.serviceimpl;

import in.sfp.main.dto.DashboardDataDTO;
import in.sfp.main.models.TotalStockBillingInfo;
import in.sfp.main.models.UserAccessInfo;
import in.sfp.main.repository.StockBillingInfoRepository;
import in.sfp.main.repository.StockInfoRepository;
import in.sfp.main.repository.UserAccessRepo;
import in.sfp.main.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

        @Autowired
        private StockBillingInfoRepository billingRepo;

        @Autowired
        private UserAccessRepo userRepo;

        @Autowired
        private StockInfoRepository stockRepo;

        @Autowired
        private in.sfp.main.repository.SystemAnnouncementRepository announcementRepo;

        @Override
        public DashboardDataDTO getClientDashboardData(String username) {
                UserAccessInfo clientUser = userRepo.findByUsername(username);
                if (clientUser == null) {
                        clientUser = userRepo.findByEmail(username);
                }

                List<TotalStockBillingInfo> userBills = billingRepo.findByStockCreatedBy(username);

                double totalRevenue = userBills.stream()
                                .filter(b -> "FINAL".equalsIgnoreCase(b.getStatus()))
                                .mapToDouble(b -> parseDouble(b.getStockTotalAmount()))
                                .sum();

                long pendingQuotations = userBills.stream()
                                .filter(b -> "DRAFT".equalsIgnoreCase(b.getStatus()))
                                .count();

                double balanceDue = userBills.stream()
                                .mapToDouble(b -> parseDouble(b.getBalancePayment()))
                                .sum();

                long totalInvoices = userBills.size();

                var clientStocks = stockRepo.findByCreatedBy(username);
                long totalProducts = clientStocks.stream()
                                .filter(s -> s.getStockType() != null
                                                && s.getStockType().toLowerCase().contains("product"))
                                .count();

                long totalServices = clientStocks.stream()
                                .filter(s -> s.getStockType() != null
                                                && s.getStockType().toLowerCase().contains("service"))
                                .count();

                List<DashboardDataDTO.RecentBillDTO> recent = userBills.stream()
                                .sorted((b1, b2) -> b2.getStockCreatedAt().compareTo(b1.getStockCreatedAt()))
                                .limit(5)
                                .map(b -> DashboardDataDTO.RecentBillDTO.builder()
                                                .id(b.getId())
                                                .invoiceNumber(b.getInvoiceNumber())
                                                .recipientName(
                                                                b.getRecipientBillingInfo() != null
                                                                                ? b.getRecipientBillingInfo()
                                                                                                .getRecipientName()
                                                                                : "N/A")
                                                .date(b.getInvoiceDate() != null ? b.getInvoiceDate().toString()
                                                                : b.getStockCreatedAt().toLocalDate().toString())
                                                .amount(parseDouble(b.getStockTotalAmount()))
                                                .status(b.getStatus())
                                                .build())
                                .collect(Collectors.toList());

                List<DashboardDataDTO.MonthlyRevenueDTO> monthlyTrend = userBills.stream()
                                .filter(b -> "FINAL".equalsIgnoreCase(b.getStatus()))
                                .collect(Collectors.groupingBy(
                                                b -> (b.getInvoiceDate() != null ? b.getInvoiceDate()
                                                                : b.getStockCreatedAt().toLocalDate())
                                                                .withDayOfMonth(1),
                                                Collectors.summingDouble(b -> parseDouble(b.getStockTotalAmount()))))
                                .entrySet().stream()
                                .sorted(java.util.Map.Entry.comparingByKey())
                                .limit(6)
                                .map(entry -> DashboardDataDTO.MonthlyRevenueDTO.builder()
                                                .month(entry.getKey().getMonth().getDisplayName(
                                                                java.time.format.TextStyle.SHORT,
                                                                java.util.Locale.ENGLISH))
                                                .revenue(entry.getValue())
                                                .build())
                                .collect(Collectors.toList());

                // Point 1: Low Stock Alerts (Stock < 10)
                List<DashboardDataDTO.LowStockDTO> lowStock = clientStocks.stream()
                                .filter(s -> s.getAvailableQuantity() < 10)
                                .map(s -> DashboardDataDTO.LowStockDTO.builder()
                                                .itemName(s.getItemName())
                                                .currentStock(s.getAvailableQuantity())
                                                .build())
                                .limit(5)
                                .collect(Collectors.toList());

                // Point 2: Top Performing Products (by Revenue)
                java.util.Map<String, Double> productRevenue = new java.util.HashMap<>();
                userBills.stream()
                                .filter(b -> "FINAL".equalsIgnoreCase(b.getStatus()))
                                .flatMap(b -> b.getBillItems().stream())
                                .forEach(item -> {
                                        productRevenue.put(item.getItemName(),
                                                        productRevenue.getOrDefault(item.getItemName(), 0.0)
                                                                        + item.getTotalItemAmount());
                                });

                List<DashboardDataDTO.TopProductDTO> topProducts = productRevenue.entrySet().stream()
                                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                                .limit(3)
                                .map(e -> DashboardDataDTO.TopProductDTO.builder()
                                                .itemName(e.getKey())
                                                .revenue(e.getValue())
                                                .build())
                                .collect(Collectors.toList());

                // Point 3: Monthly Sales Goal
                java.time.LocalDate now = java.time.LocalDate.now();
                double currentMonthRevenue = userBills.stream()
                                .filter(b -> "FINAL".equalsIgnoreCase(b.getStatus()))
                                .filter(b -> {
                                        java.time.LocalDate d = b.getInvoiceDate() != null ? b.getInvoiceDate()
                                                        : b.getStockCreatedAt().toLocalDate();
                                        return d.getMonth() == now.getMonth() && d.getYear() == now.getYear();
                                })
                                .mapToDouble(b -> parseDouble(b.getStockTotalAmount()))
                                .sum();

                double monthlyTarget = (clientUser != null && clientUser.getMonthlyTarget() != null)
                                ? clientUser.getMonthlyTarget()
                                : 100000.0;

                // Point 4: Overdue/Unpaid Tracker (Balance > 0)
                List<DashboardDataDTO.RecentBillDTO> overdue = userBills.stream()
                                .filter(b -> "FINAL".equalsIgnoreCase(b.getStatus())
                                                && parseDouble(b.getBalancePayment()) > 0)
                                .sorted((b1, b2) -> b2.getStockCreatedAt().compareTo(b1.getStockCreatedAt()))
                                .limit(5)
                                .map(b -> DashboardDataDTO.RecentBillDTO.builder()
                                                .id(b.getId())
                                                .invoiceNumber(b.getInvoiceNumber())
                                                .recipientName(b.getRecipientBillingInfo() != null
                                                                ? b.getRecipientBillingInfo().getRecipientName()
                                                                : "N/A")
                                                .amount(parseDouble(b.getBalancePayment()))
                                                .status("UNPAID")
                                                .build())
                                .collect(Collectors.toList());

                // Point 5: Draft Shortcuts
                List<DashboardDataDTO.RecentBillDTO> drafts = recent.stream()
                                .filter(b -> "DRAFT".equalsIgnoreCase(b.getStatus()))
                                .limit(3)
                                .collect(Collectors.toList());

                // Broadcast Message
                String broadcast = announcementRepo.findFirstByActiveTrueOrderByCreatedAtDesc()
                                .map(a -> a.getMessage()).orElse(null);

                return DashboardDataDTO.builder()
                                .totalRevenue(totalRevenue)
                                .pendingQuotations(pendingQuotations)
                                .balanceDue(balanceDue)
                                .totalInvoices(totalInvoices)
                                .totalProducts(totalProducts)
                                .totalServices(totalServices)
                                .recentInvoices(recent)
                                .monthlyRevenue(monthlyTrend)
                                .broadcastMessage(broadcast)
                                // New Insight Fields
                                .lowStockAlerts(lowStock)
                                .topProducts(topProducts)
                                .currentMonthRevenue(currentMonthRevenue)
                                .monthlyTarget(monthlyTarget)
                                .overdueBills(overdue)
                                .draftShortcuts(drafts)
                                .build();
        }

        @Override
        public DashboardDataDTO getAdminDashboardData() {
                List<TotalStockBillingInfo> allBills = billingRepo.findAll();

                double globalRevenue = allBills.stream()
                                .filter(b -> "FINAL".equalsIgnoreCase(b.getStatus()))
                                .mapToDouble(b -> parseDouble(b.getStockTotalAmount()))
                                .sum();

                long totalClients = userRepo.findByRole("CLIENT").size();
                long pendingRequests = userRepo.findByStatus("PENDING").size();

                // Fast-Track Approvals (Point 1)
                List<DashboardDataDTO.PendingRequestDTO> latestPending = userRepo.findByStatus("PENDING").stream()
                                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                                .limit(5)
                                .map(u -> DashboardDataDTO.PendingRequestDTO.builder()
                                                .id(u.getId())
                                                .fullName(u.getFullName())
                                                .companyName(u.getCompanyName())
                                                .requestDate(u.getCreatedAt().toLocalDate().toString())
                                                .build())
                                .collect(Collectors.toList());

                // Revenue Distribution (Point 2 - Top 10 + Others)
                java.util.Map<String, Double> categoryRevenue = new java.util.HashMap<>();
                allBills.stream()
                                .filter(b -> "FINAL".equalsIgnoreCase(b.getStatus()))
                                .forEach(b -> {
                                        b.getBillItems().forEach(item -> {
                                                String category = "Unknown";
                                                // Simplified lookup: find stock by name to get category
                                                var stock = stockRepo.findByItemName(item.getItemName());
                                                if (stock != null && stock.getStockCategories() != null) {
                                                        category = stock.getStockCategories().getCategoryName();
                                                }
                                                categoryRevenue.put(category,
                                                                categoryRevenue.getOrDefault(category, 0.0)
                                                                                + item.getTotalItemAmount());
                                        });
                                });

                List<DashboardDataDTO.TopCategoryDTO> topCategories = categoryRevenue.entrySet().stream()
                                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                                .collect(Collectors.toList()).stream()
                                .limit(10)
                                .map(e -> DashboardDataDTO.TopCategoryDTO.builder()
                                                .categoryName(e.getKey())
                                                .value(e.getValue())
                                                .build())
                                .collect(Collectors.toList());

                if (categoryRevenue.size() > 10) {
                        double othersSum = categoryRevenue.entrySet().stream()
                                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                                        .skip(10)
                                        .mapToDouble(e -> e.getValue())
                                        .sum();
                        topCategories.add(DashboardDataDTO.TopCategoryDTO.builder()
                                        .categoryName("Others")
                                        .value(othersSum)
                                        .build());
                }

                List<DashboardDataDTO.RecentBillDTO> recent = allBills.stream()
                                .sorted((b1, b2) -> b2.getStockCreatedAt().compareTo(b1.getStockCreatedAt()))
                                .limit(5)
                                .map(b -> {
                                        in.sfp.main.models.UserAccessInfo client = userRepo
                                                        .findByEmail(b.getStockCreatedBy());
                                        if (client == null)
                                                client = userRepo.findByUsername(b.getStockCreatedBy());
                                        return DashboardDataDTO.RecentBillDTO.builder()
                                                        .id(b.getId())
                                                        .invoiceNumber(b.getInvoiceNumber())
                                                        .recipientName(
                                                                        b.getRecipientBillingInfo() != null
                                                                                        ? b.getRecipientBillingInfo()
                                                                                                        .getRecipientName()
                                                                                        : "N/A")
                                                        .businessName(client != null ? client.getCompanyName()
                                                                        : "System")
                                                        .date(b.getInvoiceDate() != null ? b.getInvoiceDate().toString()
                                                                        : b.getStockCreatedAt().toLocalDate()
                                                                                        .toString())
                                                        .amount(parseDouble(b.getStockTotalAmount()))
                                                        .status(b.getStatus())
                                                        .build();
                                })
                                .collect(Collectors.toList());

                List<DashboardDataDTO.EliteClientDTO> elite = allBills.stream()
                                .filter(b -> "FINAL".equalsIgnoreCase(b.getStatus()) && b.getStockCreatedBy() != null)
                                .collect(Collectors.groupingBy(TotalStockBillingInfo::getStockCreatedBy,
                                                Collectors.summingDouble(b -> parseDouble(b.getStockTotalAmount()))))
                                .entrySet().stream()
                                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                                .limit(3)
                                .map(e -> {
                                        in.sfp.main.models.UserAccessInfo client = userRepo.findByEmail(e.getKey());
                                        if (client == null)
                                                client = userRepo.findByUsername(e.getKey());
                                        return DashboardDataDTO.EliteClientDTO.builder()
                                                        .clientName(client != null ? client.getFullName() : "Unknown")
                                                        .companyName(client != null ? client.getCompanyName()
                                                                        : "Company")
                                                        .totalRevenue(e.getValue())
                                                        .image(client != null ? client.getClientImage() : null)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                long finalizedCount = allBills.stream().filter(b -> "FINAL".equalsIgnoreCase(b.getStatus())).count();
                long draftCount = allBills.stream().filter(b -> "DRAFT".equalsIgnoreCase(b.getStatus())).count();

                List<DashboardDataDTO.MonthlyRevenueDTO> monthlyTrend = allBills.stream()
                                .filter(b -> "FINAL".equalsIgnoreCase(b.getStatus()))
                                .collect(Collectors.groupingBy(
                                                b -> (b.getInvoiceDate() != null ? b.getInvoiceDate()
                                                                : b.getStockCreatedAt().toLocalDate())
                                                                .withDayOfMonth(1),
                                                Collectors.summingDouble(b -> parseDouble(b.getStockTotalAmount()))))
                                .entrySet().stream()
                                .sorted(java.util.Map.Entry.comparingByKey())
                                .limit(6)
                                .map(entry -> DashboardDataDTO.MonthlyRevenueDTO.builder()
                                                .month(entry.getKey().getMonth().getDisplayName(
                                                                java.time.format.TextStyle.SHORT,
                                                                java.util.Locale.ENGLISH))
                                                .revenue(entry.getValue())
                                                .build())
                                .collect(Collectors.toList());

                // Broadcast Message
                String broadcast = announcementRepo.findFirstByActiveTrueOrderByCreatedAtDesc()
                                .map(a -> a.getMessage()).orElse(null);

                return DashboardDataDTO.builder()
                                .globalRevenue(globalRevenue)
                                .totalClientsCount(totalClients)
                                .pendingRequestsCount(pendingRequests)
                                .globalFinalizedInvoices(finalizedCount)
                                .globalDraftInvoices(draftCount)
                                .recentInvoices(recent)
                                .eliteClients(elite)
                                .monthlyRevenue(monthlyTrend)
                                .latestPendingRequests(latestPending)
                                .topCategories(topCategories)
                                .broadcastMessage(broadcast)
                                .build();
        }

        private double parseDouble(String value) {
                if (value == null || value.trim().isEmpty())
                        return 0.0;
                try {
                        return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                        return 0.0;
                }
        }
}
