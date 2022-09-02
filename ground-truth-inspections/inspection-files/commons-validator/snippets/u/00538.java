public abstract class AbstractCalendarValidator extends AbstractFormatValidator {
    /**
     * <p>Format an object using the specified pattern and/or
     *    <code>Locale</code>.
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to format the value.
     * @param locale The locale to use for the Format.
     * @return The value formatted as a <code>String</code>.
     */
    @Override
    public String format(Object value, String pattern, Locale locale) { // definition of a
        return format(value, pattern, locale, (TimeZone)null); // call to "b"
    }

    /**
     * <p>Format an object using the specified pattern and/or
     *    <code>Locale</code>.
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to format the value.
     * @param locale The locale to use for the Format.
     * @param timeZone The Time Zone used to format the date,
     *  system default if null (unless value is a <code>Calendar</code>.
     * @return The value formatted as a <code>String</code>.
     */
    public String format(Object value, String pattern, Locale locale, TimeZone timeZone) { // definition of b; also called from method-a
        DateFormat formatter = (DateFormat)getFormat(pattern, locale);
        if (timeZone != null) {
            formatter.setTimeZone(timeZone);
        } else if (value instanceof Calendar) {
            formatter.setTimeZone(((Calendar)value).getTimeZone());
        }
        return format(value, formatter);
    }
}

public class CalendarValidatorTest extends AbstractCalendarValidatorTest {
    /**
     * Test format methods
     */
    @Override
    public void testFormat() {
        // Set the default Locale
        Locale origDefault = Locale.getDefault();
        Locale.setDefault(Locale.UK);

        Calendar cal20050101 = createCalendar(GMT, 20051231, 11500);
        assertNull("null", calValidator.format(null));
        assertEquals("default",  "31/12/05",         calValidator.format(cal20050101));
        assertEquals("locale",   "12/31/05",         calValidator.format(cal20050101, Locale.US));
        assertEquals("patternA", "2005-12-31 01:15", calValidator.format(cal20050101, "yyyy-MM-dd HH:mm"));
        assertEquals("patternB", "2005-12-31 GMT",   calValidator.format(cal20050101, "yyyy-MM-dd z"));
        assertEquals("both",     "31 Dez 2005",      calValidator.format(cal20050101, "dd MMM yyyy", Locale.GERMAN)); // call to a

        // EST Time Zone
        assertEquals("EST default",  "30/12/05",         calValidator.format(cal20050101, EST));
        assertEquals("EST locale",   "12/30/05",         calValidator.format(cal20050101, Locale.US, EST));
        assertEquals("EST patternA", "2005-12-30 20:15", calValidator.format(cal20050101, "yyyy-MM-dd HH:mm", EST));
        assertEquals("EST patternB", "2005-12-30 EST",   calValidator.format(cal20050101, "yyyy-MM-dd z", EST));
        assertEquals("EST both",     "30 Dez 2005",      calValidator.format(cal20050101, "dd MMM yyyy", Locale.GERMAN, EST)); // call to b

        // Restore the original default
        Locale.setDefault(origDefault);
    }

}
