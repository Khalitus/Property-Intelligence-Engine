package edu.shu.propertyintel.ui;

import org.junit.jupiter.api.Test;
import javax.swing.JTextField;
import static org.junit.jupiter.api.Assertions.*;

class InputRulesTest {
    @Test
    void parsesOptionalAndBoundedPrices() {
        assertNull(InputRules.optionalPrice(""));
        assertEquals(125000.50, InputRules.optionalPrice("125000.50"));
        assertEquals(0.0, InputRules.optionalPrice("0"));
    }

    @Test
    void rejectsInvalidPriceValues() {
        assertThrows(IllegalArgumentException.class, () -> InputRules.optionalPrice("NaN"));
        assertThrows(IllegalArgumentException.class, () -> InputRules.optionalPrice("-1"));
        assertThrows(IllegalArgumentException.class, () -> InputRules.optionalPrice("1,000"));
        assertThrows(IllegalArgumentException.class, () -> InputRules.optionalPrice("1000000000000"));
    }

    @Test
    void priceInputRestrictsInvalidCharactersAndDecimalPlaces() {
        JTextField field = new JTextField();
        InputRules.price(field);
        field.setText("150000.75");
        assertEquals("150000.75", field.getText());
        field.setText("150000.755");
        assertEquals("150000.75", field.getText());
        field.setText("abc");
        assertEquals("150000.75", field.getText());
    }

    @Test
    void propertyIdInputRejectsSpaces() {
        JTextField field = new JTextField();
        InputRules.propertyId(field);
        field.setText("P0001");
        field.setText("Bad ID");
        assertEquals("P0001", field.getText());
    }
}
