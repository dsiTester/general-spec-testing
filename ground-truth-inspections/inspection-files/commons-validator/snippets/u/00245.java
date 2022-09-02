public class ValidatorAction implements Serializable {

    /**
     * Returns the ClassLoader set in the Validator contained in the parameter
     * Map.
     */
    private ClassLoader getClassLoader(Map<String, Object> params) { // definition of a
        Validator v = (Validator) params.get(Validator.VALIDATOR_PARAM);
        return v.getClassLoader();
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
                    this.loadValidationClass(loader); // throws NullPointerException
                    this.loadParameterClasses(loader);
                    this.loadValidationMethod();
                }
            }
            ...
            Object result = null;
            try {
                result =
                    validationMethod.invoke(
                        getValidationClassInstance(), // call to b
                        paramValues);

            } ...
            ...
        } ...

        return true;
    }

    private void loadValidationClass(ClassLoader loader)
        throws ValidatorException { // called from above

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
        ValidatorResources resources = setupDateResources(property, action); // calls a

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

        FormSet fs = new FormSet();
        Form form = new Form();
        form.setName("testForm");
        Field field = new Field();
        field.setProperty(property);
        field.setDepends(action);
        form.addField(field);
        fs.addForm(form);

        resources.addValidatorAction(va); // calls a
        resources.addFormSet(fs);
        resources.process();

        return resources;
    }

}
