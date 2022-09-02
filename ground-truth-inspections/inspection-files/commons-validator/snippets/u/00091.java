public class Field implements Cloneable, Serializable {

    /**
     * Sets the property name of the field.
     * @param property The field's property name.
     */
    public void setProperty(String property) { // definition of a
        this.property = property;
    }

    /**
     * Run the configured validations on this field.  Run all validations
     * in the depends clause over each item in turn, returning when the first
     * one fails.
     * @param params A Map of parameter class names to parameter values to pass
     * into validation methods.
     * @param actions A Map of validator names to ValidatorAction objects.
     * @return A ValidatorResults object containing validation messages for
     * this field.
     * @throws ValidatorException If an error occurs during validation.
     */
    public ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions)
        throws ValidatorException { // definition of b

        if (this.getDepends() == null) {
            return new ValidatorResults();
        }

        ValidatorResults allResults = new ValidatorResults();

        Object bean = params.get(Validator.BEAN_PARAM);
        int numberOfFieldsToValidate =
            this.isIndexed() ? this.getIndexedPropertySize(bean) : 1;

        for (int fieldNumber = 0; fieldNumber < numberOfFieldsToValidate; fieldNumber++) {

            ValidatorResults results = new ValidatorResults();
            synchronized(dependencyList) {
                Iterator<String> dependencies = this.dependencyList.iterator();
                while (dependencies.hasNext()) {
                    String depend = dependencies.next();

                    ValidatorAction action = actions.get(depend);
                    if (action == null) {
                        this.handleMissingAction(depend);
                    }

                    boolean good =
                        validateForRule(action, results, actions, params, fieldNumber); // validation method called here; may throw exception if a was never called and the validating method accesses Field.property

                    if (!good) {
                        allResults.merge(results);
                        return allResults;
                    }
                }
            }
            allResults.merge(results);
        }

        return allResults;
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
               results.merge(field.validate(params, actions)); // call to b
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
    private ValidatorResources setupDateResources(String property, String action) { // called from test

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
        resources.process();

        return resources;
    }

    public void testOnlyValidateField() throws ValidatorException {
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action); // calls a and b

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate(); // calls b

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property));
    }

}
