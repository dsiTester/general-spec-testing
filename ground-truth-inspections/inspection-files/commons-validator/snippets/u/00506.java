public class ValidatorResult implements Serializable {
    /**
     * Add the result of a validator action.
     * @param validatorName Name of the validator.
     * @param result Whether the validation passed or failed.
     * @param value Value returned by the validator.
     */
    public void add(String validatorName, boolean result, Object value) { // definition of a
        hAction.put(validatorName, new ResultStatus(result, value));
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

public class ValidatorResults implements Serializable {
    public void add(
            Field field,
            String validatorName,
            boolean result,
            Object value) {     // indirectly called from Validator.validate()

        ValidatorResult validatorResult = this.getValidatorResult(field.getKey());

        if (validatorResult == null) {
            validatorResult = new ValidatorResult(field);
            this.hResults.put(field.getKey(), validatorResult);
        }

        // NOTE: uncomment below to call b before a
        // System.out.println(validatorResult.isValid(validatorName));
        validatorResult.add(validatorName, result, value); // call to a
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
        ValidatorResults results = validator.validate(); // calls a

        assertNotNull("Results are null.", results);

        ValidatorResult result = results.getValidatorResult("value");

        assertNotNull(ACTION + " value ValidatorResult should not be null.", result);
        assertTrue(ACTION + " value ValidatorResult should contain the '" + ACTION + "' action.", result.containsAction(ACTION)); // assertion fails here
        assertTrue(ACTION + " value ValidatorResult for the '" + ACTION + "' action should have " + (passed ? "passed" : "failed") + ".", (passed ? result.isValid(ACTION) : !result.isValid(ACTION))); // call to b
    }


}
