public class Metaphone implements StringEncoder {

    public String metaphone(final String txt) { // definition of a, shortened.
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

    private boolean regionMatch(final StringBuilder string, final int index, final String test) { // definition of b
        boolean matches = false;
        if( index >= 0 &&
            index + test.length() - 1 < string.length() ) {
            final String substring = string.substring( index, index + test.length());
            matches = substring.equals( test );
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
