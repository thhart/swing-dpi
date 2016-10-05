/*
 * Copyright 2016 Luke Usherwood.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This project is hosted at: https://github.com/lukeu/swing-dpi
 * Comments & collaboration are both welcome.
 */

package net.bettyluke.dpi.plaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.UIResource;

import net.bettyluke.dpi.util.ScaledIcon;


/**
 * This is the default implementation of 'Tweaker'. It attempts to do some basic tweaks which
 * might be expected to be "typical" for generic / unknown L&Fs.
 *<p>
 * (You're in trouble if it is actually used as a concrete class however - there seems to be very
 * little consistency between any L&Fs especially when it comes to scaling!)
 */
public class BasicTweaker implements Tweaker {

    private static final String[] LOWER_SUFFIXES_FOR_SCALED_INTEGERS = new String[] {
        "width", "height", "indent", "size", "gap"
    };

    protected final float scaleFactor;

    protected final UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();

    /** Whether to perform other 'beautification' tweaks in addition to pure scaling tweaks. */
    protected boolean doExtraTweaks = true;

    public BasicTweaker(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    @Override
    public void initialTweaks() {
        if (doExtraTweaks) {

            // Increase the pre-scaled row height from 16 -> 19, as suggested in BasicTreeUI.java
            // (16 is Windows 95! Mac, Linux and even Windows XP used more spacing than that.)
            // It still won't update with font changes (but it will be changed with scaling)
            uiDefaults.put("Tree.rowHeight", 19);
        }
    }

    public void setDoExtraTweaks(boolean flag) {
        doExtraTweaks = flag;
    }

    @Override
    public Font modifyFont(Object key, Font original) {
        if (isUnscaled(scaleFactor)) {
            return original;
        }

        if (original instanceof UIResource) {
            return newScaledFontUIResource(original, scaleFactor);
        }

        return original;
    }

    protected static FontUIResource newScaledFontUIResource(Font original, float scale) {
        float newSize = Math.round(original.getSize() * scale);
        return new ScaledFontUIResource(original.deriveFont(newSize));
    }

    @Override
    public Icon modifyIcon(Object key, Icon original) {
        float scale = scaleFactor;
        return newScaledIconUIResource(original, scale);
    }

    protected static Icon newScaledIconUIResource(Icon original, float scale) {
        if (isUnscaled(scale) && original instanceof UIResource) {
            return original;
        }
        return new ScaledIconUIResource(new ScaledIcon(original, scale));
    }

    @Override
    public Dimension modifyDimension(Object key, Dimension original) {
        if (isUnscaled(scaleFactor) || !(original instanceof UIResource)) {
            return original;
        }
        int width = Math.round(original.width * scaleFactor);
        int height = Math.round(original.height * scaleFactor);
        return new DimensionUIResource(width, height);
    }

    @Override
    public Integer modifyInteger(Object key, Integer original) {
        return scaleIntegerIfMetric(key, original, scaleFactor);
    }

    protected static Integer scaleIntegerIfMetric(Object key, Integer original, float scale) {
        if (isUnscaled(scale) || !endsWithOneOf(lower(key), getLowerSuffixesForScaledIntegers())) {
            return original;
        }
        return Math.round(original * scale);
    }

    private static boolean endsWithOneOf(String text, String[] suffixes) {
        for (String suffix : suffixes) {
            if (text.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static String lower(Object key) {
        return (key instanceof String) ? ((String) key).toLowerCase() : "";
    }

    protected static String[] getLowerSuffixesForScaledIntegers() {
        return LOWER_SUFFIXES_FOR_SCALED_INTEGERS;
    }

    protected static boolean isUnscaled(float scale) {
        return Math.abs(scale - 1f) < 0.001f;
    }

    @Override
    public void finalTweaks() {
        if (doExtraTweaks) {
            uiDefaults.put("Tree.paintLines", false);

            fadeColor("Table.gridColor", "Table.background");
        }
    }

    private void fadeColor(String toFade, String background) {
        Color main = uiDefaults.getColor(toFade);
        Color bg = uiDefaults.getColor(background);
        if (main != null && bg != null) {
            uiDefaults.put(toFade, blendColour(main, bg, 25));
        }
    }

    private static Color blendColour(Color a, Color b, int percentA) {
        int percentB = 100 - percentA;
        return new Color(
                (a.getRed() * percentA + b.getRed() * percentB) / 100,
                (a.getGreen() * percentA + b.getGreen() * percentB) / 100,
                (a.getBlue() * percentA + b.getBlue() * percentB) / 100);
    }
}
