public class Field implements Cloneable, Serializable {
    /**
     * Gets the validation rules for this field as a comma separated list.
     * @return A comma separated list of validator names.
     */
    public String getDepends() { // definition of a
        return this.depends;
    }

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
        throws ValidatorException { // definition of b

        ValidatorResult result = results.getValidatorResult(this.getKey());
        if (result != null && result.containsAction(va.getName())) {
            return result.isValid(va.getName());
        }

        if (!this.runDependentValidators(va, results, actions, params, pos)) {
            return false;
        }

        return va.executeValidationMethod(this, params, results, pos);
    }

    public ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions)
        throws ValidatorException { // indirectly called from Validator.validate() (called from test)

        if (this.getDepends() == null) { // call to a
            return new ValidatorResults();
        }

        ValidatorResults allResults = new ValidatorResults();

        Object bean = params.get(Validator.BEAN_PARAM);
        int numberOfFieldsToValidate =
            this.isIndexed() ? this.getIndexedPropertySize(bean) : 1;

        for (int fieldNumber = 0; fieldNumber < numberOfFieldsToValidate; fieldNumber++) {

            ValidatorResults results = new ValidatorResults();
            synchronized(dependencyList) {
                Iterator<String> dependencies = this.dependencyList.iterator();
                while (dependencies.hasNext()) {
                    ...
                    boolean good =
                        validateForRule(action, results, actions, params, fieldNumber); // call to b
                    ...
                        }
            }
            allResults.merge(results);
        }

        return allResults;
    }

    private boolean validateForRule(
        ValidatorAction va,
        ValidatorResults results,
        Map<String, ValidatorAction> actions,
        Map<String, Object> params,
        int pos)
        throws ValidatorException { // called from above

        ...
        if (!this.runDependentValidators(va, results, actions, params, pos)) { // call to b
            return false;
        }

        return va.executeValidationMethod(this, params, results, pos);
    }


}

public class GenericValidatorImpl {
    public static boolean validateRequired(Object bean, Field field) {
        String value = ValidatorUtils.getValueAsString(bean, field.getProperty()); // call to b

        return !GenericValidator.isBlankOrNull(value);
    }
}

public class ExceptionTest {
    /**
     * Tests handling of checked exceptions - should become
     * ValidatorExceptions.
     */
    public void testValidatorException() {
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("VALIDATOR");

        // Construct validator based on the loaded resources
        // and the form key
        Validator validator = new Validator(resources, FORM_KEY);
        // add the name bean to the validator as a resource
        // for the validations to be performed on.
        validator.setParameter(Validator.BEAN_PARAM, info);

        // Get results of the validation which can throw ValidatorException
        try {
            validator.validate(); // calls a and b
            fail("ValidatorException should occur here!");
        } catch (ValidatorException expected) {
            assertTrue("VALIDATOR-EXCEPTION".equals(expected.getMessage())); // b throws the expected exception
        }
    }

}

public class ByteTest extends AbstractNumberTest {
    public void testByte() throws ValidatorException { // invalidated test
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("0");

        valueTest(info, true);  // calls a and b
    }
}

abstract public class AbstractNumberTest extends AbstractCommonTest {

    protected void valueTest(Object info, boolean passed) throws ValidatorException { // called from ByteTest.testByte()
        // Construct validator based on the loaded resources
        // and the form key
        Validator validator = new Validator(resources, FORM_KEY);
        // add the name bean to the validator as a resource
        // for the validations to be performed on.
        validator.setParameter(Validator.BEAN_PARAM, info);

        // Get results of the validation.
        // throws ValidatorException,
        // but we aren't catching for testing
        // since no validation methods we use
        // throw this
        ValidatorResults results = validator.validate(); // calls a and b; calls b via calling GenericValidatorImpl.validateRequired through reflection

        assertNotNull("Results are null.", results);

        ValidatorResult result = results.getValidatorResult("value");

        assertNotNull(ACTION + " value ValidatorResult should not be null.", result);
        assertTrue(ACTION + " value ValidatorResult should contain the '" + ACTION + "' action.", result.containsAction(ACTION));
        assertTrue(ACTION + " value ValidatorResult for the '" + ACTION + "' action should have " + (passed ? "passed" : "failed") + ".", (passed ? result.isValid(ACTION) : !result.isValid(ACTION)));
    }

}
