public class Field implements Cloneable, Serializable {
    /**
     * Run the configured validations on this field.  Run all validations
     * in the depends clause over each item in turn, returning when the first
     * one fails.
     * @param params A Map of parameter class names to parameter values to pass
     * into validation methods.
     * @param actions A Map of validator names to ValidatorAction objects.
     * @return A ValidatorResults object containing validation messages for
     * this field.
     * @throws ValidatorException If an error occurs during validation.
     */
    public ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions)
        throws ValidatorException { // definition of a

        if (this.getDepends() == null) {
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
                    String depend = dependencies.next();

                    ValidatorAction action = actions.get(depend);
                    if (action == null) {
                        this.handleMissingAction(depend);
                    }

                    boolean good =
                        validateForRule(action, results, actions, params, fieldNumber); // calls b

                    if (!good) {
                        allResults.merge(results);
                        return allResults;
                    }
                }
            }
            allResults.merge(results);
        }

        return allResults;
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
        throws ValidatorException {

        ...

        return va.executeValidationMethod(this, params, results, pos); // calls b
    }

    /**
     * Gets the property name of the field.
     * @return The field's property name.
     */
    public String getProperty() { // definition of b
        return this.property;
    }
}

public class ValidatorAction implements Serializable {
    boolean executeValidationMethod(
        Field field,
        // TODO What is this the correct value type?
        // both ValidatorAction and Validator are added as parameters
        Map<String, Object> params,
        ValidatorResults results,
        int pos)
        throws ValidatorException { // called from Field.validateForRule()

        params.put(Validator.VALIDATOR_ACTION_PARAM, this);

        try {
            ...
            Object result = null;
            try {
                result =
                    validationMethod.invoke( // invokes ValidatorTest.formatDate()
                        getValidationClassInstance(),
                        paramValues);

            } ...
        }
        return true;
    }

}

public class Form implements Serializable {

    ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions, int page, String fieldName)
        throws ValidatorException { // indirectly called from Validator.validate()
        ValidatorResults results = new ValidatorResults(); // likely replacement value
        params.put(Validator.VALIDATOR_RESULTS_PARAM, results);

        // Only validate a single field if specified
        if (fieldName != null) {
            Field field = getFieldMap().get(fieldName);
            if (field == null) {
               throw new ValidatorException("Unknown field "+fieldName+" in form "+getName());
            }
            params.put(Validator.FIELD_PARAM, field);

            if (field.getPage() <= page) {
               results.merge(field.validate(params, actions)); // call to a
            }
        } else {
            ...
        }

        return results;
    }

}

public class ValidatorTest extends TestCase {

    public void testOnlyValidateField() throws ValidatorException {
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action);

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate(); // calls a and b

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property)); // this assertion fails
    }

   /**
    * Formats a <code>String</code> to a <code>Date</code>.
    * The <code>Validator</code> will interpret a <code>null</code>
    * as validation having failed.
    */
   public static Date formatDate(Object bean, Field field) { // called indirectly from a via reflection
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty()); // call to b
      Date date = null;

      try {
          DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);

         formatter.setLenient(false);

         date = formatter.parse(value);
      } catch (ParseException e) {
         System.out.println("ValidatorTest.formatDate() - " + e.getMessage());
      }

      return date;
   }

}
