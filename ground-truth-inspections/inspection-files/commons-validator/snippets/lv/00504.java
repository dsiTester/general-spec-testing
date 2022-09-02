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
     * Return an Iterator of the action names contained in this Result.
     * @return The set of action names.
     */
    public Iterator<String> getActions() { // definition of b
        return Collections.unmodifiableMap(hAction).keySet().iterator();
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

        validatorResult.add(validatorName, result, value); // call to a
    }

    public Map<String, Object> getResultValueMap() { // called from test
        Map<String, Object> results = new HashMap<>();

        for (String propertyKey : hResults.keySet()) {
            ValidatorResult vr = this.getValidatorResult(propertyKey);

            for (Iterator<String> x = vr.getActions(); x.hasNext();) { // call to b
                String actionKey = x.next();
                Object result = vr.getResult(actionKey);

                if (result != null && !(result instanceof Boolean)) {
                    results.put(propertyKey, result);
                }
            }
        }

        return results;
    }

}

public class GenericTypeValidatorTest extends AbstractCommonTest {
   /**
    * Tests the us locale
    */
   public void testUSLocale() throws ValidatorException {
      // Create bean to run test on.
      TypeBean info = new TypeBean();
      info.setByte("12");
      info.setShort("129");
      info.setInteger("-144");
      info.setLong("88000");
      info.setFloat("12.1555");
      info.setDouble("129.1551511111");
      info.setDate("12/21/2010");
      localeTest(info, Locale.US); // calls a and b
   }

    /**
     * Tests the locale.
     */
    private Map<String, ?> localeTest(TypeBean info, Locale locale) throws ValidatorException {

        // Construct validator based on the loaded resources
        // and the form key
        Validator validator = new Validator(resources, "typeLocaleForm");
        // add the name bean to the validator as a resource
        // for the validations to be performed on.
        validator.setParameter(Validator.BEAN_PARAM, info);
        validator.setParameter("java.util.Locale", locale);

        // Get results of the validation.
        // throws ValidatorException,
        // but we aren't catching for testing
        // since no validation methods we use
        // throw this
        ValidatorResults results = validator.validate(); // calls a

        assertNotNull("Results are null.", results);

        Map<String, ?> hResultValues = results.getResultValueMap(); // calls b

        assertTrue("Expecting byte result to be an instance of Byte for locale: "+locale, (hResultValues.get("byte") instanceof Byte)); // assertion fails here
        assertTrue("Expecting short result to be an instance of Short for locale: "+locale, (hResultValues.get("short") instanceof Short));
        assertTrue("Expecting integer result to be an instance of Integer for locale: "+locale, (hResultValues.get("integer") instanceof Integer));
        assertTrue("Expecting long result to be an instance of Long for locale: "+locale, (hResultValues.get("long") instanceof Long));
        assertTrue("Expecting float result to be an instance of Float for locale: "+locale, (hResultValues.get("float") instanceof Float));
        assertTrue("Expecting double result to be an instance of Double for locale: "+locale, (hResultValues.get("double") instanceof Double));
        assertTrue("Expecting date result to be an instance of Date for locale: "+locale, (hResultValues.get("date") instanceof Date));

        for (String key : hResultValues.keySet()) {
            Object value = hResultValues.get(key);

            assertNotNull("value ValidatorResults.getResultValueMap() should not be null for locale: "+locale, value);
        }
        return hResultValues;
    }

}
