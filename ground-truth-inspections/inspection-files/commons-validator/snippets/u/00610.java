public class TimeValidator extends AbstractCalendarValidator {
    /**
     * <p>Validate/convert a time using the specified <i>pattern</i> and
     *    default <code>TimeZone</code>.
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to validate the value against.
     * @return The parsed <code>Calendar</code> if valid or <code>null</code> if invalid.
     */
    public Calendar validate(String value, String pattern) { // definition of a
        return (Calendar)parse(value, pattern, (Locale)null, (TimeZone)null); // call to b
    }

}

public abstract class AbstractCalendarValidator extends AbstractFormatValidator {

    /**
     * <p>Checks if the value is valid against a specified pattern.</p>
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to validate the value against, or the
     *        default for the <code>Locale</code> if <code>null</code>.
     * @param locale The locale to use for the date format, system default if null.
     * @param timeZone The Time Zone used to parse the date, system default if null.
     * @return The parsed value if valid or <code>null</code> if invalid.
     */
    protected Object parse(String value, String pattern, Locale locale, TimeZone timeZone) { // definition of b

        value = (value == null ? null : value.trim());
        if (value == null || value.isEmpty()) {
            return null;
        }
        DateFormat formatter = (DateFormat)getFormat(pattern, locale);
        if (timeZone != null) {
            formatter.setTimeZone(timeZone);
        }
        return parse(value, formatter);

    }
}

public class TimeValidatorTest extends TestCase {

    /**
     * Test Invalid Dates with "locale" validation
     */
    public void testFormat() {
        // Set the default Locale
        Locale.setDefault(Locale.UK);

        Object test = TimeValidator.getInstance().validate("16:49:23", "HH:mm:ss"); // call to a
        assertNotNull("Test Date ", test); // assertion fails here
        assertEquals("Format pattern", "16-49-23", validator.format(test, "HH-mm-ss"));
        assertEquals("Format locale",  "4:49 PM",  validator.format(test, Locale.US));
        assertEquals("Format default", "16:49",  validator.format(test));

    }

}
