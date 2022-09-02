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
     * Returns a Map of String constant names to their String values.
     * @return Map of Constants
     * @since Validator 1.2.0
     */
    @SuppressWarnings("unchecked") // FastHashMap is not generic
    protected Map<String, String> getConstants() { // definition of b
        return hConstants;
    }

    public void addValidatorAction(ValidatorAction va) { // called from digester
        va.init();

        getActions().put(va.getName(), va); // call to a; NullPointerException here

        if (getLog().isDebugEnabled()) {
            getLog().debug("Add ValidatorAction: " + va.getName() + "," + va.getClassname());
        }
    }

    private void processForms() {     // indirectly called from ValidatorResources()
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        defaultFormSet.process(getConstants()); // call to b
        // NOTE: to call a after b, uncomment below:
        // System.out.println(getActions());
        // Loop through FormSets and merge if necessary
        for (String key : getFormSets().keySet()) {
            FormSet fs = getFormSets().get(key);
            fs.merge(getParent(fs));
        }

        // Process Fully Constructed FormSets
        for (FormSet fs : getFormSets().values()) {
            if (!fs.isProcessed()) {
                fs.process(getConstants());
            }
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
