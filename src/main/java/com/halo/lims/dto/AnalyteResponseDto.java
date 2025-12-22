package com.halo.lims.dto;

import lombok.Data;

@Data
public class AnalyteResponseDto {
    private Long id;
    private String name;
    private Double price;
    private String code;
    private String associatedTest;
    private String bioReference;
    private boolean isOrgSpecific;
}
