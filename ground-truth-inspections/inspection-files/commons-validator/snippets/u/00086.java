public class Field implements Cloneable, Serializable {

    /**
     * Sets the property name of the field.
     * @param property The field's property name.
     */
    public void setProperty(String property) { // definition of a
        this.property = property;
    }

    /**
     * Gets the property name of the field.
     * @return The field's property name.
     */
    public String getProperty() { // definition of b
        return this.property;
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
    private ValidatorResources setupDateResources(String property, String action) {

        ValidatorResources resources = new ValidatorResources();

        ValidatorAction va = new ValidatorAction();
        va.setName(action);
        va.setClassname("org.apache.commons.validator.ValidatorTest");
        va.setMethod("formatDate");
        va.setMethodParams("java.lang.Object,org.apache.commons.validator.Field");

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
        ValidatorResources resources = setupDateResources(property, action);

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate(); // calls b

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property));
    }

   public static Date formatDate(Object bean, Field field) { // called via reflection
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty()); // call to b
      Date date = null;

      try {
          DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);

         formatter.setLenient(false);

         date = formatter.parse(value);
      } catch (ParseException e) {
         System.out.println("ValidatorTest.formatDate() - " + e.getMessage());
      }

      return date;
   }

}
