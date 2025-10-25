package com.halo.lims.repository;

import com.halo.lims.model.ServiceRequest;
import com.halo.lims.model.ServiceRequestItem;
import com.halo.lims.model.compositeKeys.ServiceRequestItemId;
import com.halo.lims.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRequestItemRepository extends JpaRepository<ServiceRequestItem, ServiceRequestItemId> {

    /**
     * Finds all ServiceRequestItem links for a specific ServiceRequest entity.
     * @param serviceRequest The ServiceRequest entity.
     * @return A list of ServiceRequestItem entities.
     */
    List<ServiceRequestItem> findByServiceRequest(ServiceRequest serviceRequest);

    /**
     * Finds all ServiceRequestItem links for a list of ServiceRequest entities.
     * @param serviceRequests The List of ServiceRequest entities.
     * @return A list of ServiceRequestItem entities.
     */
    List<ServiceRequestItem> findByServiceRequestIn(List<ServiceRequest> serviceRequests);

    /**
     * Finds all ServiceRequestItem links for a specific ServiceRequest by its ID.
     * @param serviceRequestId The ID of the ServiceRequest.
     * @return A list of ServiceRequestItem entities.
     */
    List<ServiceRequestItem> findByServiceRequest_Id(Integer serviceRequestId);

    /**
     * Finds a specific ServiceRequestItem link by ServiceRequest ID and Test ID.
     * @param serviceRequestId The ID of the ServiceRequest.
     * @param testId The ID of the Test.
     * @return An Optional containing the ServiceRequestItem, or empty if not found.
     */
    Optional<ServiceRequestItem> findByServiceRequest_IdAndTest_Id(Integer serviceRequestId, Integer testId);

    List<ServiceRequestItem> findByServiceRequest_IdAndTest_IdIn(Integer serviceRequestId, java.util.List<Integer> testIds);

    /**
     * Finds all ServiceRequestItem links that include a specific Test entity.
     * @param test The Test entity.
     * @return A list of ServiceRequestItem entities.
     */
    List<ServiceRequestItem> findByTest(Test test);

    /**
     * Finds all ServiceRequestItem links that include a specific Test by its ID.
     * @param testId The ID of the Test.
     * @return A list of ServiceRequestItem entities.
     */
    List<ServiceRequestItem> findByTest_Id(Integer testId);

    /**
     * Finds all ServiceRequestItem links for a given service request ID and status.
     * @param serviceRequestId The ID of the ServiceRequest.
     * @param status The status of the service request item (e.g., "requested", "completed").
     * @return A list of ServiceRequestItem entities.
     */
    List<ServiceRequestItem> findByServiceRequest_IdAndStatus(Integer serviceRequestId, String status);
}
