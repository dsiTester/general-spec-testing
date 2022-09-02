public class Metaphone implements StringEncoder {
    /**
     * The max code length for metaphone is 4
     */
    private int maxCodeLen = 4; // resource that method-a changes

    /**
     * Sets the maxCodeLen.
     * @param maxCodeLen The maxCodeLen to set
     */
    public void setMaxCodeLen(final int maxCodeLen) { this.maxCodeLen = maxCodeLen; } // definition of a

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
    public String metaphone(final String txt) { // definition of b, reduced.
        ...
        while (code.length() < this.getMaxCodeLen() && // this is where the resource changed via a is important
               n < wdsz ) { // max code size of 4 works well
            final char symb = local.charAt(n);
            // remove duplicate letters except C
            if (symb != 'C' && isPreviousChar( local, n, symb ) ) {
                n++;
            } else { // not dup
                switch(symb) {
                ...
                }
            }
            ...
        }
        ...
    }

    /**
     * Returns the maxCodeLen.
     * @return int
     */
    public int getMaxCodeLen() { return this.maxCodeLen; }

}

public class MetaphoneTest extends StringEncoderAbstractTest<Metaphone> {
    @Test
    public void testSetMaxLengthWithTruncation() {
        // should be AKSKS, but istruncated by Max Code Length
        this.getStringEncoder().setMaxCodeLen( 6 ); // call to a
        assertEquals( "AKSKSK", this.getStringEncoder().metaphone("AXEAXEAXE") ); // call to b
    }
}
