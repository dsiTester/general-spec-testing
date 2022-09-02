public class TimeValidator extends AbstractCalendarValidator {
    public Calendar validate(String value, String pattern) { // called from test
        return (Calendar)parse(value, pattern, (Locale)null, (TimeZone)null); // call to a
    }

    /**
     * <p>Convert the parsed <code>Date</code> to a <code>Calendar</code>.</p>
     *
     * @param value The parsed <code>Date</code> object created.
     * @param formatter The Format used to parse the value with.
     * @return The parsed value converted to a <code>Calendar</code>.
     */
    @Override
    protected Object processParsedValue(Object value, Format formatter) { // used implementation of b
        return ((DateFormat)formatter).getCalendar();
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
    protected Object parse(String value, String pattern, Locale locale, TimeZone timeZone) { // definition of a

        value = (value == null ? null : value.trim());
        if (value == null || value.isEmpty()) {
            return null;
        }
        DateFormat formatter = (DateFormat)getFormat(pattern, locale);
        if (timeZone != null) {
            formatter.setTimeZone(timeZone);
        }
        return parse(value, formatter); // calls b

    }
}

public abstract class AbstractFormatValidator implements Serializable {

    protected Object parse(String value, Format formatter) { // called from a

        ParsePosition pos = new ParsePosition(0);
        Object parsedValue = formatter.parseObject(value, pos);
        if (pos.getErrorIndex() > -1) {
            return null;
        }

        if (isStrict() && pos.getIndex() < value.length()) {
            return null;
        }

        if (parsedValue != null) {
            parsedValue = processParsedValue(parsedValue, formatter); // call to b
        }

        return parsedValue;

    }

    /**
     * <p>Process the parsed value, performing any further validation
     *    and type conversion required.</p>
     *
     * @param value The parsed object created.
     * @param formatter The Format used to parse the value with.
     * @return The parsed value converted to the appropriate type
     *         if valid or <code>null</code> if invalid.
     */
    protected abstract Object processParsedValue(Object value, Format formatter); // b
}

public class TimeValidatorTest extends TestCase {

    /**
     * Test Invalid Dates with "locale" validation
     */
    public void testFormat() {
        // Set the default Locale
        Locale.setDefault(Locale.UK);

        Object test = TimeValidator.getInstance().validate("16:49:23", "HH:mm:ss"); // calls a
        assertNotNull("Test Date ", test);
        assertEquals("Format pattern", "16-49-23", validator.format(test, "HH-mm-ss"));
        assertEquals("Format locale",  "4:49 PM",  validator.format(test, Locale.US));
        assertEquals("Format default", "16:49",  validator.format(test));

    }

}
