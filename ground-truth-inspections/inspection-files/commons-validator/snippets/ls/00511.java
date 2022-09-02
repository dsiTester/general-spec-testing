public class Var implements Cloneable, Serializable {
    /**
     * Gets the name of the variable.
     * @return The name of the variable.
     */
    public String getName() {   // definition of a
        return this.name;
    }

    /**
     * Gets the value of the variable.
     * @return The value of the variable.
     */
    public String getValue() {  // definition of b
        return this.value;
    }
}

public class Field implements Cloneable, Serializable {
    /**
     * Add a <code>Var</code> to the <code>Field</code>.
     * @param v The Validator Argument.
     */
    public void addVar(Var v) {               // indirectly called from digester?
        this.getVarMap().put(v.getName(), v); // call to a
    }

    void process(Map<String, String> globalConstants, Map<String, String> constants) {
        ...
        // Process Var Constant Replacement
        for (String key1 : getVarMap().keySet()) {
            String key2 = TOKEN_START + TOKEN_VAR + key1 + TOKEN_END;
            Var var = this.getVar(key1);
            String replaceValue = var.getValue(); // call to b

            this.processMessageComponents(key2, replaceValue);
        }

        hMsgs.setFast(true);
    }
}

public class RetrieveFormTest extends TestCase {

    /**
     * Load <code>ValidatorResources</code> from multiple xml files.
     */
    @Override
    protected void setUp() throws IOException, SAXException {
        InputStream[] streams =
            new InputStream[] {
                this.getClass().getResourceAsStream(
                    "RetrieveFormTest-config.xml")};

        this.resources = new ValidatorResources(streams); // calls a and b

        for (InputStream stream : streams) {
            stream.close();
        }
    }

   /**
    * Test a form not defined
    */
    public void testFormNotFound() {

        String formKey = "INVALID_NAME";

        // *** US locale ***
        checkFormNotFound(Locale.US, formKey);

        // *** French locale ***
        checkFormNotFound(Locale.FRENCH, formKey);

        // *** France locale ***
        checkFormNotFound(Locale.FRANCE, formKey);

        // *** Candian (English) locale ***
        checkFormNotFound(Locale.CANADA, formKey);

        // *** Candian French locale ***
        checkFormNotFound(Locale.CANADA_FRENCH, formKey);

        // *** Candian French Variant locale ***
        checkFormNotFound(CANADA_FRENCH_XXX, formKey);


    }

    private void checkFormNotFound(Locale locale, String formKey) {

        // Retrieve the Form
        Form testForm = resources.getForm(locale, formKey);
        assertNull("Form '" +formKey+"' not null for locale " + locale, testForm);

    }

}
