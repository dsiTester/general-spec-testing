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

    /**
     * Create a <code>Rule</code> to handle <code>arg0-arg3</code>
     * elements. This will allow validation.xml files that use the
     * versions of the DTD prior to Validator 1.2.0 to continue
     * working.
     */
    private void addOldArgRules(Digester digester) { // definition of b

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

    public ValidatorResources(String[] uris)
            throws IOException, SAXException {

        Digester digester = initDigester(); // call to a
        for (String element : uris) {
            digester.push(this); // NullPointerException here
            digester.parse(element);
        }

        this.process();
    }
}

public class EntityImportTest extends AbstractCommonTest {

    /**
     * Tests the entity import loading the <code>byteForm</code> form.
     */
    public void testEntityImport() throws Exception {
        URL url = getClass().getResource("EntityImportTest-config.xml");
        ValidatorResources resources = new ValidatorResources(url.toExternalForm()); // calls a and b
        assertNotNull("Form should be found", resources.getForm(Locale.getDefault(), "byteForm"));
    }

}
