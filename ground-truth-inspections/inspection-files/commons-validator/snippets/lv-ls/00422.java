public class ValidatorResources implements Serializable {
    /**
     * Add a <code>FormSet</code> to this <code>ValidatorResources</code>
     * object.  It will be associated with the <code>Locale</code> of the
     * <code>FormSet</code>.
     * @param fs The form set to add.
     * @since Validator 1.1
     */
    public void addFormSet(FormSet fs) { // definition of a
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

    /**
     * Returns a Map of String constant names to their String values.
     * @return Map of Constants
     * @since Validator 1.2.0
     */
    @SuppressWarnings("unchecked") // definition of b; FastHashMap is not generic
    protected Map<String, String> getConstants() {
        return hConstants;
    }

    /**
     * <p>Process the <code>Form</code> objects.  This clones the <code>Field</code>s
     * that don't exist in a <code>FormSet</code> compared to its parent
     * <code>FormSet</code>.</p>
     */
    private void processForms() {     // called from ValidatorResources.process()
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        defaultFormSet.process(getConstants()); // call to b
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

    private void processForms() { // MODIFIED VERSION FOR INSPECTION PURPOSES - run with validating test to see that the spec itself is spurious.
        FormSet temp = defaultFormSet;
        defaultFormSet = new FormSet();

        Map<String, String> constants = getConstants();
        defaultFormSet = temp;
        defaultFormSet.process(constants);
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

public class FormSetFactory extends AbstractObjectCreationFactory {
    private FormSet createFormSet(ValidatorResources resources,
                                  String language,
                                  String country,
                                  String variant) throws Exception { // called from commons-digester in validated test

        // Retrieve existing FormSet for the language/country/variant
        FormSet formSet = resources.getFormSet(language, country, variant);
        if (formSet != null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("FormSet[" + formSet.displayKey() + "] found - merging.");
            }
            return formSet;
        }
        ...
        // Add the FormSet to the validator resources
        resources.addFormSet(formSet); // call to a
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
            resources = new ValidatorResources(in); // calls both a and b
        }
    }

    /**
     * Tests if the order is mantained when extending a form. Parent form fields should
     * preceed self form fields, except if we override the rules.
    */
    public void testOrder() {   // validated test

       Form form = resources.getForm(ValidatorResources.defaultLocale, FORM_KEY);
       Form form2 = resources.getForm(ValidatorResources.defaultLocale, FORM_KEY2);

       assertNotNull(FORM_KEY + " is null.", form);
       assertTrue("There should only be 2 fields in " + FORM_KEY, form.getFields().size() == 2); // assertion failed here

       assertNotNull(FORM_KEY2 + " is null.", form2);
       assertTrue("There should only be 2 fields in " + FORM_KEY2, form2.getFields().size() == 2);

       //get the first field
       Field fieldFirstName = form.getFields().get(0);
       //get the second field
       Field fieldLastName = form.getFields().get(1);
       assertTrue("firstName in " + FORM_KEY + " should be the first in the list", fieldFirstName.getKey().equals("firstName"));
       assertTrue("lastName in " + FORM_KEY + " should be the first in the list", fieldLastName.getKey().equals("lastName"));

//     get the second field
       fieldLastName = form2.getFields().get(0);
        //get the first field
        fieldFirstName = form2.getFields().get(1);
        assertTrue("firstName in " + FORM_KEY2 + " should be the first in the list", fieldFirstName.getKey().equals("firstName"));
       assertTrue("lastName in " + FORM_KEY2 + " should be the first in the list", fieldLastName.getKey().equals("lastName"));

    }

}

public class ValidatorTest extends TestCase {
   public void testOnlyValidateField() throws ValidatorException { // invalidated test
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action); // calls a and b

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate();

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property)); // this assertion fails
   }

    private ValidatorResources setupDateResources(String property, String action) {

        ValidatorResources resources = new ValidatorResources();

        ValidatorAction va = new ValidatorAction();
        va.setName(action);
        va.setClassname("org.apache.commons.validator.ValidatorTest");
        va.setMethod("formatDate");
        va.setMethodParams("java.lang.Object,org.apache.commons.validator.Field");

        FormSet fs = new FormSet();
        Form form = new Form();
        form.setName("testForm");
        Field field = new Field();
        field.setProperty(property);
        field.setDepends(action);
        form.addField(field);
        fs.addForm(form);

        resources.addValidatorAction(va);
        resources.addFormSet(fs); // call to a
        resources.process();      // calls b

        return resources;
    }
}
