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
     * Converts a List of parameter class names into their Class objects.
     * Stores the output in {@link #parameterClasses}.  This
     * array is in the same order as the given List and is suitable for passing
     * to the validation method.
     * @throws ValidatorException if a class cannot be loaded.
     */
    private void loadParameterClasses(ClassLoader loader)
        throws ValidatorException { // definition of b

        if (this.parameterClasses != null) {
            return;
        }

        Class<?>[] parameterClasses = new Class[this.methodParameterList.size()];

        for (int i = 0; i < this.methodParameterList.size(); i++) {
            String paramClassName = this.methodParameterList.get(i);

            try {
                parameterClasses[i] = loader.loadClass(paramClassName);

            } catch (ClassNotFoundException e) {
                throw new ValidatorException(e.getMessage());
            }
        }

        this.parameterClasses = parameterClasses;
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
                    this.loadParameterClasses(loader); // call to b
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

    /**
     * Load the Method object for the configured validation method name.
     * @throws ValidatorException
     */
    private void loadValidationMethod() throws ValidatorException { // called from above
        if (this.validationMethod != null) {
            return;
        }

        try {
            this.validationMethod =
                this.validationClass.getMethod(this.method, this.parameterClasses); // throws NullPointerException here if a was not called by this point

        } catch (NoSuchMethodException e) {
            throw new ValidatorException("No such validation method: " +
                e.getMessage());
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
