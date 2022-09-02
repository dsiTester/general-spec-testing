public class FormSet implements Serializable {

    /**
     * Sets the equivalent of the language component of <code>Locale</code>.
     *
     * @param language  The new language value
     */
    public void setLanguage(String language) {
        this.language = language;
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
        formSet.setCountry(country);
        formSet.setVariant(variant);

        // Add the FormSet to the validator resources
        resources.addFormSet(formSet); // calls b

        if (getLog().isDebugEnabled()) {
            getLog().debug("FormSet[" + formSet.displayKey() + "] created.");
        }

        return formSet;

    }

}

public class ValidatorResources implements Serializable {

    public void addFormSet(FormSet fs) { // called from FormSetFactory.createFormSet()
        String key = this.buildKey(fs);  // calls b
        ...
    }

    protected String buildKey(FormSet fs) { // called from above
        return
                this.buildLocale(fs.getLanguage(), fs.getCountry(), fs.getVariant()); // call to b
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
