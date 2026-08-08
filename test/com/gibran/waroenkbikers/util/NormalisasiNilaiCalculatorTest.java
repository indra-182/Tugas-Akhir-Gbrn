package com.gibran.waroenkbikers.util;

public final class NormalisasiNilaiCalculatorTest {
    private NormalisasiNilaiCalculatorTest() {
    }

    public static void main(String[] args) {
        shouldNormalizeAgainstColumnMaximum();
        shouldNormalizeZeroColumnAsOne();
    }

    private static void shouldNormalizeAgainstColumnMaximum() {
        double result = NormalisasiNilaiCalculator.hitungNilai(75.0,
                new double[]{50.0, 75.0, 100.0});
        assertClose(0.75, result);
    }

    private static void shouldNormalizeZeroColumnAsOne() {
        assertClose(1.0, NormalisasiNilaiCalculator.hitungNilai(0.0,
                new double[]{0.0, 0.0}));
    }

    private static void assertClose(double expected, double actual) {
        if (Math.abs(expected - actual) > 0.000001) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
