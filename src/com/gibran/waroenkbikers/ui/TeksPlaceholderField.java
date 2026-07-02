package com.gibran.waroenkbikers.ui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JTextField;

public class TeksPlaceholderField extends JTextField {
    private final String placeholder;

    public TeksPlaceholderField(String placeholder, int kolom) {
        super(kolom);
        this.placeholder = placeholder;
        setToolTipText(placeholder);
    }

    @Override
    protected void paintComponent(Graphics grafik) {
        super.paintComponent(grafik);
        if (getText().length() == 0) {
            grafik.setColor(new Color(156, 163, 175));
            grafik.setFont(getFont());
            grafik.drawString(placeholder, 8, getHeight() / 2 + grafik.getFontMetrics().getAscent() / 2 - 2);
        }
    }
}
