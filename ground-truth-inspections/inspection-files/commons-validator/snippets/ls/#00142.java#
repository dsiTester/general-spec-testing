public class FormSet implements Serializable {

    /**
     * Processes all of the <code>Form</code>s.
     *
     * @param globalConstants  Global constants
     */
    synchronized void process(Map<String, String> globalConstants) { // definition of a
        for (Form f : forms.values()) {
            f.process(globalConstants, constants, forms);
        }

        processed = true;
    }

    /**
     * Retrieve a <code>Form</code> based on the form name.
     *
     * @param formName  The form name
     * @return          The form
     */
    public Form getForm(String formName) { // definition of b
        return this.forms.get(formName);
    }
}

public class ValidatorResources implements Serializable {

    private void processForms() {     // indirectly called from ValidatorResources.validate()
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        // NOTE: one can call b before a by uncommenting the following
        // System.out.println(defaultFormSet.getForm("nameForm"));
        defaultFormSet.process(getConstants()); // call to a
        ...
    }

    public Form getForm(String language, String country, String variant,
            String formKey) {   // indirectly called from Validator.validate()

        Form form = null;
        ...
        // Try default formset
        if (form == null) {
            form = defaultFormSet.getForm(formKey); // call to b
            key = "default";
        }

        if (form == null) {
            if (getLog().isWarnEnabled()) {
                getLog().warn("Form '" + formKey + "' not found for locale '" +
                         localeKey + "'");
            }
        } else {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Form '" + formKey + "' found in formset '" +
                          key + "' for locale '" + localeKey + "'");
            }
        }

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

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();    // calls a

        return resources;
    }

}
