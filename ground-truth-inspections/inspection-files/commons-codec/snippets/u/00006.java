public interface StringEncoder {
    /**
     * Encodes a String and returns a String.
     *
     * @param source
     *            the String to encode
     * @return the encoded String
     * @throws EncoderException
     *             thrown if there is an error condition during the encoding process.
     */
    String encode(String source) throws EncoderException; // a
}

public class Metaphone {
    /**
     * Encodes a String using the Metaphone algorithm.
     *
     * @param str String object to encode
     * @return The metaphone code corresponding to the String supplied
     */
    @Override
    public String encode(final String str) { // used implementation of a
        return metaphone(str); // call to b
    }

    /**
     * Find the metaphone value of a String. This is similar to the
     * soundex algorithm, but better at finding similar sounding words.
     * All input is converted to upper case.
     * Limitations: Input format is expected to be a single ASCII word
     * with only characters in the A - Z range, no punctuation or numbers.
     *
     * @param txt String to find the metaphone code for
     * @return A metaphone code corresponding to the String supplied
     */
    public String metaphone(final String txt) { // definition of b, shortened
        boolean hard = false;
        final int txtLength;
        if (txt == null || (txtLength = txt.length()) == 0) {
            return "";
        }
        // single character is itself
        if (txtLength == 1) {
            return txt.toUpperCase(java.util.Locale.ENGLISH);
        }

        final char[] inwd = txt.toUpperCase(java.util.Locale.ENGLISH).toCharArray();

        final StringBuilder local = new StringBuilder(40); // manipulate
        final StringBuilder code = new StringBuilder(10); //   output
        // handle initial 2 characters exceptions
        switch(inwd[0]) {
            ...
        default:
            local.append(inwd);
        } // now local has working string with initials fixed

        final int wdsz = local.length();
        int n = 0;

        while (code.length() < this.getMaxCodeLen() &&
               n < wdsz ) { // max code size of 4 works well
            final char symb = local.charAt(n);
            // remove duplicate letters except C
            if (symb != 'C' && isPreviousChar( local, n, symb ) ) {
                n++;
            } else { // not dup
                switch(symb) {
                case 'A':
                case 'E':
                case 'I':
                case 'O':
                case 'U':
                    if (n == 0) {
                        code.append(symb);
                    }
                    break; // only use vowel if leading char
                case 'B':
                    if ( isPreviousChar(local, n, 'M') &&
                         isLastChar(wdsz, n) ) { // B is silent if word ends in MB
                        break;
                    }
                    code.append(symb);
                    break;
                case 'C': // lots of C special cases
                    /* discard if SCI, SCE or SCY */
                    ...
                    break;
                case 'D':
                    if (!isLastChar(wdsz, n + 1) &&
                        isNextChar(local, n, 'G') &&
                        FRONTV.indexOf(local.charAt(n + 2)) >= 0) { // DGE DGI DGY -> J
                        code.append('J'); n += 2;
                    } else {
                        code.append('T');
                    }
                    break;
                case 'G': // GH silent at end or before consonant
                    ...
                    break;
                case 'H':
                    if (isLastChar(wdsz, n)) {
                        break; // terminal H
                    }
                    if (n > 0 &&
                        VARSON.indexOf(local.charAt(n - 1)) >= 0) {
                        break;
                    }
                    if (isVowel(local,n+1)) {
                        code.append('H'); // Hvowel
                    }
                    break;
                case 'F':
                case 'J':
                case 'L':
                case 'M':
                case 'N':
                case 'R':
                    code.append(symb);
                    break;
                case 'K':
                    if (n > 0) { // not initial
                        if (!isPreviousChar(local, n, 'C')) {
                            code.append(symb);
                        }
                    } else {
                        code.append(symb); // initial K
                    }
                    break;
                case 'P':
                    if (isNextChar(local,n,'H')) {
                        // PH -> F
                        code.append('F');
                    } else {
                        code.append(symb);
                    }
                    break;
                case 'Q':
                    code.append('K');
                    break;
                case 'S':
                    ...
                    break;
                case 'T':
                    ...
                    break;
                case 'V':
                    code.append('F'); break;
                case 'W':
                case 'Y': // silent if not followed by vowel
                    if (!isLastChar(wdsz,n) &&
                        isVowel(local,n+1)) {
                        code.append(symb);
                    }
                    break;
                case 'X':
                    code.append('K');
                    code.append('S');
                    break;
                case 'Z':
                    code.append('S');
                    break;
                default:
                    // do nothing
                    break;
                } // end switch
                n++;
            } // end else from symb != 'C'
            if (code.length() > this.getMaxCodeLen()) {
                code.setLength(this.getMaxCodeLen());
            }
        }
        return code.toString();
    }

}

public abstract class StringEncoderAbstractTest {
    @Test
    public void testEncodeNull() throws Exception {
        final StringEncoder encoder = this.getStringEncoder();
        try {
            encoder.encode(null); // call to a
            // NOTE: NO fail() HERE!!!
        } catch (final EncoderException ee) {
            // An exception should be thrown
        }
    }
}

public class MetaphoneTest extends StringEncoderAbstractTest<Metaphone> {
    ...
}
