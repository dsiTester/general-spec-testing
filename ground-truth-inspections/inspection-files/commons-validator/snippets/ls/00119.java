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
     * Add a <code>Field</code> to the <code>Form</code>.
     *
     * @param f  The field
     */
    public void addField(Field f) { // definition of b
        this.lFields.add(f);
        getFieldMap().put(f.getKey(), f);
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
        } catch (Exception e) {
            fail("An exception was thrown while calling Validator.validate()");
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
        form.setName("testForm"); // call to a
        Field field = new Field();
        field.setProperty(property);
        field.setDepends(action);
        form.addField(field);   // call to b
        fs.addForm(form);

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();

        return resources;
    }

}
