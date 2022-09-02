public class ValidatorResources implements Serializable {

    public void process() {     // called from ValidatorResources()
        hFormSets.setFast(true);
        hConstants.setFast(true);
        hActions.setFast(true);

        this.processForms();    // call to a
    }

    /**
     * <p>Process the <code>Form</code> objects.  This clones the <code>Field</code>s
     * that don't exist in a <code>FormSet</code> compared to its parent
     * <code>FormSet</code>.</p>
     */
    private void processForms() {     // definition of a
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        defaultFormSet.process(getConstants());
        // Loop through FormSets and merge if necessary
        for (String key : getFormSets().keySet()) {
            FormSet fs = getFormSets().get(key);
            FormSet parent = getParent(fs); // call to b
            fs.merge(fs);
        }

        // Process Fully Constructed FormSets
        for (FormSet fs : getFormSets().values()) {
            if (!fs.isProcessed()) {
                fs.process(getConstants());
            }
        }
    }

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

}
