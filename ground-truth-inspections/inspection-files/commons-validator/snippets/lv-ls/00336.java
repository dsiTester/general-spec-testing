public class ValidatorAction implements Serializable {

    /**
     * Read a javascript function from a file.
     * @param javascriptFileName The file containing the javascript.
     * @return The javascript function or null if it could not be loaded.
     */
    private String readJavascriptFile(String javascriptFileName) { // definition of a
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        InputStream is = classLoader.getResourceAsStream(javascriptFileName);
        if (is == null) {
            is = this.getClass().getResourceAsStream(javascriptFileName);
        }

        if (is == null) {
            getLog().debug("  Unable to read javascript name "+javascriptFileName);
            return null;
        }

        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is)); // TODO encoding
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

        } catch(IOException e) {
            getLog().error("Error reading javascript file.", e);

        } finally {
            try {
                reader.close();
            } catch(IOException e) {
                getLog().error("Error closing stream to javascript file.", e);
            }
        }

        String function = buffer.toString();
        return function.equals("") ? null : function;
    }

    /**
     * Dynamically runs the validation method for this validator and returns
     * true if the data is valid.
     * @param field
     * @param params A Map of class names to parameter values.
     * @param results
     * @param pos The index of the list property to validate if it's indexed.
     * @throws ValidatorException
     */
    boolean executeValidationMethod(
        Field field,
        // TODO What is this the correct value type?
        // both ValidatorAction and Validator are added as parameters
        Map<String, Object> params,
        ValidatorResults results,
        int pos)
        throws ValidatorException { // definition of b

        params.put(Validator.VALIDATOR_ACTION_PARAM, this);

        try {
            if (this.validationMethod == null) {
                synchronized(this) {
                    ClassLoader loader = this.getClassLoader(params);
                    this.loadValidationClass(loader);
                    this.loadParameterClasses(loader);
                    this.loadValidationMethod();
                }
            }

            Object[] paramValues = this.getParameterValues(params);

            if (field.isIndexed()) {
                this.handleIndexedField(field, pos, paramValues);
            }

            Object result = null;
            try {
                result =
                    validationMethod.invoke(
                        getValidationClassInstance(),
                        paramValues);

            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new ValidatorException(e.getMessage());
            } catch (InvocationTargetException e) {

                if (e.getTargetException() instanceof Exception) {
                    throw (Exception) e.getTargetException();

                }
                if (e.getTargetException() instanceof Error) {
                    throw (Error) e.getTargetException();
                }
            }
            System.out.println(result);
            boolean valid = this.isValid(result);
            if (!valid || (valid && !onlyReturnErrors(params))) {
                results.add(field, this.name, valid, result);
            }

            if (!valid) {
                return false;
            }

            // TODO This catch block remains for backward compatibility.  Remove
            // this for Validator 2.0 when exception scheme changes.
        } catch (Exception e) {
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

    protected void init() {     // indirectly called from commons-digester/test
        this.loadJavascriptFunction(); // calls a
    }

    protected synchronized void loadJavascriptFunction() { // called from above

        ...
        String javascriptFileName = this.formatJavascriptFileName();

        if (getLog().isTraceEnabled()) {
            getLog().trace("  Loading js function '" + javascriptFileName + "'");
        }

        this.javascript = this.readJavascriptFile(javascriptFileName); // call to a

        if (getLog().isTraceEnabled()) {
            getLog().trace("  Loading javascript function completed");
        }

    }

}

public class Field implements Cloneable, Serializable {

    private boolean validateForRule(
        ValidatorAction va,
        ValidatorResults results,
        Map<String, ValidatorAction> actions,
        Map<String, Object> params,
        int pos)
        throws ValidatorException { // indirectly called from Validator.validate()

        ValidatorResult result = results.getValidatorResult(this.getKey());
        if (result != null && result.containsAction(va.getName())) {
            return result.isValid(va.getName());
        }

        if (!this.runDependentValidators(va, results, actions, params, pos)) {
            return false;
        }

        return va.executeValidationMethod(this, params, results, pos); // calls b
    }

}

public class ExceptionTest extends AbstractCommonTest {
    /**
     * Load <code>ValidatorResources</code> from
     * validator-exception.xml.
     */
    @Override
    protected void setUp() throws IOException, SAXException {
        loadResources("ExceptionTest-config.xml"); // calls a
    }

    /**
     * Tests handling of checked exceptions - should become
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
            validator.validate(); // calls b
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
        validatorresources resources = setupDateResources(property, action); // calls a

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
        va.setMethod("formatDate");
        va.setMethodParams("java.lang.Object,org.apache.commons.validator.Field");

        ...
        resources.addValidatorAction(va); // calls a
        resources.addFormSet(fs);
        resources.process();

        return resources;
    }

}
