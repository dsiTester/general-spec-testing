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
    private FormSet getParent(FormSet fs) { // definition of a

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
     * Get an unmodifiable <code>Map</code> of the <code>ValidatorAction</code>s.
     * @return Map of validator actions.
     */
    public Map<String, ValidatorAction> getValidatorActions() { // definition of b
        return Collections.unmodifiableMap(getActions());
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


}

public class Validator implements Serializable {
    public ValidatorResults validate() throws ValidatorException { // called from test
        ...
        Form form = this.resources.getForm(locale, this.formName);
        if (form != null) {
            this.setParameter(FORM_PARAM, form);
            return form.validate(
                this.parameters,
                this.resources.getValidatorActions(), // call to b
                this.page,
                this.fieldName);
        }

        return new ValidatorResults();
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
                    lFields.remove(old); // seems like the modification triggering the exception is done here?
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

        this.resources = new ValidatorResources(streams); // calls a

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
