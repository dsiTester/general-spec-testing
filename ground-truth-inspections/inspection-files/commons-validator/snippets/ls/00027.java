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
     * Returns a Map of String Var names to Var objects.
     * @since Validator 1.2.0
     * @return A Map of the Field's variables.
     */
    @SuppressWarnings("unchecked") // FastHashMap does not support generics
    protected Map<String, Var> getVarMap() { // definition of b
        return hVars;
    }

    void process(Map<String, String> globalConstants, Map<String, String> constants) {
        ...
        // Process Var Constant Replacement
        for (String key1 : getVarMap().keySet()) { // call to b
            String key2 = TOKEN_START + TOKEN_VAR + key1 + TOKEN_END;
            Var var = this.getVar(key1);
            String replaceValue = var.getValue();

            this.processMessageComponents(key2, replaceValue);
        }

        hMsgs.setFast(true);
    }

}

public class Form implements Serializable {
    public void addField(Field f) {
        this.lFields.add(f);
        getfieldmap().put(f.getKey(), f); // call to a
    }

}

public class EntityImportTest extends AbstractCommonTest {

    /**
     * Tests the entity import loading the <code>byteForm</code> form.
     */
    public void testEntityImport() throws Exception {
        URL url = getClass().getResource("EntityImportTest-config.xml");
        ValidatorResources resources = new ValidatorResources(url.toExternalForm());
        assertNotNull("Form should be found", resources.getForm(Locale.getDefault(), "byteForm"));
    }

}
