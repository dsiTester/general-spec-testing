public class Field implements Cloneable, Serializable {

    /**
     * Retrieve a variable.
     * @param mainKey The Variable's key
     * @return the Variable
     */
    public Var getVar(String mainKey) { // definition of a
        return getVarMap().get(mainKey); // getVarMap is a naive getter, and this get is on a map
    }

    /**
     * Replace the args key value with the key/value pairs passed in.
     */
    private void processMessageComponents(String key, String replaceValue) { // definition of b
        String varKey = TOKEN_START + TOKEN_VAR;
        // Process Messages
        if (key != null && !key.startsWith(varKey)) {
            for (Msg msg : getMsgMap().values()) {
                msg.setKey(ValidatorUtils.replace(msg.getKey(), key, replaceValue));
            }
        }

        this.processArg(key, replaceValue);
    }

    void process(Map<String, String> globalConstants, Map<String, String> constants) { // indirectly called from DateTest.setUp()
        ...
        // Process Var Constant Replacement
        for (String key1 : getVarMap().keySet()) {
            String key2 = TOKEN_START + TOKEN_VAR + key1 + TOKEN_END;
            Var var = this.getVar(key1); // call to a
            String replaceValue = var.getValue(); // NullPointerException here

            this.processMessageComponents(key2, replaceValue); // call to b
        }

        hMsgs.setFast(true);
    }

}

public class DateTest extends AbstractCommonTest {
    @Override
    protected void setUp() throws IOException, SAXException {
        // Load resources
        loadResources("DateTest-config.xml"); // calls a and b
    }

    /**
     * Tests the date validation.
     */
    public void testInvalidDate() throws ValidatorException {
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("12/01as/2005");
        valueTest(info, false);
    }

    protected void valueTest(Object info, boolean passed) throws ValidatorException {
        // Construct validator based on the loaded resources
        // and the form key
        Validator validator = new Validator(resources, FORM_KEY);
        // add the name bean to the validator as a resource
        // for the validations to be performed on.
        validator.setParameter(Validator.BEAN_PARAM, info);
        validator.setParameter(Validator.LOCALE_PARAM, Locale.US);

        // Get results of the validation.
        // throws ValidatorException,
        // but we aren't catching for testing
        // since no validation methods we use
        // throw this
        ValidatorResults results = validator.validate();

        assertNotNull("Results are null.", results);

        ValidatorResult result = results.getValidatorResult("value");

        assertNotNull(ACTION + " value ValidatorResult should not be null.", result);
        assertTrue(ACTION + " value ValidatorResult should contain the '" + ACTION + "' action.", result.containsAction(ACTION));
        assertTrue(ACTION + " value ValidatorResult for the '" + ACTION + "' action should have " + (passed ? "passed" : "failed") + ".", (passed ? result.isValid(ACTION) : !result.isValid(ACTION)));
    }

}
