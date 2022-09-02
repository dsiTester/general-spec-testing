// method-a defined in TestCase, a third party class.

public abstract class AbstractCheckDigitTest extends TestCase {
    /**
     * Returns an array of codes with invalid check digits.
     *
     * @param codes Codes with valid check digits
     * @return Codes with invalid check digits
     */
    protected String[] createInvalidCodes(String[] codes) { // definition of b
        List<String> list = new ArrayList<>();

        // create invalid check digit values
        for (String fullCode : codes) {
            String code = removeCheckDigit(fullCode);
            String check = checkDigit(fullCode);
            for (int j = 0; j < POSSIBLE_CHECK_DIGITS.length(); j++) {
                String curr = POSSIBLE_CHECK_DIGITS.substring(j, j + 1);//"" + Character.forDigit(j, 10);
                if (!curr.equals(check)) {
                    list.add(code + curr);
                }
            }
        }

        return list.toArray(new String[list.size()]);
    }

    public void testIsValidFalse() {
        if (log.isDebugEnabled()) {
            log.debug("testIsValidFalse() for " + routine.getClass().getName());
        }

        // test invalid code values
        for (int i = 0; i < invalid.length; i++) {
            if (log.isDebugEnabled()) {
                log.debug("   " + i + " Testing Invalid Code=[" + invalid[i] + "]");
            }
            assertFalse("invalid[" + i +"]: " + invalid[i], routine.isValid(invalid[i]));
        }

        // test invalid check digit values
        String[] invalidCheckDigits = createInvalidCodes(valid); // call to b
        for (int i = 0; i < invalidCheckDigits.length; i++) {
            if (log.isDebugEnabled()) {
                log.debug("   " + i + " Testing Invalid Check Digit, Code=[" + invalidCheckDigits[i] + "]");
            }
            assertFalse("invalid check digit[" + i +"]: " + invalidCheckDigits[i], routine.isValid(invalidCheckDigits[i]));
        }
    }

}

public class ABANumberCheckDigitTest extends AbstractCheckDigitTest {
    /**
     * Set up routine & valid codes.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();          // call to a
        routine = ABANumberCheckDigit.ABAN_CHECK_DIGIT;
        valid = new String[] {
                "123456780",
                "123123123",
                "011000015",
                "111000038",
                "231381116",
                "121181976"
                };
    }


}
