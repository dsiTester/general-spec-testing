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
     * Processes all of the <code>Form</code>'s <code>Field</code>s.
     *
     * @param globalConstants  A map of global constants
     * @param constants        Local constants
     * @param forms            Map of forms
     * @since                  Validator 1.2.0
     */
    protected void process(Map<String, String> globalConstants, Map<String, String> constants, Map<String, Form> forms) { // definition of b
        if (isProcessed()) {
            return;
        }

        int n = 0;//we want the fields from its parent first
        if (isExtending()) {
            Form parent = forms.get(inherit);
            if (parent != null) {
                if (!parent.isProcessed()) {
                    //we want to go all the way up the tree
                    parent.process(constants, globalConstants, forms);
                }
                for (Field f : parent.getFields()) {
                    //we want to be able to override any fields we like
                    if (getFieldMap().get(f.getKey()) == null) {
                        lFields.add(n, f);
                        getFieldMap().put(f.getKey(), f);
                        n++;
                    }
                }
            }
        }
        hFields.setFast(true);
        //no need to reprocess parent's fields, we iterate from 'n'
        for (Iterator<Field> i = lFields.listIterator(n); i.hasNext(); ) {
            Field f = i.next();
            f.process(globalConstants, constants);
        }

        processed = true;
    }

    public void addField(Field f) { // called from commons-digester
        this.lFields.add(f);
        getFieldMap().put(f.getKey(), f); // call to a; NullPointerException here
    }
}

public class FormSet implements Serializable {
    synchronized void process(Map<String, String> globalConstants) {
        for (Form f : forms.values()) {
            f.process(globalConstants, constants, forms); // call to b
        }

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
