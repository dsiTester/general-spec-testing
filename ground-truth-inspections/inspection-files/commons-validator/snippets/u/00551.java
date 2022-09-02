public class CalendarValidator extends AbstractCalendarValidator {
    /**
     * <p>Validate/convert a <code>Calendar</code> using the specified
     *    <code>TimeZone</code> and default <code>Locale</code>.
     *
     * @param value The value validation is being performed on.
     * @param timeZone The Time Zone used to parse the date, system default if null.
     * @return The parsed <code>Calendar</code> if valid or <code>null</code>
     *  if invalid.
     */
    public Calendar validate(String value, TimeZone timeZone) { // definition of a
        return (Calendar)parse(value, (String)null, (Locale)null, timeZone);
    }

    /**
     * <p>Validate/convert a <code>Calendar</code> using the specified
     *    pattern, and <code>Locale</code> and <code>TimeZone</code>.
     *
     * @param value The value validation is being performed on.
     * @param pattern The pattern used to validate the value against, or the
     *        default for the <code>Locale</code> if <code>null</code>.
     * @param locale The locale to use for the date format, system default if null.
     * @param timeZone The Time Zone used to parse the date, system default if null.
     * @return The parsed <code>Calendar</code> if valid or <code>null</code> if invalid.
     */
    public Calendar validate(String value, String pattern, Locale locale, TimeZone timeZone) { // definition of b
        return (Calendar)parse(value, pattern, locale, timeZone);
    }

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

public class CalendarValidatorTest extends AbstractCalendarValidatorTest {
    public void testCalendarValidatorMethods() {
        Locale.setDefault(Locale.US);
        Locale locale     = Locale.GERMAN;
        String pattern    = "yyyy-MM-dd";
        String patternVal = "2005-12-31";
        String germanVal     = "31 Dez 2005";
        String germanPattern = "dd MMM yyyy";
        String localeVal  = "31.12.2005";
        String defaultVal = "12/31/05";
        String XXXX    = "XXXX";
        Date expected = createCalendar(null, 20051231, 0).getTime();
        assertEquals("validate(A) default", expected, CalendarValidator.getInstance().validate(defaultVal).getTime());
        assertEquals("validate(A) locale ", expected, CalendarValidator.getInstance().validate(localeVal, locale).getTime());
        assertEquals("validate(A) pattern", expected, CalendarValidator.getInstance().validate(patternVal, pattern).getTime());
        assertEquals("validate(A) both",    expected, CalendarValidator.getInstance().validate(germanVal, germanPattern, Locale.GERMAN).getTime());

        assertTrue("isValid(A) default", CalendarValidator.getInstance().isValid(defaultVal));
        assertTrue("isValid(A) locale ", CalendarValidator.getInstance().isValid(localeVal, locale));
        assertTrue("isValid(A) pattern", CalendarValidator.getInstance().isValid(patternVal, pattern));
        assertTrue("isValid(A) both",    CalendarValidator.getInstance().isValid(germanVal, germanPattern, Locale.GERMAN));

        assertNull("validate(B) default", CalendarValidator.getInstance().validate(XXXX));
        assertNull("validate(B) locale ", CalendarValidator.getInstance().validate(XXXX, locale));
        assertNull("validate(B) pattern", CalendarValidator.getInstance().validate(XXXX, pattern));
        assertNull("validate(B) both",    CalendarValidator.getInstance().validate("31 Dec 2005", germanPattern, Locale.GERMAN));

        assertFalse("isValid(B) default", CalendarValidator.getInstance().isValid(XXXX));
        assertFalse("isValid(B) locale ", CalendarValidator.getInstance().isValid(XXXX, locale));
        assertFalse("isValid(B) pattern", CalendarValidator.getInstance().isValid(XXXX, pattern));
        assertFalse("isValid(B) both",    CalendarValidator.getInstance().isValid("31 Dec 2005", germanPattern, Locale.GERMAN));

        // Test Time Zone
        TimeZone zone = (TimeZone.getDefault().getRawOffset() == EET.getRawOffset() ? EST : EET);
        Date expectedZone = createCalendar(zone, 20051231, 0).getTime();
        assertFalse("default/EET same ", expected.getTime() == expectedZone.getTime());

        assertEquals("validate(C) default", expectedZone, CalendarValidator.getInstance().validate(defaultVal, zone).getTime()); // call to a
        assertEquals("validate(C) locale ", expectedZone, CalendarValidator.getInstance().validate(localeVal, locale, zone).getTime());
        assertEquals("validate(C) pattern", expectedZone, CalendarValidator.getInstance().validate(patternVal, pattern, zone).getTime());
        assertEquals("validate(C) both",    expectedZone, CalendarValidator.getInstance().validate(germanVal, germanPattern, Locale.GERMAN, zone).getTime()); // call to b
    }
}
