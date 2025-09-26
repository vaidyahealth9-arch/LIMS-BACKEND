package com.halo.lims.model.compositeKeys;

import com.halo.lims.model.Test;
import com.halo.lims.model.TestPanel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanelTestId implements Serializable {
    private TestPanel panel;
    private Test test;
}
