public class Field implements Cloneable, Serializable {
    private void determineArgPosition(Arg arg) { // definition of a

        int position = arg.getPosition();

        // position has been explicity set
        if (position >= 0) {
            return;
        }

        // first arg to be added
        if (args == null || args.length == 0) {
            arg.setPosition(0);
            return;
        }

        // determine the position of the last argument with
        // the same name or the last default argument
        String keyName = arg.getName() == null ? DEFAULT_ARG : arg.getName();
        int lastPosition = -1;
        int lastDefault  = -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && args[i].containsKey(keyName)) {
                lastPosition = i;
            }
            if (args[i] != null && args[i].containsKey(DEFAULT_ARG)) {
                lastDefault = i;
            }
        }

        if (lastPosition < 0) {
            lastPosition = lastDefault;
        }

        // allocate the next position
        arg.setPosition(++lastPosition);

    }

    /**
     * Replace constants with values in fields and process the depends field
     * to create the dependency <code>Map</code>.
     */
    void process(Map<String, String> globalConstants, Map<String, String> constants) { // definition of b
        this.hMsgs.setFast(false);
        this.hVars.setFast(true);

        this.generateKey();

        // Process FormSet Constants
        for (Entry<String, String> entry : constants.entrySet()) {
            String key1 = entry.getKey();
            String key2 = TOKEN_START + key1 + TOKEN_END;
            String replaceValue = entry.getValue();

            property = ValidatorUtils.replace(property, key2, replaceValue);

            processVars(key2, replaceValue);

            this.processMessageComponents(key2, replaceValue);
        }

        // Process Global Constants
        for (Entry<String, String> entry : globalConstants.entrySet()) {
            String key1 = entry.getKey();
            String key2 = TOKEN_START + key1 + TOKEN_END;
            String replaceValue = entry.getValue();

            property = ValidatorUtils.replace(property, key2, replaceValue);

            processVars(key2, replaceValue);

            this.processMessageComponents(key2, replaceValue);
        }

        // Process Var Constant Replacement
        for (String key1 : getVarMap().keySet()) {
            String key2 = TOKEN_START + TOKEN_VAR + key1 + TOKEN_END;
            Var var = this.getVar(key1);
            String replaceValue = var.getValue();

            this.processMessageComponents(key2, replaceValue);
        }

        hMsgs.setFast(true);
    }

    public void addArg(Arg arg) { // called from ValidatorResources.addOldArgs
        // Todo this first if check can go away after arg0, etc. are removed from dtd
        if (arg == null || arg.getKey() == null || arg.getKey().isEmpty()) {
            return;
        }

        determineArgPosition(arg); // call to a
        ensureArgsCapacity(arg);
        ...

    }

}

public class Form implements Serializable {
    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) { // indirectly called from ValidatorResources.process()
        ...
        hFields.setFast(true);
        //no need to reprocess parent's fields, we iterate from 'n'
        for (Iterator<Field> i = lFields.listIterator(n); i.hasNext(); ) {
            Field f = i.next();
            f.process(globalConstants, constants); // call to b
        }

        processed = true;
    }

}

abstract public class AbstractCommonTest extends TestCase { // extended via invalidated test class
    protected void loadResources(String file) throws IOException, SAXException {
        // Load resources
        try (InputStream in = this.getClass().getResourceAsStream(file)) {
            resources = new ValidatorResources(in); // calls a
        }
    }
}

public class RequiredNameTest extends AbstractCommonTest {
   @Override
   protected void setUp() throws IOException, SAXException {
      // Load resources
      loadResources("RequiredNameTest-config.xml"); // calls a and b; defined in AbstractCommonTest
   }

   public void testRequired() throws ValidatorException { // invalidated test
      ...
   }

}

public class ValidatorResources implements Serializable {
    public ValidatorResources(InputStream in) throws IOException, SAXException { // called from invalidated test
        this(new InputStream[]{in});
    }

    public ValidatorResources(InputStream[] streams)
            throws IOException, SAXException { // called from unknown test

        Digester digester = initDigester(); // calls a
        ...
        this.process();         // calls b
    }

    private Digester initDigester() {
        ...
        // Add rules for arg0-arg3 elements
        addOldArgRules(digester); // calls a
        ...
        return digester;
    }

    private void addOldArgRules(Digester digester) {

        // Create a new rule to process args elements
        Rule rule = new Rule() {
            @Override
            public void begin(String namespace, String name,
                               Attributes attributes) throws Exception {
                ...
                // Add the arg to the parent field
                ((Field)getDigester().peek(0)).addArg(arg); // calls a
            }
        };

        // Add the rule for each of the arg elements
        digester.addRule(ARGS_PATTERN + "0", rule);
        digester.addRule(ARGS_PATTERN + "1", rule);
        digester.addRule(ARGS_PATTERN + "2", rule);
        digester.addRule(ARGS_PATTERN + "3", rule);

    }

}

public class LocaleTest extends AbstractCommonTest {
    @Override
    protected void setUp()
        throws IOException, SAXException {
        // Load resources
        loadResources("LocaleTest-config.xml"); // calls a and b
    }

    public void testLocale1()
        throws ValidatorException {
        // Create bean to run test on.
        NameBean name = new NameBean();
        name.setFirstName("");
        name.setLastName("");

        valueTest(name, new Locale("en", "US", "TEST1"), false, false, false);
    }

}
