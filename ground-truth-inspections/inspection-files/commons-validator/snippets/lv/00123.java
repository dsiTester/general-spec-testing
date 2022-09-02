public class Form implements Serializable {
    /**
     * Sets the name/key of the set of validation rules.
     *
     * @param name  The new name value
     */
    public void setName(String name) { // definition of a
        this.name = name;
    }

    /**
     * Processes all of the <code>Form</code>'s <code>Field</code>s.
     *
     * @param globalConstants  A map of global constants
     * @param constants        Local constants
     * @param forms            Map of forms
     * @since                  Validator 1.2.0
     */
    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) { // definition of b
        if (isProcessed()) {
            return;
        }

        int n = 0;//we want the fields from its parent first
        if (isExtending()) {
            Form parent = forms.get(inherit);
            if (parent != null) {
                if (!parent.isProcessed()) {
                    //we want to go all the way up the tree
                    parent.process(constants, globalConstants, forms);
                }
                for (Field f : parent.getFields()) {
                    //we want to be able to override any fields we like
                    if (getFieldMap().get(f.getKey()) == null) {
                        lFields.add(n, f);
                        getFieldMap().put(f.getKey(), f);
                        n++;
                    }
                }
            }
        }
        hFields.setFast(true);
        //no need to reprocess parent's fields, we iterate from 'n'
        for (Iterator<Field> i = lFields.listIterator(n); i.hasNext(); ) {
            Field f = i.next();
            f.process(globalConstants, constants);
        }

        processed = true;
    }

}

public class FormSet implements Serializable {
    synchronized void process(Map<String, String> globalConstants) {
        for (Form f : forms.values()) {
            f.process(globalConstants, constants, forms); // call to b
        }

        processed = true;
    }

}

public class ValidatorTest extends TestCase {
    public void testManualObject() {
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        ValidatorResources resources = setupDateResources(property, action); // calls a and b

        TestBean bean = new TestBean();
        bean.setDate("2/3/1999");

        Validator validator = new Validator(resources, "testForm");
        validator.setParameter(Validator.BEAN_PARAM, bean);

        try {
            ValidatorResults results = validator.validate();
            ...
            assertTrue("ValidatorResult does not contain '" + action + "' validator result.", result.containsAction(action)); // this assertion fails
            ...
        } catch (Exception e) {
            fail("An exception was thrown while calling Validator.validate()"); // exception thrown here
        }
        ...

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
        // NOTE: one can call b before a by uncommenting the following:
        // form.process(null, null, null);
        form.setName("testForm"); // call to a
        Field field = new Field();
        field.setProperty(property);
        field.setDepends(action);
        form.addField(field);
        fs.addForm(form);

        resources.addValidatorAction(va);
        resources.addFormSet(fs);
        resources.process();   // calls b

        return resources;
    }

}
