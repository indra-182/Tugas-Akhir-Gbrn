package com.gibran.waroenkbikers.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class TanggalLaporanFormatterTest {
    private TanggalLaporanFormatterTest() {
    }

    public static void main(String[] args) throws Exception {
        shouldFormatIndonesianReportDateWithCalendarCorrectWeekday();
    }

    private static void shouldFormatIndonesianReportDateWithCalendarCorrectWeekday() throws Exception {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        parser.setLenient(false);

        String result = TanggalLaporanFormatter.format(parser.parse("2026-08-08"));

        if (!"Jakarta, Sabtu 8 Agustus 2026".equals(result)) {
            throw new AssertionError("Unexpected report date: " + result);
        }
    }
}
