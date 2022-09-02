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
     * Indicate whether a specified validator is in the Result.
     * @param validatorName Name of the validator.
     * @return true if the validator is in the result.
     */
    public boolean containsAction(String validatorName) { // call to b
        return hAction.containsKey(validatorName);
    }
}

public class ValidatorResults implements Serializable {
    public void add(
            Field field,
            String validatorName,
            boolean result,
            Object value) {

        ValidatorResult validatorResult = this.getValidatorResult(field.getKey());

        if (validatorResult == null) {
            validatorResult = new ValidatorResult(field);
            this.hResults.put(field.getKey(), validatorResult);
        }

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
        valueTest(info, true);
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
        assertTrue(ACTION + " value ValidatorResult should contain the '" + ACTION + "' action.", result.containsAction(ACTION)); // call to b; assertion fails here
        assertTrue(ACTION + " value ValidatorResult for the '" + ACTION + "' action should have " + (passed ? "passed" : "failed") + ".", (passed ? result.isValid(ACTION) : !result.isValid(ACTION)));
    }
}

public class ValidatorTest extends TestCase {

   /**
    * Verify that one value generates an error and the other passes.  The validation
    * method being tested returns an object (<code>null</code> will be considered an error).
    */
   public void testManualObject() { // unknown test
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action);

      TestBean bean = new TestBean();
      bean.setDate("2/3/1999");

      Validator validator = new Validator(resources, "testForm");
      validator.setParameter(Validator.BEAN_PARAM, bean);

      try {
         ValidatorResults results = validator.validate(); // calls a

         assertNotNull("Results are null.", results);

         ValidatorResult result = results.getValidatorResult(property);

         assertNotNull("Results are null.", results);

         assertTrue("ValidatorResult does not contain '" + action + "' validator result.", result.containsAction(action)); // call to b; assertion fails here

         assertTrue("Validation of the date formatting has failed.", result.isValid(action));
      } catch (Exception e) {
         fail("An exception was thrown while calling Validator.validate()");
      }

      bean.setDate("2/30/1999");

      try {
         ValidatorResults results = validator.validate(); // calls a

         assertNotNull("Results are null.", results);

         ValidatorResult result = results.getValidatorResult(property);

         assertNotNull("Results are null.", results);

         assertTrue("ValidatorResult does not contain '" + action + "' validator result.", result.containsAction(action)); // call to b

         assertTrue("Validation of the date formatting has passed when it should have failed.", !result.isValid(action));
      } catch (Exception e) {
         fail("An exception was thrown while calling Validator.validate()");
      }

   }
}
