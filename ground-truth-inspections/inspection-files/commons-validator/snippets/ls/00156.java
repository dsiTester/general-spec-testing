public class FormSet implements Serializable {

    /**
     * Sets the equivalent of the language component of <code>Locale</code>.
     *
     * @param language  The new language value
     */
    public void setLanguage(String language) { // definition of a
        this.language = language;
    }

    /**
     * Sets the equivalent of the country component of <code>Locale</code>.
     *
     * @param country  The new country value
     */
    public void setCountry(String country) { // definition of b
        this.country = country;
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
        formSet.setLanguage(language); // call to a
        formSet.setCountry(country); // call to b
        formSet.setVariant(variant);

        // Add the FormSet to the validator resources
        resources.addFormSet(formSet);

        if (getLog().isDebugEnabled()) {
            getLog().debug("FormSet[" + formSet.displayKey() + "] created.");
        }

        return formSet;

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
