public class Form implements Serializable {
    /**
     * Sets the name/key of the set of validation rules.
     *
     * @param name  The new name value
     */
    public void setName(String name) { // definition of a
        this.name = name;
    }

    /**
     * Whether or not the this <code>Form</code> was processed for replacing
     * variables in strings with their values.
     *
     * @return   The processed value
     * @since    Validator 1.2.0
     */
    public boolean isProcessed() {
        return processed;
    }

    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) { // indirectly called from ValidatorResources.process()
        if (isProcessed()) {    // call to b
            return;
        }

        int n = 0;//we want the fields from its parent first
        if (isExtending()) {
            ...
        }
    }
}

public class ValidatorTest extends TestCase {
    public void testManualObject() {
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action); // calls a and b

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm");
        validator.setParameter(Validator.BEAN_PARAM, bean);

        try {
            ValidatorResults results = validator.validate();
            ...
            assertTrue("ValidatorResult does not contain '" + action + "' validator result.", result.containsAction(action)); // this assertion fails
            ...
        } catch (Exception e) {
            fail("An exception was thrown while calling Validator.validate()"); // exception thrown here
        }
        ...

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
        // NOTE: one can call b before a by uncommenting the following:
        // System.out.println(form.isExtending());
        form.setName("testForm"); // call to a
        Field field = new Field();
        field.setProperty(property);
        field.setDepends(action);
        form.addField(field);
        fs.addForm(form);

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();   // calls b

        return resources;
    }

}
