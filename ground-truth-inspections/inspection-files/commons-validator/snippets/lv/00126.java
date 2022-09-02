public class FormSet implements Serializable {
    /**
     * Add a <code>Form</code> to the <code>FormSet</code>.
     *
     * @param f  The form
     */
    public void addForm(Form f) { // definition of a

        String formName = f.getName();
        if (forms.containsKey(formName)) {
            getLog().error("Form '" + formName + "' already exists in FormSet["
                      + this.displayKey() + "] - ignoring.");

        } else {
            forms.put(f.getName(), f);
        }

    }

    /**
     * Retrieve a <code>Form</code> based on the form name.
     *
     * @param formName  The form name
     * @return          The form
     */
    public Form getForm(String formName) {
        return this.forms.get(formName);
    }
}

public class ValidatorResources implements Serializable {
    public Form getForm(String language, String country, String variant,
            String formKey) {

        ...
        // Try default formset
        if (form == null) {
            form = defaultFormSet.getForm(formKey); // call to b
            key = "default";
        }
        ...

        return form;

    }

}
public class ValidatorTest extends TestCase {
   public void testOnlyValidateField() throws ValidatorException {
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action);

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate();

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property)); // assertion fails here
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
        form.setName("testForm");
        Field field = new Field();
        field.setProperty(property);
        field.setDepends(action);
        form.addField(field);
        fs.addForm(form);       // call to a

        resources.addValidatorAction(va);
        resources.addFormSet(fs);   // calls b
        resources.process();

        return resources;
    }

}
