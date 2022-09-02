public class ValidatorResources implements Serializable {

    /**
     * Add a <code>ValidatorAction</code> to the resource.  It also creates an
     * instance of the class based on the <code>ValidatorAction</code>s
     * classname and retrieves the <code>Method</code> instance and sets them
     * in the <code>ValidatorAction</code>.
     * @param va The validator action.
     */
    public void addValidatorAction(ValidatorAction va) { // definition of a
        va.init();

        getActions().put(va.getName(), va);

        if (getLog().isDebugEnabled()) {
            getLog().debug("Add ValidatorAction: " + va.getName() + "," + va.getClassname());
        }
    }

    /**
     * Builds a key to store the <code>FormSet</code> under based on it's
     * language, country, and variant values.
     * @param fs The Form Set.
     * @return generated key for a formset.
     */
    protected String buildKey(FormSet fs) { // definition of b
        return
                this.buildLocale(fs.getLanguage(), fs.getCountry(), fs.getVariant());
    }

    public void addFormSet(FormSet fs) { // called from test
        String key = this.buildKey(fs);  // call to b
        if (key.isEmpty()) {// there can only be one default formset
            if (getLog().isWarnEnabled() && defaultFormSet != null) {
                // warn the user he might not get the expected results
                getLog().warn("Overriding default FormSet definition.");
            }
            defaultFormSet = fs;
        } ...
    }

}

public class ValidatorTest extends TestCase {
   public void testOnlyValidateField() throws ValidatorException { // invalidated test
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action); // calls a and b

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm", property);
        validator.setParameter(Validator.BEAN_PARAM, bean);

        ValidatorResults results = validator.validate();

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property)); // this assertion fails
   }

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
        field.setDepends(action);
        form.addField(field);
        fs.addForm(form);

        resources.addValidatorAction(va); // call to a
        resources.addFormSet(fs); // calls b
        resources.process();

        return resources;
    }
}
