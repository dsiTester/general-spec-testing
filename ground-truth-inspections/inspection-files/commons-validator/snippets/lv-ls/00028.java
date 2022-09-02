public class Field implements Cloneable, Serializable {
    /**
     * Gets a unique key based on the property and indexedProperty fields.
     * @return a unique key for the field.
     */
    public String getKey() {    // definition of a
        if (this.key == null) {
            this.generateKey();
        }

        return this.key;
    }

    /**
     * Replace constants with values in fields and process the depends field
     * to create the dependency <code>Map</code>.
     */
    void process(Map<String, String> globalConstants, Map<String, String> constants) { // definition of b
        this.hMsgs.setFast(false);
        this.hVars.setFast(true);

        this.generateKey();

        // Process FormSet Constants
        for (Entry<String, String> entry : constants.entrySet()) {
            String key1 = entry.getKey();
            String key2 = TOKEN_START + key1 + TOKEN_END;
            String replaceValue = entry.getValue();

            property = ValidatorUtils.replace(property, key2, replaceValue);

            processVars(key2, replaceValue);

            this.processMessageComponents(key2, replaceValue);
        }

        // Process Global Constants
        for (Entry<String, String> entry : globalConstants.entrySet()) {
            String key1 = entry.getKey();
            String key2 = TOKEN_START + key1 + TOKEN_END;
            String replaceValue = entry.getValue();

            property = ValidatorUtils.replace(property, key2, replaceValue);

            processVars(key2, replaceValue);

            this.processMessageComponents(key2, replaceValue);
        }

        // Process Var Constant Replacement
        for (String key1 : getVarMap().keySet()) {
            String key2 = TOKEN_START + TOKEN_VAR + key1 + TOKEN_END;
            Var var = this.getVar(key1);
            String replaceValue = var.getValue();

            this.processMessageComponents(key2, replaceValue);
        }

        hMsgs.setFast(true);
    }

    /**
     * Generate correct <code>key</code> value.
     */
    public void generateKey() { // called from both method-a and method-b
        if (this.isIndexed()) {
            this.key = this.indexedListProperty + TOKEN_INDEXED + "." + this.property;
        } else {
            this.key = this.property;
        }
    }

}

public class Form implements Serializable {
    public void addField(Field f) { // called via reflection from commons-digester
        this.lFields.add(f);
        getfieldmap().put(f.getKey(), f); // call to a
    }

    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) { // called after digester finishes parsing
        if (isProcessed()) {
            return;
        }

        int n = 0;//we want the fields from its parent first
        ...
        //no need to reprocess parent's fields, we iterate from 'n'
        for (Iterator<Field> i = lFields.listIterator(n); i.hasNext(); ) {
            Field f = i.next();
            f.process(globalConstants, constants); // call to b
        }

        processed = true;
    }
}

public class ExceptionTest extends AbstractCommonTest {
    /**
     * Tests handling of checked exceptions - should become
     * ValidatorExceptions.
     */
    public void testValidatorException() { // validated test
        // Create bean to run test on.
        ValueBean info = new ValueBean();
        info.setValue("VALIDATOR");

        // Construct validator based on the loaded resources
        // and the form key
        Validator validator = new Validator(resources, FORM_KEY);
        // add the name bean to the validator as a resource
        // for the validations to be performed on.
        validator.setParameter(Validator.BEAN_PARAM, info);

        // Get results of the validation which can throw ValidatorException
        try {
            validator.validate(); // calls a and b
            fail("ValidatorException should occur here!");
        } catch (ValidatorException expected) {
            assertTrue("VALIDATOR-EXCEPTION".equals(expected.getMessage()));
        }
    }

}

public class EntityImportTest extends AbstractCommonTest {

    /**
     * Tests the entity import loading the <code>byteForm</code> form.
     */
    public void testEntityImport() throws Exception { // invalidated test
        URL url = getClass().getResource("EntityImportTest-config.xml");
        ValidatorResources resources = new ValidatorResources(url.toExternalForm()); // indirectly calls a and b
        assertNotNull("Form should be found", resources.getForm(Locale.getDefault(), "byteForm"));
    }

}
