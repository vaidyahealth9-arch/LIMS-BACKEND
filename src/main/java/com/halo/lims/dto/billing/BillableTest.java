package com.halo.lims.dto.billing;

import java.math.BigDecimal;

public class BillableTest {
    private Integer testId;
    private String testName;
    private BigDecimal price;

    public BillableTest(Integer testId, String testName, BigDecimal price) {
        this.testId = testId;
        this.testName = testName;
        this.price = price;
    }

    public Integer getTestId() {
        return testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
