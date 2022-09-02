public class Throwable {

    public String getMessage() { // decompiled definition of a
        return this.detailMessage;
    }

    public synchronized Throwable getCause() { // decompiled definition of b
        return this.cause == this ? null : this.cause;
    }
}

// DecoderException indirectly extends Throwable
public class DecoderException extends Exception {
    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently
     * be initialized by a call to {@link #initCause}.
     *
     * @param message
     *            The detail message which is saved for later retrieval by the {@link #getMessage()} method.
     */
    public DecoderException(final String message) {
        super(message);
    }
}

public class DecoderExceptionTest {

    private static final String MSG = "TEST";

    @Test
    public void testConstructorString() {
        final DecoderException e = new DecoderException(MSG);
        assertEquals(MSG, e.getMessage()); // call to a; assertion fails here
        assertNull(e.getCause()); // call to b
    }
}
