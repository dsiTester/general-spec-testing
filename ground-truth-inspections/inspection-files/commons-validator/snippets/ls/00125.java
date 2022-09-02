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
     * Gets the equivalent of the country component of <code>Locale</code>.
     *
     * @return   The country value
     */
    public String getCountry() { // definition of b
        return country;
    }
}

public class ValidatorResources implements Serializable {
    public void addFormSet(FormSet fs) { // called from test
        String key = this.buildKey(fs);  // calls b
        if (key.isEmpty()) {// there can only be one default formset
            if (getLog().isWarnEnabled() && defaultFormSet != null) {
                // warn the user he might not get the expected results
                getLog().warn("Overriding default FormSet definition.");
            }
            defaultFormSet = fs;
        } else {
            FormSet formset = getFormSets().get(key);
            if (formset == null) {// it hasn't been included yet
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Adding FormSet '" + fs.toString() + "'.");
                }
            } else if (getLog().isWarnEnabled()) {// warn the user he might not
                                                // get the expected results
                getLog()
                        .warn("Overriding FormSet definition. Duplicate for locale: "
                                + key);
            }
            getFormSets().put(key, fs);
        }
    }

    /**
     * Builds a key to store the <code>FormSet</code> under based on it's
     * language, country, and variant values.
     * @param fs The Form Set.
     * @return generated key for a formset.
     */
    protected String buildKey(FormSet fs) {
        return
            this.buildLocale(fs.getLanguage(), fs.getCountry(), fs.getVariant()); // call to b
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
        fs.addForm(form);       // call to a

        resources.addValidatorAction(va);
        resources.addFormSet(fs);   // calls b
        resources.process();

        return resources;
    }

}
