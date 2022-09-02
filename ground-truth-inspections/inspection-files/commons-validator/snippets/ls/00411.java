public class ValidatorAction implements Serializable {
    /**
     * Sets the name of the validator action.
     * @param name Validator Action name.
     */
    public void setName(String name) { // definition of a
        this.name = name;
    }

    /**
     * @return true if the javascript for this action has already been loaded.
     */
    private boolean javascriptAlreadyLoaded() { // definition of b
        return (this.javascript != null);
    }

    protected synchronized void loadJavascriptFunction() { // indirectly called from ValidatorResources.addValidatorAction()
        if (this.javascriptAlreadyLoaded()) { // call to b
            return;
        }
        ...
        if (this.jsFunction == null) {
            this.jsFunction = this.generateJsFunction(); // would throw NullPointerException if a is not called at this point
        }
        ...
        String javascriptFileName = this.formatJavascriptFileName();
        ...
        this.javascript = this.readJavascriptFile(javascriptFileName);
        ...
    }

    /**
     * Used to generate the javascript name when it is not specified.
     */
    private String generateJsFunction() { // called from above
        StringBuilder jsName =
                new StringBuilder("org.apache.commons.validator.javascript");

        jsName.append(".validate");
        jsName.append(name.substring(0, 1).toUpperCase()); // would throw NullPointerException here if a was not called at this point
        jsName.append(name.substring(1));

        return jsName.toString();
    }

}

public class ValidatorTest extends TestCase {
   public void testOnlyValidateField() throws ValidatorException {
        //     property name of the method we are validating
        String property = "date";
        // name of ValidatorAction
        String action = "date";
        validatorresources resources = setupDateResources(property, action); // calls a and b

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
        va.setMethodParams("java.lang.Object,org.apache.commons.validator.Field"); // call to a

        FormSet fs = new FormSet();
        Form form = new Form();
        form.setName("testForm");
        Field field = new Field();
        field.setProperty(property);
        field.setDepends(action);
        form.addField(field);
        fs.addForm(form);

        resources.addValidatorAction(va); // calls b
        resources.addFormSet(fs);
        resources.process();

        return resources;
    }
}
