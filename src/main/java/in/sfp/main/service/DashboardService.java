package in.sfp.main.service;

import in.sfp.main.dto.DashboardDataDTO;

public interface DashboardService {
    DashboardDataDTO getClientDashboardData(String username);

    DashboardDataDTO getAdminDashboardData();
}
