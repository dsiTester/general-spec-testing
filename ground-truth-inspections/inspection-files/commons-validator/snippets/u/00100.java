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

        if (!this.runDependentValidators(va, results, actions, params, pos)) { // call to b
            return false;
        }

        return va.executeValidationMethod(this, params, results, pos);
    }

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
        throws ValidatorException { // definition of b

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
       assertTrue("First Name ValidatorResult for the '" + ACTION +"' action should have '" + CHECK_MSG_KEY + " as a key.", firstNameResult.field.getArg(0).getKey().equals(CHECK_MSG_KEY));

       assertNull("Last Name ValidatorResult should be null.", lastNameResult);
    }
}
