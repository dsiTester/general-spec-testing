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
     * Processes all of the <code>Form</code>s.
     *
     * @param globalConstants  Global constants
     */
    synchronized void process(Map<String, String> globalConstants) { // definition of b
        for (Form f : forms.values()) {
            f.process(globalConstants, constants, forms);
        }

        processed = true;
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
        ...

        return formSet;

    }

}

public class ValidatorResources implements Serializable {
    private void processForms() {     // called from ValidatorResources.process()
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        defaultFormSet.process(getConstants()); // call to b for both validated and invalidated cases (first perturbation for validated case)
        // Loop through FormSets and merge if necessary
        for (String key : getFormSets().keySet()) {
            FormSet fs = getFormSets().get(key);
            fs.merge(getParent(fs));
        }

        // Process Fully Constructed FormSets
        for (FormSet fs : getFormSets().values()) {
            if (!fs.isProcessed()) {
                fs.process(getConstants()); // call to b for validated case (second perturbation)
            }
        }
    }

}

public class LocaleTest extends AbstractCommonTest {
    @Override
    protected void setUp()
        throws IOException, SAXException {
        // Load resources
        loadResources("LocaleTest-config.xml"); // calls a via commons-digester; calls b
    }

    public void testLocale2()
        throws ValidatorException { // validated test
        // Create bean to run test on.
        NameBean name = new NameBean();
        name.setFirstName("");
        name.setLastName("");

        valueTest(name, new Locale("en", "US", "TEST2"), true, false, true);
    }

    private void valueTest(Object name, Locale loc, boolean firstGood, boolean lastGood, boolean middleGood)
        throws ValidatorException {

        // Construct validator based on the loaded resources
        // and the form key
        Validator validator = new Validator(resources, FORM_KEY);
        // add the name bean to the validator as a resource
        // for the validations to be performed on.
        validator.setParameter(Validator.BEAN_PARAM, name);
        validator.setParameter(Validator.LOCALE_PARAM, loc);
        // Get results of the validation.
        // throws ValidatorException,
        // but we aren't catching for testing
        // since no validation methods we use
        // throw this
        ValidatorResults results = validator.validate();

        assertNotNull("Results are null.", results);

        ValidatorResult resultlast = results.getValidatorResult("lastName");
        ValidatorResult resultfirst = results.getValidatorResult("firstName");
        ValidatorResult resultmiddle = results.getValidatorResult("middleName");

        if (firstGood) {
            assertNull(resultfirst);
        }
        else {
            assertNotNull(resultfirst);
        }

        if (middleGood) {
            assertNull(resultmiddle); // this assertion failed
        }
        else {
            assertNotNull(resultmiddle);
        }

        if (lastGood) {
            assertNull(resultlast);
        }
        else {
            assertNotNull(resultlast);
        }
    }
}

public class DateTest extends AbstractCommonTest {
    @Override
    protected void setUp() throws IOException, SAXException {
        // Load resources
        loadResources("DateTest-config.xml"); // calls a via commons-digester; calls b
    }

    /**
     * Tests the date validation.
     */
    public void testValidDate() throws ValidatorException { // invalidated test
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("12/01/2005");
        valueTest(info, true);
    }

}
