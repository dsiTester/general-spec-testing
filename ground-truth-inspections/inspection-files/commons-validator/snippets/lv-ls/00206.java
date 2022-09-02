public class Validator implements Serializable {
    /**
     * Returns the value of the specified parameter that will be used during the
     * processing of validations.
     *
     * @param parameterClassName The full class name of the parameter of the
     * validation method that corresponds to the value/instance passed in with it.
     * @return value of the specified parameter.
     */
    public Object getParameterValue(String parameterClassName) { // definition of a
        return this.parameters.get(parameterClassName);
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

    public ValidatorResults validate() throws ValidatorException { // called from test
        // NOTE: uncomment the following to call b before a
        //         ClassLoader l = getClassLoader();
        Locale locale = (Locale) this.getParameterValue(LOCALE_PARAM); // call to a

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

}

public class ValidatorAction implements Serializable {

    boolean executeValidationMethod(
        Field field,
        // TODO What is this the correct value type?
        // both ValidatorAction and Validator are added as parameters
        Map<String, Object> params,
        ValidatorResults results,
        int pos)
        throws ValidatorException { // indirectly called from Validator.validate()

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
            Object result = null;
            try {
                result =
                    validationMethod.invoke(
                        getValidationClassInstance(),
                        paramValues);

            } ...

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

    private ClassLoader getClassLoader(Map<String, Object> params) { // called from executeValidationMethod()
        Validator v = (Validator) params.get(Validator.VALIDATOR_PARAM);
        return v.getClassLoader(); // call to b
    }
}

public class LocaleTest extends AbstractCommonTest {
    public void testLocale1()
        throws ValidatorException { // validated test
        // Create bean to run test on.
        NameBean name = new NameBean();
        name.setFirstName("");
        name.setLastName("");

        valueTest(name, new Locale("en", "US", "TEST1"), false, false, false);
    }

    private void valueTest(Object name, Locale loc, boolean firstGood, boolean lastGood, boolean middleGood)
        throws ValidatorException {

        // Construct validator based on the loaded resources
        // and the form key
        Validator validator = new Validator(resources, FORM_KEY);
        // add the name bean to the validator as a resource
        // for the validations to be performed on.
        validator.setParameter(Validator.BEAN_PARAM, name);
        validator.setParameter(Validator.LOCALE_PARAM, loc);
        // Get results of the validation.
        // throws ValidatorException,
        // but we aren't catching for testing
        // since no validation methods we use
        // throw this
        ValidatorResults results = validator.validate(); // calls a and b

        assertNotNull("Results are null.", results);

        ValidatorResult resultlast = results.getValidatorResult("lastName");
        ValidatorResult resultfirst = results.getValidatorResult("firstName");
        ValidatorResult resultmiddle = results.getValidatorResult("middleName");

        ...
        if (middleGood) {
            assertNull(resultmiddle);
        }
        else {
            assertNotNull(resultmiddle); // assertion fails here
        }
        ...
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
        ValidatorResults results = validator.validate();

        assertNotNull("Results are null.", results);

        ValidatorResult result = results.getValidatorResult("value");

        assertNotNull(ACTION + " value ValidatorResult should not be null.", result);
        assertTrue(ACTION + " value ValidatorResult should contain the '" + ACTION + "' action.", result.containsAction(ACTION));
        assertTrue(ACTION + " value ValidatorResult for the '" + ACTION + "' action should have " + (passed ? "passed" : "failed") + ".", (passed ? result.isValid(ACTION) : !result.isValid(ACTION)));
    }

}
