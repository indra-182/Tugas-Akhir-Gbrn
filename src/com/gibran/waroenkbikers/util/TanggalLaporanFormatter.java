package com.gibran.waroenkbikers.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class TanggalLaporanFormatter {
    private static final Locale INDONESIA = new Locale("id", "ID");

    private TanggalLaporanFormatter() {
    }

    public static String format(Date tanggal) {
        return "Jakarta, " + new SimpleDateFormat("EEEE d MMMM yyyy", INDONESIA).format(tanggal);
    }
}
