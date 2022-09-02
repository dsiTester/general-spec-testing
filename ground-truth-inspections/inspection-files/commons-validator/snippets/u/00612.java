public class TimeValidator extends AbstractCalendarValidator {
    /**
     * <p>Validate/convert a time using the specified <i>pattern</i>
     *    and <code>TimeZone</code>.
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to validate the value against.
     * @param timeZone The Time Zone used to parse the time, system default if null.
     * @return The parsed <code>Calendar</code> if valid or <code>null</code> if invalid.
     */
    public Calendar validate(String value, String pattern, TimeZone timeZone) { // definition of a
        return (Calendar)parse(value, pattern, (Locale)null, timeZone);
    }

    /**
     * <p>Validate/convert a time using the specified pattern and <code>Locale</code>
     *    and the default <code>TimeZone</code>.
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to validate the value against, or the
     *        default for the <code>Locale</code> if <code>null</code>.
     * @param locale The locale to use for the date format, system default if null.
     * @return The parsed <code>Calendar</code> if valid or <code>null</code> if invalid.
     */
    public Calendar validate(String value, String pattern, Locale locale) { // definition of b
        return (Calendar)parse(value, pattern, locale, (TimeZone)null);
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
        result = validator.validate("18:01");
        assertNotNull("default result", result);
        ...
        result = null;

        // Default Locale, diff TimeZone
        result = validator.validate("16:49", EST);
        assertNotNull("zone result", result);
        ...
        result = null;

        // Pattern, diff TimeZone
        result = validator.validate("14-34", "HH-mm", EST); // call to a
        assertNotNull("pattern result", result);      // assertion fails here
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
        result = validator.validate("31/Dez/05 21-05", "dd/MMM/yy HH-mm", Locale.GERMAN); // call to b
        assertNotNull("pattern result", result);
        ...
        result = null;

    }

}
