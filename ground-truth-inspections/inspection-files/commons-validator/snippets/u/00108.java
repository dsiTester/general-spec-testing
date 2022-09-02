public class Form implements Serializable {
    /**
     * Returns a Map of String field keys to Field objects.
     *
     * @return   The fieldMap value
     * @since    Validator 1.2.0
     */
    @SuppressWarnings("unchecked") // FastHashMap is not generic
    protected Map<String, Field> getFieldMap() { // definition of a
        return hFields;
    }

    /**
     * Whether or not the this <code>Form</code> was processed for replacing
     * variables in strings with their values.
     *
     * @return   The processed value
     * @since    Validator 1.2.0
     */
    public boolean isProcessed() { // definition of b
        return processed;
    }

    public void addField(Field f) { // called from commons-digester
        this.lFields.add(f);
        getFieldMap().put(f.getKey(), f); // call to a; NullPointerException here
    }

    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) { // indirectly called from ValidatorResources.process()
        if (isProcessed()) {    // call to b
            return;
        }
        ...

        processed = true;
    }

}

public class DateTest extends AbstractCommonTest {
    @Override
    protected void setUp() throws IOException, SAXException {
        // Load resources
        loadResources("DateTest-config.xml");
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

abstract public class AbstractCommonTest extends TestCase {

    /**
     * Load <code>ValidatorResources</code> from
     * validator-numeric.xml.
     */
    protected void loadResources(String file) throws IOException, SAXException {
        // Load resources
        try (InputStream in = this.getClass().getResourceAsStream(file)) {
            resources = new ValidatorResources(in);
        }
    }
}
