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
     * Builds a key to store the <code>FormSet</code> under based on it's
     * language, country, and variant values.
     * @param fs The Form Set.
     * @return generated key for a formset.
     */
    protected String buildKey(FormSet fs) { // definition of b
        return
                this.buildLocale(fs.getLanguage(), fs.getCountry(), fs.getVariant());
    }

    public void addValidatorAction(ValidatorAction va) { // called from digester
        va.init();

        getActions().put(va.getName(), va); // call to a; NullPointerException here

        if (getLog().isDebugEnabled()) {
            getLog().debug("Add ValidatorAction: " + va.getName() + "," + va.getClassname());
        }
    }

    public void addFormSet(FormSet fs) { // indirectly called from digester
        String key = this.buildKey(fs);  // call to b
        // NOTE: to call a after b, uncomment below:
        // System.out.println(getActions());
        if (key.isEmpty()) {// there can only be one default formset
            if (getLog().isWarnEnabled() && defaultFormSet != null) {
                // warn the user he might not get the expected results
                getLog().warn("Overriding default FormSet definition.");
            }
            defaultFormSet = fs;
        } else {
            FormSet formset = getFormSets().get(key);
            ...
            getFormSets().put(key, fs);
        }
    }

}

public class ExtensionTest extends TestCase {

    /**
     * Load <code>ValidatorResources</code> from
     * validator-extension.xml.
    */
    @Override
    protected void setUp() throws Exception {
        // Load resources
        try (InputStream in = this.getClass().getResourceAsStream("ExtensionTest-config.xml")) {
            resources = new ValidatorResources(in); // calls a and b
        }
    }

    /**
     * Tests if the order is mantained when extending a form. Parent form fields should
     * preceed self form fields, except if we override the rules.
    */
    public void testOrder() {
        ...
    }

}
