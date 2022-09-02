public abstract class BaseNCodec {

    /**
     * Returns true if decoding behavior is strict. Decoding will raise an {@link IllegalArgumentException} if trailing
     * bits are not part of a valid encoding.
     *
     * <p>
     * The default is false for lenient decoding. Decoding will compose trailing bits into 8-bit bytes and discard the
     * remainder.
     * </p>
     *
     * @return true if using strict decoding
     * @since 1.15
     */
    public boolean isStrictDecoding() { // definition of a; not defined in Base32
        return decodingPolicy == CodecPolicy.STRICT;
    }

    /**
     * Returns the amount of buffered data available for reading.
     *
     * @param context the context to be used
     * @return The amount of buffered data available for reading.
     */
    int available(final Context context) {  // definition of b
        return context.buffer != null ? context.pos - context.readPos : 0;
    }

    int readResults(final byte[] b, final int bPos, final int bAvail, final Context context) { // called from BaseNCodecInputStream.read()
        if (context.buffer != null) {
            final int len = Math.min(available(context), bAvail); // call to b
            ...
            return len;
        }
        return context.eof ? EOF : 0;
    }
}

public class Base32 extends BaseNCodec {

    // redacting Base32.decode() because it's not clear which call to validateCharacter was made, and the method is lengthy

    private void validateCharacter(final long emptyBitsMask, final Context context) {
        // Use the long bit work area
        if (isStrictDecoding() && (context.lbitWorkArea & emptyBitsMask) != 0) { // call to a
            throw new IllegalArgumentException(
                "Strict decoding: Last encoded character (before the paddings if any) is a valid " +
                "base 32 alphabet but not a possible encoding. " +
                "Expected the discarded bits from the character to be zero.");
        }
    }

}

public class Base32InputStream extends BaseNCodecInputStream {
    @Override
    public long skip(final long n) throws IOException { // called from test
        ...
        while (todo > 0) {
            int len = (int) Math.min(b.length, todo);
            len = this.read(b, 0, len); // calls a and b
            if (len == EOF) {
                break;
            }
            todo -= len;
        }

        return n - todo;
    }
}

public class BaseNCodecInputStream {
    @Override
    public int read(final byte array[], final int offset, final int len) throws IOException {
        ...
        while (readLen == 0) {
            if (!baseNCodec.hasData(context)) {
                final byte[] buf = new byte[doEncode ? 4096 : 8192];
                final int c = in.read(buf);
                if (doEncode) {
                    baseNCodec.encode(buf, 0, c, context);
                } else {
                    baseNCodec.decode(buf, 0, c, context); // calls a
                }
            }
            readLen = baseNCodec.readResults(array, offset, len, context); // calls b?
        }
        return readLen;
    }
}

public class Base32InputStreamTest {
    @Test
    public void testAvailable() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        try (final Base32InputStream b32stream = new Base32InputStream(ins)) {
            assertEquals(1, b32stream.available());
            assertEquals(3, b32stream.skip(10)); // calls a and b
            // End of stream reached
            assertEquals(0, b32stream.available());
            assertEquals(-1, b32stream.read());
            assertEquals(-1, b32stream.read());
            assertEquals(0, b32stream.available());
        }
    }
}
