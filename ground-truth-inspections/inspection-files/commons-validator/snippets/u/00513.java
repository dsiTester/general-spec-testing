// Note that AbstractCalendarValidator extends AbstractFormatValidator, and does not override either a or b
public abstract class AbstractFormatValidator implements Serializable {
    /**
     * <p>Format an object into a <code>String</code> using
     * the specified pattern.</p>
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to format the value.
     * @return The value formatted as a <code>String</code>.
     */
    public String format(Object value, String pattern) { // definition of a
        return format(value, pattern, (Locale)null);
    }

    /**
     * <p>Format an object into a <code>String</code> using
     * the specified Locale.</p>
     *
     * @param value The value validation is being performed on.
     * @param locale The locale to use for the Format.
     * @return The value formatted as a <code>String</code>.
     */
    public String format(Object value, Locale locale) { // definition of b
        return format(value, (String)null, locale);
    }

    /**
     * <p>Format an object using the specified pattern and/or
     *    <code>Locale</code>.
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to format the value.
     * @param locale The locale to use for the Format.
     * @return The value formatted as a <code>String</code>.
     */
    public String format(Object value, String pattern, Locale locale) { // called by both a and b
        Format formatter = getFormat(pattern, locale);
        return format(value, formatter);
    }
}



public class DateValidatorTest extends AbstractCalendarValidatorTest {
    ...
}

public abstract class AbstractCalendarValidatorTest extends TestCase {
    /**
     * Test Invalid Dates with "locale" validation
     */
    public void testFormat() {

        // Create a Date or Calendar
        Object test = validator.parse("2005-11-28", "yyyy-MM-dd", null, null);
        assertNotNull("Test Date ", test);
        assertEquals("Format pattern", "28.11.05", validator.format(test, "dd.MM.yy")); // call to a
        assertEquals("Format locale",  "11/28/05", validator.format(test, Locale.US)); // call to b
    }

}
