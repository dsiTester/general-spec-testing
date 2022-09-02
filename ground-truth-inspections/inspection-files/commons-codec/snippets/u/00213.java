public class HmacUtils {
    /**
     * Returns the digest for the stream.
     *
     * @param valueToDigest
     *            the data to use
     *            <p>
     *            The InputStream must not be null and will not be closed
     *            </p>
     * @return the digest as a hex String
     * @throws IOException
     *             If an I/O error occurs.
     * @since 1.11
     */
    public String hmacHex(final InputStream valueToDigest) throws IOException { // definition of a
        return Hex.encodeHexString(hmac(valueToDigest)); // call to b
    }

    /**
     * Returns the digest for the stream.
     *
     * @param valueToDigest
     *            the data to use
     *            <p>
     *            The InputStream must not be null and will not be closed
     *            </p>
     * @return the digest
     * @throws IOException
     *             If an I/O error occurs.
     * @since 1.11
     */
    public byte[] hmac(final InputStream valueToDigest) throws IOException { // definition of b
        final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
        int read;

        while ((read = valueToDigest.read(buffer, 0, STREAM_BUFFER_LENGTH) ) > -1) {
            mac.update(buffer, 0, read);
        }
        return mac.doFinal();
    }

    @Deprecated
    public static String hmacSha1Hex(final byte[] key, final InputStream valueToDigest) throws IOException {
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_1, key).hmacHex(valueToDigest); // call to a
    }

}

public class HmacUtilsTest {
    @SuppressWarnings("deprecation") // most of the static methods are deprecated
    @Test
    public void testHmacSha1Hex() throws IOException {
        assertEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_STRING, HmacUtils.hmacSha1Hex(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING));
        assertEquals("f42bb0eeb018ebbd4597ae7213711ec60760843f", HmacUtils.hmacSha1Hex(HmacAlgorithmsTest.STANDARD_KEY_STRING, ""));
        assertEquals("effcdf6ae5eb2fa2d27416d5f184df9c259a7c79",
                HmacUtils.hmacSha1Hex("Jefe", "what do ya want for nothing?"));
        assertEquals(
                "effcdf6ae5eb2fa2d27416d5f184df9c259a7c79",
                HmacUtils.hmacSha1Hex("Jefe".getBytes(), // calls a and b
                        new ByteArrayInputStream("what do ya want for nothing?".getBytes()))); // assertion fails because method-a is replaced with the empty string
    }

}
