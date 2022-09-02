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
     * Gets the validation rules for this field as a comma separated list.
     * @return A comma separated list of validator names.
     */
    public String getDepends() { // definition of b
        return this.depends;
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

    public ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions)
        throws ValidatorException { // indirectly called from test

        if (this.getDepends() == null) { // call to b
            return new ValidatorResults();
        }
        ...
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

        ValidatorResults results = validator.validate(); // calls b

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
}
