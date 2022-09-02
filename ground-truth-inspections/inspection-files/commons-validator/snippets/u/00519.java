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

    /**
     * <p>Returns a <code>DateFormat</code> for the specified Locale.</p>
     *
     * @param locale The locale a <code>DateFormat</code> is required for,
     *        system default if null.
     * @return The <code>DateFormat</code> to created.
     */
    protected Format getFormat(Locale locale) { // definition of b

        DateFormat formatter = null;
        if (dateStyle >= 0 && timeStyle >= 0) {
            if (locale == null) {
                formatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle);
            } else {
                formatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
            }
        } else if (timeStyle >= 0) {
            if (locale == null) {
                formatter = DateFormat.getTimeInstance(timeStyle);
            } else {
                formatter = DateFormat.getTimeInstance(timeStyle, locale);
            }
        } else {
            int useDateStyle = dateStyle >= 0 ? dateStyle : DateFormat.SHORT;
            if (locale == null) {
                formatter = DateFormat.getDateInstance(useDateStyle);
            } else {
                formatter = DateFormat.getDateInstance(useDateStyle, locale);
            }
        }
        formatter.setLenient(false);
        return formatter;

    }

    @Override
    protected Format getFormat(String pattern, Locale locale) { // called from AbstractFormatValidator.format(Object, String, Locale)
        DateFormat formatter = null;
        boolean usePattern = pattern != null && !pattern.isEmpty();
        if (!usePattern) {
            formatter = (DateFormat)getFormat(locale); // call to b
        } else if (locale == null) {
            formatter = new SimpleDateFormat(pattern);
        } else {
            DateFormatSymbols symbols = new DateFormatSymbols(locale);
            formatter = new SimpleDateFormat(pattern, symbols);
        }
        formatter.setLenient(false);
        return formatter;
    }

}

public abstract class AbstractFormatValidator implements Serializable {

    public String format(Object value, Locale locale) { // called from test
        return format(value, (String)null, locale);     // calls b
    }

    public String format(Object value, String pattern, Locale locale) { // called from above
        Format formatter = getFormat(pattern, locale); // calls b
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

        // NOTE: commenting out the below **technically** calls b before a
        // System.out.println(validator.format(new Date(), Locale.US));;
        // Create a Date or Calendar
        Object test = validator.parse("2005-11-28", "yyyy-MM-dd", null, null); // call to a
        assertNotNull("Test Date ", test); // assertion fails here
        assertEquals("Format pattern", "28.11.05", validator.format(test, "dd.MM.yy"));
        assertEquals("Format locale",  "11/28/05", validator.format(test, Locale.US)); // calls b
    }

}
