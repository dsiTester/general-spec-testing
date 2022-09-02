public class Flags implements Serializable, Cloneable {
    /**
     * Turn on all 64 flags.
     */
    public void turnOnAll() {
        this.flags = 0xFFFFFFFFFFFFFFFFL; // definition of a
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
    public void testTurnOnAll() {
        Flags f = new Flags();
        f.turnOnAll();          // call to a
        assertEquals(~0, f.getFlags()); // call to b
    }
}
