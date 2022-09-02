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
     * Accessor method for Log instance.
     *
     * The Log instance variable is transient and
     * accessing it through this method ensures it
     * is re-initialized when this instance is
     * de-serialized.
     *
     * @return The Log instance.
     */
    private Log getLog() {      // definition of b
        if (log == null) {
            log =  LogFactory.getLog(ValidatorResources.class);
        }
        return log;
    }

    public ValidatorResources(InputStream[] streams)
            throws IOException, SAXException { // called from test

        Digester digester = initDigester(); // call to a
        for (int i = 0; i < streams.length; i++) {
            if (streams[i] == null) {
                throw new IllegalArgumentException("Stream[" + i + "] is null"); // throws expected exception
            }
            digester.push(this);
            digester.parse(streams[i]);
        }

        this.process();
    }

}

public class ValidatorResourcesTest extends TestCase {

    /**
     * Constructor.
     */
    public ValidatorResourcesTest(String name) {
        super(name);
    }

    /**
     * Test null Input Stream for Validator Resources.
     */
    public void testNullInputStream() throws Exception {

        try {
            new ValidatorResources((InputStream)null); // calls a and b
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // expected result
            // System.out.println("Exception: " + e);
        }

    }

}
