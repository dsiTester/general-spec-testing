public class DigestUtils {
    /**
     * Reads through a File and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @param options
     *            options How to open the file
     * @return the digest as a hex string
     * @throws IOException
     *             On error reading from the stream
     * @since 1.11
     */
    public String digestAsHex(final Path data, final OpenOption... options) throws IOException { // definition of a
        return Hex.encodeHexString(digest(data, options)); // call to b
    }

    /**
     * Reads through a File and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @param options
     *            options How to open the file
     * @return the digest
     * @throws IOException
     *             On error reading from the stream
     * @since 1.14
     */
    public byte[] digest(final Path data, final OpenOption... options) throws IOException { // definition of b
        return updateDigest(messageDigest, data, options).digest();
    }
}

public class DigestUtilsTest {
    @Test
    public void testSha224_PathAsHex() throws IOException {
        assumeJava8();
        assertEquals("d14a028c2a3a2bc9476102bb288234c415a2b01f828ea62ac5b3e42f",
                new DigestUtils(MessageDigestAlgorithms.SHA_224).digestAsHex(Paths.get("src/test/resources/org/apache/commons/codec/empty.bin"))); // calls a
    }
}
