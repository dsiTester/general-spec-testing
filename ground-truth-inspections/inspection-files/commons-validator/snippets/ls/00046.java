public class Field implements Cloneable, Serializable {

    /**
     * Returns a Map of String Var names to Var objects.
     * @since Validator 1.2.0
     * @return A Map of the Field's variables.
     */
    @SuppressWarnings("unchecked") // FastHashMap does not support generics
    protected Map<String, Var> getVarMap() { // definition of a
        return hVars;
    }

    /**
     * Gets the property name of the field.
     * @return The field's property name.
     */
    public String getProperty() { // definition of b
        return this.property;
    }

    void process(Map<String, String> globalConstants, Map<String, String> constants) { // indirectly called from test
        ...
        // Process Var Constant Replacement
        for (String key1 : getVarMap().keySet()) { // call to a
            String key2 = TOKEN_START + TOKEN_VAR + key1 + TOKEN_END;
            Var var = this.getVar(key1);
            String replaceValue = var.getValue();

            this.processMessageComponents(key2, replaceValue);
        }

        hMsgs.setFast(true);
    }

}

public class Form implements Serializable {
    ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions, int page, String fieldName)
        throws ValidatorException { // indirectly called from Validator.validate()
        ValidatorResults results = new ValidatorResults();
        params.put(Validator.VALIDATOR_RESULTS_PARAM, results);

        // Only validate a single field if specified
        if (fieldName != null) {
            ...
        } else {
            Iterator<Field> fields = this.lFields.iterator();
            while (fields.hasNext()) {
                Field field = fields.next();

                params.put(Validator.FIELD_PARAM, field);

                if (field.getPage() <= page) { // call to b
                    results.merge(field.validate(params, actions));
                }
            }
        }

        return results;
    }

}

public class ValidatorTest extends TestCase {

   public void testOnlyValidateField() throws ValidatorException {
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action); // calls a

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate(); // calls b (via calling the below with reflection)

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property));
   }

    private ValidatorResources setupDateResources(String property, String action) {

        ValidatorResources resources = new ValidatorResources();
        ...
        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();    // calls a

        return resources;
    }

   /**
    * Formats a <code>String</code> to a <code>Date</code>.
    * The <code>Validator</code> will interpret a <code>null</code>
    * as validation having failed.
    */
   public static Date formatDate(Object bean, Field field) {
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
