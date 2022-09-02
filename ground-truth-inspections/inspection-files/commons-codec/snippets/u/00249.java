public class MatchRatingApproachEncoder {

    /**
     * Determines if two names are homophonous via Match Rating Approach (MRA) algorithm. It should be noted that the
     * strings are cleaned in the same way as {@link #encode(String)}.
     *
     * @param name1
     *            First of the 2 strings (names) to compare
     * @param name2
     *            Second of the 2 names to compare
     * @return {@code true} if the encodings are identical {@code false} otherwise.
     */
    public boolean isEncodeEquals(String name1, String name2) { // definition of a
        ...
        // 5. Obtain the minimum rating value by calculating the length sum of the
        // encoded Strings and sending it down.
        final int sumLength = Math.abs(name1.length() + name2.length());
        final int minRating = getMinRating(sumLength); // call to b

        // 6. Process the encoded Strings from left to right and remove any
        // identical characters found from both Strings respectively.
        final int count = leftToRightThenRightToLeftProcessing(name1, name2);

        // 7. Each PNI item that has a similarity rating equal to or greater than
        // the min is considered to be a good candidate match
        return count >= minRating;

    }

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
    int getMinRating(final int sumLength) { // definition of b
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


}

public class MatchRatingApproachEncoderTest {
    @Test
    public final void testCompare_BRIAN_BRYAN_SuccessfullyMatched() {
        assertTrue(this.getStringEncoder().isEncodeEquals("Brian", "Bryan")); // call to a
    }

}
