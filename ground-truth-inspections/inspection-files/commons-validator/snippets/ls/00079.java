public class Field implements Cloneable, Serializable {

    /**
     * A comma separated list of validator's this field depends on.
     */
    protected String depends = null;

    /**
     * Sets the validation rules for this field as a comma separated list.
     * @param depends A comma separated list of validator names.
     */
    public void setDepends(String depends) { // definition of a
        this.depends = depends;

        this.dependencyList.clear();

        StringTokenizer st = new StringTokenizer(depends, ",");
        while (st.hasMoreTokens()) {
            String depend = st.nextToken().trim();

            if (depend != null && !depend.isEmpty()) {
                this.dependencyList.add(depend);
            }
        }
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

    /**
     * Replace constants with values in fields and process the depends field
     * to create the dependency <code>Map</code>.
     */
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
        field.setProperty(property);
        field.setDepends(action); // call to a
        form.addField(field);
        fs.addForm(form);

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();    // calls b

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

        ValidatorResults results = validator.validate();

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property));
    }

}
