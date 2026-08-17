package com.gibran.waroenkbikers.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class NormalisasiNilaiCalculatorTest {
    private NormalisasiNilaiCalculatorTest() {
    }

    public static void main(String[] args) throws Exception {
        shouldNormalizeAgainstColumnMaximum();
        shouldNormalizeZeroScoreAsZero();
        shouldKeepDummyC3AtOne();
        shouldKeepPrimaryC3Separate();
        shouldNotExposeCriterionTypeInCriteriaPanel();
    }

    private static void shouldNormalizeAgainstColumnMaximum() {
        double result = NormalisasiNilaiCalculator.hitungNilai(75.0,
                new double[]{50.0, 75.0, 90.0});
        assertClose(75.0 / 90.0, result);
    }

    private static void shouldNormalizeZeroScoreAsZero() {
        assertClose(0.0, NormalisasiNilaiCalculator.hitungNilai(0.0,
                new double[]{0.0, 0.0}));
    }

    private static void shouldKeepDummyC3AtOne() {
        assertClose(1.0, NormalisasiNilaiCalculator.hitungNilai(60.0,
                new double[]{60.0, 60.0, 60.0}));
    }

    private static void shouldKeepPrimaryC3Separate() {
        assertClose(88.0 / 90.0, NormalisasiNilaiCalculator.hitungNilai(88.0,
                new double[]{88.0, 84.0, 90.0}));
    }

    private static void shouldNotExposeCriterionTypeInCriteriaPanel() throws Exception {
        String source = new String(Files.readAllBytes(Paths.get(
                "src/com/gibran/waroenkbikers/ui/KriteriaPanel.java")), StandardCharsets.UTF_8);
        if (source.contains("tipeComboBox") || source.contains("\"Jenis\"")) {
            throw new AssertionError("Criterion type controls must be removed");
        }
    }

    private static void assertClose(double expected, double actual) {
        if (Math.abs(expected - actual) > 0.000001) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
