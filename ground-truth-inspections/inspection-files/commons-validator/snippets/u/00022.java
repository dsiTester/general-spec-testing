public class Field implements Cloneable, Serializable {
    /**
     * Gets the default <code>Arg</code> object at the given position.
     * @param position Validation message argument's position.
     * @return The default Arg or null if not found.
     * @since Validator 1.1
     */
    public Arg getArg(int position) { // definition of a
        return this.getArg(DEFAULT_ARG, position); // call to b
    }

    /**
     * Gets the <code>Arg</code> object at the given position.  If the key
     * finds a <code>null</code> value then the default value will be
     * retrieved.
     * @param key The name the Arg is stored under.  If not found, the default
     * Arg for the given position (if any) will be retrieved.
     * @param position The Arg number to find.
     * @return The Arg with the given name and position or null if not found.
     * @since Validator 1.1
     */
    public Arg getArg(String key, int position) { // definition of b
        if ((position >= this.args.length) || (this.args[position] == null)) {
            return null;
        }

        Arg arg = args[position].get(key);

        // Didn't find default arg so exit, otherwise we would get into
        // infinite recursion
        if ((arg == null) && key.equals(DEFAULT_ARG)) {
            return null;
        }

        return (arg == null) ? this.getArg(position) : arg;
    }

}

public class ExtensionTest extends TestCase {
    /**
     * Tests if we can override a rule. We "can" override a rule if the message shown
     * when the firstName required test fails and the lastName test is null.
    */
    public void testOverrideRule() throws ValidatorException {

       // Create bean to run test on.
       NameBean name = new NameBean();
       name.setLastName("Smith");

       // Construct validator based on the loaded resources
       // and the form key
       Validator validator = new Validator(resources, FORM_KEY2);
       // add the name bean to the validator as a resource
       // for the validations to be performed on.
       validator.setParameter(Validator.BEAN_PARAM, name);
       ...
       assertTrue("First Name ValidatorResult for the '" + ACTION +"' action should have '" + CHECK_MSG_KEY + " as a key.", firstNameResult.field.getArg(0).getKey().equals(CHECK_MSG_KEY)); // call to a

       assertNull("Last Name ValidatorResult should be null.", lastNameResult);
    }

}
