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
     * If the result object is a <code>Boolean</code>, it will return its
     * value.  If not it will return <code>false</code> if the object is
     * <code>null</code> and <code>true</code> if it isn't.
     */
    private boolean isValid(Object result) { // definition of b
        if (result instanceof Boolean) {
            Boolean valid = (Boolean) result;
            return valid.booleanValue();
        }
        return result != null;
    }

    boolean executeValidationMethod(
        Field field,
        // TODO What is this the correct value type?
        // both ValidatorAction and Validator are added as parameters
        Map<String, Object> params,
        ValidatorResults results,
        int pos)
        throws ValidatorException { // indirectly called from Validator.validate()

        // NOTE: one can call b before a like the following:
        // System.out.println(this.isValid(this));

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
                        getValidationClassInstance(), // throws exception
                        paramvalues);

            } ...
            ...
            boolean valid = this.isValid(result); // call to b
            if (!valid || (valid && !onlyReturnErrors(params))) {
                results.add(field, this.name, valid, result);
            }
            ...
        }
        ...
    }

    private Object getValidationClassInstance() throws ValidatorException { // called from above
        if (Modifier.isStatic(this.validationMethod.getModifiers())) { // throws NullPointerException because this.validationMethod was never set
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
