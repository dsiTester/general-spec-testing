public class Field implements Cloneable, Serializable {
    /**
     * Executes the given ValidatorAction and all ValidatorActions that it
     * depends on.
     * @return true if the validation succeeded.
     */
    private boolean validateForRule(
        ValidatorAction va,
        ValidatorResults results,
        Map<String, ValidatorAction> actions,
        Map<String, Object> params,
        int pos)
        throws ValidatorException { // definition of a

        ValidatorResult result = results.getValidatorResult(this.getKey());
        if (result != null && result.containsAction(va.getName())) {
            return result.isValid(va.getName());
        }

        if (!this.runDependentValidators(va, results, actions, params, pos)) {
            return false;
        }

        return va.executeValidationMethod(this, params, results, pos);
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

    public ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions)
        throws ValidatorException { // indirectly called from Validator.validate()
        ...
        ValidatorResults allResults = new ValidatorResults();
        ...
        for (int fieldNumber = 0; fieldNumber < numberOfFieldsToValidate; fieldNumber++) {

            ValidatorResults results = new ValidatorResults();
            synchronized(dependencyList) {
                Iterator<String> dependencies = this.dependencyList.iterator();
                while (dependencies.hasNext()) {
                    String depend = dependencies.next();

                    ValidatorAction action = actions.get(depend);
                    ...
                    // NOTE: uncomment the below to experimentally call b before a
                    // System.out.println(this.getArg(0));
                    boolean good =
                        validateForRule(action, results, actions, params, fieldNumber); // call to a
                    ...
                }
            }
            allResults.merge(results);
        }

        return allResults;
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

       assertNotNull("Results are null.", results);

       ValidatorResult firstNameResult = results.getValidatorResult("firstName");
       ValidatorResult lastNameResult = results.getValidatorResult("lastName");
       assertNotNull("First Name ValidatorResult should not be null.", firstNameResult); // assertion fails here
       assertTrue("First Name ValidatorResult for the '" + ACTION +"' action should have '" + CHECK_MSG_KEY + " as a key.", firstNameResult.field.getArg(0).getKey().equals(CHECK_MSG_KEY)); // call to b

       assertNull("Last Name ValidatorResult should be null.", lastNameResult);
    }
}
