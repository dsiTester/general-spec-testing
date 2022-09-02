public class DaitchMokotoffSoundex {
    /**
     * Inner class representing a branch during DM soundex encoding.
     */
    private static final class Branch {

        /**
         * Process the next replacement to be added to this branch.
         *
         * @param replacement
         *            the next replacement to append
         * @param forceAppend
         *            indicates if the default processing shall be overridden
         */
        public void processNextReplacement(final String replacement, final boolean forceAppend) { // definition of a
            final boolean append = lastReplacement == null || !lastReplacement.endsWith(replacement) || forceAppend;

            if (append && builder.length() < MAX_LENGTH) {
                builder.append(replacement);
                // remove all characters after the maximum length
                if (builder.length() > MAX_LENGTH) {
                    builder.delete(MAX_LENGTH, builder.length());
                }
                cachedString = null;
            }

            lastReplacement = replacement;
        }
    }

    /**
     * Finish this branch by appending '0's until the maximum code length has been reached.
     */
    public void finish() {      // definition of b
        while (builder.length() < MAX_LENGTH) {
            builder.append('0');
            cachedString = null;
        }
    }


    private String[] soundex(final String source, final boolean branching) {
        ...
        for (int index = 0; index < input.length(); index++) {
            ...
            // use an EMPTY_LIST to avoid false positive warnings wrt potential null pointer access
            final List<Branch> nextBranches = branching ? new ArrayList<Branch>() : Collections.<Branch>emptyList();

            for (final Rule rule : rules) {
                if (rule.matches(inputContext)) {
                    ...
                    final String[] replacements = rule.getReplacements(inputContext, lastChar == '\0');
                    ...
                    for (final Branch branch : currentBranches) {
                        for (final String nextReplacement : replacements) {
                            // if we have multiple replacements, always create a new branch
                            final Branch nextBranch = branchingRequired ? branch.createBranch() : branch;

                            // special rule: occurrences of mn or nm are treated differently
                            final boolean force = (lastChar == 'm' && ch == 'n') || (lastChar == 'n' && ch == 'm');

                            nextBranch.processNextReplacement(nextReplacement, force); // call to a

                            if (!branching) {
                                break;
                            }
                            nextBranches.add(nextBranch);
                        }
                    }

                    ...
                    break;
                }
            }

            lastChar = ch;
        }

        final String[] result = new String[currentBranches.size()];
        int index = 0;
        for (final Branch branch : currentBranches) {
            branch.finish();    // call to b
            result[index++] = branch.toString();
        }

        return result;
    }


}

public abstract class StringEncoderAbstractTest<T extends StringEncoder> {
    @Test
    public void testLocaleIndependence() throws Exception {
        final StringEncoder encoder = this.getStringEncoder();

        final String[] data = {"I", "i",};

        final Locale orig = Locale.getDefault();
        final Locale[] locales = {Locale.ENGLISH, new Locale("tr"), Locale.getDefault()};

        try {
            for (final String element : data) {
                String ref = null;
                for (int j = 0; j < locales.length; j++) {
                    Locale.setDefault(locales[j]);
                    if (j <= 0) {
                        ref = encoder.encode(element);
                    } else {
                        String cur = null;
                        try {
                            cur = encoder.encode(element);
                        } catch (final Exception e) {
                            Assert.fail(Locale.getDefault().toString() + ": " + e.getMessage());
                        }
                        Assert.assertEquals(Locale.getDefault().toString() + ": ", ref, cur);
                    }
                }
            }
        } finally {
            Locale.setDefault(orig);
        }
    }

}

public class DaitchMokotoffSoundexTest extends StringEncoderAbstractTest<DaitchMokotoffSoundex> {
    ...
}
