public class Metaphone implements StringEncoder {
    private boolean regionMatch(final StringBuilder string, final int index, final String test) { // definition of a
        boolean matches = false;
        if( index >= 0 &&
            index + test.length() - 1 < string.length() ) {
            final String substring = string.substring( index, index + test.length());
            matches = substring.equals( test );
        }
        return matches;
    }

    private boolean isNextChar(final StringBuilder string, final int index, final char c) { // definition of b
        boolean matches = false;
        if( index >= 0 &&
            index < string.length() - 1 ) {
            matches = string.charAt(index + 1) == c;
        }
        return matches;
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
                    /* discard if SCI, SCE or SCY */
                    if ( isPreviousChar(local, n, 'S') &&
                         !isLastChar(wdsz, n) &&
                         FRONTV.indexOf(local.charAt(n + 1)) >= 0 ) {
                        break;
                    }
                    if (regionMatch(local, n, "CIA")) { // "CIA" -> X - call to a
                        code.append('X');
                        break;
                    }
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
                }
            }
            ...
        }
        ...
    }
}

public class MetaphoneTest extends StringEncoderAbstractTest<Metaphone> {
    @Test
    public void testWordsWithCIA() {
        assertEquals( "XP", this.getStringEncoder().metaphone("CIAPO") );
    }
}
