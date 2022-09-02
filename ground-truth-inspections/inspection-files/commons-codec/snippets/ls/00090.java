public class Base32 {
    // redacting Base32.decode() because it's not clear which call to validateCharacter (method-a) was made, and the method is lengthy

    /**
     * Validates whether decoding the final trailing character is possible in the context
     * of the set of possible base 32 values.
     *
     * <p>The character is valid if the lower bits within the provided mask are zero. This
     * is used to test the final trailing base-32 digit is zero in the bits that will be discarded.
     *
     * @param emptyBitsMask The mask of the lower bits that should be empty
     * @param context the context to be used
     *
     * @throws IllegalArgumentException if the bits being checked contain any non-zero value
     */
    private void validateCharacter(final long emptyBitsMask, final Context context) { // definition of a
        // Use the long bit work area
        if (isStrictDecoding() && (context.lbitWorkArea & emptyBitsMask) != 0) {
            throw new IllegalArgumentException(
                "Strict decoding: Last encoded character (before the paddings if any) is a valid " +
                "base 32 alphabet but not a possible encoding. " +
                "Expected the discarded bits from the character to be zero.");
        }
    }

}

public abstract class BaseNCodec {

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

public class BaseNCodecInputStream {
    @Override
    public int read(final byte array[], final int offset, final int len) throws IOException { // called from Base32InputStream.skip()
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
            readLen = baseNCodec.readResults(array, offset, len, context); // calls b
        }
        return readLen;
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
