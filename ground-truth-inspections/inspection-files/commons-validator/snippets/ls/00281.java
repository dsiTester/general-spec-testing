public class ValidatorAction implements Serializable {

    /**
     * Initialize based on set.
     */
    protected void init() {     // definition of a
        this.loadJavascriptFunction();
    }

    /**
     * Converts a List of parameter class names into their values contained in
     * the parameters Map.
     * @param params A Map of class names to parameter values.
     * @return An array containing the value object for each parameter.  This
     * array is in the same order as the given List and is suitable for passing
     * to the validation method.
     */
    private Object[] getParameterValues(Map<String, ? super Object> params) { // definition of b

        Object[] paramValue = new Object[this.methodParameterList.size()];

        for (int i = 0; i < this.methodParameterList.size(); i++) {
            String paramClassName = this.methodParameterList.get(i);
            paramValue[i] = params.get(paramClassName);
        }

        return paramValue;
    }

    protected synchronized void loadJavascriptFunction() { // called from a

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

        this.javascript = this.readJavascriptFile(javascriptFileName);

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
        throws ValidatorException { // indirectly called from Validator.validate()

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
            Object[] paramValues = this.getParameterValues(params); // call to b
            ...
        }
        ...
    }
}

public class ValidatorResources implements Serializable {
    public void addValidatorAction(ValidatorAction va) { // called from commons-digester/test
        va.init();              // call to a

        getActions().put(va.getName(), va);

        if (getLog().isDebugEnabled()) {
            getLog().debug("Add ValidatorAction: " + va.getName() + "," + va.getClassname());
        }
    }

}

public class ValidatorTest extends TestCase {
   public void testOnlyValidateField() throws ValidatorException {
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
