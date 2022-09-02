public class Arg implements Cloneable, Serializable {
    /**
     * Sets the key/value.
     * @param key They to access the argument.
     */
    public void setKey(String key) { // definition of a
        this.key = key;
    }

    /**
     * Gets the name of the dependency.
     * @return the name of the dependency.
     */
    public String getName() {   // definition of b
        return this.name;
    }
}

public class Field implements Cloneable, Serializable {
    public void addArg(Arg arg) { // called from test
        // TODO this first if check can go away after arg0, etc. are removed from dtd
        if (arg == null || arg.getKey() == null || arg.getKey().isEmpty()) { // this check prevents method-b from being called
            return;
        }

        determineArgPosition(arg);
        ensureArgsCapacity(arg);

        Map<String, Arg> argMap = this.args[arg.getPosition()];
        if (argMap == null) {
            argMap = new HashMap<>();
            this.args[arg.getPosition()] = argMap;
        }

        if (arg.getName() == null) { // call to b
            argMap.put(DEFAULT_ARG, arg);
        } else {
            argMap.put(arg.getName(), arg);
        }

    }

}

public class FieldTest extends TestCase {
    public void testDefaultUsingPositions() {

        field.addArg(createArg("default-position-1", 1)); // createArg calls a; addArg calls b
        field.addArg(createArg("default-position-0", 0)); // createArg calls a; addArg calls b
        field.addArg(createArg("default-position-2", 2)); // createArg calls a; addArg calls b

        assertEquals("testDefaultUsingPositions(1) ", 3, field.getArgs("required").length);
        assertEquals("testDefaultUsingPositions(2) ", "default-position-0", field.getArg("required", 0).getKey());
        assertEquals("testDefaultUsingPositions(3) ", "default-position-1", field.getArg("required", 1).getKey());
        assertEquals("testDefaultUsingPositions(4) ", "default-position-2", field.getArg("required", 2).getKey());

    }

    private Arg createArg(String key, int position) { // called from test
        Arg arg = createArg(key);                     // calls a
        arg.setPosition(position);
        return arg;
    }

    private Arg createArg(String key) {
        Arg arg = new Arg();
        arg.setKey(key);        // call to a
        return arg;
    }

}
