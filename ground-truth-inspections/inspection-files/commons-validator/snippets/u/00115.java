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
                if (!this.containsField(fieldKey)) {
                    templFields.add(defaultField);
                    temphFields.put(fieldKey, defaultField);
                }
                else {
                    Field old = getField(fieldKey); // call to b
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
     * Returns the Field with the given name or null if this Form has no such
     * field.
     *
     * @param fieldName  The field name
     * @return           The field value
     * @since            Validator 1.1
     */
    public Field getField(String fieldName) { // definition of b
        return getFieldMap().get(fieldName);
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
    * Test a form not defined
    */
    public void testFormNotFound() {

        String formKey = "INVALID_NAME";

        // *** US locale ***
        checkFormNotFound(Locale.US, formKey);

        // *** French locale ***
        checkFormNotFound(Locale.FRENCH, formKey);

        // *** France locale ***
        checkFormNotFound(Locale.FRANCE, formKey);

        // *** Candian (English) locale ***
        checkFormNotFound(Locale.CANADA, formKey);

        // *** Candian French locale ***
        checkFormNotFound(Locale.CANADA_FRENCH, formKey);

        // *** Candian French Variant locale ***
        checkFormNotFound(CANADA_FRENCH_XXX, formKey);


    }

}
