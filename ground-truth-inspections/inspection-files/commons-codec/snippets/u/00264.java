public class Metaphone implements StringEncoder {

    public String metaphone(final String txt) { // definition of a, shortened.
        while (code.length() < this.getMaxCodeLen() &&
               n < wdsz ) { // max code size of 4 works well
            final char symb = local.charAt(n);
            // remove duplicate letters except C
            if (symb != 'C' && isPreviousChar( local, n, symb ) ) {
                n++;
            } else { // not dup
                switch(symb) {
                ...
                case 'W':
                case 'Y': // silent if not followed by vowel
                    if (!isLastChar(wdsz,n) &&
                        isVowel(local,n+1)) { // call to b
                        code.append(symb);
                    }
                    break;
                ...
                }
                ...
            }
            ...
        }
        ...
    }

    private boolean isVowel(final StringBuilder string, final int index) { // definition of b
        return VOWELS.indexOf(string.charAt(index)) >= 0;
    }
}

public class MetaphoneTest extends StringEncoderAbstractTest<Metaphone> {
    @Test
    public void testWhy() {
        // PHP returns "H". The original metaphone returns an empty string.
        assertEquals("", this.getStringEncoder().metaphone("WHY"));
    }
}
