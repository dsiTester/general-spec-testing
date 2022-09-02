public abstract class AbstractFormatValidator implements Serializable {

    /**
     * <p>Validate using the specified <i>pattern</i>.
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to validate the value against.
     * @return <code>true</code> if the value is valid.
     */
    public boolean isValid(String value, String pattern) { // definition of a
        return isValid(value, pattern, (Locale)null);      // calls b based on implementation
    }
}

public abstract class AbstractNumberValidator extends AbstractFormatValidator {

    /**
     * <p>Validate using the specified <code>Locale</code>.</p>
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to validate the value against, or the
     *        default for the <code>Locale</code> if <code>null</code>.
     * @param locale The locale to use for the date format, system default if null.
     * @return <code>true</code> if the value is valid.
     */
    @Override
    public boolean isValid(String value, String pattern, Locale locale) { // definition of b
        Object parsedValue = parse(value, pattern, locale);
        return (parsedValue == null ? false : true);
    }

}

public class CurrencyValidatorTest extends TestCase {
    /**
     * Test currency values with a pattern
     */
    public void testPattern() {
        // Set the default Locale
        Locale origDefault = Locale.getDefault();
        Locale.setDefault(Locale.UK);

        BigDecimalValidator validator = CurrencyValidator.getInstance();
        String basicPattern = CURRENCY_SYMBOL + "#,##0.000";
        String pattern = basicPattern + ";[" + basicPattern +"]";
        BigDecimal expected   = new BigDecimal("1234.567");
        BigDecimal negative   = new BigDecimal("-1234.567");

        // Test Pattern
        assertEquals("default",        expected,   validator.validate(UK_POUND + "1,234.567", pattern));
        assertEquals("negative",       negative,   validator.validate("[" + UK_POUND + "1,234.567]", pattern));
        assertEquals("no symbol +ve",  expected,   validator.validate("1,234.567",    pattern));
        assertEquals("no symbol -ve",  negative,   validator.validate("[1,234.567]",  pattern));

        // Test Pattern & Locale
        assertEquals("default",        expected,   validator.validate(US_DOLLAR + "1,234.567", pattern, Locale.US));
        assertEquals("negative",       negative,   validator.validate("[" + US_DOLLAR + "1,234.567]", pattern, Locale.US));
        assertEquals("no symbol +ve",  expected,   validator.validate("1,234.567",    pattern, Locale.US));
        assertEquals("no symbol -ve",  negative,   validator.validate("[1,234.567]",  pattern, Locale.US));

        // invalid
        assertFalse("invalid symbol",  validator.isValid(US_DOLLAR + "1,234.567", pattern)); // call to a
        assertFalse("invalid symbol",  validator.isValid(UK_POUND  + "1,234.567", pattern, Locale.US)); // call to b

        // Restore the original default
        Locale.setDefault(origDefault);
    }

}
