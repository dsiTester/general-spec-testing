public class Field implements Cloneable, Serializable {

    /**
     * Replace the args key value with the key/value pairs passed in.
     */
    private void processMessageComponents(String key, String replaceValue) { // definition of a
        String varKey = TOKEN_START + TOKEN_VAR;
        // Process Messages
        if (key != null && !key.startsWith(varKey)) {
            for (Msg msg : getMsgMap().values()) {
                msg.setKey(ValidatorUtils.replace(msg.getKey(), key, replaceValue));
            }
        }

        this.processArg(key, replaceValue); // call to b
    }

    /**
     * Replace the arg <code>Collection</code> key value with the key/value
     * pairs passed in.
     */
    private void processArg(String key, String replaceValue) { // definition of b
        for (Map<String, Arg> argMap : this.args) {

            if (argMap == null) {
                continue;
            }

            Iterator<Arg> iter = argMap.values().iterator();
            while (iter.hasNext()) {
                Arg arg = iter.next();

                if (arg != null) {
                    arg.setKey(
                            ValidatorUtils.replace(arg.getKey(), key, replaceValue));
                }
            }
        }
    }

    void process(Map<String, String> globalConstants, Map<String, String> constants) {
        ...
        // Process Var Constant Replacement
        for (String key1 : getVarMap().keySet()) {
            String key2 = TOKEN_START + TOKEN_VAR + key1 + TOKEN_END;
            Var var = this.getVar(key1);
            String replaceValue = var.getValue();

            this.processMessageComponents(key2, replaceValue); // call to a
        }

        hMsgs.setFast(true);
    }

}

public class DateTest extends AbstractCommonTest {
    @Override
    protected void setUp() throws IOException, SAXException {
        // Load resources
        loadResources("DateTest-config.xml"); // calls a
    }

    /**
     * Tests the date validation.
     */
    public void testValidDate() throws ValidatorException {
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("12/01/2005");
        valueTest(info, true);  // calls b
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
        ValidatorResults results = validator.validate(); // calls b

        assertNotNull("Results are null.", results);

        ValidatorResult result = results.getValidatorResult("value");

        assertNotNull(ACTION + " value ValidatorResult should not be null.", result);
        assertTrue(ACTION + " value ValidatorResult should contain the '" + ACTION + "' action.", result.containsAction(ACTION));
        assertTrue(ACTION + " value ValidatorResult for the '" + ACTION + "' action should have " + (passed ? "passed" : "failed") + ".", (passed ? result.isValid(ACTION) : !result.isValid(ACTION)));
    }
}
