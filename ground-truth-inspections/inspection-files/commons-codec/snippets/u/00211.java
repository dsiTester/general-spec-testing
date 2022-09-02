public class DigestUtils {
    /**
     * Reads through a byte array and returns the digest for the data.
     *
     * @param data
     *            Data to digest
     * @return the digest as a hex string
     * @since 1.11
     */
    public String digestAsHex(final byte[] data) { // definition of a
        return Hex.encodeHexString(digest(data));  // call to b
    }

    /**
     * Reads through a byte array and returns the digest for the data.
     *
     * @param data
     *            Data to digest
     * @return the digest
     * @since 1.11
     */
    public byte[] digest(final byte[] data) { // definition of b
        return updateDigest(messageDigest, data).digest();
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
        assertEquals(expected, digestUtils.digestAsHex(new File(pathname)));
        try (final FileInputStream inputStream = new FileInputStream(pathname)) {
            assertEquals(expected, digestUtils.digestAsHex(inputStream));
        }
        final byte[] allBytes = Files.readAllBytes(Paths.get(pathname));
        assertEquals(expected, digestUtils.digestAsHex(allBytes)); // call to a
        assertEquals(expected, digestUtils.digestAsHex(ByteBuffer.wrap(allBytes)));
    }

}
