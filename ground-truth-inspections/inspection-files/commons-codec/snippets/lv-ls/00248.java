public class MatchRatingApproachEncoder {

    /**
     * Obtains the min rating of the length sum of the 2 names. In essence the larger the sum length the smaller the
     * min rating. Values strictly from documentation.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param sumLength
     *            The length of 2 strings sent down
     * @return The min rating value
     */
    int getMinRating(final int sumLength) { // definition of a
        int minRating = 0;

        if (sumLength <= FOUR) {
            minRating = FIVE;
        } else if (sumLength <= SEVEN) { // aready know it is at least 5
            minRating = FOUR;
        } else if (sumLength <= ELEVEN) { // aready know it is at least 8
            minRating = THREE;
        } else if (sumLength == TWELVE) {
            minRating = TWO;
        } else {
            minRating = ONE; // docs said little here.
        }

        return minRating;
    }

    /**
     * Processes the names from left to right (first) then right to left removing identical letters in same positions.
     * Then subtracts the longer string that remains from 6 and returns this.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param name1
     *            name2
     * @return the length as above
     */
    int leftToRightThenRightToLeftProcessing(final String name1, final String name2) { // definition of b
        final char[] name1Char = name1.toCharArray();
        final char[] name2Char = name2.toCharArray();

        final int name1Size = name1.length() - 1;
        final int name2Size = name2.length() - 1;

        String name1LtRStart = EMPTY;
        String name1LtREnd = EMPTY;

        String name2RtLStart = EMPTY;
        String name2RtLEnd = EMPTY;

        for (int i = 0; i < name1Char.length; i++) {
            if (i > name2Size) {
                break;
            }

            name1LtRStart = name1.substring(i, i + 1);
            name1LtREnd = name1.substring(name1Size - i, name1Size - i + 1);

            name2RtLStart = name2.substring(i, i + 1);
            name2RtLEnd = name2.substring(name2Size - i, name2Size - i + 1);

            // Left to right...
            if (name1LtRStart.equals(name2RtLStart)) {
                name1Char[i] = ' ';
                name2Char[i] = ' ';
            }

            // Right to left...
            if (name1LtREnd.equals(name2RtLEnd)) {
                name1Char[name1Size - i] = ' ';
                name2Char[name2Size - i] = ' ';
            }
        }

        // Char arrays -> string & remove extraneous space
        final String strA = new String(name1Char).replaceAll("\\s+", EMPTY);
        final String strB = new String(name2Char).replaceAll("\\s+", EMPTY);

        // Final bit - subtract longest string from 6 and return this int value
        if (strA.length() > strB.length()) {
            return Math.abs(SIX - strA.length());
        }
        return Math.abs(SIX - strB.length());
    }

    public boolean isEncodeEquals(String name1, String name2) { // called from test
        ...
        // Preprocessing
        name1 = cleanName(name1);
        name2 = cleanName(name2);

        // Actual MRA Algorithm

        // 1. Remove vowels
        name1 = removeVowels(name1);
        name2 = removeVowels(name2);

        // 2. Remove double consonants
        name1 = removeDoubleConsonants(name1);
        name2 = removeDoubleConsonants(name2);

        // 3. Reduce down to 3 letters
        name1 = getFirst3Last3(name1);
        name2 = getFirst3Last3(name2);

        // 4. Check for length difference - if 3 or greater then no similarity
        // comparison is done
        if (Math.abs(name1.length() - name2.length()) >= THREE) {
            return false;
        }

        // 5. Obtain the minimum rating value by calculating the length sum of the
        // encoded Strings and sending it down.
        final int sumLength = Math.abs(name1.length() + name2.length());
        final int minRating = getMinRating(sumLength); // call to a

        // 6. Process the encoded Strings from left to right and remove any
        // identical characters found from both Strings respectively.
        final int count = leftToRightThenRightToLeftProcessing(name1, name2); // call to b

        // 7. Each PNI item that has a similarity rating equal to or greater than
        // the min is considered to be a good candidate match
        return count >= minRating;

    }

}

public class MatchRatingApproachEncoderTest {
    @Test
    public final void testCompare_BRIAN_BRYAN_SuccessfullyMatched() { // validated test
        assertTrue(this.getStringEncoder().isEncodeEquals("Brian", "Bryan")); // calls a and b
    }

    @Test
    public final void testCompare_SAM_SAMUEL_SuccessfullyMatched() { // invalidated test
        assertTrue(this.getStringEncoder().isEncodeEquals("Sam", "Samuel")); // calls a and b
    }
}
