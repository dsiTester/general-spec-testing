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
     * Gets the name of the validator action.
     * @return Validator Action name.
     */
    public String getName() {   // definition of b
        return name;
    }

}

public class ValidatorResources implements Serializable {
    public void addValidatorAction(ValidatorAction va) { // called from test
        va.init();

        getActions().put(va.getName(), va); // call to b

        if (getLog().isDebugEnabled()) {
            getLog().debug("Add ValidatorAction: " + va.getName() + "," + va.getClassname());
        }
    }

}

public class ValidatorTest extends TestCase {
   public void testOnlyValidateField() throws ValidatorException {
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        validatorresources resources = setupDateResources(property, action); // calls a and b

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate();

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

        resources.addValidatorAction(va); // calls b
        resources.addFormSet(fs);
        resources.process();

        return resources;
    }
}
