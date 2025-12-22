package com.halo.lims.dto;

import lombok.Data;

@Data
public class UpsertOrgAnalyteRequestDto {
    private Integer analyteId;
    private Double price;
    private String code;
    private String bioReference;
}
