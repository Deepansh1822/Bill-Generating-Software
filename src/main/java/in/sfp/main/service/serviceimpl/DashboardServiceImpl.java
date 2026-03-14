package in.sfp.main.service.serviceimpl;

import in.sfp.main.dto.DashboardDataDTO;
import in.sfp.main.models.StockInfo;
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
                // Optimized Stats fetching via JPQL
                double totalRevenue = normalize(billingRepo.sumTotalAmountForFinalizedByUser(username));
                long pendingQuotations = billingRepo.countByStatusAndStockCreatedBy("DRAFT", username);
                double balanceDue = normalize(billingRepo.sumBalanceDueByUser(username));
                long totalInvoices = billingRepo.countByStatusAndStockCreatedBy("FINAL", username);

                // Optimized Count fetching
                long totalProducts = stockRepo.countByCreatedByAndStockTypeContainingIgnoreCase(username, "PRODUCT");
                long totalServices = stockRepo.countByCreatedByAndStockTypeContainingIgnoreCase(username, "SERVICE");

                // Optimized sorting and limiting at DB level
                List<TotalStockBillingInfo> recentRaw = billingRepo.findRecentByCreatedBy(username,
                                org.springframework.data.domain.PageRequest.of(0, 5));

                List<DashboardDataDTO.RecentBillDTO> recent = recentRaw.stream()
                                .map(b -> DashboardDataDTO.RecentBillDTO.builder()
                                                .id(b.getId())
                                                .invoiceNumber(b.getInvoiceNumber())
                                                .recipientName(b.getRecipientBillingInfo() != null
                                                                ? b.getRecipientBillingInfo().getRecipientName()
                                                                : "N/A")
                                                .date(b.getInvoiceDate() != null ? b.getInvoiceDate().toString()
                                                                : b.getStockCreatedAt().toLocalDate().toString())
                                                .amount(parseDouble(b.getStockTotalAmount()))
                                                .status(b.getStatus())
                                                .balancePayment(parseDouble(b.getBalancePayment()))
                                                .build())
                                .collect(Collectors.toList());

                // For complex trends, we load a small subset. Since we want 6 months, we load
                // only those bills
                // But for 100% safety with massive data, we'll still use the loaded recent
                // bills or a specific range
                // For now, loading user bills for trend is safer than global, but lets limit it
                // to last 6 months in query if possible.
                // We'll stick to a slightly more efficient stream but still on userBills for
                // now to avoid breaking complex logic.
                List<TotalStockBillingInfo> userBills = billingRepo.findByStockCreatedBy(username);

                List<DashboardDataDTO.MonthlyRevenueDTO> monthlyTrend = userBills.stream()
                                .filter(b -> "FINAL".equalsIgnoreCase(b.getStatus()))
                                .collect(Collectors.groupingBy(
                                                b -> (b.getInvoiceDate() != null ? b.getInvoiceDate()
                                                                : b.getStockCreatedAt().toLocalDate())
                                                                .withDayOfMonth(1),
                                                Collectors.summingDouble(b -> parseDouble(b.getStockTotalAmount()))))
                                .entrySet().stream()
                                .sorted(Collections.reverseOrder(java.util.Map.Entry.comparingByKey()))
                                .limit(6)
                                .sorted(java.util.Map.Entry.comparingByKey())
                                .map(entry -> DashboardDataDTO.MonthlyRevenueDTO.builder()
                                                .month(entry.getKey().getMonth().getDisplayName(
                                                                java.time.format.TextStyle.SHORT,
                                                                java.util.Locale.ENGLISH))
                                                .revenue(entry.getValue())
                                                .build())
                                .collect(Collectors.toList());

                // Low Stock Alerts (Limited at DB level later if needed, but client stock is
                // usually small)
                var clientStocks = stockRepo.findByCreatedBy(username);
                List<DashboardDataDTO.LowStockDTO> lowStock = clientStocks.stream()
                                .filter(s -> s.getStockType() != null
                                                && s.getStockType().toLowerCase().contains("product"))
                                .filter(s -> s.getAvailableQuantity() < 10)
                                .sorted(java.util.Comparator
                                                .comparingInt(in.sfp.main.models.StockInfo::getAvailableQuantity))
                                .map(s -> DashboardDataDTO.LowStockDTO.builder()
                                                .itemName(s.getItemName())
                                                .currentStock(s.getAvailableQuantity())
                                                .build())
                                .limit(5)
                                .collect(Collectors.toList());

                // Top Products
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

                // Monthly Sales Goal
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

                UserAccessInfo user = userRepo.findByUsername(username);
                if (user == null)
                        user = userRepo.findByEmail(username);
                double monthlyTarget = (user != null && user.getMonthlyTarget() != null)
                                ? user.getMonthlyTarget()
                                : 100000.0;

                // Broadcast
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
                                .lowStockAlerts(lowStock)
                                .topProducts(topProducts)
                                .currentMonthRevenue(currentMonthRevenue)
                                .monthlyTarget(monthlyTarget)
                                .overdueBills(userBills.stream()
                                                .filter(b -> "FINAL".equalsIgnoreCase(b.getStatus())
                                                                && parseDouble(b.getBalancePayment()) > 0)
                                                .sorted((b1, b2) -> b2.getStockCreatedAt()
                                                                .compareTo(b1.getStockCreatedAt()))
                                                .limit(5).map(b -> DashboardDataDTO.RecentBillDTO.builder()
                                                                .id(b.getId())
                                                                .invoiceNumber(b.getInvoiceNumber())
                                                                .recipientName(b.getRecipientBillingInfo() != null
                                                                                ? b.getRecipientBillingInfo()
                                                                                                .getRecipientName()
                                                                                : "N/A")
                                                                .amount(parseDouble(b.getBalancePayment()))
                                                                .status("UNPAID")
                                                                .build())
                                                .collect(Collectors.toList()))
                                .draftShortcuts(userBills.stream()
                                                .filter(b -> "DRAFT".equalsIgnoreCase(b.getStatus()))
                                                .sorted((b1, b2) -> b2.getStockCreatedAt()
                                                                .compareTo(b1.getStockCreatedAt()))
                                                .limit(3).map(b -> DashboardDataDTO.RecentBillDTO.builder()
                                                                .id(b.getId())
                                                                .invoiceNumber(b.getInvoiceNumber())
                                                                .recipientName(b.getRecipientBillingInfo() != null
                                                                                ? b.getRecipientBillingInfo()
                                                                                                .getRecipientName()
                                                                                : "N/A")
                                                                .status("DRAFT")
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();
        }

        @Override
        public DashboardDataDTO getAdminDashboardData() {
                // OPTIMIZATION: Avoid loading ALL bills. Use SQL Aggregates instead.
                double globalRevenue = normalize(billingRepo.sumTotalAmountForFinalized());
                long finalizedCount = billingRepo.countByStatus("FINAL");
                long draftCount = billingRepo.countByStatus("DRAFT");

                long totalClients = userRepo.countByRoleAndStatus("CLIENT", "APPROVED");
                long pendingRequestsCount = userRepo.countByStatus("PENDING");

                // Optimized Pending Requests
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

                // Optimized Recent Bills
                List<TotalStockBillingInfo> recentRaw = billingRepo
                                .findRecentGlobal(org.springframework.data.domain.PageRequest.of(0, 5));

                // Pre-fetch businesses for these bills to avoid N+1
                java.util.Set<String> creatorEmails = recentRaw.stream().map(TotalStockBillingInfo::getStockCreatedBy)
                                .collect(Collectors.toSet());
                java.util.Map<String, String> businessMap = new java.util.HashMap<>();
                for (String email : creatorEmails) {
                        UserAccessInfo u = userRepo.findByEmail(email);
                        if (u == null)
                                u = userRepo.findByUsername(email);
                        if (u != null)
                                businessMap.put(email, u.getCompanyName());
                }

                List<DashboardDataDTO.RecentBillDTO> recent = recentRaw.stream()
                                .map(b -> DashboardDataDTO.RecentBillDTO.builder()
                                                .id(b.getId())
                                                .invoiceNumber(b.getInvoiceNumber())
                                                .recipientName(b.getRecipientBillingInfo() != null
                                                                ? b.getRecipientBillingInfo().getRecipientName()
                                                                : "N/A")
                                                .businessName(businessMap.getOrDefault(b.getStockCreatedBy(), "System"))
                                                .date(b.getInvoiceDate() != null ? b.getInvoiceDate().toString()
                                                                : b.getStockCreatedAt().toLocalDate().toString())
                                                .amount(parseDouble(b.getStockTotalAmount()))
                                                .status(b.getStatus())
                                                .balancePayment(parseDouble(b.getBalancePayment()))
                                                .build())
                                .collect(Collectors.toList());

                // Top Revenue Clients (Optimized via JPQL)
                List<Object[]> topClientsRaw = billingRepo
                                .findTopClientsByRevenue(org.springframework.data.domain.PageRequest.of(0, 3));
                List<DashboardDataDTO.EliteClientDTO> elite = topClientsRaw.stream()
                                .map(arr -> {
                                        String email = (String) arr[0];
                                        double rev = (Double) arr[1];
                                        UserAccessInfo u = userRepo.findByEmail(email);
                                        if (u == null)
                                                u = userRepo.findByUsername(email);
                                        return DashboardDataDTO.EliteClientDTO.builder()
                                                        .clientName(u != null ? u.getFullName() : "Unknown")
                                                        .companyName(u != null ? u.getCompanyName() : email)
                                                        .totalRevenue(rev)
                                                        .image(null) // REMOVED Base64 blob to speed up response
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // Monthly Trend & Categories (Load limited set with billItems for efficiency)
                List<TotalStockBillingInfo> trendBills = billingRepo
                                .findTrendBillsGlobal(org.springframework.data.domain.PageRequest.of(0, 500));

                List<DashboardDataDTO.MonthlyRevenueDTO> monthlyTrend = trendBills.stream()
                                .filter(b -> "FINAL".equalsIgnoreCase(b.getStatus()))
                                .collect(Collectors.groupingBy(
                                                b -> (b.getInvoiceDate() != null ? b.getInvoiceDate()
                                                                : b.getStockCreatedAt().toLocalDate())
                                                                .withDayOfMonth(1),
                                                Collectors.summingDouble(b -> parseDouble(b.getStockTotalAmount()))))
                                .entrySet().stream()
                                .sorted(Collections.reverseOrder(java.util.Map.Entry.comparingByKey()))
                                .limit(6)
                                .sorted(java.util.Map.Entry.comparingByKey())
                                .map(entry -> DashboardDataDTO.MonthlyRevenueDTO.builder()
                                                .month(entry.getKey().getMonth().getDisplayName(
                                                                java.time.format.TextStyle.SHORT,
                                                                java.util.Locale.ENGLISH))
                                                .revenue(entry.getValue())
                                                .build())
                                .collect(Collectors.toList());

                // Top Categories Calculation (Optimized Batch Processing)
                java.util.Map<String, Double> itemRevenueMap = new java.util.HashMap<>();
                trendBills.forEach(b -> {
                        b.getBillItems().forEach(item -> {
                                itemRevenueMap.put(item.getItemName(),
                                                itemRevenueMap.getOrDefault(item.getItemName(), 0.0)
                                                                + item.getTotalItemAmount());
                        });
                });

                java.util.List<StockInfo> stocks = stockRepo.findByItemNameIn(itemRevenueMap.keySet());
                java.util.Map<String, String> itemToCategoryMap = stocks.stream()
                                .filter(s -> s.getStockCategories() != null)
                                .collect(Collectors.toMap(StockInfo::getItemName,
                                                s -> {
                                                        String categoryName = s.getStockCategories().getCategoryName();
                                                        return categoryName != null ? categoryName : "Uncategorized";
                                                }, (a, b) -> a));

                java.util.Map<String, Double> categoryRevenue = new java.util.HashMap<>();
                itemRevenueMap.forEach((name, rev) -> {
                        String cat = itemToCategoryMap.getOrDefault(name, "Uncategorized");
                        categoryRevenue.put(cat, categoryRevenue.getOrDefault(cat, 0.0) + rev);
                });

                List<DashboardDataDTO.TopCategoryDTO> topCategories = categoryRevenue.entrySet().stream()
                                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                                .limit(5)
                                .map(e -> DashboardDataDTO.TopCategoryDTO.builder().categoryName(e.getKey())
                                                .value(e.getValue()).build())
                                .collect(Collectors.toList());

                // Broadcast
                String broadcast = announcementRepo.findFirstByActiveTrueOrderByCreatedAtDesc().map(a -> a.getMessage())
                                .orElse(null);

                return DashboardDataDTO.builder()
                                .globalRevenue(globalRevenue)
                                .totalClientsCount(totalClients)
                                .pendingRequestsCount(pendingRequestsCount)
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

        private double normalize(Double d) {
                return d == null ? 0.0 : d;
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
