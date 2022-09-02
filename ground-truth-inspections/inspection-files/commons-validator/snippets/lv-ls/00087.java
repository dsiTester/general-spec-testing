public class Field implements Cloneable, Serializable {

    /**
     * Sets the property name of the field.
     * @param property The field's property name.
     */
    public void setProperty(String property) { // definition of a
        this.property = property;
    }

    /**
     * Returns a Map of String Var names to Var objects.
     * @since Validator 1.2.0
     * @return A Map of the Field's variables.
     */
    @SuppressWarnings("unchecked") // FastHashMap does not support generics
    protected Map<String, Var> getVarMap() { // definition of b
        return hVars;
    }

    void process(Map<String, String> globalConstants, Map<String, String> constants) { // indirectly called from ValidatorResources.process()
        ...
        // Process Var Constant Replacement
        for (String key1 : getVarMap().keySet()) { // call to b
            String key2 = TOKEN_START + TOKEN_VAR + key1 + TOKEN_END;
            Var var = this.getVar(key1);
            String replaceValue = var.getValue();

            this.processMessageComponents(key2, replaceValue);
        }

        hMsgs.setFast(true);
    }

    /**
     * Gets a unique key based on the property and indexedProperty fields.
     * @return a unique key for the field.
     */
    public String getKey() {    // called from Form.addField()
        if (this.key == null) {
            this.generateKey();
        }

        return this.key;
    }

    /**
     * Generate correct <code>key</code> value.
     */
    public void generateKey() { // called from above
        if (this.isIndexed()) {
            this.key = this.indexedListProperty + TOKEN_INDEXED + "." + this.property;
        } else {
            this.key = this.property;
        }
    }

}

public class Form implements Serializable {
    ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions, int page, String fieldName)
        throws ValidatorException { // indirectly called from Validator.validate()
        ValidatorResults results = new ValidatorResults();
        params.put(Validator.VALIDATOR_RESULTS_PARAM, results);

        // Only validate a single field if specified
        if (fieldName != null) {
            Field field = getFieldMap().get(fieldName);
            if (field == null) {
               throw new ValidatorException("Unknown field "+fieldName+" in form "+getName()); // exception thrown here
            }
            params.put(Validator.FIELD_PARAM, field);

            if (field.getPage() <= page) {
               results.merge(field.validate(params, actions));
            }
        } else {
            ...
        }

        return results;
    }

    /**
     * Add a <code>Field</code> to the <code>Form</code>.
     *
     * @param f  The field
     */
    public void addField(Field f) { // refer to notes; a needs to be called before this point...
        this.lFields.add(f);
        getFieldMap().put(f.getKey(), f);
    }

    /**
     * Returns a Map of String field keys to Field objects.
     *
     * @return   The fieldMap value
     * @since    Validator 1.2.0
     */
    @SuppressWarnings("unchecked") // FastHashMap is not generic
    protected Map<String, Field> getFieldMap() {
        return hFields;
    }
}

public class ValidatorTest extends TestCase {
    private ValidatorResources setupDateResources(String property, String action) { // called from validated test

        ValidatorResources resources = new ValidatorResources();

        ...
        FormSet fs = new FormSet();
        Form form = new Form();
        form.setName("testForm");
        Field field = new Field();
        field.setProperty(property); // call to a
        field.setDepends(action);
        form.addField(field);
        fs.addForm(form);

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();    // calls b

        return resources;
    }

    public void testOnlyValidateField() throws ValidatorException { // validated test
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action); // calls a and b
        ...
    }

    public void testManualBoolean() {
        ValidatorResources resources = new ValidatorResources();

        ValidatorAction va = new ValidatorAction();
        va.setName("capLetter");
        va.setClassname("org.apache.commons.validator.ValidatorTest");
        va.setMethod("isCapLetter");
        va.setMethodParams("java.lang.Object,org.apache.commons.validator.Field,java.util.List");

        FormSet fs = new FormSet();
        Form form = new Form();
        form.setName("testForm");
        Field field = new Field();
        field.setProperty("letter"); // call to a
        field.setDepends("capLetter");
        form.addField(field);
        fs.addForm(form);

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();    // calls b
        ...
    }

}
