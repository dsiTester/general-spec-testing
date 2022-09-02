public class FormSet implements Serializable {
    /**
     * Sets the equivalent of the variant component of <code>Locale</code>.
     *
     * @param variant  The new variant value
     */
    public void setVariant(String variant) { // definition of a
        this.variant = variant;
    }

    /**
     * A <code>Map</code> of <code>Form</code>s is returned as an unmodifiable
     * <code>Map</code> with the key based on the form name.
     *
     * @return   The forms map
     */
    public Map<String, Form> getForms() { // definition of b
        return Collections.unmodifiableMap(forms);
    }
}

public class FormSetFactory extends AbstractObjectCreationFactory {
    private FormSet createFormSet(ValidatorResources resources,
                                  String language,
                                  String country,
                                  String variant) throws Exception { // indirectly called from commons-digester

        // Retrieve existing FormSet for the language/country/variant
        FormSet formSet = resources.getFormSet(language, country, variant);
        if (formSet != null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("FormSet[" + formSet.displayKey() + "] found - merging.");
            }
            return formSet;
        }

        // Create a new FormSet for the language/country/variant
        formSet = new FormSet();
        formSet.setLanguage(language);
        formSet.setCountry(country);
        formSet.setVariant(variant); // call to a

        // Add the FormSet to the validator resources
        resources.addFormSet(formSet);

        if (getLog().isDebugEnabled()) {
            getLog().debug("FormSet[" + formSet.displayKey() + "] created.");
        }

        return formSet;

    }

}

public class ValidatorResources implements Serializable {

    protected void merge(FormSet depends) { // indirectly called from ValidatorResources.process()
        if (depends != null) {
            Map<String, Form> pForms = getForms(); // call to b (second perturbation)
            map<String, Form> dForms = depends.getForms(); // call to b (first perturbation)
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
