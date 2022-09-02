public class Validator implements Serializable {
    /**
     * Return the class loader to be used for instantiating application objects
     * when required.  This is determined based upon the following rules:
     * <ul>
     * <li>The class loader set by <code>setClassLoader()</code>, if any</li>
     * <li>The thread context class loader, if it exists and the
     *     <code>useContextClassLoader</code> property is set to true</li>
     * <li>The class loader used to load the Digester class itself.
     * </ul>
     * @return the class loader.
     */
    public ClassLoader getClassLoader() { // definition of a
        if (this.classLoader != null) {
            return this.classLoader;
        }

        if (this.useContextClassLoader) {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            if (contextLoader != null) {
                return contextLoader;
            }
        }

        return this.getClass().getClassLoader();
    }

    /**
     * Configures which Fields the Validator returns from the validate() method.  Set this
     * to true to only return Fields that failed validation.  By default, validate() returns
     * all fields.
     * @param onlyReturnErrors whether only failed fields are returned.
     */
    public void setOnlyReturnErrors(boolean onlyReturnErrors) { // definition of b
        this.onlyReturnErrors = onlyReturnErrors;
    }
}

public class ValidatorAction implements Serializable {
    private ClassLoader getClassLoader(Map<String, Object> params) {
        Validator v = (Validator) params.get(Validator.VALIDATOR_PARAM);
        return v.getClassLoader(); // call to a
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
                    ClassLoader loader = this.getClassLoader(params); // calls a
                    this.loadValidationClass(loader); // throws NullPointerException
                    this.loadParameterClasses(loader);
                    this.loadValidationMethod(); // sets this.validationMethod to a non-null value, which is why the second call to Validator.validate() in the test doesn't call a
                }
            }
            ...
            Object result = null;
            try {
                result =
                    validationMethod.invoke(
                        getValidationClassInstance(),
                        paramValues);

            } ...

        } catch (Exception e) { // exception swallowed here
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

}

public class ValidatorTest extends TestCase {
   public void testOnlyReturnErrors() throws ValidatorException {
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action);

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm");
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate(); // calls a

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property));

        // Field passed but should not be in results
        validator.setOnlyReturnErrors(true); // call to b
        results = validator.validate();      // doesn't call a
        assertFalse(results.getPropertyNames().contains(property));
   }
}
