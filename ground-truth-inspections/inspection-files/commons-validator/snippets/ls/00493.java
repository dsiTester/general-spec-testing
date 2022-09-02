public class ValidatorResources implements Serializable {

    /**
     * Process the <code>ValidatorResources</code> object. Currently sets the
     * <code>FastHashMap</code> s to the 'fast' mode and call the processes
     * all other resources. <strong>Note </strong>: The framework calls this
     * automatically when ValidatorResources is created from an XML file. If you
     * create an instance of this class by hand you <strong>must </strong> call
     * this method when finished.
     */
    public void process() {     // definition of a
        hFormSets.setFast(true);
        hConstants.setFast(true);
        hActions.setFast(true);

        this.processForms();
    }

    /**
     * <p>Gets a <code>Form</code> based on the name of the form and the
     * <code>Locale</code> that most closely matches the <code>Locale</code>
     * passed in.  The order of <code>Locale</code> matching is:</p>
     * <ol>
     *    <li>language + country + variant</li>
     *    <li>language + country</li>
     *    <li>language</li>
     *    <li>default locale</li>
     * </ol>
     * @param language The locale's language.
     * @param country The locale's country.
     * @param variant The locale's language variant.
     * @param formKey The key for the Form.
     * @return The validator Form.
     * @since Validator 1.1
     */
    public Form getForm(String language, String country, String variant,
            String formKey) {   // definition of b

        Form form = null;

        // Try language/country/variant
        String key = this.buildLocale(language, country, variant);
        if (!key.isEmpty()) {
            FormSet formSet = getFormSets().get(key);
            if (formSet != null) {
                form = formSet.getForm(formKey);
            }
        }
        String localeKey  = key;


        // Try language/country
        if (form == null) {
            key = buildLocale(language, country, null);
            if (!key.isEmpty()) {
                FormSet formSet = getFormSets().get(key);
                if (formSet != null) {
                    form = formSet.getForm(formKey);
                }
            }
        }

        // Try language
        if (form == null) {
            key = buildLocale(language, null, null);
            if (!key.isEmpty()) {
                FormSet formSet = getFormSets().get(key);
                if (formSet != null) {
                    form = formSet.getForm(formKey);
                }
            }
        }

        // Try default formset
        if (form == null) {
            form = defaultFormSet.getForm(formKey);
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

    private void processForms() {     // called from a
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        defaultFormSet.process(getConstants());
        // Loop through FormSets and merge if necessary
        for (String key : getFormSets().keySet()) {
            FormSet fs = getFormSets().get(key);
            FormSet parent = getParent(fs);
            fs.merge(fs);
        }

        // Process Fully Constructed FormSets
        for (FormSet fs : getFormSets().values()) {
            if (!fs.isProcessed()) {
                fs.process(getConstants());
            }
        }
    }

    public Form getForm(Locale locale, String formKey) { // called from Validator.validate()
        return this.getForm(locale.getLanguage(), locale.getCountry(), locale // call to b
                .getVariant(), formKey);
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

    private ValidatorResources setupDateResources(String property, String action) { // called from above

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
        resources.process();    // call to a

        return resources;
    }

}
