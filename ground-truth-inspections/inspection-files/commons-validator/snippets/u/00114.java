public class Form implements Serializable {
    /**
     * Merges the given form into this one. For any field in <code>depends</code>
     * not present in this form, include it. <code>depends</code> has precedence
     * in the way the fields are ordered.
     *
     * @param depends  the form we want to merge
     * @since          Validator 1.2.0
     */
    protected void merge(Form depends) { // definition of a

        List<Field> templFields = new ArrayList<>();
        @SuppressWarnings("unchecked") // FastHashMap is not generic
        Map<String, Field> temphFields = new FastHashMap();
        Iterator<Field> dependsIt = depends.getFields().iterator();
        while (dependsIt.hasNext()) {
            Field defaultField = dependsIt.next();
            if (defaultField != null) {
                String fieldKey = defaultField.getKey();
                if (!this.containsField(fieldKey)) { // call to b
                    templFields.add(defaultField);
                    temphFields.put(fieldKey, defaultField);
                }
                else {
                    Field old = getField(fieldKey);
                    getFieldMap().remove(fieldKey);
                    lFields.remove(old);
                    templFields.add(old);
                    temphFields.put(fieldKey, old);
                }
            }
        }
        lFields.addAll(0, templFields);
        getFieldMap().putAll(temphFields);
    }

    /**
     * Returns true if this Form contains a Field with the given name.
     *
     * @param fieldName  The field name
     * @return           True if this form contains the field by the given name
     * @since            Validator 1.1
     */
    public boolean containsField(String fieldName) { // definition of b
        return getFieldMap().containsKey(fieldName);
    }
}

public class FormSet implements Serializable {
    protected void merge(FormSet depends) { // indirectly called from ValidatorResources.process()
        if (depends != null) {
            Map<String, Form> pForms = getForms();
            Map<String, Form> dForms = depends.getForms();
            for (Entry<String, Form> entry : dForms.entrySet()) {
                String key = entry.getKey();
                Form pForm = pForms.get(key);
                if (pForm != null) {//merge, but principal 'rules', don't overwrite
                    // anything
                    pForm.merge(entry.getValue()); // call to a
                }
                else {//just add
                    addForm(entry.getValue());
                }
            }
        }
        merged = true;
    }

}

public class RetrieveFormTest extends TestCase {

    @Override
    protected void setUp() throws IOException, SAXException {
        InputStream[] streams =
            new InputStream[] {
                this.getClass().getResourceAsStream(
                    "RetrieveFormTest-config.xml")};

        this.resources = new ValidatorResources(streams); // calls a and b

        for (InputStream stream : streams) {
            stream.close();
        }
    }

   /**
    * Test a form defined only in the "default" formset.
    */
    public void testDefaultForm() {

        String formKey = FORM_PREFIX + "default";

        // *** US locale ***
        checkForm(Locale.US, formKey, "default");

        // *** French locale ***
        checkForm(Locale.FRENCH, formKey, "default");

        // *** France locale ***
        checkForm(Locale.FRANCE, formKey, "default");

        // *** Candian (English) locale ***
        checkForm(Locale.CANADA, formKey, "default");

        // *** Candian French locale ***
        checkForm(Locale.CANADA_FRENCH, formKey, "default");

        // *** Candian French Variant locale ***
        checkForm(CANADA_FRENCH_XXX, formKey, "default");

    }

}
