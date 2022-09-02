public class DigestUtils {
    /**
     * Reads through an InputStream and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @return the digest as a hex string
     * @throws IOException
     *             On error reading from the stream
     * @since 1.11
     */
    public String digestAsHex(final InputStream data) throws IOException { // definition of a
        return Hex.encodeHexString(digest(data));
    }

    /**
     * Reads through a ByteBuffer and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @return the digest as a hex string
     *
     * @since 1.11
     */
    public String digestAsHex(final ByteBuffer data) { // definition of b
        return Hex.encodeHexString(digest(data));
    }


}

public class DigestUtilsTest {
    @Test
    public void testSha224_FileAsHex() throws IOException {
        assumeJava8();
        final String expected = "d14a028c2a3a2bc9476102bb288234c415a2b01f828ea62ac5b3e42f";
        final String pathname = "src/test/resources/org/apache/commons/codec/empty.bin";
        final String algo = MessageDigestAlgorithms.SHA_224;
        final DigestUtils digestUtils = new DigestUtils(algo);
        assertEquals(expected, digestUtils.digestAsHex(new File(pathname))); // call to a; assertion fails here
        try (final FileInputStream inputStream = new FileInputStream(pathname)) {
            assertEquals(expected, digestUtils.digestAsHex(inputStream));
        }
        final byte[] allBytes = Files.readAllBytes(Paths.get(pathname));
        assertEquals(expected, digestUtils.digestAsHex(allBytes));
        assertEquals(expected, digestUtils.digestAsHex(ByteBuffer.wrap(allBytes))); // call to b
    }

}
