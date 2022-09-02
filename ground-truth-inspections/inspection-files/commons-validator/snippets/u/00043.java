public class Field implements Cloneable, Serializable {

    /**
     * Retrieve a variable.
     * @param mainKey The Variable's key
     * @return the Variable
     */
    public Var getVar(String mainKey) { // definition of a
        return getVarMap().get(mainKey); // getVarMap is a naive getter, and this get is on a map
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
        throws ValidatorException { // indirectly invoked from Validator.validate()
        ...
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
                        validateForRule(action, results, actions, params, fieldNumber); // call to b

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

}

public class DateTest extends AbstractCommonTest {
    @Override
    protected void setUp() throws IOException, SAXException {
        // Load resources
        loadResources("DateTest-config.xml"); // calls a
    }

    /**
     * Tests the date validation.
     */
    public void testInvalidDate() throws ValidatorException {
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("12/01as/2005");
        valueTest(info, false);
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
        ValidatorResults results = validator.validate(); // calls b

        assertNotNull("Results are null.", results);

        ValidatorResult result = results.getValidatorResult("value");

        assertNotNull(ACTION + " value ValidatorResult should not be null.", result);
        assertTrue(ACTION + " value ValidatorResult should contain the '" + ACTION + "' action.", result.containsAction(ACTION));
        assertTrue(ACTION + " value ValidatorResult for the '" + ACTION + "' action should have " + (passed ? "passed" : "failed") + ".", (passed ? result.isValid(ACTION) : !result.isValid(ACTION)));
    }

}
