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
     * Returns the onlyReturnErrors setting in the Validator contained in the
     * parameter Map.
     */
    private boolean onlyReturnErrors(Map<String, Object> params) { // definition of b
        Validator v = (Validator) params.get(Validator.VALIDATOR_PARAM);
        return v.getOnlyReturnErrors();
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
            // NOTE: uncomment the following line to call b before a
            // System.out.println(onlyReturnErrors(params));
            if (this.validationMethod == null) {
                synchronized(this) {
                    ClassLoader loader = this.getClassLoader(params);
                    this.loadValidationClass(loader); // call to a
                    this.loadParameterClasses(loader);
                    this.loadValidationMethod(); // throws NullPointerException; refer to below
                }
            }
            ...
            boolean valid = this.isValid(result);
            if (!valid || (valid && !onlyReturnErrors(params))) { // call to b
                results.add(field, this.name, valid, result);
            }
            ...
        } ...

        return true;
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

public class ExtensionTest extends TestCase {
    /**
     * Tests the required validation for first name.
     */
    public void testRequiredFirstName() throws ValidatorException { // validated test
        ...
        // Get results of the validation.
        ValidatorResults results = validator.validate(); // calls a and b

        assertNotNull("Results are null.", results);

        ValidatorResult firstNameResult = results.getValidatorResult("firstName");
        ValidatorResult lastNameResult = results.getValidatorResult("lastName");

        assertNotNull("First Name ValidatorResult should not be null.", firstNameResult);
        assertTrue("First Name ValidatorResult should contain the '" + ACTION +"' action.", firstNameResult.containsAction(ACTION));
        assertTrue("First Name ValidatorResult for the '" + ACTION +"' action should have passed.", firstNameResult.isValid(ACTION)); // this assertion fails

        ...
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

        ValidatorResults results = validator.validate(); // calls a and b

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property));

        // Field passed but should not be in results
        validator.setOnlyReturnErrors(true);
        results = validator.validate(); // second call to caller of a and b
        assertFalse(results.getPropertyNames().contains(property));
   }

   public void testOnlyValidateField() throws ValidatorException { // unknown test
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action);

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate(); // calls a and b

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

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();

        return resources;
    }

}
