package com.halo.lims.model.compositeKeys;

import com.halo.lims.model.Test;
import com.halo.lims.model.TestPanel;
import java.io.Serializable;
import java.util.Objects;

public class PanelTestId implements Serializable {
    private TestPanel panel;
    private Test test;

    public PanelTestId() {}

    public PanelTestId(TestPanel panel, Test test) {
        this.panel = panel;
        this.test = test;
    }

    // Getters and Setters
    public TestPanel getPanel() { return panel; }
    public void setPanel(TestPanel panel) { this.panel = panel; }

    public Test getTest() { return test; }
    public void setTest(Test test) { this.test = test; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PanelTestId that = (PanelTestId) o;
        return Objects.equals(panel, that.panel) && Objects.equals(test, that.test);
    }

    @Override
    public int hashCode() {
        return Objects.hash(panel, test);
    }
}
