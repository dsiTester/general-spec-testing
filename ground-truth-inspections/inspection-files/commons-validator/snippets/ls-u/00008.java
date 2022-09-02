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
     * Gets the property name of the field.
     * @return The field's property name.
     */
    public String getProperty() {
        return this.property;
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

public class GenericValidatorImpl {
   public static boolean validateRequired(Object bean, Field field) { // called via reflection
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty()); // call to b

      return !GenericValidator.isBlankOrNull(value);
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
      loadResources("RequiredNameTest-config.xml"); // calls a
   }

   public void testRequired() throws ValidatorException { // invalidated test
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
      ValidatorResults results = validator.validate(); // calls b

      ...
   }

}

public class MultipleConfigFilesTest extends TestCase {

    @Override
    protected void setUp() throws IOException, SAXException {
        InputStream[] streams =
            new InputStream[] {
                this.getClass().getResourceAsStream(
                    "MultipleConfigFilesTest-1-config.xml"),
                this.getClass().getResourceAsStream(
                    "MultipleConfigFilesTest-2-config.xml")};

        this.resources = new ValidatorResources(streams); // calls a?

        for (InputStream stream : streams) {
            stream.close();
        }
    }

    /**
    * With nothing provided, we should fail both because both are required.
    */
    public void testBothBlank() throws ValidatorException { // unknown test
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
        ValidatorResults results = validator.validate(); // calls b

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
        this.process();
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
