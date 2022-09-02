public class FormSet implements Serializable {
    /**
     * Merges the given <code>FormSet</code> into this one. If any of <code>depends</code>
     * s <code>Forms</code> are not in this <code>FormSet</code> then, include
     * them, else merge both <code>Forms</code>. Theoretically we should only
     * merge a "parent" formSet.
     *
     * @param depends  FormSet to be merged
     * @since          Validator 1.2.0
     */
    protected void merge(FormSet depends) { // definition of a
        if (depends != null) {
            Map<String, Form> pForms = getForms();
            Map<String, Form> dForms = depends.getForms();
            for (Entry<String, Form> entry : dForms.entrySet()) {
                String key = entry.getKey();
                Form pForm = pForms.get(key);
                if (pForm != null) {//merge, but principal 'rules', don't overwrite
                    // anything
                    pForm.merge(entry.getValue());
                }
                else {//just add
                    addForm(entry.getValue());
                }
            }
        }
        merged = true;
    }

    /**
     * Whether or not the this <code>FormSet</code> was processed for replacing
     * variables in strings with their values.
     *
     * @return   The processed value
     */
    public boolean isProcessed() { // definition of b
        return processed;
    }
}

public class ValidatorResources implements Serializable {

    private void processForms() {     // called from ValidatorResources.process()
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        defaultFormSet.process(getConstants());
        // Loop through FormSets and merge if necessary
        for (String key : getFormSets().keySet()) {
            FormSet fs = getFormSets().get(key);
            fs.merge(getParent(fs)); // calls a
        }

        // Process Fully Constructed FormSets
        for (FormSet fs : getFormSets().values()) {
            if (!fs.isProcessed()) { // call to b
                fs.process(getConstants());
            }
        }
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

        this.resources = new ValidatorResources(streams); // calls a and b

        for (InputStream stream : streams) {
            stream.close();
        }
    }

    public void testRequiredLastNameShort() throws ValidatorException {
        // Create bean to run test on.
        NameBean name = new NameBean();
        name.setFirstName("Test");
        name.setLastName("Test");

        // Construct validator based on the loaded resources
        // and the form key
        Validator validator = new Validator(resources, FORM_KEY);
        // add the name bean to the validator as a resource
        // for the validations to be performed on.
        validator.setParameter(Validator.BEAN_PARAM, name);

        // Get results of the validation.
        ValidatorResults results = validator.validate();

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
