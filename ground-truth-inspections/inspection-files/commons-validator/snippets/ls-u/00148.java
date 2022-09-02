public class FormSet implements Serializable {
    /**
     * Sets the equivalent of the country component of <code>Locale</code>.
     *
     * @param country  The new country value
     */
    public void setCountry(String country) { // definition of a
        this.country = country;
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

    protected int getType() {   // called from ValidatorResources.processForms()
        if (getVariant() != null) {
            if (getLanguage() == null || getCountry() == null) { // getCountry() returns null because a wasn't called
                throw new NullPointerException(
                    "When variant is specified, country and language must be specified."); // NullPointerException thrown here for unknown test
            }
            return VARIANT_FORMSET;
        }
        ...
        return GLOBAL_FORMSET;
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
        formSet.setCountry(country); // call to a
        formSet.setVariant(variant);

        // Add the FormSet to the validator resources
        resources.addFormSet(formSet);

        if (getLog().isDebugEnabled()) {
            getLog().debug("FormSet[" + formSet.displayKey() + "] created.");
        }

        return formSet;

    }

}

public class ValidatorResources implements Serializable {
    private void processForms() {
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        defaultFormSet.process(getConstants()); // call to b for both invalidated and unknown cases (first perturbation for unknown case)
        // Loop through FormSets and merge if necessary
        for (String key : getFormSets().keySet()) {
            FormSet fs = getFormSets().get(key);
            fs.merge(getParent(fs)); // calls FormSet.getType(), which throws an exception in the unknown case. the invalidated case doesn't cover this line.
        }

        // Process Fully Constructed FormSets
        for (FormSet fs : getFormSets().values()) {
            if (!fs.isProcessed()) {
                fs.process(getConstants()); // call to b for unknown case (second perturbation)
            }
        }
    }

}

public class DateTest extends AbstractCommonTest {
    @Override
    protected void setUp() throws IOException, SAXException {
        // Load resources
        loadResources("DateTest-config.xml"); // calls a and b via commons-digester
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

public class LocaleTest extends AbstractCommonTest {

    @Override
    protected void setUp()
        throws IOException, SAXException {
        // Load resources
        loadResources("LocaleTest-config.xml"); // calls a and b via commons-digester
    }

    /**
     * See what happens when we try to validate with a Locale, Country and
     * variant. Also check if the added locale validation field is getting used.
     *
     * @throws ValidatorException  If something goes wrong
     */
    public void testLocale1()
        throws ValidatorException { // unknown verdict test
        // Create bean to run test on.
        NameBean name = new NameBean();
        name.setFirstName("");
        name.setLastName("");

        valueTest(name, new Locale("en", "US", "TEST1"), false, false, false);
    }

}
