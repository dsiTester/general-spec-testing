public class ValidatorResources implements Serializable {
    /**
     * Finds the given formSet's parent. ex: A formSet with locale en_UK_TEST1
     * has a direct parent in the formSet with locale en_UK. If it doesn't
     * exist, find the formSet with locale en, if no found get the
     * defaultFormSet.
     *
     * @param fs
     *            the formSet we want to get the parent from
     * @return fs's parent
     */
    private FormSet getParent(FormSet fs) { // definition of b

        FormSet parent = null;
        if (fs.getType() == FormSet.LANGUAGE_FORMSET) {
            parent = defaultFormSet;
        } else if (fs.getType() == FormSet.COUNTRY_FORMSET) {
            parent = getFormSets().get(buildLocale(fs.getLanguage(),
                    null, null));
            if (parent == null) {
                parent = defaultFormSet;
            }
        } else if (fs.getType() == FormSet.VARIANT_FORMSET) {
            parent = getFormSets().get(buildLocale(fs.getLanguage(), fs
                    .getCountry(), null));
            if (parent == null) {
                parent = getFormSets().get(buildLocale(fs.getLanguage(),
                        null, null));
                if (parent == null) {
                    parent = defaultFormSet;
                }
            }
        }
        return parent;
    }

    /**
     * <p>Gets a <code>Form</code> based on the name of the form and the
     * <code>Locale</code> that most closely matches the <code>Locale</code>
     * passed in.  The order of <code>Locale</code> matching is:</p>
     * <ol>
     *    <li>language + country + variant</li>
     *    <li>language + country</li>
     *    <li>language</li>
     *    <li>default locale</li>
     * </ol>
     * @param language The locale's language.
     * @param country The locale's country.
     * @param variant The locale's language variant.
     * @param formKey The key for the Form.
     * @return The validator Form.
     * @since Validator 1.1
     */
    public Form getForm(String language, String country, String variant,
            String formKey) {

        Form form = null;

        // Try language/country/variant
        String key = this.buildLocale(language, country, variant);
        if (!key.isEmpty()) {
            FormSet formSet = getFormSets().get(key);
            if (formSet != null) {
                form = formSet.getForm(formKey);
            }
        }
        String localeKey  = key;


        // Try language/country
        if (form == null) {
            key = buildLocale(language, country, null);
            if (!key.isEmpty()) {
                FormSet formSet = getFormSets().get(key);
                if (formSet != null) {
                    form = formSet.getForm(formKey);
                }
            }
        }

        // Try language
        if (form == null) {
            key = buildLocale(language, null, null);
            if (!key.isEmpty()) {
                FormSet formSet = getFormSets().get(key);
                if (formSet != null) {
                    form = formSet.getForm(formKey);
                }
            }
        }

        // Try default formset
        if (form == null) {
            form = defaultFormSet.getForm(formKey);
            key = "default";
        }

        if (form == null) {
            if (getLog().isWarnEnabled()) {
                getLog().warn("Form '" + formKey + "' not found for locale '" +
                         localeKey + "'");
            }
        } else {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Form '" + formKey + "' found in formset '" +
                          key + "' for locale '" + localeKey + "'");
            }
        }

        return form;

    }

    /**
     * <p>Process the <code>Form</code> objects.  This clones the <code>Field</code>s
     * that don't exist in a <code>FormSet</code> compared to its parent
     * <code>FormSet</code>.</p>
     */
    private void processForms() {     // indirectly called from ValidatorResources()
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        defaultFormSet.process(getConstants());
        // Loop through FormSets and merge if necessary
        for (String key : getFormSets().keySet()) {
            FormSet fs = getFormSets().get(key);
            fs.merge(getParent(fs)); // call to a
        }

        // Process Fully Constructed FormSets
        for (FormSet fs : getFormSets().values()) {
            if (!fs.isProcessed()) {
                fs.process(getConstants());
            }
        }
    }

    public Form getForm(Locale locale, String formKey) { // called from Validator.validate()
        return this.getForm(locale.getLanguage(), locale.getCountry(), locale
                .getVariant(), formKey);
    }

}

public class Form implements Serializable {
    protected void merge(Form depends) { // indirectly called from ValidatorResources.processForms()

        List<Field> templFields = new ArrayList<>();
        @SuppressWarnings("unchecked") // FastHashMap is not generic
        Map<String, Field> temphFields = new FastHashMap();
        Iterator<Field> dependsIt = depends.getFields().iterator();
        while (dependsIt.hasNext()) {
            Field defaultField = dependsIt.next(); // throws ConcurrentModificationException
            if (defaultField != null) {
                String fieldKey = defaultField.getKey();
                if (!this.containsField(fieldKey)) {
                    templFields.add(defaultField);
                    temphFields.put(fieldKey, defaultField);
                }
                else {
                    Field old = getField(fieldKey);
                    getFieldMap().remove(fieldKey);
                    lFields.remove(old); // seems like the modification is done here?
                    templFields.add(old);
                    temphFields.put(fieldKey, old);
                }
            }
        }
        lFields.addAll(0, templFields);
        getFieldMap().putAll(temphFields);
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
        ...
        // Get results of the validation.
        ValidatorResults results = validator.validate(); // calls b

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
