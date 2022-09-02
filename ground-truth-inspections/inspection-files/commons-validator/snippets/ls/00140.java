public class FormSet implements Serializable {

    /**
     * Gets the equivalent of the variant component of <code>Locale</code>.
     *
     * @return   The variant value
     */
    public String getVariant() { // definition of a
        return variant;
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
    public void addFormSet(FormSet fs) { // called from test
        String key = this.buildKey(fs);  // calls a
        ...
    }

    /**
     * Builds a key to store the <code>FormSet</code> under based on it's
     * language, country, and variant values.
     * @param fs The Form Set.
     * @return generated key for a formset.
     */
    protected String buildKey(FormSet fs) {
        return
            this.buildLocale(fs.getLanguage(), fs.getCountry(), fs.getVariant()); // call to a
    }

    private void processForms() {
        if (defaultFormSet == null) {// it isn't mandatory to have a
            // default formset
            defaultFormSet = new FormSet();
        }
        defaultFormSet.process(getConstants()); // call to b
        ...
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
        fs.addForm(form);       // calls a

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();    // calls b

        return resources;
    }

}
