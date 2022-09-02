public class DigestUtils {
    /**
     * Reads through a byte array and returns the digest for the data.
     *
     * @param data
     *            Data to digest treated as UTF-8 string
     * @return the digest as a hex string
     * @since 1.11
     */
    public String digestAsHex(final String data) { // definition of a
        return Hex.encodeHexString(digest(data));  // call to b
    }

    /**
     * Reads through a byte array and returns the digest for the data.
     *
     * @param data
     *            Data to digest treated as UTF-8 string
     * @return the digest
     * @since 1.11
     */
    public byte[] digest(final String data) { // definition of b
        return updateDigest(messageDigest, data).digest();
    }

}

public class DigestUtilsTest {
    @Test
    public void testSha224_StringAsHex() {
        assumeJava8();
        assertEquals("d14a028c2a3a2bc9476102bb288234c415a2b01f828ea62ac5b3e42f",
                new DigestUtils(MessageDigestAlgorithms.SHA_224).digestAsHex(EMPTY_STRING)); // call to a
        assertEquals("730e109bd7a8a32b1cb9d9a09aa2325d2430587ddbc0c38bad911525",
                new DigestUtils(MessageDigestAlgorithms.SHA_224).digestAsHex("The quick brown fox jumps over the lazy dog")); // call to a

        // Examples from FIPS 180-4?
    }
}
