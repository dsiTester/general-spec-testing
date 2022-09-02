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
        return (Calendar)parse(value, pattern, (Locale)null, (TimeZone)null); // calls b
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

    protected Object parse(String value, String pattern, Locale locale, TimeZone timeZone) { // called from a

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

    protected Object parse(String value, Format formatter) { // called from AbstractCalendarValidator.parse()

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

        Object test = TimeValidator.getInstance().validate("16:49:23", "HH:mm:ss"); // call to a
        assertNotNull("Test Date ", test);
        assertEquals("Format pattern", "16-49-23", validator.format(test, "HH-mm-ss"));
        assertEquals("Format locale",  "4:49 PM",  validator.format(test, Locale.US));
        assertEquals("Format default", "16:49",  validator.format(test));

    }

}
