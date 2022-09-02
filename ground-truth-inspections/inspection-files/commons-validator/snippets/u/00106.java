public class Form implements Serializable {

    /**
     * Returns true if this Form contains a Field with the given name.
     *
     * @param fieldName  The field name
     * @return           True if this form contains the field by the given name
     * @since            Validator 1.1
     */
    public boolean containsField(String fieldName) { // definition of a
        return getFieldMap().containsKey(fieldName);
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

    protected void merge(Form depends) { // indirectly called from ValidatorResources.process() (called from ValidatorResources())

        List<Field> templFields = new ArrayList<>();
        @SuppressWarnings("unchecked") // FastHashMap is not generic
        Map<String, Field> temphFields = new FastHashMap();
        Iterator<Field> dependsIt = depends.getFields().iterator();
        while (dependsIt.hasNext()) {
            Field defaultField = dependsIt.next();
            if (defaultField != null) {
                String fieldKey = defaultField.getKey();
                if (!this.containsField(fieldKey)) { // call to a
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

}

public class RetrieveFormTest extends TestCase {
    /**
     * Load <code>ValidatorResources</code> from multiple xml files.
     */
    @Override
    protected void setUp() throws IOException, SAXException {
        InputStream[] streams =
            new InputStream[] {
                this.getClass().getResourceAsStream(
                    "RetrieveFormTest-config.xml")};

        this.resources = new ValidatorResources(streams);

        for (InputStream stream : streams) {
            stream.close();
        }
    }

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
