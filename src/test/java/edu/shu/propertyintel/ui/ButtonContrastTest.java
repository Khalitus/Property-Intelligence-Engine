package edu.shu.propertyintel.ui;

import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class ButtonContrastTest {
    @Test
    void primaryAndSecondaryButtonsUseReadableForegrounds() {
        JButton primary = UiTheme.button("Search", true);
        JButton secondary = UiTheme.button("Reset", false);
        assertEquals(Color.WHITE, primary.getForeground());
        assertEquals(UiTheme.INK, secondary.getForeground());
        assertFalse(primary.getBackground().equals(Color.WHITE));
        assertFalse(secondary.getBackground().equals(Color.WHITE));
    }

    @Test
    void paintingPrimaryAndSecondaryButtonsPreservesContrast() {
        for (boolean primary : new boolean[]{true, false}) {
            JButton button = UiTheme.button("Check", primary);
            button.setSize(150, 42);
            BufferedImage rendered = new BufferedImage(150, 42, BufferedImage.TYPE_INT_RGB);
            button.paint(rendered.getGraphics());
            assertEquals(primary ? Color.WHITE : UiTheme.INK, button.getForeground());
            assertEquals(button.getBackground().getRGB(), rendered.getRGB(5, 5) | 0xff000000);
        }
    }

    @Test
    void navigationSelectionReversesTextContrastCorrectly() {
        JButton button = UiTheme.navigationButton("Valuation");
        UiTheme.selectNavigation(button, true);
        assertEquals(Color.WHITE, button.getForeground());
        UiTheme.selectNavigation(button, false);
        assertEquals(UiTheme.INK, button.getForeground());
    }
}
