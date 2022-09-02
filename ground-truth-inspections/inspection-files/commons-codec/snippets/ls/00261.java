public class Metaphone {
    private boolean isNextChar(final StringBuilder string, final int index, final char c) { // definition of a
        boolean matches = false;
        if( index >= 0 &&
            index < string.length() - 1 ) {
            matches = string.charAt(index + 1) == c;
        }
        return matches;
    }

    private boolean isVowel(final StringBuilder string, final int index) { // definition of b
        return VOWELS.indexOf(string.charAt(index)) >= 0;
    }

    public String metaphone(final String txt) {
        ...
        while (code.length() < this.getMaxCodeLen() &&
               n < wdsz ) { // max code size of 4 works well
            final char symb = local.charAt(n);
            // remove duplicate letters except C
            if (symb != 'C' && isPreviousChar( local, n, symb ) ) {
                n++;
            } else { // not dup
                switch(symb) {
                ...
                case 'C': // lots of C special cases
                    ...
                    if (isNextChar(local, n, 'H')) { // detect CH - call to a
                        if (n == 0 &&
                            wdsz >= 3 &&
                            isVowel(local,2) ) { // CH consonant -> K consonant
                            code.append('K');
                        } else {
                            code.append('X'); // CHvowel -> X
                        }
                    } else {
                        code.append('K');
                    }
                    break;
                ...
                case 'W':
                case 'Y': // silent if not followed by vowel
                    if (!isLastChar(wdsz,n) &&
                        isVowel(local,n+1)) { // call to b
                        code.append(symb);
                    }
                    break;
                ...
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

public class MetaphoneTest extends StringEncoderAbstractTest<Metaphone> {
    @Test
    public void testWordEndingInMB() {
        assertEquals( "KM", this.getStringEncoder().metaphone("COMB") ); // calls a?
        assertEquals( "TM", this.getStringEncoder().metaphone("TOMB") );
        assertEquals( "WM", this.getStringEncoder().metaphone("WOMB") ); // calls b?
    }
}
