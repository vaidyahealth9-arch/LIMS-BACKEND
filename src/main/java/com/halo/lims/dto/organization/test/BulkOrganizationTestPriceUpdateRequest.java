package com.halo.lims.dto.organization.test;

import java.math.BigDecimal;
import java.util.List;

public class BulkOrganizationTestPriceUpdateRequest {
    private List<Integer> testIds;
    private BigDecimal price;

    public BulkOrganizationTestPriceUpdateRequest() {}

    public List<Integer> getTestIds() {
        return testIds;
    }

    public void setTestIds(List<Integer> testIds) {
        this.testIds = testIds;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}