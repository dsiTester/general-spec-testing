public class FormSet implements Serializable {
    /**
     * Sets the equivalent of the country component of <code>Locale</code>.
     *
     * @param country  The new country value
     */
    public void setCountry(String country) { // definition of a
        this.country = country;
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

public class FormSetFactory extends AbstractObjectCreationFactory {
    private FormSet createFormSet(ValidatorResources resources,
                                  String language,
                                  String country,
                                  String variant) throws Exception { // indirectly called from commons-digester

        // Retrieve existing FormSet for the language/country/variant
        FormSet formSet = resources.getFormSet(language, country, variant);
        if (formSet != null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("FormSet[" + formSet.displayKey() + "] found - merging.");
            }
            return formSet;
        }

        // Create a new FormSet for the language/country/variant
        formSet = new FormSet();
        formSet.setLanguage(language);
        formSet.setCountry(country); // call to a
        formSet.setVariant(variant);

        // Add the FormSet to the validator resources
        resources.addFormSet(formSet);

        if (getLog().isDebugEnabled()) {
            getLog().debug("FormSet[" + formSet.displayKey() + "] created.");
        }

        return formSet;

    }

}

public class ValidatorResources implements Serializable {

    public Form getForm(String language, String country, String variant,
            String formKey) {

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

public class DateTest extends AbstractCommonTest {
    @Override
    protected void setUp() throws IOException, SAXException {
        // Load resources
        loadResources("DateTest-config.xml"); // calls a and b via commons-digester
    }

    /**
     * Tests the date validation.
     */
    public void testValidDate() throws ValidatorException {
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("12/01/2005");
        valueTest(info, true);
    }

}
