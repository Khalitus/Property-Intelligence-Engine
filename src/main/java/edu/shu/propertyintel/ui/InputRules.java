package edu.shu.propertyintel.ui;

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public final class InputRules {
    private InputRules() {
    }

    public static void price(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(
                new PatternFilter("[0-9]{0,12}(\\.[0-9]{0,2})?", 15));
        UiTheme.field(field, "Optional nonnegative amount, up to 12 digits and 2 decimals; no commas.");
    }

    public static void propertyId(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(
                new PatternFilter("[A-Za-z0-9_-]{0,24}", 24));
        UiTheme.field(field, "Property ID, for example P0001 (up to 24 letters, digits, - or _).");
    }

    public static Double optionalPrice(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        double price;
        try {
            price = Double.parseDouble(input.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a valid price without commas.");
        }
        if (!Double.isFinite(price) || price < 0 || price > 999_999_999_999.99) {
            throw new IllegalArgumentException("Prices must be between 0 and 999,999,999,999.99.");
        }
        return price;
    }

    private static final class PatternFilter extends DocumentFilter {
        private final String pattern;
        private final int maxLength;

        private PatternFilter(String pattern, int maxLength) {
            this.pattern = pattern;
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass bypass, int offset, String text, AttributeSet attributes)
                throws BadLocationException {
            replace(bypass, offset, 0, text, attributes);
        }

        @Override
        public void replace(FilterBypass bypass, int offset, int length, String text, AttributeSet attributes)
                throws BadLocationException {
            if (text == null) {
                return;
            }
            String old = bypass.getDocument().getText(0, bypass.getDocument().getLength());
            String next = old.substring(0, offset) + text + old.substring(offset + length);
            if (next.length() <= maxLength && next.matches(pattern)) {
                super.replace(bypass, offset, length, text, attributes);
            }
        }
    }
}
