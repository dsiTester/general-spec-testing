public class Flags implements Serializable, Cloneable {
    /**
     * Turn off all flags.  This is a synonym for <code>turnOffAll()</code>.
     * @since Validator 1.1.1
     */
    public void clear() {       // definition of a
        this.flags = 0;
    }

    /**
     * Returns the current flags.
     *
     * @return collection of boolean flags represented.
     */
    public long getFlags() {    // definition of b
        return this.flags;
    }
}

public class FlagsTest extends TestCase {
    public void testClear() {
        Flags f = new Flags(98432);
        f.clear();              // call to a
        assertEquals(0, f.getFlags()); // call to b
    }
}
