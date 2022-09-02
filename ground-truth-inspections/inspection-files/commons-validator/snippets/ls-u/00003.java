public class Arg implements Cloneable, Serializable {
    /**
     * Sets the key/value.
     * @param key They to access the argument.
     */
    public void setKey(String key) { // definition of a
        this.key = key;
    }

    /**
     * Set this argument's replacement position.
     * @param position set this argument's replacement position.
     */
    public void setPosition(int position) { // definition of b
        this.position = position;
    }
}

public class Field implements Cloneable, Serializable {
    public void addArg(Arg arg) {
        // TODO this first if check can go away after arg0, etc. are removed from dtd
        if (arg == null || arg.getKey() == null || arg.getKey().isEmpty()) { // DSI experiment makes this conditional true, causing the 1st and 3rd perturbations in the unknown test to not be able to call b
            return;
        }

        determineArgPosition(arg); // calls b for 1st and 3rd perturbations of unknown test
        ensureArgsCapacity(arg);

        ...
    }

    /**
     * Calculate the position of the Arg
     */
    private void determineArgPosition(Arg arg) {

        int position = arg.getPosition();

        // position has been explicity set
        if (position >= 0) {
            return;             // returns here in 2nd of unknown test
        }

        // first arg to be added
        if (args == null || args.length == 0) {
            arg.setPosition(0); // call to b in 1st invocation of unknown test
            return;
        }
        ...
        // allocate the next position
        arg.setPosition(++lastPosition); // call to b in 3rd invocation of unknown test

    }

}

public class FieldTest extends TestCase {
    public void testDefaultUsingPositions() { // invalidated test

        field.addArg(createArg("default-position-1", 1)); // createArg calls a and b
        field.addArg(createArg("default-position-0", 0)); // createArg calls a and b
        field.addArg(createArg("default-position-2", 2)); // createArg calls a and b

        assertEquals("testDefaultUsingPositions(1) ", 3, field.getArgs("required").length);
        assertEquals("testDefaultUsingPositions(2) ", "default-position-0", field.getArg("required", 0).getKey());
        assertEquals("testDefaultUsingPositions(3) ", "default-position-1", field.getArg("required", 1).getKey());
        assertEquals("testDefaultUsingPositions(4) ", "default-position-2", field.getArg("required", 2).getKey());

    }

    /**
     * test Field with only 'default' arguments, position specified for one argument
     */
    public void testDefaultOnePosition() { // unknown test

        field.addArg(createArg("default-position-0"));    // createArg calls a; addArg calls b
        field.addArg(createArg("default-position-2", 2)); // createArg calls a and b
        field.addArg(createArg("default-position-3"));    // createArg calls a; addArg calls b

        assertEquals("testDefaultOnePosition(1) ", 4, field.getArgs("required").length);
        assertEquals("testDefaultOnePosition(2) ", "default-position-0", field.getArg("required", 0).getKey());
        assertNull("testDefaultOnePosition(3) ", field.getArg("required", 1));
        assertEquals("testDefaultOnePosition(4) ", "default-position-2", field.getArg("required", 2).getKey());
        assertEquals("testDefaultOnePosition(5) ", "default-position-3", field.getArg("required", 3).getKey());

    }

    private Arg createArg(String key, int position) { // called from all 3 perturbations in invalidated test, called from 2nd perturb in unknown test
        Arg arg = createArg(key);                     // calls a
        arg.setPosition(position);                    // call to b in invalidated test and second perturbation in unknown test
        return arg;
    }

    private Arg createArg(String key) { // called from above when above is called; called directly from 1st and 3rd perturb in unknown test
        Arg arg = new Arg();
        arg.setKey(key);        // call to a
        return arg;
    }

}
