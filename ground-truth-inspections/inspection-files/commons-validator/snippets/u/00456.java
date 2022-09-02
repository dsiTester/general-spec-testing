public class ValidatorResources implements Serializable {

    /**
     * Returns a Map of String ValidatorAction names to their ValidatorAction.
     * @return Map of Validator Actions
     * @since Validator 1.2.0
     */
    @SuppressWarnings("unchecked") // FastHashMap is not generic
    protected Map<String, ValidatorAction> getActions() { // definition of a
        return hActions;
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

    public void addValidatorAction(ValidatorAction va) { // called from digester
        va.init();

        getActions().put(va.getName(), va); // call to a; NullPointerException here

        if (getLog().isDebugEnabled()) {
            getLog().debug("Add ValidatorAction: " + va.getName() + "," + va.getClassname());
        }
    }

    public Form getForm(Locale locale, String formKey) { // called from test
        return this.getForm(locale.getLanguage(), locale.getCountry(), locale // call to b
                .getVariant(), formKey);
    }

}

public class EntityImportTest extends AbstractCommonTest {

    public EntityImportTest(String name) {
        super(name);
    }

    /**
     * Tests the entity import loading the <code>byteForm</code> form.
     */
    public void testEntityImport() throws Exception {
        URL url = getClass().getResource("EntityImportTest-config.xml");
        ValidatorResources resources = new ValidatorResources(url.toExternalForm()); // calls a
        assertNotNull("Form should be found", resources.getForm(Locale.getDefault(), "byteForm")); // calls b
        // NOTE: uncomment following to call a after b
        // System.out.println(resources.getActions());
    }

}
