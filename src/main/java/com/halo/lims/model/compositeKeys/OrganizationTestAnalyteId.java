package com.halo.lims.model.compositeKeys;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationTestAnalyteId implements Serializable {
    private Integer organization;
    private Integer testAnalyte;
}
