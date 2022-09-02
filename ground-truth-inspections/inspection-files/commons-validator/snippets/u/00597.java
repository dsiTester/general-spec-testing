// TimeValidator extends AbstractFormatValidator, and doesn't implement either of the two methods
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
    public String format(Object value, String pattern, Locale locale) { // called from both a and b
        Format formatter = getFormat(pattern, locale);
        return format(value, formatter);
    }
}

public class TimeValidatorTest extends TestCase {

    /**
     * Test Invalid Dates with "locale" validation
     */
    public void testFormat() {
        // Set the default Locale
        Locale.setDefault(Locale.UK);

        Object test = TimeValidator.getInstance().validate("16:49:23", "HH:mm:ss");
        assertNotNull("Test Date ", test);
        assertEquals("Format pattern", "16-49-23", validator.format(test, "HH-mm-ss")); // call to a; assertion fails here
        assertEquals("Format locale",  "4:49 PM",  validator.format(test, Locale.US)); // call to b
        assertEquals("Format default", "16:49",  validator.format(test));

    }

}
