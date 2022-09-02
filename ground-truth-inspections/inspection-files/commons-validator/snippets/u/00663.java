public class Flags implements Serializable, Cloneable {
    /**
     * Turn off all flags.
     */
    public void turnOffAll() {  // definition of a
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
    public void testTurnOffAll() {
        Flags f = new Flags(98432);
        f.turnOffAll();         // call to a
        assertEquals(0, f.getFlags()); // call to b
    }

}
