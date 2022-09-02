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
     * Executes the given ValidatorAction and all ValidatorActions that it
     * depends on.
     * @return true if the validation succeeded.
     */
    private boolean validateForRule(
        ValidatorAction va,
        ValidatorResults results,
        Map<String, ValidatorAction> actions,
        Map<String, Object> params,
        int pos)
        throws ValidatorException { // definition of b

        ValidatorResult result = results.getValidatorResult(this.getKey());
        if (result != null && result.containsAction(va.getName())) {
            return result.isValid(va.getName());
        }

        if (!this.runDependentValidators(va, results, actions, params, pos)) {
            return false;
        }

        return va.executeValidationMethod(this, params, results, pos);
    }

    public ValidatorResults validate(Map<String, Object> params, Map<String, ValidatorAction> actions)
        throws ValidatorException { // indirectly called from Validator.validate()

        if (this.getDepends() == null) {
            return new ValidatorResults(); // returns here if a was not called
        }

        ValidatorResults allResults = new ValidatorResults();
        ...
        for (int fieldNumber = 0; fieldNumber < numberOfFieldsToValidate; fieldNumber++) {

            ValidatorResults results = new ValidatorResults();
            synchronized(dependencyList) {
                Iterator<String> dependencies = this.dependencyList.iterator(); // uses state changed by a
                while (dependencies.hasNext()) {
                    String depend = dependencies.next();

                    ValidatorAction action = actions.get(depend);
                    ...
                    boolean good =
                        validateForRule(action, results, actions, params, fieldNumber); // call to b

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
        assertTrue(results.getPropertyNames().contains(property)); // assertion fails here
    }

}
