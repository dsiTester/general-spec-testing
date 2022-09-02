public class ValidatorResources implements Serializable {

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
    public Form getForm(Locale locale, String formKey) { // called from Validator.validate()
        return this.getForm(locale.getLanguage(), locale.getCountry(), locale
                .getVariant(), formKey);
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

}

public class Validator implements Serializable {
    public ValidatorResults validate() throws ValidatorException { // called from test
        ...
        Form form = this.resources.getForm(locale, this.formName); // call to a
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
        ValidatorResources resources = setupDateResources(property, action);

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate(); // calls a and b

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property)); // assertion fails here
   }
}
