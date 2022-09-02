public class Field implements Cloneable, Serializable {
    /**
     * Ensures that the args array can hold the given arg.  Resizes the array as
     * necessary.
     * @param arg Determine if the args array is long enough to store this arg's
     * position.
     */
    private void ensureArgsCapacity(Arg arg) { // definition of a
        if (arg.getPosition() >= this.args.length) {
            @SuppressWarnings("unchecked") // cannot check this at compile time, but it is OK
            Map<String, Arg>[] newArgs = new Map[arg.getPosition() + 1];
            System.arraycopy(this.args, 0, newArgs, 0, this.args.length);
            this.args = newArgs;
        }
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

    public void addArg(Arg arg) { // indirectly called from test setUp
        // TODO this first if check can go away after arg0, etc. are removed from dtd
        if (arg == null || arg.getKey() == null || arg.getKey().isEmpty()) {
            return;
        }

        determineArgPosition(arg);
        // NOTE: to experiment with calling method-b before method-a, uncomment the next line
        // process(new FastHashMap(), new HashMap<String, String>());
        ensureArgsCapacity(arg); // call to a

        Map<String, Arg> argMap = this.args[arg.getPosition()]; // exception gets thrown here
        if (argMap == null) {
            argMap = new HashMap<>();
            this.args[arg.getPosition()] = argMap;
        }
        ...
    }

}

public class Form implements Serializable {
    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) { // called indirectly from ValidatorResources.process
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

public class ValidatorResources {
    public ValidatorResources(InputStream[] streams)
            throws IOException, SAXException { // indirectly called from loadResources (from test)

        Digester digester = initDigester(); // calls a
        for (int i = 0; i < streams.length; i++) {
            if (streams[i] == null) {
                throw new IllegalArgumentException("Stream[" + i + "] is null");
            }
            digester.push(this);
            digester.parse(streams[i]);
        }

        this.process();         // calls b
    }

}

public class RequiredNameTest extends AbstractCommonTest {
   @Override
protected void setUp() throws IOException, SAXException {
      // Load resources
      loadResources("RequiredNameTest-config.xml"); // calls a and b
   }

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

      ...
   }
}
