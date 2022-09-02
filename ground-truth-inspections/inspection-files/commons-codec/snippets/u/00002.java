public class Throwable {

    public String getMessage() { // decompiled definition of a
        return this.detailMessage;
    }

    public synchronized Throwable getCause() { // decompiled definition of b
        return this.cause == this ? null : this.cause;
    }
}

// EncoderException extends Throwable
public class EncoderException {
    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * <p>
     * Note that the detail message associated with {@code cause} is not automatically incorporated into this
     * exception's detail message.
     * </p>
     *
     * @param message
     *            The detail message which is saved for later retrieval by the {@link #getMessage()} method.
     * @param cause
     *            The cause which is saved for later retrieval by the {@link #getCause()} method. A {@code null}
     *            value is permitted, and indicates that the cause is nonexistent or unknown.
     * @since 1.4
     */
    public EncoderException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

public class EncoderExceptionTest {

    private static final String MSG = "TEST";

    private static final Throwable t = new Exception();

    @Test
    public void testConstructorStringThrowable() {
        final EncoderException e = new EncoderException(MSG, t);
        assertEquals(MSG, e.getMessage()); // call to a
        assertEquals(t, e.getCause()); // call to b
    }
}
