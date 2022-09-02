public class ValidatorResources implements Serializable {
    /**
     * Add a <code>FormSet</code> to this <code>ValidatorResources</code>
     * object.  It will be associated with the <code>Locale</code> of the
     * <code>FormSet</code>.
     * @param fs The form set to add.
     * @since Validator 1.1
     */
    public void addFormSet(FormSet fs) { // definition of a
        String key = this.buildKey(fs);
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
     * <p>Gets a <code>Form</code> based on the name of the form and the
     * <code>Locale</code> that most closely matches the <code>Locale</code>
     * passed in.  The order of <code>Locale</code> matching is:</p>
     * <ol>
     *    <li>language + country + variant</li>
     *    <li>language + country</li>
     *    <li>language</li>
     *    <li>default locale</li>
     * </ol>
     * @param locale The Locale.
     * @param formKey The key for the Form.
     * @return The validator Form.
     * @since Validator 1.1
     */
    public Form getForm(Locale locale, String formKey) { // definition of b
        return this.getForm(locale.getLanguage(), locale.getCountry(), locale
                .getVariant(), formKey);
    }

    public Form getForm(String language, String country, String variant,
            String formKey) {   // called from b
        ...
        // Try default formset
        if (form == null) {
            form = defaultFormSet.getForm(formKey);
            key = "default";
        }

        ...
        return form;

    }
}

public class Validator implements Serializable {
    public ValidatorResults validate() throws ValidatorException { // called from test
        ...

        Form form = this.resources.getForm(locale, this.formName); // call to b
        if (form != null) {
            this.setParameter(FORM_PARAM, form);
            return form.validate(
                this.parameters,
                this.resources.getValidatorActions(),
                this.page,
                this.fieldName);
        }

        return new ValidatorResults();
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
        assertTrue(results.getPropertyNames().contains(property)); // this assertion fails
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
        resources.addFormSet(fs); // call to a
        resources.process();

        return resources;
    }
}
