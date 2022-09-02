public class ValidatorAction implements Serializable {

    /**
     * Load the Method object for the configured validation method name.
     * @throws ValidatorException
     */
    private void loadValidationMethod() throws ValidatorException { // definition of a
        if (this.validationMethod != null) {
            return;
        }

        try {
            this.validationMethod =
                this.validationClass.getMethod(this.method, this.parameterClasses);

        } catch (NoSuchMethodException e) {
            throw new ValidatorException("No such validation method: " +
                e.getMessage());
        }
    }

    /**
     * Return an instance of the validation class or null if the validation
     * method is static so does not require an instance to be executed.
     */
    private Object getValidationClassInstance() throws ValidatorException { // definition of b
        if (Modifier.isStatic(this.validationMethod.getModifiers())) { // throws NullPointerException here
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
                    this.loadValidationMethod(); // call to a
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
        validatorresources resources = setupDateResources(property, action);

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate(); // calls a and b

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property));
   }

}
