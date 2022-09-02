public abstract class StringEncoderAbstractTest {
    protected abstract T createStringEncoder(); // a

    protected T stringEncoder = this.createStringEncoder(); // call to a

    public T getStringEncoder() { // definition of b
        return this.stringEncoder;
    }

    @Test
    public void testEncodeNull() throws Exception { // validated test
        final StringEncoder encoder = this.getStringEncoder();
        try {
            encoder.encode(null);
        } catch (final EncoderException ee) {
            // An exception should be thrown
        }
    }

    @Test
    public void testEncodeWithInvalidObject() throws Exception { // invalidated test
        boolean exceptionThrown = false;
        try {
            final StringEncoder encoder = this.getStringEncoder(); // call to b
            encoder.encode(Float.valueOf(3.4f));
        } catch (final Exception e) { // this catches NullPointerException as well
            exceptionThrown = true;
        }
        Assert.assertTrue("An exception was not thrown when we tried to encode " + "a Float object", exceptionThrown);
    }
}

public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Override
    protected StringEncoder createStringEncoder() { // used implementation of a
        return new BeiderMorseEncoder();
    }
    ...
}
