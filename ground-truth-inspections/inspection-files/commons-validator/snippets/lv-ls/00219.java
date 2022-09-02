public class ValidatorAction implements Serializable {
    /**
     * @return A filename suitable for passing to a
     * ClassLoader.getResourceAsStream() method.
     */
    private String formatJavascriptFileName() { // definition of a
        String fname = this.jsFunction.substring(1);

        if (!this.jsFunction.startsWith("/")) {
            fname = jsFunction.replace('.', '/') + ".js";
        }

        return fname;
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

    protected synchronized void loadJavascriptFunction() { // indirectly called from ValidatorResources.addValidatorAction()

        if (this.javascriptAlreadyLoaded()) {
            return;
        }

        if (getLog().isTraceEnabled()) {
            getLog().trace("  Loading function begun");
        }

        if (this.jsFunction == null) {
            this.jsFunction = this.generateJsFunction();
        }

        String javascriptFileName = this.formatJavascriptFileName(); // call to a

        if (getLog().isTraceEnabled()) {
            getLog().trace("  Loading js function '" + javascriptFileName + "'");
        }

        this.javascript = this.readJavascriptFile(javascriptFileName); // this field variable gets set to null in both tests, regardless of whether the return value of method-a is the "valid" value or not

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
        ...
        return va.executeValidationMethod(this, params, results, pos); // calls b
    }
}

public class ExceptionTest extends AbstractCommonTest {
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
            validator.validate(); // calls b; exception thrown here
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
        va.setMethod("formatDate");
        va.setMethodParams("java.lang.Object,org.apache.commons.validator.Field");

        FormSet fs = new FormSet();
        Form form = new Form();
        form.setName("testForm");
        Field field = new Field();
        field.setProperty(property);
        field.setDepends(action);
        form.addField(field);
        fs.addForm(form);

        resources.addValidatorAction(va); // calls a
        resources.addFormSet(fs);
        resources.process();

        return resources;
    }

}
