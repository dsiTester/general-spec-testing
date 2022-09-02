public class Field implements Cloneable, Serializable {
    /**
     * Calls all of the validators that this validator depends on.
     * TODO ValidatorAction should know how to run its own dependencies.
     * @param va Run dependent validators for this action.
     * @param results
     * @param actions
     * @param pos
     * @return true if all of the dependent validations passed.
     * @throws ValidatorException If there's an error running a validator
     */
    private boolean runDependentValidators(
        ValidatorAction va,
        ValidatorResults results,
        Map<String, ValidatorAction> actions,
        Map<String, Object> params,
        int pos)
        throws ValidatorException { // definition of a

        List<String> dependentValidators = va.getDependencyList();

        if (dependentValidators.isEmpty()) {
            return true;
        }

        Iterator<String> iter = dependentValidators.iterator();
        while (iter.hasNext()) {
            String depend = iter.next();

            ValidatorAction action = actions.get(depend);
            if (action == null) {
                this.handleMissingAction(depend);
            }

            if (!this.validateForRule(action, results, actions, params, pos)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the default <code>Arg</code> object at the given position.
     * @param position Validation message argument's position.
     * @return The default Arg or null if not found.
     * @since Validator 1.1
     */
    public Arg getArg(int position) { // definition of b
        return this.getArg(DEFAULT_ARG, position);
    }

    private boolean validateForRule(
        ValidatorAction va,
        ValidatorResults results,
        Map<String, ValidatorAction> actions,
        Map<String, Object> params,
        int pos)
        throws ValidatorException { // indirectly called from Validator.validate()

        ValidatorResult result = results.getValidatorResult(this.getKey());
        if (result != null && result.containsAction(va.getName())) {
            return result.isValid(va.getName());
        }

        // NOTE: uncomment below to call b before a
        // System.out.println(getArg(0));
        if (!this.runDependentValidators(va, results, actions, params, pos)) { // call to a
            return false;
        }

        return va.executeValidationMethod(this, params, results, pos);
    }
}

public class ExtensionTest extends TestCase {
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

       // Get results of the validation.
       ValidatorResults results = validator.validate(); // calls a

       assertNotNull("Results are null.", results); // assertion fails here

       ValidatorResult firstNameResult = results.getValidatorResult("firstName");
       ValidatorResult lastNameResult = results.getValidatorResult("lastName");
       assertNotNull("First Name ValidatorResult should not be null.", firstNameResult);
       assertTrue("First Name ValidatorResult for the '" + ACTION +"' action should have '" + CHECK_MSG_KEY + " as a key.", firstNameResult.field.getArg(0).getKey().equals(CHECK_MSG_KEY)); // call to b

       assertNull("Last Name ValidatorResult should be null.", lastNameResult);
    }
}
