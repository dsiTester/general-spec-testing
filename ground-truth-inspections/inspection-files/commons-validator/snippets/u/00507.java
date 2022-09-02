public class ValidatorResult implements Serializable {

    /**
     * Indicate whether a specified validator is in the Result.
     * @param validatorName Name of the validator.
     * @return true if the validator is in the result.
     */
    public boolean containsAction(String validatorName) { // definition of a
        return hAction.containsKey(validatorName);
    }

    /**
     * Indicate whether a specified validation passed.
     * @param validatorName Name of the validator.
     * @return true if the validation passed.
     */
    public boolean isValid(String validatorName) { // definition of b
        ResultStatus status = hAction.get(validatorName);
        return (status == null) ? false : status.isValid();
    }
}

public class DateTest extends AbstractCommonTest {
    /**
     * Tests the date validation.
     */
    public void testValidDate() throws ValidatorException {
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("12/01/2005");
        valueTest(info, true);  // calls a and b
    }

    protected void valueTest(Object info, boolean passed) throws ValidatorException {
        // Construct validator based on the loaded resources
        // and the form key
        Validator validator = new Validator(resources, FORM_KEY);
        // add the name bean to the validator as a resource
        // for the validations to be performed on.
        validator.setParameter(Validator.BEAN_PARAM, info);
        validator.setParameter(Validator.LOCALE_PARAM, Locale.US);

        // Get results of the validation.
        // throws ValidatorException,
        // but we aren't catching for testing
        // since no validation methods we use
        // throw this
        ValidatorResults results = validator.validate();

        assertNotNull("Results are null.", results);

        ValidatorResult result = results.getValidatorResult("value");

        assertNotNull(ACTION + " value ValidatorResult should not be null.", result);
        assertTrue(ACTION + " value ValidatorResult should contain the '" + ACTION + "' action.", result.containsAction(ACTION)); // call to a; assertion fails here
        assertTrue(ACTION + " value ValidatorResult for the '" + ACTION + "' action should have " + (passed ? "passed" : "failed") + ".", (passed ? result.isValid(ACTION) : !result.isValid(ACTION))); // call to b
    }


}
