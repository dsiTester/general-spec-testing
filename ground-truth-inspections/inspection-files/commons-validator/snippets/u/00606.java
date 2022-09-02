public class TimeValidator extends AbstractCalendarValidator {
    /**
     * <p>Validate/convert a time using the default <code>Locale</code>
     *    and <code>TimeZone</code>.
     *
     * @param value The value validation is being performed on.
     * @return The parsed <code>Calendar</code> if valid or <code>null</code>
     *  if invalid.
     */
    public Calendar validate(String value) { // definition of a
        return (Calendar)parse(value, (String)null, (Locale)null, (TimeZone)null);
    }

    /**
     * <p>Validate/convert a time using the specified <code>TimeZone</code>
     *    and default <code>Locale</code>.
     *
     * @param value The value validation is being performed on.
     * @param timeZone The Time Zone used to parse the time, system default if null.
     * @return The parsed <code>Calendar</code> if valid or <code>null</code> if invalid.
     */
    public Calendar validate(String value, TimeZone timeZone) { // definition of b
        return (Calendar)parse(value, (String)null, (Locale)null, timeZone);
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
    protected Object parse(String value, String pattern, Locale locale, TimeZone timeZone) { // called by both a and b

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

    public void testTimeZone() {
        // Set the default Locale & TimeZone
        Locale.setDefault(Locale.UK);
        TimeZone.setDefault(GMT);

        Calendar result;

        // Default Locale, Default TimeZone
        result = validator.validate("18:01"); // call to a
        assertNotNull("default result", result); // assertion fails here
        ...
        result = null;

        // Default Locale, diff TimeZone
        result = validator.validate("16:49", EST); // call to b
        assertNotNull("zone result", result);
        ...
        result = null;

        // Pattern, diff TimeZone
        result = validator.validate("14-34", "HH-mm", EST);
        assertNotNull("pattern result", result);
        ...
        result = null;

        // Locale, diff TimeZone
        result = validator.validate("7:18 PM", Locale.US, EST);
        assertNotNull("locale result", result);
        ...
        result = null;

        // Locale & Pattern, diff TimeZone
        result = validator.validate("31/Dez/05 21-05", "dd/MMM/yy HH-mm", Locale.GERMAN, EST);
        assertNotNull("pattern result", result);
        ...
        result = null;

        // Locale & Pattern, default TimeZone
        result = validator.validate("31/Dez/05 21-05", "dd/MMM/yy HH-mm", Locale.GERMAN);
        assertNotNull("pattern result", result);
        ...
        result = null;

    }

}
