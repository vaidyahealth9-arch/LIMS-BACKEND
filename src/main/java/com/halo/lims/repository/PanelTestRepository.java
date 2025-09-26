package com.halo.lims.repository;

import com.halo.lims.model.PanelTest;
import com.halo.lims.model.compositeKeys.PanelTestId;
import com.halo.lims.model.Test;
import com.halo.lims.model.TestPanel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PanelTestRepository extends JpaRepository<PanelTest, PanelTestId> {
    /**
     * Finds all PanelTest links for a specific TestPanel entity.
     * @param panel The TestPanel entity.
     * @return A list of PanelTest entities, representing tests within that panel.
     */
    List<PanelTest> findByPanel(TestPanel panel);

    /**
     * Finds all PanelTest links for a specific TestPanel by its ID.
     * @param panelId The ID of the TestPanel.
     * @return A list of PanelTest entities.
     */
    List<PanelTest> findByPanel_Id(Integer panelId);

    /**
     * Finds a specific PanelTest link by Panel ID and Test ID.
     * @param panelId The ID of the TestPanel.
     * @param testId The ID of the Test.
     * @return An Optional containing the PanelTest, or empty if not found.
     */
    Optional<PanelTest> findByPanel_IdAndTest_Id(Integer panelId, Integer testId);

    /**
     * Finds all PanelTest links that include a specific Test entity.
     * @param test The Test entity.
     * @return A list of PanelTest entities, representing panels containing that test.
     */
    List<PanelTest> findByTest(Test test);

    /**
     * Finds all PanelTest links that include a specific Test by its ID.
     * @param testId The ID of the Test.
     * @return A list of PanelTest entities.
     */
    List<PanelTest> findByTest_Id(Integer testId);

    /**
     * Finds all PanelTest links for a given panel ID, ordered by display order.
     * @param panelId The ID of the TestPanel.
     * @return A list of PanelTest entities, ordered by displayOrder.
     */
    List<PanelTest> findByPanel_IdOrderByDisplayOrderAsc(Integer panelId);
}
