public class DigestUtils {
    /**
     * Reads through a File and returns the digest for the data
     *
     * @param data
     *            Data to digest
     * @return the digest as a hex string
     * @throws IOException
     *             On error reading from the stream
     * @since 1.11
     */
    public String digestAsHex(final File data) throws IOException { // definition of a
        return Hex.encodeHexString(digest(data)); // call to b
    }

    /**
     * Converts an array of bytes into a String representing the hexadecimal values of each byte in order. The returned
     * String will be double the length of the passed array, as it takes two characters to represent any given byte.
     *
     * @param data a byte[] to convert to hex characters
     * @return A String containing lower-case hexadecimal characters
     * @since 1.4
     */
    public static String encodeHexString(final byte[] data) { // definition of b
        return new String(encodeHex(data));
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
        assertEquals(expected, digestUtils.digestAsHex(new File(pathname))); // call to a
        try (final FileInputStream inputStream = new FileInputStream(pathname)) {
            assertEquals(expected, digestUtils.digestAsHex(inputStream));
        }
        final byte[] allBytes = Files.readAllBytes(Paths.get(pathname));
        assertEquals(expected, digestUtils.digestAsHex(allBytes));
        assertEquals(expected, digestUtils.digestAsHex(ByteBuffer.wrap(allBytes)));
    }

}
