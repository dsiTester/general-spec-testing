public class ValidatorResources implements Serializable {
    /**
     * Create a <code>Rule</code> to handle <code>arg0-arg3</code>
     * elements. This will allow validation.xml files that use the
     * versions of the DTD prior to Validator 1.2.0 to continue
     * working.
     */
    private void addOldArgRules(Digester digester) { // definition of a

        // Create a new rule to process args elements
        Rule rule = new Rule() {
            @Override
            public void begin(String namespace, String name,
                               Attributes attributes) throws Exception {
                // Create the Arg
                Arg arg = new Arg();
                arg.setKey(attributes.getValue("key"));
                arg.setName(attributes.getValue("name"));
                if ("false".equalsIgnoreCase(attributes.getValue("resource"))) {
                    arg.setResource(false);
                }
                try {
                    final int length = "arg".length(); // skip the arg prefix
                    arg.setPosition(Integer.parseInt(name.substring(length)));
                } catch (Exception ex) {
                    getLog().error("Error parsing Arg position: "
                               + name + " " + arg + " " + ex);
                }

                // Add the arg to the parent field
                ((Field)getDigester().peek(0)).addArg(arg);
            }
        };

        // Add the rule for each of the arg elements
        digester.addRule(ARGS_PATTERN + "0", rule);
        digester.addRule(ARGS_PATTERN + "1", rule);
        digester.addRule(ARGS_PATTERN + "2", rule);
        digester.addRule(ARGS_PATTERN + "3", rule);

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

    private Digester initDigester() { // called from ValidatorResources() when using XML file to specify validation
        ...
        // Add rules for arg0-arg3 elements
        addOldArgRules(digester); // call to b

        // register DTDs
        for (int i = 0; i < REGISTRATIONS.length; i += 2) {
            URL url = this.getClass().getResource(REGISTRATIONS[i + 1]);
            if (url != null) {
                digester.register(REGISTRATIONS[i], url.toString());
            }
        }
        return digester;
    }

    public Form getForm(Locale locale, String formKey) { // called from Validator.validate()
        return this.getForm(locale.getLanguage(), locale.getCountry(), locale
                .getVariant(), formKey);
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
        valueTest(info, true);  // calls b
    }

    /**
     * Utlity class to run a test on a value.
     *
     * @param    info    Value to run test on.
     * @param    passed    Whether or not the test is expected to pass.
     */
    protected void valueTest(Object info, boolean passed) throws ValidatorException {
        // Construct validator based on the loaded resources
        // and the form key
        Validator validator = new Validator(resources, FORM_KEY);
        // add the name bean to the validator as a resource
        // for the validations to be performed on.
        validator.setParameter(Validator.BEAN_PARAM, info);
        validator.setParameter(Validator.LOCALE_PARAM, Locale.US);

        // Get results of the validation.
        // throws ValidatorException,
        // but we aren't catching for testing
        // since no validation methods we use
        // throw this
        ValidatorResults results = validator.validate(); // calls b

        assertNotNull("Results are null.", results);

        ValidatorResult result = results.getValidatorResult("value");

        assertNotNull(ACTION + " value ValidatorResult should not be null.", result);
        assertTrue(ACTION + " value ValidatorResult should contain the '" + ACTION + "' action.", result.containsAction(ACTION));
        assertTrue(ACTION + " value ValidatorResult for the '" + ACTION + "' action should have " + (passed ? "passed" : "failed") + ".", (passed ? result.isValid(ACTION) : !result.isValid(ACTION)));
    }

}

abstract public class AbstractCommonTest extends TestCase {
    /**
     * Load <code>ValidatorResources</code> from
     * validator-numeric.xml.
     */
    protected void loadResources(String file) throws IOException, SAXException { // called from test setUp()
        // Load resources
        try (InputStream in = this.getClass().getResourceAsStream(file)) {
            resources = new ValidatorResources(in); // calls a
        }
    }
}
