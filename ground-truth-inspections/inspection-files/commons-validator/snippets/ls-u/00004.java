public class Arg implements Cloneable, Serializable {
    /**
     * Set this argument's replacement position.
     * @param position set this argument's replacement position.
     */
    public void setPosition(int position) { // definition of a
        this.position = position;
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
    public void addArg(Arg arg) { // called directly from invalidated test; called when invoked by digester for unknown test
        // TODO this first if check can go away after arg0, etc. are removed from dtd
        if (arg == null || arg.getKey() == null || arg.getKey().isEmpty()) {
            return;
        }

        determineArgPosition(arg); // calls a in unknown test
        ensureArgsCapacity(arg);
        ...
        if (arg.getName() == null) { // call to b
            argMap.put(DEFAULT_ARG, arg);
        } else {
            argMap.put(arg.getName(), arg);
        }
    }

    /**
     * Calculate the position of the Arg
     */
    private void determineArgPosition(Arg arg) {

        int position = arg.getPosition();

        // position has been explicity set
        if (position >= 0) {
            return;
        }

        // first arg to be added
        if (args == null || args.length == 0) {
            arg.setPosition(0); // call to a in unknown test
            return;
        }
        ...
        // allocate the next position
        arg.setPosition(++lastPosition);

    }

}

public class ValidatorResources implements Serializable {
    public ValidatorResources(InputStream[] streams)
            throws IOException, SAXException {

        Digester digester = initDigester(); // calls a and b for unknown test
        ...
        this.process();
    }

    private Digester initDigester() {
        URL rulesUrl = this.getClass().getResource(VALIDATOR_RULES);
        ...
        Digester digester = DigesterLoader.createDigester(rulesUrl);
        ...
        // Add rules for arg0-arg3 elements
        addOldArgRules(digester); // sets up digester so that it calls a and b

        ...
        return digester;
    }

    private void addOldArgRules(Digester digester) {

        // Create a new rule to process args elements
        Rule rule = new Rule() {
            @Override
            public void begin(String namespace, String name,
                               Attributes attributes) throws Exception {
                // Create the Arg
                Arg arg = new Arg();
                arg.setKey(attributes.getValue("key"));
                arg.setName(attributes.getValue("name"));
                ...
                // Add the arg to the parent field
                ((Field)getDigester().peek(0)).addArg(arg); // calls a and b when invoked from digester
            }
        };

        // Add the rule for each of the arg elements
        digester.addRule(ARGS_PATTERN + "0", rule);
        digester.addRule(ARGS_PATTERN + "1", rule);
        digester.addRule(ARGS_PATTERN + "2", rule);
        digester.addRule(ARGS_PATTERN + "3", rule);

    }

}

public class FieldTest extends TestCase {
    public void testDefaultUsingPositions() { // invalidated test

        field.addArg(createArg("default-position-1", 1)); // createArg calls a
        field.addArg(createArg("default-position-0", 0)); // createArg calls a
        field.addArg(createArg("default-position-2", 2)); // createArg calls a

        assertEquals("testDefaultUsingPositions(1) ", 3, field.getArgs("required").length);
        assertEquals("testDefaultUsingPositions(2) ", "default-position-0", field.getArg("required", 0).getKey());
        assertEquals("testDefaultUsingPositions(3) ", "default-position-1", field.getArg("required", 1).getKey());
        assertEquals("testDefaultUsingPositions(4) ", "default-position-2", field.getArg("required", 2).getKey());

    }

    private Arg createArg(String key, int position) { // called from invalidated test
        Arg arg = createArg(key);                     // calls a in invalidated test
        arg.setPosition(position);
        return arg;
    }

    private Arg createArg(String key) { // called from above
        Arg arg = new Arg();
        arg.setKey(key);        // call to a
        return arg;
    }

}

public class ExtensionTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        // Load resources
        try (InputStream in = this.getClass().getResourceAsStream("ExtensionTest-config.xml")) {
            resources = new ValidatorResources(in); // calls a and b
        }
    }

    /**
     * Tests if the order is mantained when extending a form. Parent form fields should
     * preceed self form fields, except if we override the rules.
    */
    public void testOrder() {   // unknown test
        ...
    }
}
