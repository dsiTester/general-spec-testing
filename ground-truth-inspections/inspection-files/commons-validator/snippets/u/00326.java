public class ValidatorAction implements Serializable {

    /**
     * Load the Class object for the configured validation class name.
     * @param loader The ClassLoader used to load the Class object.
     * @throws ValidatorException
     */
    private void loadValidationClass(ClassLoader loader)
        throws ValidatorException { // definition of a

        if (this.validationClass != null) {
            return;
        }

        try {
            this.validationClass = loader.loadClass(this.classname);
        } catch (ClassNotFoundException e) {
            throw new ValidatorException(e.toString());
        }
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
                    this.loadValidationClass(loader); // call to a
                    this.loadParameterClasses(loader);
                    this.loadValidationMethod();       // throws exception here; check below
                }
            }
            Object[] paramValues = this.getParameterValues(params); // call to b
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
        }
        ...
    }

    private void loadValidationMethod() throws ValidatorException { // called from executeValidationMethod()
        if (this.validationMethod != null) {
            return;
        }

        try {
            this.validationMethod =
                this.validationClass.getMethod(this.method, this.parameterClasses); // throws NullPointerException because this.validationClass was never populated (when a is delayed)

        } catch (NoSuchMethodException e) {
            throw new ValidatorException("No such validation method: " + // throws exception here
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
