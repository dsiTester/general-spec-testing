public class ValidatorResources implements Serializable {

    /**
     * <p>Gets a <code>FormSet</code> based on the language, country
     *    and variant.</p>
     * @param language The locale's language.
     * @param country The locale's country.
     * @param variant The locale's language variant.
     * @return The FormSet for a locale.
     * @since Validator 1.2
     */
    FormSet getFormSet(String language, String country, String variant) { // definition of a

        String key = buildLocale(language, country, variant);

        if (key.isEmpty()) {
            return defaultFormSet;
        }

        return getFormSets().get(key);
    }

    /**
     * Process the <code>ValidatorResources</code> object. Currently sets the
     * <code>FastHashMap</code> s to the 'fast' mode and call the processes
     * all other resources. <strong>Note </strong>: The framework calls this
     * automatically when ValidatorResources is created from an XML file. If you
     * create an instance of this class by hand you <strong>must </strong> call
     * this method when finished.
     */
    public void process() {     // definition of b
        hFormSets.setFast(true);
        hConstants.setFast(true);
        hActions.setFast(true);

        this.processForms();
    }

    public ValidatorResources(InputStream[] streams)
            throws IOException, SAXException {

        Digester digester = initDigester();
        for (int i = 0; i < streams.length; i++) {
            if (streams[i] == null) {
                throw new IllegalArgumentException("Stream[" + i + "] is null");
            }
            digester.push(this);
            digester.parse(streams[i]); // probably calls a?
        }

        this.process();         // call to b
    }

}

public class FormSetFactory extends AbstractObjectCreationFactory {
    private FormSet createFormSet(ValidatorResources resources,
                                  String language,
                                  String country,
                                  String variant) throws Exception { // called via digester

        // Retrieve existing FormSet for the language/country/variant
        FormSet formSet = resources.getFormSet(language, country, variant); // call to a
        if (formSet != null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("FormSet[" + formSet.displayKey() + "] found - merging.");
            }
            return formSet;
        }

        ...
        return formSet;

    }

}

public class DateTest extends AbstractCommonTest {

    /**
     * Load <code>ValidatorResources</code> from
     * validator-numeric.xml.
     */
    @Override
    protected void setUp() throws IOException, SAXException {
        // Load resources
        loadResources("DateTest-config.xml"); // calls a and b
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
