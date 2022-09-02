public class FormSet implements Serializable {
    /**
     * Add a <code>Form</code> to the <code>FormSet</code>.
     *
     * @param f  The form
     */
    public void addForm(Form f) { // definition of a

        String formName = f.getName();
        if (forms.containsKey(formName)) {
            getLog().error("Form '" + formName + "' already exists in FormSet["
                      + this.displayKey() + "] - ignoring.");

        } else {
            forms.put(f.getName(), f);
        }

    }

    /**
     * Processes all of the <code>Form</code>s.
     *
     * @param globalConstants  Global constants
     */
    synchronized void process(Map<String, String> globalConstants) { // definition of b
        for (Form f : forms.values()) {
            f.process(globalConstants, constants, forms);
        }

        processed = true;
    }
}

public class ValidatorResources implements Serializable {
    public void process() {
        hFormSets.setFast(true);
        hConstants.setFast(true);
        hActions.setFast(true);

        this.processForms();
    }

    private void processForms() {     // called from above
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        defaultFormSet.process(getConstants()); // call to b
        // Loop through FormSets and merge if necessary
        for (String key : getFormSets().keySet()) {
            FormSet fs = getFormSets().get(key);
            fs.merge(getParent(fs));
        }

        // Process Fully Constructed FormSets
        for (FormSet fs : getFormSets().values()) {
            if (!fs.isProcessed()) {
                fs.process(getConstants());
            }
        }
    }

}

public class ValidatorTest extends TestCase {
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
        fs.addForm(form);       // call to a

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();   // calls b

        return resources;
    }

}
