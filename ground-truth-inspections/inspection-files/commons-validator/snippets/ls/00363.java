public class ValidatorAction implements Serializable {

    /**
     * Sets the class of the validator action.
     * @param classname Class name of the validator Action.
     */
    public void setClassname(String classname) { // definition of a
        this.classname = classname;
    }

    /**
     * Read a javascript function from a file.
     * @param javascriptFileName The file containing the javascript.
     * @return The javascript function or null if it could not be loaded.
     */
    private String readJavascriptFile(String javascriptFileName) { // definition of b
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

        String javascriptFileName = this.formatJavascriptFileName();

        if (getLog().isTraceEnabled()) {
            getLog().trace("  Loading js function '" + javascriptFileName + "'");
        }

        this.javascript = this.readJavascriptFile(javascriptFileName); // call to b

        if (getLog().isTraceEnabled()) {
            getLog().trace("  Loading javascript function completed");
        }

    }

    boolean executeValidationMethod(
        Field field,
        // TODO What is this the correct value type?
        // both ValidatorAction and Validator are added as parameters
        Map<String, Object> params,
        ValidatorResults results,
        int pos)
        throws ValidatorException { // uses ValidatorAction.classname indirectly called from Validator.validate()

        params.put(Validator.VALIDATOR_ACTION_PARAM, this);
        try {
            if (this.validationMethod == null) {
                synchronized(this) {
                    ClassLoader loader = this.getClassLoader(params);
                    this.loadValidationClass(loader); // uses ValidatorAction.classname, the field variable set by a - would throw NullPointerException if the state was not restored
                    this.loadParameterClasses(loader);
                    this.loadValidationMethod();
                }
            }
            Object[] paramValues = this.getParameterValues(params);
            ...
            Object result = null;
            try {
                result =
                    validationMethod.invoke(
                        getValidationClassInstance(),
                        paramvalues);

            } ...
            ...
            boolean valid = this.isValid(result);
            if (!valid || (valid && !onlyReturnErrors(params))) {
                results.add(field, this.name, valid, result);
            }
            ...
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
        ...
    }

    /**
     * Load the Class object for the configured validation class name.
     * @param loader The ClassLoader used to load the Class object.
     * @throws ValidatorException
     */
    private void loadValidationClass(ClassLoader loader)
        throws ValidatorException {

        if (this.validationClass != null) {
            return;
        }

        try {
            this.validationClass = loader.loadClass(this.classname); // throws NullPointerException here
        } catch (ClassNotFoundException e) {
            throw new ValidatorException(e.toString());
        }
    }

}

public class ValidatorTest extends TestCase {
   public void testOnlyValidateField() throws ValidatorException {
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        validatorresources resources = setupDateResources(property, action); // calls a and b

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate();

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property));
   }

    private ValidatorResources setupDateResources(String property, String action) {

        ValidatorResources resources = new ValidatorResources();

        ValidatorAction va = new ValidatorAction();
        va.setName(action);
        va.setClassname("org.apache.commons.validator.ValidatorTest"); // call to a
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

        resources.addValidatorAction(va); // calls b
        resources.addFormSet(fs);
        resources.process();

        return resources;
    }
}
