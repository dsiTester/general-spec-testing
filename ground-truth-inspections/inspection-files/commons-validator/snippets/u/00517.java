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
    protected Object parse(String value, String pattern, Locale locale, TimeZone timeZone) { // definition of a

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

public abstract class AbstractFormatValidator implements Serializable {
    /**
     * <p>Format an object into a <code>String</code> using
     * the specified pattern.</p>
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to format the value.
     * @return The value formatted as a <code>String</code>.
     */
    public String format(Object value, String pattern) { // definition of b
        return format(value, pattern, (Locale)null);
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

        // NOTE: commenting out the below **technically** calls b before a
        // System.out.println(validator.format(new Date(), "dd.MM.yy"));;
        // Create a Date or Calendar
        Object test = validator.parse("2005-11-28", "yyyy-MM-dd", null, null); // call to a
        assertNotNull("Test Date ", test); // assertion fails here
        assertEquals("Format pattern", "28.11.05", validator.format(test, "dd.MM.yy")); // call to b
        assertEquals("Format locale",  "11/28/05", validator.format(test, Locale.US));
    }

}
