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
                case 'P':
                    if (isNextChar(local,n,'H')) { // call to b
                        // PH -> F
                        code.append('F');
                    } else {
                        code.append(symb);
                    }
                    break;
                ...
                case 'S':
                    if (regionMatch(local,n,"SH") ||
                        regionMatch(local,n,"SIO") ||
                        regionMatch(local,n,"SIA")) {
                        code.append('X');
                    } else {
                        code.append('S');
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


    private boolean isNextChar(final StringBuilder string, final int index, final char c) { // definition of b
        boolean matches = false;
        if( index >= 0 &&
            index < string.length() - 1 ) {
            matches = string.charAt(index + 1) == c;
        }
        return matches;
    }
}

public class MetaphoneTest extends StringEncoderAbstractTest<Metaphone> {
    @Test
    public void testPHTOF() {
        assertEquals( "FX", this.getStringEncoder().metaphone("PHISH") );
    }
}
