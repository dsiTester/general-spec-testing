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
     * Add a <code>FormSet</code> to this <code>ValidatorResources</code>
     * object.  It will be associated with the <code>Locale</code> of the
     * <code>FormSet</code>.
     * @param fs The form set to add.
     * @since Validator 1.1
     */
    public void addFormSet(FormSet fs) { // definition of b
        String key = this.buildKey(fs);
        if (key.isEmpty()) {// there can only be one default formset
            if (getLog().isWarnEnabled() && defaultFormSet != null) {
                // warn the user he might not get the expected results
                getLog().warn("Overriding default FormSet definition.");
            }
            defaultFormSet = fs;
        } else {
            FormSet formset = getFormSets().get(key);
            if (formset == null) {// it hasn't been included yet
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Adding FormSet '" + fs.toString() + "'.");
                }
            } else if (getLog().isWarnEnabled()) {// warn the user he might not
                                                // get the expected results
                getLog()
                        .warn("Overriding FormSet definition. Duplicate for locale: "
                                + key);
            }
            getFormSets().put(key, fs);
        }
    }

    public void addValidatorAction(ValidatorAction va) { // called from digester
        va.init();

        getActions().put(va.getName(), va); // call to a; NullPointerException here

        if (getLog().isDebugEnabled()) {
            getLog().debug("Add ValidatorAction: " + va.getName() + "," + va.getClassname());
        }
    }

}

public class FormSetFactory extends AbstractObjectCreationFactory {

    private FormSet createFormSet(ValidatorResources resources,
                                  String language,
                                  String country,
                                  String variant) throws Exception { // called from digester

        // Retrieve existing FormSet for the language/country/variant
        FormSet formSet = resources.getFormSet(language, country, variant);
        ...
        // Create a new FormSet for the language/country/variant
        formSet = new FormSet();
        formSet.setLanguage(language);
        formSet.setCountry(country);
        formSet.setVariant(variant);

        // Add the FormSet to the validator resources
        resources.addFormSet(formSet); // call to b
        // NOTE: can call a after b via uncommenting the below
        // System.out.println(resources.getActions());

        ...
        return formSet;

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
