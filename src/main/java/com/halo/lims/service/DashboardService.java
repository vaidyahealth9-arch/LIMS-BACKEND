package com.halo.lims.service;

import com.halo.lims.constant.ServiceRequestStatus;
import com.halo.lims.dto.dashboard.DashboardResponse;
import com.halo.lims.model.Observation;
import com.halo.lims.model.ServiceRequest;
import com.halo.lims.model.User;
import com.halo.lims.repository.BillRepository;
import com.halo.lims.repository.ObservationRepository;
import com.halo.lims.repository.PatientRepository;
import com.halo.lims.repository.ServiceRequestRepository;
import com.halo.lims.security.SecurityService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DashboardService {

    private final PatientRepository patientRepository;
    private final BillRepository billRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ObservationRepository observationRepository;
    private final SecurityService securityService;

    public DashboardService(PatientRepository patientRepository, BillRepository billRepository, ServiceRequestRepository serviceRequestRepository, ObservationRepository observationRepository, SecurityService securityService) {
        this.patientRepository = patientRepository;
        this.billRepository = billRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.observationRepository = observationRepository;
        this.securityService = securityService;
    }

    public DashboardResponse getDashboardData() {
        DashboardResponse response = new DashboardResponse();
        response.setNewPatientsToday(getNewPatientsToday());
        response.setRevenueToday(getRevenueToday());
        response.setPendingServiceRequests(getPendingServiceRequests());
        response.setWeeklyRevenue(getWeeklyRevenue());
        response.setAverageTat(getWorkTat());
        return response;
    }

    private long getNewPatientsToday() {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getOrganization() == null) {
            return 0;
        }
        Integer organizationId = currentUser.getOrganization().getId();
        OffsetDateTime startOfDay = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
        return patientRepository.countByOrganizationIdAndCreatedAtBetween(organizationId, startOfDay, endOfDay);
    }

    private BigDecimal getRevenueToday() {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getOrganization() == null) {
            return BigDecimal.ZERO;
        }
        Integer organizationId = currentUser.getOrganization().getId();
        OffsetDateTime startOfDay = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
        BigDecimal revenue = billRepository.sumNetAmountByOrganizationIdAndInvoiceDateBetween(organizationId, startOfDay, endOfDay);
        return revenue == null ? BigDecimal.ZERO : revenue;
    }

    private long getPendingServiceRequests() {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getOrganization() == null) {
            return 0;
        }
        Integer organizationId = currentUser.getOrganization().getId();
        return serviceRequestRepository.countByPatient_OrganizationIdAndStatus(organizationId, ServiceRequestStatus.ACTIVE.getCode());
    }

    private List<Map<String, Object>> getWeeklyRevenue() {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getOrganization() == null) {
            return Collections.emptyList();
        }
        Integer organizationId = currentUser.getOrganization().getId();
        OffsetDateTime startDate = LocalDate.now().minus(6, ChronoUnit.DAYS).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<Object[]> results = billRepository.findWeeklyRevenueByOrganization(organizationId, startDate);
        Map<LocalDate, BigDecimal> revenueByDate = new HashMap<>();
        for (Object[] result : results) {
            revenueByDate.put(((java.sql.Date) result[0]).toLocalDate(), (BigDecimal) result[1]);
        }

        List<Map<String, Object>> weeklyRevenue = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minus(i, ChronoUnit.DAYS);
            BigDecimal revenue = revenueByDate.getOrDefault(date, BigDecimal.ZERO);
            Map<String, Object> dayRevenue = new HashMap<>();
            dayRevenue.put("date", date);
            dayRevenue.put("revenue", revenue);
            weeklyRevenue.add(dayRevenue);
        }

        return weeklyRevenue;
    }

    private double getWorkTat() {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getOrganization() == null) {
            return 0.0;
        }
        Integer organizationId = currentUser.getOrganization().getId();
        List<ServiceRequest> completedRequests = serviceRequestRepository.findByPatient_OrganizationIdAndStatus(organizationId, ServiceRequestStatus.COMPLETED.getCode());

        if (completedRequests.isEmpty()) {
            return 0.0;
        }

        long totalTat = 0;
        int count = 0;

        for (ServiceRequest request : completedRequests) {
            Optional<Observation> lastObservation = observationRepository.findTopByServiceRequestIdOrderByEffectiveDateTimeDesc(request.getId());
            if (lastObservation.isPresent()) {
                long tat = ChronoUnit.HOURS.between(request.getOrderDate(), lastObservation.get().getEffectiveDateTime());
                totalTat += tat;
                count++;
            }
        }

        return count > 0 ? (double) totalTat / count : 0.0;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            return null; // Or throw AccessDeniedException
        }
        return (User) authentication.getPrincipal();
    }
}