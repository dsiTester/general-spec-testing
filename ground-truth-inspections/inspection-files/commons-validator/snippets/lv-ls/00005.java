public class Field implements Cloneable, Serializable {

    /**
     * Calculate the position of the Arg
     */
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
     * Ensures that the args array can hold the given arg.  Resizes the array as
     * necessary.
     * @param arg Determine if the args array is long enough to store this arg's
     * position.
     */
    private void ensureArgsCapacity(Arg arg) { // definition of b
        if (arg.getPosition() >= this.args.length) {
            @SuppressWarnings("unchecked") // cannot check this at compile time, but it is OK
            Map<String, Arg>[] newArgs = new Map[arg.getPosition() + 1];
            System.arraycopy(this.args, 0, newArgs, 0, this.args.length);
            this.args = newArgs;
        }
    }

    public void addArg(Arg arg) { // called from 
        // TODO this first if check can go away after arg0, etc. are removed from dtd
        if (arg == null || arg.getKey() == null || arg.getKey().isEmpty()) { // DSI experiment makes this conditional true, causing the first and third perturbations 
            return;
        }

        determineArgPosition(arg); // call to a
        ensureArgsCapacity(arg);   // call to b
        ...
        if (arg.getName() == null) {
            argMap.put(DEFAULT_ARG, arg);
        } else {
            argMap.put(arg.getName(), arg);
        }
    }

}

public class LocaleTest extends AbstractCommonTest {
    @Override
    protected void setUp()
        throws IOException, SAXException {      // a before method?
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

public class RequiredNameTest extends AbstractCommonTest {
   @Override
protected void setUp() throws IOException, SAXException {
      // Load resources
      loadResources("RequiredNameTest-config.xml");
   }

  /**
    * Tests the required validation failure.
    */
   public void testRequired() throws ValidatorException {
      // Create bean to run test on.
      NameBean name = new NameBean();

      // Construct validator based on the loaded resources
      // and the form key
      Validator validator = new Validator(resources, FORM_KEY);
      // add the name bean to the validator as a resource
      // for the validations to be performed on.
      validator.setParameter(Validator.BEAN_PARAM, name);

      // Get results of the validation.
      // throws ValidatorException,
      // but we aren't catching for testing
      // since no validation methods we use
      // throw this
      ValidatorResults results = validator.validate();

      assertNotNull("Results are null.", results);

      ValidatorResult firstNameResult = results.getValidatorResult("firstName");
      ValidatorResult lastNameResult = results.getValidatorResult("lastName");

      assertNotNull("First Name ValidatorResult should not be null.", firstNameResult);
      assertTrue("First Name ValidatorResult should contain the '" + ACTION +"' action.", firstNameResult.containsAction(ACTION));
      assertTrue("First Name ValidatorResult for the '" + ACTION +"' action should have failed.", !firstNameResult.isValid(ACTION));

      assertNotNull("First Name ValidatorResult should not be null.", lastNameResult);
      assertTrue("Last Name ValidatorResult should contain the '" + ACTION +"' action.", lastNameResult.containsAction(ACTION));
      assertTrue("Last Name ValidatorResult for the '" + ACTION +"' action should have failed.", !lastNameResult.isValid(ACTION));
   }
}

abstract public class AbstractCommonTest extends TestCase {

    /**
     * Load <code>ValidatorResources</code> from
     * validator-numeric.xml.
     */
    protected void loadResources(String file) throws IOException, SAXException { // called from LocaleTest.setUp()
        // Load resources
        try (InputStream in = this.getClass().getResourceAsStream(file)) { // this reflection simply allows loading the xml file
            resources = new ValidatorResources(in); // calls a and b
        }
    }

}

public class ValidatorResources implements Serializable {
    public ValidatorResources(InputStream in) throws IOException, SAXException {
        this(new InputStream[]{in});
    }

    public ValidatorResources(InputStream[] streams)
            throws IOException, SAXException {

        Digester digester = initDigester(); // calls a and b
        ...
        this.process();
    }

    private Digester initDigester() {
        ...
        // Add rules for arg0-arg3 elements
        addOldArgRules(digester); // calls a and b
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
                ((Field)getDigester().peek(0)).addArg(arg); // calls a and b
            }
        };

        // Add the rule for each of the arg elements
        digester.addRule(ARGS_PATTERN + "0", rule);
        digester.addRule(ARGS_PATTERN + "1", rule);
        digester.addRule(ARGS_PATTERN + "2", rule);
        digester.addRule(ARGS_PATTERN + "3", rule);

    }

}
