package com.halo.lims.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardResponse {
    private long newPatientsToday;
    private BigDecimal revenueToday;
    private long pendingServiceRequests;
    private List<Map<String, Object>> weeklyRevenue;
    private double averageTat;
}
