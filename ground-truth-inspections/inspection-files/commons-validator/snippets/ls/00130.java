public class FormSet implements Serializable {

    /**
     * Gets the equivalent of the country component of <code>Locale</code>.
     *
     * @return   The country value
     */
    public String getCountry() { // definition of a
        return country;
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
    public void addFormSet(FormSet fs) { // called from test
        String key = this.buildKey(fs);  // calls a
        ...
    }

    /**
     * Builds a key to store the <code>FormSet</code> under based on it's
     * language, country, and variant values.
     * @param fs The Form Set.
     * @return generated key for a formset.
     */
    protected String buildKey(FormSet fs) {
        return
            this.buildLocale(fs.getLanguage(), fs.getCountry(), fs.getVariant()); // call to a
    }

    public Form getForm(String language, String country, String variant,
            String formKey) {   // indirectly called from Validator.validate()

        ...
        // try default formset
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
        fs.addForm(form);       // calls a

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();

        return resources;
    }

}
