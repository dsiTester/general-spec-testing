xpublic class Field implements Cloneable, Serializable {

    /**
     * Returns a Map of String Var names to Var objects.
     * @since Validator 1.2.0
     * @return A Map of the Field's variables.
     */
    @SuppressWarnings("unchecked") // FastHashMap does not support generics
    protected Map<String, Var> getVarMap() { // definition of a
        return hVars;
    }

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
        throws ValidatorException { // definition of b

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
                        validateForRule(action, results, actions, params, fieldNumber); // method called via reflection here leads to expected exception in the validated case.

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

    void process(Map<String, String> globalConstants, Map<String, String> constants) { // indirectly called from test
        ...
        // Process Var Constant Replacement
        for (String key1 : getVarMap().keySet()) { // call to a
            String key2 = TOKEN_START + TOKEN_VAR + key1 + TOKEN_END;
            Var var = this.getVar(key1);
            String replaceValue = var.getValue();

            this.processMessageComponents(key2, replaceValue);
        }

        hMsgs.setFast(true);
    }

}

public class Form implements Serializable {
    ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions, int page, String fieldName)
        throws ValidatorException { // indirectly called from Validator.validate()
        ValidatorResults results = new ValidatorResults();
        params.put(Validator.VALIDATOR_RESULTS_PARAM, results);

        // Only validate a single field if specified
        if (fieldName != null) {
            Field field = getFieldMap().get(fieldName);

            if (field == null) {
               throw new ValidatorException("Unknown field "+fieldName+" in form "+getName());
            }
            params.put(Validator.FIELD_PARAM, field);

            if (field.getPage() <= page) {
               results.merge(field.validate(params, actions)); // call to b in invalidated test
            }
        } else {
            Iterator<Field> fields = this.lFields.iterator();
            while (fields.hasNext()) {
                Field field = fields.next();

                params.put(Validator.FIELD_PARAM, field);

                if (field.getPage() <= page) {
                    results.merge(field.validate(params, actions)); // call to b in validated test
                }
            }
        }

        return results;
    }

}

public class ExceptionTest extends AbstractCommonTest {

    @Override
    protected void setUp() throws IOException, SAXException {
        loadResources("ExceptionTest-config.xml"); // calls a
    }

    /**
     * tests handling of checked exceptions - should become
     * ValidatorExceptions.
     */
    public void testValidatorException() { // validated test
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
            validator.validate(); // calls b; throws expected exception
            fail("ValidatorException should occur here!");
        } catch (ValidatorException expected) {
            assertTrue("VALIDATOR-EXCEPTION".equals(expected.getMessage()));
        }
    }

}

public class ValidatorTest extends TestCase {

   public void testOnlyValidateField() throws ValidatorException { // invalidated test
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action); // calls a

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate(); // calls b

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property));
   }

    private ValidatorResources setupDateResources(String property, String action) {

        ValidatorResources resources = new ValidatorResources();

        ValidatorAction va = new ValidatorAction();
        va.setName(action);
        va.setClassname("org.apache.commons.validator.ValidatorTest");
        va.setMethod("formatDate"); // setting up the call to below
        va.setMethodParams("java.lang.Object,org.apache.commons.validator.Field");
        ...
        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();    // calls a

        return resources;
    }
}
