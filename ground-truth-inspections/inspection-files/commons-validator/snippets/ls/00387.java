public class ValidatorAction implements Serializable {
    /**
     * Sets the method parameters for the method.
     * @param methodParams A comma separated list of parameters.
     */
    public void setMethodParams(String methodParams) { // definition of a
        this.methodParams = methodParams;

        this.methodParameterList.clear();

        StringTokenizer st = new StringTokenizer(methodParams, ",");
        while (st.hasMoreTokens()) {
            String value = st.nextToken().trim();

            if (value != null && !value.isEmpty()) {
                this.methodParameterList.add(value);
            }
        }
    }

    /**
     * Returns the ClassLoader set in the Validator contained in the parameter
     * Map.
     */
    private ClassLoader getClassLoader(Map<String, Object> params) { // definition of b
        Validator v = (Validator) params.get(Validator.VALIDATOR_PARAM);
        return v.getClassLoader();
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
                    ClassLoader loader = this.getClassLoader(params); // call to b
                    this.loadValidationClass(loader);
                    this.loadParameterClasses(loader);
                    this.loadValidationMethod(); // throws ValidatorException here
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
                throw (ValidatorException) e; // exception gets propagated forwards
            }

            getLog().error(
                "Unhandled exception thrown during validation: " + e.getMessage(),
                e);

            results.add(field, this.name, false);
            return false;
        }
        ...
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
        va.setMethodParams("java.lang.Object,org.apache.commons.validator.Field"); // call to a

        FormSet fs = new FormSet();
        Form form = new Form();
        form.setName("testForm");
        Field field = new Field();
        field.setProperty(property);
        field.setDepends(action);
        form.addField(field);
        fs.addForm(form);

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();

        return resources;
    }
}
