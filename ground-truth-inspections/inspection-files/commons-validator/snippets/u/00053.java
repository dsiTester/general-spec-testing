public class Field implements Cloneable, Serializable {

    /**
     * Replace constants with values in fields and process the depends field
     * to create the dependency <code>Map</code>.
     */
    void process(Map<String, String> globalConstants, Map<String, String> constants) { // definition of a
        ...

        // Process Var Constant Replacement
        for (String key1 : getVarMap().keySet()) {
            String key2 = TOKEN_START + TOKEN_VAR + key1 + TOKEN_END;
            Var var = this.getVar(key1); // call to b
            String replaceValue = var.getValue();

            this.processMessageComponents(key2, replaceValue);
        }

        hMsgs.setFast(true);
    }

    /**
     * Retrieve a variable.
     * @param mainKey The Variable's key
     * @return the Variable
     */
    public Var getVar(String mainKey) { // definition of b
        return getVarMap().get(mainKey);
    }
}

public class Form implements Serializable {
    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) {
        ...
        hFields.setFast(true);
        //no need to reprocess parent's fields, we iterate from 'n'
        for (Iterator<Field> i = lFields.listIterator(n); i.hasNext(); ) {
            Field f = i.next();
            f.process(globalConstants, constants); // call to a
        }

        processed = true;
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
    public void testValidDate() throws ValidatorException {
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("12/01/2005");
        valueTest(info, true);
    }
}
