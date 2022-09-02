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
     * Gets the property name of the field.
     * @return The field's property name.
     */
    public String getProperty() {
        return this.property;
    }

    public void addArg(Arg arg) { // indirectly called from test setUp
        // TODO this first if check can go away after arg0, etc. are removed from dtd
        if (arg == null || arg.getKey() == null || arg.getKey().isEmpty()) {
            return;
        }

        determineArgPosition(arg);
        // NOTE: to experiment with calling method-b before method-a, uncomment the next line
        // System.out.println(getProperty());
        ensureArgsCapacity(arg); // call to a

        Map<String, Arg> argMap = this.args[arg.getPosition()]; // exception gets thrown here
        if (argMap == null) {
            argMap = new HashMap<>();
            this.args[arg.getPosition()] = argMap;
        }
        ...
    }

}

public class GenericValidatorImpl {
   public static boolean validateRequired(Object bean, Field field) { // indirectly called from test (Validator.validate) - using reflection
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty()); // call to b

      return !GenericValidator.isBlankOrNull(value);
   }

}
public class RequiredNameTest extends AbstractCommonTest {
   @Override
protected void setUp() throws IOException, SAXException {
      // Load resources
      loadResources("RequiredNameTest-config.xml"); // calls a
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
      ValidatorResults results = validator.validate(); // calls b

      ...
   }
}
