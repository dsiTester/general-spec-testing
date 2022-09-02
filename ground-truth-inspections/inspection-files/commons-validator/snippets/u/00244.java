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
                    this.loadValidationClass(loader); // throws NullPointerException
                    this.loadParameterClasses(loader);
                    this.loadValidationMethod(); // call to b
                }
            }
            Object[] paramValues = this.getParameterValues(params);
            ...
            // TODO This catch block remains for backward compatibility.  Remove
            // this for Validator 2.0 when exception scheme changes.
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
