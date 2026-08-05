package com.halo.lims.service;

import com.halo.lims.model.Observation;
import com.halo.lims.model.ReferenceRange;
import com.halo.lims.model.TestAnalyte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ReportDtoBuilderTest {

    private ReportDtoBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ReportDtoBuilder(null, null, null, null, null, null);
    }

    @Test
    void testGetNumericBounds_fromDatabaseValues() {
        ReferenceRange rr = ReferenceRange.builder()
                .lowValue(new BigDecimal("10.5"))
                .highValue(new BigDecimal("20.5"))
                .build();
        Observation obs = Observation.builder().referenceRange(rr).build();

        BigDecimal[] bounds = builder.getNumericBounds(obs);
        assertNotNull(bounds);
        assertEquals(new BigDecimal("10.5"), bounds[0]);
        assertEquals(new BigDecimal("20.5"), bounds[1]);
    }

    @Test
    void testGetNumericBounds_parseRangeFromTextRange() {
        ReferenceRange rr = ReferenceRange.builder()
                .textRange("74 - 139 mg/dL")
                .build();
        Observation obs = Observation.builder().referenceRange(rr).build();

        BigDecimal[] bounds = builder.getNumericBounds(obs);
        assertNotNull(bounds);
        assertEquals(new BigDecimal("74"), bounds[0]);
        assertEquals(new BigDecimal("139"), bounds[1]);
    }

    @Test
    void testGetNumericBounds_parseRangeFromBiologicalRefInterval() {
        TestAnalyte analyte = TestAnalyte.builder()
                .biologicalRefInterval("3.5 to 5.3")
                .build();
        Observation obs = Observation.builder().analyte(analyte).build();

        BigDecimal[] bounds = builder.getNumericBounds(obs);
        assertNotNull(bounds);
        assertEquals(new BigDecimal("3.5"), bounds[0]);
        assertEquals(new BigDecimal("5.3"), bounds[1]);
    }

    @Test
    void testGetNumericBounds_parseLessThan() {
        ReferenceRange rr = ReferenceRange.builder()
                .textRange("< 20")
                .build();
        Observation obs = Observation.builder().referenceRange(rr).build();

        BigDecimal[] bounds = builder.getNumericBounds(obs);
        assertNotNull(bounds);
        assertEquals(BigDecimal.ZERO, bounds[0]);
        assertEquals(new BigDecimal("20"), bounds[1]);
    }

    @Test
    void testGetNumericBounds_parseGreaterThan() {
        ReferenceRange rr = ReferenceRange.builder()
                .textRange(">= 60")
                .build();
        Observation obs = Observation.builder().referenceRange(rr).build();

        BigDecimal[] bounds = builder.getNumericBounds(obs);
        assertNotNull(bounds);
        assertEquals(new BigDecimal("60"), bounds[0]);
        assertNull(bounds[1]);
    }

    @Test
    void testComputeMarkerPercent_standardRange() {
        ReferenceRange rr = ReferenceRange.builder()
                .textRange("10 - 20")
                .build();
        // Middle value (15) should be right in the middle (50%)
        Observation obs1 = Observation.builder()
                .referenceRange(rr)
                .valueNumeric(new BigDecimal("15"))
                .build();
        assertEquals(50, builder.computeMarkerPercent(obs1));

        // Low bound value (10) should be at 30%
        Observation obs2 = Observation.builder()
                .referenceRange(rr)
                .valueNumeric(new BigDecimal("10"))
                .build();
        assertEquals(30, builder.computeMarkerPercent(obs2));

        // High bound value (20) should be at 70%
        Observation obs3 = Observation.builder()
                .referenceRange(rr)
                .valueNumeric(new BigDecimal("20"))
                .build();
        assertEquals(70, builder.computeMarkerPercent(obs3));

        // Lower than low (5) should be mapped to the left side (< 30%)
        Observation obs4 = Observation.builder()
                .referenceRange(rr)
                .valueNumeric(new BigDecimal("5"))
                .build();
        // 30 + ((5 - 10)/10)*40 = 30 - 20 = 10
        assertEquals(10, builder.computeMarkerPercent(obs4));
    }

    @Test
    void testComputeMarkerPercent_greaterThan() {
        ReferenceRange rr = ReferenceRange.builder()
                .textRange(">= 60")
                .build();
        // Above 60 should be right side (70%)
        Observation obs1 = Observation.builder()
                .referenceRange(rr)
                .valueNumeric(new BigDecimal("65"))
                .build();
        assertEquals(70, builder.computeMarkerPercent(obs1));

        // Below 60 should be left side (15%)
        Observation obs2 = Observation.builder()
                .referenceRange(rr)
                .valueNumeric(new BigDecimal("55"))
                .build();
        assertEquals(15, builder.computeMarkerPercent(obs2));
    }

    @Test
    void testGetNumericBounds_parseMultiPartRange() {
        ReferenceRange rr = ReferenceRange.builder()
                .textRange("Low: <40 | High: >60")
                .build();
        Observation obs = Observation.builder().referenceRange(rr).build();

        BigDecimal[] bounds = builder.getNumericBounds(obs);
        assertNotNull(bounds);
        assertEquals(new BigDecimal("40"), bounds[0]);
        assertEquals(new BigDecimal("60"), bounds[1]);
    }

    @Test
    void testGetNumericBounds_pediatricRanges() {
        ReferenceRange rr = ReferenceRange.builder()
                .textRange("0 - 4 d : 2.8 - 4.4, 4 d-14 y: 3.8 - 5.4")
                .lowValue(new BigDecimal("3.5"))
                .highValue(new BigDecimal("5.0"))
                .build();

        // 1. Patient is 2 days old (newborn) -> matches "0 - 4 d" -> [2.8, 4.4]
        com.halo.lims.model.Patient pNewborn = com.halo.lims.model.Patient.builder()
                .dateOfBirth(java.time.LocalDate.now().minusDays(2))
                .build();
        Observation obsNewborn = Observation.builder().referenceRange(rr).patient(pNewborn).build();
        BigDecimal[] boundsNewborn = builder.getNumericBounds(obsNewborn);
        assertEquals(new BigDecimal("2.8"), boundsNewborn[0]);
        assertEquals(new BigDecimal("4.4"), boundsNewborn[1]);

        // 2. Patient is 5 years old -> matches "4 d-14 y" -> [3.8, 5.4]
        com.halo.lims.model.Patient pChild = com.halo.lims.model.Patient.builder()
                .dateOfBirth(java.time.LocalDate.now().minusYears(5))
                .build();
        Observation obsChild = Observation.builder().referenceRange(rr).patient(pChild).build();
        BigDecimal[] boundsChild = builder.getNumericBounds(obsChild);
        assertEquals(new BigDecimal("3.8"), boundsChild[0]);
        assertEquals(new BigDecimal("5.4"), boundsChild[1]);

        // 3. Patient is 25 years old -> matches neither -> falls back to database defaults [3.5, 5.0]
        com.halo.lims.model.Patient pAdult = com.halo.lims.model.Patient.builder()
                .dateOfBirth(java.time.LocalDate.now().minusYears(25))
                .build();
        Observation obsAdult = Observation.builder().referenceRange(rr).patient(pAdult).build();
        BigDecimal[] boundsAdult = builder.getNumericBounds(obsAdult);
        assertEquals(new BigDecimal("3.5"), boundsAdult[0]);
        assertEquals(new BigDecimal("5.0"), boundsAdult[1]);
    }

    @Test
    void testGetNumericBounds_multiPartNormalAggregation() {
        ReferenceRange rr = ReferenceRange.builder()
                .textRange("<100 Optimal | 100-129 Desirable | 130-159 Borderline | 160-189 High | >189 Very High")
                .build();
        Observation obs = Observation.builder().referenceRange(rr).build();
        BigDecimal[] bounds = builder.getNumericBounds(obs);
        assertNotNull(bounds);
        // "<100 Optimal" (0 to 100) and "100-129 Desirable" (100 to 129) should aggregate to [0, 129]
        assertEquals(BigDecimal.ZERO, bounds[0]);
        assertEquals(new BigDecimal("129"), bounds[1]);
    }

    @Test
    void testGetNumericBounds_controlValueRanges() {
        // PT control
        ReferenceRange rrPt = ReferenceRange.builder().textRange("Control- 11.5").build();
        Observation obsPt = Observation.builder().referenceRange(rrPt).build();
        BigDecimal[] boundsPt = builder.getNumericBounds(obsPt);
        assertEquals(BigDecimal.ZERO, boundsPt[0]);
        assertEquals(new BigDecimal("11.5"), boundsPt[1]);

        // APTT control
        ReferenceRange rrAptt = ReferenceRange.builder().textRange("Control- 25").build();
        Observation obsAptt = Observation.builder().referenceRange(rrAptt).build();
        BigDecimal[] boundsAptt = builder.getNumericBounds(obsAptt);
        assertEquals(BigDecimal.ZERO, boundsAptt[0]);
        assertEquals(new BigDecimal("25"), boundsAptt[1]);
    }

    @Test
    void testGetNumericBounds_genderSpecificRanges() {
        ReferenceRange rrGgt = ReferenceRange.builder()
                .textRange("Male : 8 - 61, Females : 5 - 36")
                .build();

        // 1. Male patient
        com.halo.lims.model.Patient pMale = com.halo.lims.model.Patient.builder()
                .gender("male")
                .build();
        Observation obsMale = Observation.builder().referenceRange(rrGgt).patient(pMale).build();
        BigDecimal[] boundsMale = builder.getNumericBounds(obsMale);
        assertEquals(new BigDecimal("8"), boundsMale[0]);
        assertEquals(new BigDecimal("61"), boundsMale[1]);

        // 2. Female patient
        com.halo.lims.model.Patient pFemale = com.halo.lims.model.Patient.builder()
                .gender("female")
                .build();
        Observation obsFemale = Observation.builder().referenceRange(rrGgt).patient(pFemale).build();
        BigDecimal[] boundsFemale = builder.getNumericBounds(obsFemale);
        assertEquals(new BigDecimal("5"), boundsFemale[0]);
        assertEquals(new BigDecimal("36"), boundsFemale[1]);
    }

    @Test
    void testGetNumericBounds_genderAbbreviationRanges() {
        ReferenceRange rr = ReferenceRange.builder()
                .textRange("M: 10 - 20, F: 5 - 15")
                .build();

        // 1. Male patient
        com.halo.lims.model.Patient pMale = com.halo.lims.model.Patient.builder()
                .gender("male")
                .build();
        Observation obsMale = Observation.builder().referenceRange(rr).patient(pMale).build();
        BigDecimal[] boundsMale = builder.getNumericBounds(obsMale);
        assertEquals(new BigDecimal("10"), boundsMale[0]);
        assertEquals(new BigDecimal("20"), boundsMale[1]);

        // 2. Female patient
        com.halo.lims.model.Patient pFemale = com.halo.lims.model.Patient.builder()
                .gender("female")
                .build();
        Observation obsFemale = Observation.builder().referenceRange(rr).patient(pFemale).build();
        BigDecimal[] boundsFemale = builder.getNumericBounds(obsFemale);
        assertEquals(new BigDecimal("5"), boundsFemale[0]);
        assertEquals(new BigDecimal("15"), boundsFemale[1]);
    }

    @Test
    void testGetNumericValue_fallbackString() {
        // valueNumeric is set -> returns valueNumeric
        Observation obs1 = Observation.builder().valueNumeric(new BigDecimal("12.34")).build();
        assertEquals(new BigDecimal("12.34"), builder.getNumericValue(obs1));

        // valueNumeric is null, valueString has decimal -> parses decimal
        Observation obs2 = Observation.builder().valueString("15.67").build();
        assertEquals(new BigDecimal("15.67"), builder.getNumericValue(obs2));

        // valueNumeric is null, valueString has operator -> extracts decimal
        Observation obs3 = Observation.builder().valueString("< 0.05").build();
        assertEquals(new BigDecimal("0.05"), builder.getNumericValue(obs3));

        // valueNumeric is null, valueString is qualitative -> returns null
        Observation obs4 = Observation.builder().valueString("Negative").build();
        assertNull(builder.getNumericValue(obs4));
    }
}
