package com.halo.lims.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardResponse {
    private long newPatientsToday;
    private BigDecimal revenueToday;
    private long pendingServiceRequests;
    private List<Map<String, Object>> weeklyRevenue;
    private double averageTat;

    public DashboardResponse() {}

    public long getNewPatientsToday() { return newPatientsToday; }
    public void setNewPatientsToday(long newPatientsToday) { this.newPatientsToday = newPatientsToday; }

    public BigDecimal getRevenueToday() { return revenueToday; }
    public void setRevenueToday(BigDecimal revenueToday) { this.revenueToday = revenueToday; }

    public long getPendingServiceRequests() { return pendingServiceRequests; }
    public void setPendingServiceRequests(long pendingServiceRequests) { this.pendingServiceRequests = pendingServiceRequests; }

    public List<Map<String, Object>> getWeeklyRevenue() { return weeklyRevenue; }
    public void setWeeklyRevenue(List<Map<String, Object>> weeklyRevenue) { this.weeklyRevenue = weeklyRevenue; }

    public double getAverageTat() { return averageTat; }
    public void setAverageTat(double averageTat) { this.averageTat = averageTat; }
}
