public class Form implements Serializable {
    /**
     * Whether or not the this <code>Form</code> was processed for replacing
     * variables in strings with their values.
     *
     * @return   The processed value
     * @since    Validator 1.2.0
     */
    public boolean isProcessed() { // definition of a
        return processed;
    }

    /**
     * Get extends flag.
     *
     * @return   The extending value
     * @since    Validator 1.2.0
     */
    public boolean isExtending() { // definition of b
        return inherit != null;
    }

    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) { // indirectly called from ValidatorResources.process()
        if (isProcessed()) {    // call to a
            return;             // this doesn't happen because a returned false/was replaced with false
        }

        int n = 0;//we want the fields from its parent first
        if (isExtending()) {    // call to b
            ...
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

abstract public class AbstractCommonTest extends TestCase {

    /**
     * Load <code>ValidatorResources</code> from
     * validator-numeric.xml.
     */
    protected void loadResources(String file) throws IOException, SAXException {
        // Load resources
        try (InputStream in = this.getClass().getResourceAsStream(file)) {
            resources = new ValidatorResources(in); // calls a and b
        }
    }
}
