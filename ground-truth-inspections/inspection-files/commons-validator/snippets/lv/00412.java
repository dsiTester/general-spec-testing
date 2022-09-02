public class ValidatorAction implements Serializable {

    /**
     * Sets the name of the validator action.
     * @param name Validator Action name.
     */
    public void setName(String name) { // definition of a
        this.name = name;
    }

    protected synchronized void loadJavascriptFunction() { // definition of b
        if (this.javascriptAlreadyLoaded()) {
            return;
        }
        ...
        if (this.jsFunction == null) {
            this.jsFunction = this.generateJsFunction(); // throws NullPointerException
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
    private String generateJsFunction() { // called from b
        StringBuilder jsName =
                new StringBuilder("org.apache.commons.validator.javascript");

        jsName.append(".validate");
        jsName.append(name.substring(0, 1).toUpperCase()); // throws NullPointerException here
        jsName.append(name.substring(1));

        return jsName.toString();
    }

    /**
     * Initialize based on set.
     */
    protected void init() {     // called from ValidatorResources.addValidatorAction()
        this.loadJavascriptFunction(); // call to b
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
        va.setName(action); // call to a
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

        resources.addValidatorAction(va); // calls b
        resources.addFormSet(fs);
        resources.process();

        return resources;
    }
}
