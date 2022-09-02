public class Metaphone implements StringEncoder {

    private boolean isNextChar(final StringBuilder string, final int index, final char c) { // definition of a
        boolean matches = false;
        if( index >= 0 &&
            index < string.length() - 1 ) {
            matches = string.charAt(index + 1) == c;
        }
        return matches;
    }

    private boolean regionMatch(final StringBuilder string, final int index, final String test) { // definition of b
        boolean matches = false;
        if( index >= 0 &&
            index + test.length() - 1 < string.length() ) {
            final String substring = string.substring( index, index + test.length());
            matches = substring.equals( test );
        }
        return matches;
    }

    public String metaphone(final String txt) {
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
                    if (isNextChar(local,n,'H')) { // call to a
                        // PH -> F
                        code.append('F');
                    } else {
                        code.append(symb);
                    }
                    break;
                ...
                case 'S':
                    if (regionMatch(local,n,"SH") || // call to b
                        regionMatch(local,n,"SIO") || // not called bc short circuit
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
}

public class MetaphoneTest extends StringEncoderAbstractTest<Metaphone> {
    @Test
    public void testPHTOF() {
        assertEquals( "FX", this.getStringEncoder().metaphone("PHISH") );
    }
}
