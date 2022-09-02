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
     * Return an instance of the validation class or null if the validation
     * method is static so does not require an instance to be executed.
     */
    private Object getValidationClassInstance() throws ValidatorException { // definition of b
        if (Modifier.isStatic(this.validationMethod.getModifiers())) {
            this.instance = null;

        } else {
            if (this.instance == null) {
                try {
                    this.instance = this.validationClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    String msg1 =
                        "Couldn't create instance of "
                            + this.classname
                            + ".  "
                            + e.getMessage();

                    throw new ValidatorException(msg1);
                }
            }
        }

        return this.instance;
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
                    this.loadValidationMethod(); // throws exception; refer to below
                }
            }
            Object[] paramValues = this.getParameterValues(params);
            ...
            Object result = null;
            try {
                result =
                    validationMethod.invoke(
                        getValidationClassInstance(), // call to b
                        paramvalues);

            } ...
            ...
            boolean valid = this.isValid(result);
            if (!valid || (valid && !onlyReturnErrors(params))) {
                results.add(field, this.name, valid, result);
            }
            ...
        } catch (Exception e) { // exception is caught
            if (e instanceof ValidatorException) {
                throw (ValidatorException) e; // the exception propagates outside of this catch block
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
                this.validationClass.getMethod(this.method, this.parameterClasses); // throws NoSuchMethodException because this.parameterClasses is a size 0 list, when it shouldn't be

        } catch (NoSuchMethodException e) {
            throw new ValidatorException("No such validation method: " +
                e.getMessage());
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

        return va.executeValidationMethod(this, params, results, pos); // call to b
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
        va.setMethodParams("java.lang.Object,org.apache.commons.validator.Field"); // call to b

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
