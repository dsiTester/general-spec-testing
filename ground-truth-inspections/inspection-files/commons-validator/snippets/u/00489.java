public class ValidatorResources implements Serializable {

    /**
     *  Initialize the digester.
     */
    private Digester initDigester() { // definition of a
        URL rulesUrl = this.getClass().getResource(VALIDATOR_RULES);
        if (rulesUrl == null) {
            // Fix for Issue# VALIDATOR-195
            rulesUrl = ValidatorResources.class.getResource(VALIDATOR_RULES);
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Loading rules from '" + rulesUrl + "'");
        }
        Digester digester = DigesterLoader.createDigester(rulesUrl);
        digester.setNamespaceAware(true);
        digester.setValidating(true);
        digester.setUseContextClassLoader(true);

        // Add rules for arg0-arg3 elements
        addOldArgRules(digester);

        // register DTDs
        for (int i = 0; i < REGISTRATIONS.length; i += 2) {
            URL url = this.getClass().getResource(REGISTRATIONS[i + 1]);
            if (url != null) {
                digester.register(REGISTRATIONS[i], url.toString());
            }
        }
        return digester;
    }

    /**
     * Get an unmodifiable <code>Map</code> of the <code>ValidatorAction</code>s.
     * @return Map of validator actions.
     */
    public Map<String, ValidatorAction> getValidatorActions() {
        return Collections.unmodifiableMap(getActions());
    }

    public ValidatorResources(InputStream[] streams)
            throws IOException, SAXException {

        Digester digester = initDigester(); // call to a
        for (int i = 0; i < streams.length; i++) {
            if (streams[i] == null) {
                throw new IllegalArgumentException("Stream[" + i + "] is null");
            }
            digester.push(this);
            digester.parse(streams[i]);
        }

        this.process();
    }

}

public class Validator implements Serializable {
    public ValidatorResults validate() throws ValidatorException {
        Locale locale = (Locale) this.getParameterValue(LOCALE_PARAM);

        if (locale == null) {
            locale = Locale.getDefault();
        }

        this.setParameter(VALIDATOR_PARAM, this);

        Form form = this.resources.getForm(locale, this.formName);
        if (form != null) {
            this.setParameter(FORM_PARAM, form);
            return form.validate(
                this.parameters,
                this.resources.getValidatorActions(), // call to b
                this.page,
                this.fieldName);
        }

        return new ValidatorResults();
    }
}
public class MultipleConfigFilesTest extends TestCase {
    /**
     * Load <code>ValidatorResources</code> from multiple xml files.
     */
    @Override
    protected void setUp() throws IOException, SAXException {
        InputStream[] streams =
            new InputStream[] {
                this.getClass().getResourceAsStream(
                    "MultipleConfigFilesTest-1-config.xml"),
                this.getClass().getResourceAsStream(
                    "MultipleConfigFilesTest-2-config.xml")};

        this.resources = new ValidatorResources(streams); // calls a

        for (InputStream stream : streams) {
            stream.close();
        }
    }

    public void testRequiredLastNameShort() throws ValidatorException {
        ...
        // Get results of the validation.
        ValidatorResults results = validator.validate(); // calls b

        assertNotNull("Results are null.", results);

        ValidatorResult firstNameResult = results.getValidatorResult("firstName");
        ValidatorResult lastNameResult = results.getValidatorResult("lastName");

        assertNotNull(firstNameResult);
        assertTrue(firstNameResult.containsAction(ACTION));
        assertTrue(firstNameResult.isValid(ACTION));

        assertNotNull(lastNameResult);
        assertTrue(lastNameResult.containsAction("int"));
        assertTrue(!lastNameResult.isValid("int"));
    }
}

