package com.gibran.waroenkbikers.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

public final class CetakPreviewRenderer {
    private CetakPreviewRenderer() {
    }

    public static List<BufferedImage> renderAll(Printable printable, PageFormat pageFormat)
            throws PrinterException {
        int lebarHalaman = (int) Math.ceil(pageFormat.getWidth());
        int tinggiHalaman = (int) Math.ceil(pageFormat.getHeight());
        List<BufferedImage> halaman = new ArrayList<BufferedImage>();

        for (int indeksHalaman = 0; ; indeksHalaman++) {
            BufferedImage gambar = new BufferedImage(lebarHalaman, tinggiHalaman,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D grafik = gambar.createGraphics();
            int hasilCetak;
            try {
                grafik.setColor(Color.WHITE);
                grafik.fillRect(0, 0, lebarHalaman, tinggiHalaman);
                hasilCetak = printable.print(grafik, pageFormat, indeksHalaman);
            } finally {
                grafik.dispose();
            }
            if (hasilCetak == Printable.NO_SUCH_PAGE) {
                return halaman;
            }
            halaman.add(gambar);
        }
    }
}
