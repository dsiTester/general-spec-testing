public class HmacUtils {

    /**
     * Returns the digest for the input data.
     *
     * @param valueToDigest the input to use, treated as UTF-8
     * @return the digest as a hex String
     * @since 1.11
     */
    public String hmacHex(final String valueToDigest) {  // definition of a
        return Hex.encodeHexString(hmac(valueToDigest)); // call to b
    }

    @Deprecated
    public static String hmacSha1Hex(final String key, final String valueToDigest) {
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_1, key).hmacHex(valueToDigest); // call to a
    }


}

public class HmacUtilsTest {
    @SuppressWarnings("deprecation") // most of the static methods are deprecated
    @Test
    public void testHmacSha1Hex() throws IOException {
        assertEquals(HmacAlgorithmsTest.STANDARD_SHA1_RESULT_STRING, HmacUtils.hmacSha1Hex(HmacAlgorithmsTest.STANDARD_KEY_STRING, HmacAlgorithmsTest.STANDARD_PHRASE_STRING)); // calls a
        assertEquals("f42bb0eeb018ebbd4597ae7213711ec60760843f", HmacUtils.hmacSha1Hex(HmacAlgorithmsTest.STANDARD_KEY_STRING, "")); // calls a
        assertEquals("effcdf6ae5eb2fa2d27416d5f184df9c259a7c79",
                HmacUtils.hmacSha1Hex("Jefe", "what do ya want for nothing?")); // calls a
        assertEquals(
                "effcdf6ae5eb2fa2d27416d5f184df9c259a7c79",
                HmacUtils.hmacSha1Hex("Jefe".getBytes(), // calls a and b
                        new ByteArrayInputStream("what do ya want for nothing?".getBytes())));
    }

}
