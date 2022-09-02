public class ValidatorResources implements Serializable {

    public void process() {     // called from test
        hFormSets.setFast(true);
        hConstants.setFast(true);
        hActions.setFast(true);

        this.processForms();    // call to a
    }

    /**
     * <p>Process the <code>Form</code> objects.  This clones the <code>Field</code>s
     * that don't exist in a <code>FormSet</code> compared to its parent
     * <code>FormSet</code>.</p>
     */
    private void processForms() {     // definition of a
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        defaultFormSet.process(getConstants()); // call to b
        // Loop through FormSets and merge if necessary
        for (String key : getFormSets().keySet()) {
            FormSet fs = getFormSets().get(key);
            FormSet parent = getParent(fs);
            fs.merge(fs);
        }

        // Process Fully Constructed FormSets
        for (FormSet fs : getFormSets().values()) {
            if (!fs.isProcessed()) {
                fs.process(getConstants());
            }
        }
    }

    /**
     * Returns a Map of String constant names to their String values.
     * @return Map of Constants
     * @since Validator 1.2.0
     */
    @SuppressWarnings("unchecked") // FastHashMap is not generic
    protected Map<String, String> getConstants() { // definition of b
        return hConstants;
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

        ValidatorResults results = validator.validate();

        assertNotNull(results);

        // Field passed and should be in results
        assertTrue(results.getPropertyNames().contains(property));
   }

    private ValidatorResources setupDateResources(String property, String action) { // called from above

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

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();    // call to a

        return resources;
    }

}
