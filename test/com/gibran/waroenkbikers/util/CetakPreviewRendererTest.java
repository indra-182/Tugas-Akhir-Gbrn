package com.gibran.waroenkbikers.util;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;

public final class CetakPreviewRendererTest {
    private CetakPreviewRendererTest() {
    }

    public static void main(String[] args) throws Exception {
        shouldRenderEveryPageUntilPrintableHasNoSuchPage();
    }

    private static void shouldRenderEveryPageUntilPrintableHasNoSuchPage() throws Exception {
        final List<Integer> requestedPages = new ArrayList<Integer>();
        Printable threePagePrintable = new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
                requestedPages.add(pageIndex);
                return pageIndex < 3 ? PAGE_EXISTS : NO_SUCH_PAGE;
            }
        };

        int renderedPageCount = CetakPreviewRenderer.renderAll(threePagePrintable,
                new PageFormat()).size();

        if (renderedPageCount != 3) {
            throw new AssertionError("Expected 3 preview pages but got " + renderedPageCount);
        }
        if (!requestedPages.equals(java.util.Arrays.asList(0, 1, 2, 3))) {
            throw new AssertionError("Unexpected requested pages: " + requestedPages);
        }
    }
}
