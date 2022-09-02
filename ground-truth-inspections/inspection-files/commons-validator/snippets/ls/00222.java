public class ValidatorAction implements Serializable {
    /**
     * @return A filename suitable for passing to a
     * ClassLoader.getResourceAsStream() method.
     */
    private String formatJavascriptFileName() { // definition of a
        String fname = this.jsFunction.substring(1);

        if (!this.jsFunction.startsWith("/")) {
            fname = jsFunction.replace('.', '/') + ".js";
        }

        return fname;
    }

    /**
     * Gets the name of the validator action.
     * @return Validator Action name.
     */
    public String getName() {   // definition of b
        return name;
    }

    protected synchronized void loadJavascriptFunction() { // indirectly called from ValidatorResources.addValidatorAction()

        if (this.javascriptAlreadyLoaded()) {
            return;
        }

        if (getLog().isTraceEnabled()) {
            getLog().trace("  Loading function begun");
        }

        if (this.jsFunction == null) {
            this.jsFunction = this.generateJsFunction();
        }

        String javascriptFileName = this.formatJavascriptFileName(); // call to a

        if (getLog().isTraceEnabled()) {
            getLog().trace("  Loading js function '" + javascriptFileName + "'");
        }

        this.javascript = this.readJavascriptFile(javascriptFileName); // this field variable gets set to null for the test, regardless of whether the return value of method-a is the "valid" value or not

        if (getLog().isTraceEnabled()) {
            getLog().trace("  Loading javascript function completed");
        }

    }
}

public class ValidatorResources implements Serializable {
    public void addValidatorAction(ValidatorAction va) { // called from test
        va.init();              // calls a

        getActions().put(va.getName(), va); // call to b

        if (getLog().isDebugEnabled()) {
            getLog().debug("Add ValidatorAction: " + va.getName() + "," + va.getClassname());
        }
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

        resources.addValidatorAction(va); // calls a and b
        resources.addFormSet(fs);
        resources.process();

        return resources;
    }

}
