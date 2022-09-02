public class Validator implements Serializable {

    /**
     * Performs validations based on the configured resources.
     *
     * @return The <code>Map</code> returned uses the property of the
     * <code>Field</code> for the key and the value is the number of error the
     * field had.
     * @throws ValidatorException If an error occurs during validation
     */
    public ValidatorResults validate() throws ValidatorException { // definition of a
        Locale locale = (Locale) this.getParameterValue(LOCALE_PARAM);

        if (locale == null) {
            locale = Locale.getDefault();
        }

        this.setParameter(VALIDATOR_PARAM, this);

        Form form = this.resources.getForm(locale, this.formName);
        if (form != null) {
            this.setParameter(FORM_PARAM, form);
            return form.validate(
                this.parameters,
                this.resources.getValidatorActions(),
                this.page,
                this.fieldName);
        }

        return new ValidatorResults();
    }

    /**
     * Return the class loader to be used for instantiating application objects
     * when required.  This is determined based upon the following rules:
     * <ul>
     * <li>The class loader set by <code>setClassLoader()</code>, if any</li>
     * <li>The thread context class loader, if it exists and the
     *     <code>useContextClassLoader</code> property is set to true</li>
     * <li>The class loader used to load the Digester class itself.
     * </ul>
     * @return the class loader.
     */
    public ClassLoader getClassLoader() { // definition of b
        if (this.classLoader != null) {
            return this.classLoader;
        }

        if (this.useContextClassLoader) {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            if (contextLoader != null) {
                return contextLoader;
            }
        }

        return this.getClass().getClassLoader();
    }

}

public class Form implements Serializable {
    ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions, int page, String fieldName)
        throws ValidatorException {
        ValidatorResults results = new ValidatorResults();
        params.put(Validator.VALIDATOR_RESULTS_PARAM, results);

        // Only validate a single field if specified
        if (fieldName != null) {
            ...
        } else {
            Iterator<Field> fields = this.lFields.iterator();
            while (fields.hasNext()) {
                Field field = fields.next();

                params.put(Validator.FIELD_PARAM, field);

                if (field.getPage() <= page) {
                    results.merge(field.validate(params, actions)); // calls b
                }
            }
        }

        return results;
    }
}

public class Field implements Cloneable, Serializable {
    public ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions)
        throws ValidatorException { // called from Form.validate()
        ...
        for (int fieldNumber = 0; fieldNumber < numberOfFieldsToValidate; fieldNumber++) {

            ValidatorResults results = new ValidatorResults();
            synchronized(dependencyList) {
                Iterator<String> dependencies = this.dependencyList.iterator();
                while (dependencies.hasNext()) {
                    String depend = dependencies.next();
                    ...
                    boolean good =
                        validateForRule(action, results, actions, params, fieldNumber); // calls b

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
        throws ValidatorException {

        ValidatorResult result = results.getValidatorResult(this.getKey());
        ...
        return va.executeValidationMethod(this, params, results, pos); // calls b
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
            if (this.validationMethod == null) {
                synchronized(this) {
                    ClassLoader loader = this.getClassLoader(params); // calls b
                    this.loadValidationClass(loader);
                    this.loadParameterClasses(loader);
                    this.loadValidationMethod();
                }
            }
            ...
        } catch (Exception e) { // exception swallowed here
            if (e instanceof ValidatorException) {
                throw (ValidatorException) e;
            }

            getLog().error(
                "Unhandled exception thrown during validation: " + e.getMessage(),
                e);

            results.add(field, this.name, false);
            return false;
        }

        return true;
    }

    private ClassLoader getClassLoader(Map<String, Object> params) { // called from above
        Validator v = (Validator) params.get(Validator.VALIDATOR_PARAM);
        return v.getClassLoader(); // call to b
    }
}

public class DateTest extends AbstractCommonTest {
    /**
     * Tests the date validation.
     */
    public void testValidDate() throws ValidatorException { // invalidated test
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("12/01/2005");
        valueTest(info, true);
    }

    /**
     * Utlity class to run a test on a value.
     *
     * @param    info    Value to run test on.
     * @param    passed    Whether or not the test is expected to pass.
     */
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
        ValidatorResults results = validator.validate(); // call to a; calls b

        assertNotNull("Results are null.", results);

        ValidatorResult result = results.getValidatorResult("value");

        assertNotNull(ACTION + " value ValidatorResult should not be null.", result);
        assertTrue(ACTION + " value ValidatorResult should contain the '" + ACTION + "' action.", result.containsAction(ACTION));
        assertTrue(ACTION + " value ValidatorResult for the '" + ACTION + "' action should have " + (passed ? "passed" : "failed") + ".", (passed ? result.isValid(ACTION) : !result.isValid(ACTION)));
    }

}
